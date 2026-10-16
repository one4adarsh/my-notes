package org.example.model;

import org.example.exception.InvalidStateTransitionException;

/**
 * Owns its own status and guards every transition (data-specific rules live here).
 * Transition methods are synchronized because the dispatcher (assign) and the
 * service (release on deliver/cancel) may touch the same partner concurrently.
 */
public class DeliveryPartner {

    private final String id;
    private final String name;
    private PartnerStatus status;
    private String currentOrderId;

    public DeliveryPartner(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = PartnerStatus.AVAILABLE;
    }

    /** AVAILABLE -> BUSY. */
    public synchronized void assign(String orderId) {
        if (status != PartnerStatus.AVAILABLE) {
            throw new InvalidStateTransitionException(
                    "Partner " + id + " is " + status + " with order " + currentOrderId
                            + "; cannot assign order " + orderId);
        }
        this.currentOrderId = orderId;
        this.status = PartnerStatus.BUSY;
    }

    /** BUSY -> AVAILABLE (after deliver, or cancel-before-pickup). */
    public synchronized void release() {
        if (status != PartnerStatus.BUSY) {
            throw new InvalidStateTransitionException(
                    "Partner " + id + " is already " + status + "; nothing to release");
        }
        this.currentOrderId = null;
        this.status = PartnerStatus.AVAILABLE;
    }

    public synchronized boolean isAvailable() {
        return status == PartnerStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public synchronized PartnerStatus getStatus() {
        return status;
    }

    public synchronized String getCurrentOrderId() {
        return currentOrderId;
    }

    @Override
    public synchronized String toString() {
        return "DeliveryPartner{id='" + id + "', name='" + name
                + "', status=" + status + ", currentOrderId='" + currentOrderId + "'}";
    }
}
