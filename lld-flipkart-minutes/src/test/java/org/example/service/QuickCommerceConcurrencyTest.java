package org.example.service;

import org.example.exception.QuickCommerceException;
import org.example.model.DeliveryPartner;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.PartnerStatus;
import org.example.repository.CustomerRepository;
import org.example.repository.DeliveryPartnerRepository;
import org.example.repository.OrderRepository;
import org.example.strategy.FirstAvailablePartnerStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proves the thread-safety claim: concurrent customers and partners never double-book a
 * partner, never lose an order, and always leave the system in a consistent state.
 */
public class QuickCommerceConcurrencyTest {

    private static final int PARTNERS = 4;
    private static final int ORDERS = 200;
    private static final int THREADS = 32;

    private DeliveryPartnerRepository partnerRepository;
    private QuickCommerceService service;

    @Before
    public void setUp() {
        partnerRepository = new DeliveryPartnerRepository();
        OrderDispatcher dispatcher = new OrderDispatcher(partnerRepository, new FirstAvailablePartnerStrategy());
        service = new QuickCommerceService(
                new CustomerRepository(), partnerRepository, new OrderRepository(), dispatcher);
        service.onboardCustomer("C1", "Asha");
        for (int i = 1; i <= PARTNERS; i++) {
            service.onboardDeliveryPartner("P" + i, "Partner-" + i);
        }
    }

    @Test
    public void concurrentPlacementNeverDoubleBooksAPartner() throws Exception {
        List<Order> placed = runConcurrently(ORDERS, () -> service.placeOrder("C1", "item"));

        long assigned = placed.stream().filter(o -> o.getStatus() == OrderStatus.ASSIGNED).count();
        long pending = placed.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        Set<String> distinctPartners = new HashSet<>();
        placed.stream().map(Order::getPartnerId).filter(Objects::nonNull).forEach(distinctPartners::add);

        assertEquals(ORDERS, placed.size());
        assertEquals(PARTNERS, assigned);
        assertEquals(PARTNERS, distinctPartners.size());   // no partner used twice
        assertEquals(ORDERS - PARTNERS, pending);           // nothing lost
        assertPartnerOrderLinksConsistent();
    }

    @Test
    public void concurrentPlaceAndCancelKeepsPartnerOrderLinksConsistent() throws Exception {
        List<Order> placed = runConcurrently(ORDERS, () -> service.placeOrder("C1", "item"));

        // Cancel roughly a third of the orders from many threads while others are still being assigned.
        List<Order> toCancel = new ArrayList<>();
        for (int i = 0; i < placed.size(); i += 3) {
            toCancel.add(placed.get(i));
        }
        runConcurrently(toCancel.size(), new CancelTasks(toCancel));

        assertPartnerOrderLinksConsistent();

        // Drain: delivering an assigned order pulls the next queued one; repeat until nothing is assigned.
        boolean progressed = true;
        while (progressed) {
            progressed = false;
            for (Order o : placed) {
                if (o.getStatus() == OrderStatus.ASSIGNED) {
                    service.pickupOrder(o.getPartnerId(), o.getId());
                    service.deliverOrder(o.getPartnerId(), o.getId());
                    progressed = true;
                }
            }
        }

        long delivered = placed.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = placed.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        assertEquals(ORDERS, delivered + cancelled);                 // every order reached a terminal state
        for (int i = 1; i <= PARTNERS; i++) {
            assertEquals(PartnerStatus.AVAILABLE, service.getPartnerStatus("P" + i));
        }
    }

    // ---- helpers ----

    /** Every BUSY partner points at exactly one ASSIGNED/PICKED_UP order, and that order points back. */
    private void assertPartnerOrderLinksConsistent() {
        for (DeliveryPartner p : partnerRepository.findAll()) {
            if (p.isAvailable()) {
                continue;
            }
            String orderId = p.getCurrentOrderId();
            OrderStatus status = service.getOrderStatus(orderId);
            assertTrue("partner " + p.getId() + " holds " + orderId + " in status " + status,
                    status == OrderStatus.ASSIGNED || status == OrderStatus.PICKED_UP);
        }
    }

    /** Runs {@code count} copies of {@code task} released simultaneously by a latch. */
    private static <T> List<T> runConcurrently(int count, Callable<T> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return task.call();
            }));
        }
        start.countDown();
        List<T> results = new ArrayList<>();
        try {
            for (Future<T> f : futures) {
                results.add(f.get(10, TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }
        return results;
    }

    /** Each call cancels the next order in the list; already-picked-up orders are legitimately rejected. */
    private final class CancelTasks implements Callable<Void> {
        private final List<Order> orders;
        private int next = 0;

        CancelTasks(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public Void call() {
            Order o;
            synchronized (this) {
                o = orders.get(next++);
            }
            try {
                service.cancelOrder(o.getId());
            } catch (QuickCommerceException ignored) {
                // expected for orders already past cancellable state
            }
            return null;
        }
    }
}
