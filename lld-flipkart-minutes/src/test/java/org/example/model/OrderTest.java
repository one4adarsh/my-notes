package org.example.model;

import org.example.exception.InvalidStateTransitionException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Pins the Order state machine: legal transitions succeed, everything else throws. */
public class OrderTest {

    private Order order;

    @Before
    public void setUp() {
        order = new Order("ORD-1", "C1", "milk");
    }

    @Test
    public void newOrderIsPendingWithNoPartner() {
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNull(order.getPartnerId());
    }

    @Test
    public void happyPathPendingToDelivered() {
        order.assignTo("P1");
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals("P1", order.getPartnerId());

        order.markPickedUp();
        assertEquals(OrderStatus.PICKED_UP, order.getStatus());

        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    public void cancelFromPending() {
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    public void cancelFromAssignedRetainsPartnerForAudit() {
        order.assignTo("P1");
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals("P1", order.getPartnerId());
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void cancelAfterPickupIsRejected() {
        order.assignTo("P1");
        order.markPickedUp();
        order.cancel();
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void cancelAfterDeliveredIsRejected() {
        order.assignTo("P1");
        order.markPickedUp();
        order.markDelivered();
        order.cancel();
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void doubleCancelIsRejected() {
        order.cancel();
        order.cancel();
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void pickupBeforeAssignIsRejected() {
        order.markPickedUp();
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void deliverBeforePickupIsRejected() {
        order.assignTo("P1");
        order.markDelivered();
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void doubleAssignIsRejected() {
        order.assignTo("P1");
        order.assignTo("P2");
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void assignCancelledOrderIsRejected() {
        order.cancel();
        order.assignTo("P1");
    }
}
