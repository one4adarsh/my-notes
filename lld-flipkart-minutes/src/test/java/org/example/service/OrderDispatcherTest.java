package org.example.service;

import org.example.model.DeliveryPartner;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.DeliveryPartnerRepository;
import org.example.strategy.FirstAvailablePartnerStrategy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Workflow rules: immediate assign, FIFO queue, skip cancelled, free partner on cancel/deliver. */
public class OrderDispatcherTest {

    private DeliveryPartnerRepository partners;
    private OrderDispatcher dispatcher;
    private DeliveryPartner p1;

    @Before
    public void setUp() {
        partners = new DeliveryPartnerRepository();
        dispatcher = new OrderDispatcher(partners, new FirstAvailablePartnerStrategy());
        p1 = new DeliveryPartner("P1", "Dev");
        partners.save(p1);
    }

    private Order order(String id) {
        return new Order(id, "C1", "item");
    }

    @Test
    public void submitAssignsImmediatelyWhenPartnerFree() {
        Order o1 = order("O1");
        dispatcher.submit(o1);

        assertEquals(OrderStatus.ASSIGNED, o1.getStatus());
        assertEquals("P1", o1.getPartnerId());
        assertEquals("O1", p1.getCurrentOrderId());
        assertFalse(p1.isAvailable());
    }

    @Test
    public void submitQueuesWhenNoPartnerFree() {
        Order o1 = order("O1"), o2 = order("O2");
        dispatcher.submit(o1);
        dispatcher.submit(o2);

        assertEquals(OrderStatus.PENDING, o2.getStatus());
        assertNull(o2.getPartnerId());
    }

    @Test
    public void releasePartnerAssignsNextInFifoOrder() {
        Order o1 = order("O1"), o2 = order("O2"), o3 = order("O3");
        dispatcher.submit(o1);
        dispatcher.submit(o2);
        dispatcher.submit(o3);

        deliver(o1);
        dispatcher.releasePartner(p1);

        assertEquals(OrderStatus.ASSIGNED, o2.getStatus());
        assertEquals(OrderStatus.PENDING, o3.getStatus());
        assertEquals("O2", p1.getCurrentOrderId());
    }

    @Test
    public void cancelledQueuedOrderIsSkippedOnDrain() {
        Order o1 = order("O1"), o2 = order("O2"), o3 = order("O3");
        dispatcher.submit(o1);
        dispatcher.submit(o2);
        dispatcher.submit(o3);

        o2.cancel();                      // cancelled while queued, not removed eagerly
        deliver(o1);
        dispatcher.releasePartner(p1);

        assertEquals(OrderStatus.CANCELLED, o2.getStatus());
        assertNull(o2.getPartnerId());
        assertEquals(OrderStatus.ASSIGNED, o3.getStatus());
    }

    @Test
    public void onOrderCancelledForPendingOrderRemovesFromQueue() {
        Order o1 = order("O1"), o2 = order("O2"), o3 = order("O3");
        dispatcher.submit(o1);
        dispatcher.submit(o2);
        dispatcher.submit(o3);

        o2.cancel();
        dispatcher.onOrderCancelled(o2);
        assertFalse(p1.isAvailable());    // partner untouched

        deliver(o1);
        dispatcher.releasePartner(p1);
        assertEquals(OrderStatus.ASSIGNED, o3.getStatus());
    }

    @Test
    public void onOrderCancelledForAssignedOrderFreesPartnerAndReassigns() {
        Order o1 = order("O1"), o2 = order("O2");
        dispatcher.submit(o1);
        dispatcher.submit(o2);

        o1.cancel();
        dispatcher.onOrderCancelled(o1);

        assertEquals(OrderStatus.ASSIGNED, o2.getStatus());
        assertEquals("O2", p1.getCurrentOrderId());
    }

    @Test
    public void onOrderCancelledForAssignedOrderWithEmptyQueueLeavesPartnerAvailable() {
        Order o1 = order("O1");
        dispatcher.submit(o1);

        o1.cancel();
        dispatcher.onOrderCancelled(o1);

        assertTrue(p1.isAvailable());
        assertNull(p1.getCurrentOrderId());
    }

    @Test
    public void submitOfAlreadyCancelledOrderIsNoOp() {
        Order o1 = order("O1");
        o1.cancel();
        dispatcher.submit(o1);

        assertTrue(p1.isAvailable());
        assertNull(o1.getPartnerId());
    }

    @Test
    public void releasePartnerWithEmptyQueueLeavesPartnerAvailable() {
        Order o1 = order("O1");
        dispatcher.submit(o1);
        deliver(o1);
        dispatcher.releasePartner(p1);

        assertTrue(p1.isAvailable());
    }

    @Test
    public void multiplePartnersEachGetOneOrder() {
        DeliveryPartner p2 = new DeliveryPartner("P2", "Meena");
        partners.save(p2);
        Order o1 = order("O1"), o2 = order("O2"), o3 = order("O3");
        dispatcher.submit(o1);
        dispatcher.submit(o2);
        dispatcher.submit(o3);

        assertEquals(OrderStatus.ASSIGNED, o1.getStatus());
        assertEquals(OrderStatus.ASSIGNED, o2.getStatus());
        assertEquals(OrderStatus.PENDING, o3.getStatus());
        assertFalse(p1.isAvailable());
        assertFalse(p2.isAvailable());
        assertTrue(!o1.getPartnerId().equals(o2.getPartnerId()));
    }

    private static void deliver(Order order) {
        order.markPickedUp();
        order.markDelivered();
    }
}
