package org.example.model;

import org.example.exception.InvalidStateTransitionException;

import java.time.Instant;

/**
 * Owns its own status and guards every transition (data-specific rules live here).
 * Transition methods are synchronized because multiple threads (customer cancel,
 * dispatcher assign, partner pickup/deliver) may touch the same order.
 */
public class Order {

    private final String id;
    private final String customerId;
    private final String itemName;
    private final Instant createdAt;
    private OrderStatus status;
    private String partnerId;

    public Order(String id, String customerId, String itemName) {
        this.id = id;
        this.customerId = customerId;
        this.itemName = itemName;
        this.createdAt = Instant.now();
        this.status = OrderStatus.PENDING;
    }

    /** PENDING -> ASSIGNED. */
    public synchronized void assignTo(String partnerId) {
        requireStatus(OrderStatus.PENDING, "assign");
        this.partnerId = partnerId;
        this.status = OrderStatus.ASSIGNED;
    }

    /** ASSIGNED -> PICKED_UP. */
    public synchronized void markPickedUp() {
        requireStatus(OrderStatus.ASSIGNED, "pick up");
        this.status = OrderStatus.PICKED_UP;
    }

    /** PICKED_UP -> DELIVERED. */
    public synchronized void markDelivered() {
        requireStatus(OrderStatus.PICKED_UP, "deliver");
        this.status = OrderStatus.DELIVERED;
    }

    /** PENDING | ASSIGNED -> CANCELLED; otherwise throws InvalidStateTransitionException. */
    public synchronized void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.ASSIGNED) {
            throw new InvalidStateTransitionException(
                    "Cannot cancel order " + id + " in status " + status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new InvalidStateTransitionException(
                    "Cannot " + action + " order " + id + ": expected " + expected + " but was " + status);
        }
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getItemName() {
        return itemName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized String getPartnerId() {
        return partnerId;
    }

    @Override
    public synchronized String toString() {
        return "Order{id='" + id + "', customerId='" + customerId + "', itemName='" + itemName
                + "', status=" + status + ", partnerId='" + partnerId + "'}";
    }
}
