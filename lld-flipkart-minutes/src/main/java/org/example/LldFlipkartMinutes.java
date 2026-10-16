package org.example;

import org.example.exception.QuickCommerceException;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.PartnerStatus;
import org.example.repository.CustomerRepository;
import org.example.repository.DeliveryPartnerRepository;
import org.example.repository.OrderRepository;
import org.example.service.OrderDispatcher;
import org.example.service.QuickCommerceService;
import org.example.strategy.FirstAvailablePartnerStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Driver program (demo for the evaluator).
 * <p>
 * Assumptions:
 * 1. Order ids are system-generated; customer/partner ids are supplied by the caller.
 * 2. One item per order; item is just a name (no catalog, no stock).
 * 3. Partner selection = any available partner (pluggable via PartnerAssignmentStrategy).
 * 4. pickup/deliver take partnerId so the acting partner can be validated against the assignee.
 * 5. Bonus features (notifications, ratings, dashboard, auto-cancel) are out of scope for P0.
 */
public class LldFlipkartMinutes {

    public static void main(String[] args) {
        QuickCommerceService service = bootstrap();

        // ---- Scenario 1: onboarding ----
        section("1. Onboard customers & partners");
        service.onboardCustomer("C1", "Asha");
        service.onboardCustomer("C2", "Ravi");
        service.onboardDeliveryPartner("P1", "Dev");
        System.out.println("P1 -> " + service.getPartnerStatus("P1"));

        // ---- Scenario 2: place orders; 1 partner, 3 orders -> first ASSIGNED, rest PENDING ----
        section("2. Place 3 orders with 1 partner");
        Order o1 = service.placeOrder("C1", "milk");
        Order o2 = service.placeOrder("C2", "eggs");
        Order o3 = service.placeOrder("C1", "bread");
        printOrders(service, o1, o2, o3);
        System.out.println("P1 -> " + service.getPartnerStatus("P1"));

        // ---- Scenario 3: cancel a queued (PENDING) order ----
        section("3. Cancel queued order " + o2.getId());
        service.cancelOrder(o2.getId());
        printOrders(service, o2);

        // ---- Scenario 4: pickup, then cancel must fail ----
        section("4. Pickup " + o1.getId() + " then try to cancel");
        service.pickupOrder("P1", o1.getId());
        printOrders(service, o1);
        expectFailure(() -> service.cancelOrder(o1.getId()), "cancel after pickup");

        // ---- Scenario 5: deliver -> partner freed -> next queued order auto-assigned ----
        section("5. Deliver " + o1.getId() + " -> P1 freed -> " + o3.getId() + " auto-assigned (" + o2.getId() + " skipped)");
        service.deliverOrder("P1", o1.getId());
        printOrders(service, o1, o2, o3);
        System.out.println("P1 -> " + service.getPartnerStatus("P1"));

        // ---- Scenario 6: cancel an ASSIGNED order -> partner back to AVAILABLE ----
        section("6. Cancel ASSIGNED order " + o3.getId() + " before pickup");
        service.cancelOrder(o3.getId());
        printOrders(service, o3);
        System.out.println("P1 -> " + service.getPartnerStatus("P1"));

        // ---- Scenario 7: error handling ----
        section("7. Error cases");
        expectFailure(() -> service.onboardCustomer("C1", "dup"), "duplicate customer id");
        expectFailure(() -> service.onboardDeliveryPartner("P1", "dup"), "duplicate partner id");
        expectFailure(() -> service.placeOrder("C9", "x"), "unknown customer");
        expectFailure(() -> service.getOrderStatus("ORD-999"), "unknown order");
        expectFailure(() -> service.cancelOrder(o3.getId()), "double cancel");
        Order o4 = service.placeOrder("C2", "butter");          // assigned to P1
        service.onboardDeliveryPartner("P2", "Meena");
        expectFailure(() -> service.pickupOrder("P2", o4.getId()), "pickup by non-assigned partner");
        expectFailure(() -> service.deliverOrder("P1", o4.getId()), "deliver before pickup");
        expectFailure(() -> service.pickupOrder("P9", o4.getId()), "unknown partner");

        // ---- Scenario 8: concurrency - 20 threads place orders simultaneously against 3 partners ----
        section("8. Concurrency: 20 concurrent orders, 3 partners");
        runConcurrencyScenario(20, 3);
    }

    private static QuickCommerceService bootstrap() {
        CustomerRepository customerRepository = new CustomerRepository();
        DeliveryPartnerRepository partnerRepository = new DeliveryPartnerRepository();
        OrderRepository orderRepository = new OrderRepository();
        OrderDispatcher dispatcher = new OrderDispatcher(partnerRepository, new FirstAvailablePartnerStrategy());
        return new QuickCommerceService(customerRepository, partnerRepository, orderRepository, dispatcher);
    }

    // ---- demo helpers ----

    /**
     * Fresh service, {@code partners} partners, {@code orders} orders placed from parallel threads
     * released by a single latch. Invariants checked afterwards:
     * exactly min(orders, partners) ASSIGNED, no partner holds two orders, rest PENDING.
     * Then drains the queue by delivering until every order is DELIVERED.
     */
    private static void runConcurrencyScenario(int orders, int partners) {
        QuickCommerceService service = bootstrap();
        service.onboardCustomer("C1", "Asha");
        for (int i = 1; i <= partners; i++) {
            service.onboardDeliveryPartner("P" + i, "Partner-" + i);
        }

        ExecutorService pool = Executors.newFixedThreadPool(orders);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Order>> futures = new ArrayList<>();
        for (int i = 0; i < orders; i++) {
            futures.add(pool.submit(() -> {
                start.await();                       // all threads hit placeOrder together
                return service.placeOrder("C1", "item");
            }));
        }
        start.countDown();
        List<Order> placed = new ArrayList<>();
        try {
            for (Future<Order> f : futures) {
                placed.add(f.get(5, TimeUnit.SECONDS));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Concurrent placement failed", e);
        } finally {
            pool.shutdown();
        }

        long assigned = placed.stream().filter(o -> o.getStatus() == OrderStatus.ASSIGNED).count();
        long pending = placed.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        Set<String> distinctPartners = new HashSet<>();
        placed.stream().map(Order::getPartnerId).filter(Objects::nonNull).forEach(distinctPartners::add);

        System.out.println("placed=" + placed.size() + " assigned=" + assigned + " pending=" + pending
                + " distinctPartnersUsed=" + distinctPartners.size());
        boolean invariantsHold = assigned == Math.min(orders, partners)
                && assigned == distinctPartners.size()
                && assigned + pending == orders;
        System.out.println(invariantsHold ? "PASS: no partner double-booked, queue holds the rest"
                : "FAIL: invariants violated");

        // Drain: deliver whatever is assigned, which pulls the next queued order, until all delivered.
        int delivered = 0;
        while (delivered < orders) {
            for (Order o : placed) {
                if (o.getStatus() == OrderStatus.ASSIGNED) {
                    service.pickupOrder(o.getPartnerId(), o.getId());
                    service.deliverOrder(o.getPartnerId(), o.getId());
                    delivered++;
                }
            }
        }
        boolean allDelivered = placed.stream().allMatch(o -> o.getStatus() == OrderStatus.DELIVERED);
        boolean allFree = true;
        for (int i = 1; i <= partners; i++) {
            allFree &= service.getPartnerStatus("P" + i) == PartnerStatus.AVAILABLE;
        }
        System.out.println("drained: delivered=" + delivered + " allDelivered=" + allDelivered + " allPartnersFree=" + allFree);
        System.out.println(allDelivered && allFree ? "PASS: queue fully drained via auto-reassignment"
                : "FAIL: drain incomplete");
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void printOrders(QuickCommerceService service, Order... orders) {
        for (Order o : orders) {
            System.out.println(o.getId() + " -> " + service.getOrderStatus(o.getId())
                    + (o.getPartnerId() != null ? " (partner " + o.getPartnerId() + ")" : ""));
        }
    }

    private static void expectFailure(Runnable action, String label) {
        try {
            action.run();
            System.out.println("UNEXPECTED SUCCESS: " + label);
        } catch (QuickCommerceException e) {
            System.out.println("Rejected [" + label + "]: " + e.getMessage());
        }
    }
}