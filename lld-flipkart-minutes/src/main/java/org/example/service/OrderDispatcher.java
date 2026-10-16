package org.example.service;

import org.example.exception.InvalidStateTransitionException;
import org.example.model.DeliveryPartner;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.DeliveryPartnerRepository;
import org.example.strategy.PartnerAssignmentStrategy;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The real orchestrator for assignment. Owns the FIFO queue of pending orders.
 * <p>
 * Workflow rules live here:
 * - no free partner → enqueue
 * - partner freed → assign next non-canceled queued order
 * <p>
 * A single lock wraps "select partner + assign" so two concurrent placeOrder calls
 * can never grab the same partner. Lock order is always dispatcher → order/partner.
 */
public class OrderDispatcher {

    private final Queue<Order> pendingOrders = new ConcurrentLinkedQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final DeliveryPartnerRepository partnerRepository;
    private final PartnerAssignmentStrategy assignmentStrategy;

    public OrderDispatcher(DeliveryPartnerRepository partnerRepository,
                           PartnerAssignmentStrategy assignmentStrategy) {
        this.partnerRepository = partnerRepository;
        this.assignmentStrategy = assignmentStrategy;
    }

    /** Try to assign immediately; otherwise enqueue. */
    public void submit(Order order) {
        lock.lock();
        try {
            if (order.getStatus() != OrderStatus.PENDING) {
                return; // canceled before it reached the dispatcher
            }
            Optional<DeliveryPartner> partner =
                    assignmentStrategy.select(partnerRepository.findAvailable());
            if (partner.isEmpty() || !tryAssign(order, partner.get())) {
                pendingOrders.offer(order);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called after a successful delivery: free the partner and hand them the next PENDING order.
     * Release happens under the dispatcher lock so no submit() can observe a half-freed partner.
     */
    public void releasePartner(DeliveryPartner partner) {
        lock.lock();
        try {
            partner.release();
            assignNextPending(partner);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called after {@code order.cancel()} succeeded. Under the lock, the order is either
     * still queued (never assigned) or fully assigned to a partner — never half-assigned,
     * because {@link #tryAssign} runs entirely inside the same lock.
     */
    public void onOrderCancelled(Order order) {
        lock.lock();
        try {
            String partnerId = order.getPartnerId();
            if (partnerId == null) {
                pendingOrders.remove(order);
                return;
            }
            partnerRepository.findById(partnerId).ifPresent(partner -> {
                partner.release();
                assignNextPending(partner);
            });
        } finally {
            lock.unlock();
        }
    }

    /** Must be called under {@link #lock}. Polls the queue, skipping orders cancelled while queued. */
    private void assignNextPending(DeliveryPartner partner) {
        Order next;
        while ((next = pendingOrders.poll()) != null) {
            if (tryAssign(next, partner)) {
                return;
            }
        }
    }

    /**
     * Must be called under {@link #lock}. Order transitions first: if the order was canceled
     * concurrently, {@code assignTo} throws and the partner is left untouched.
     */
    private boolean tryAssign(Order order, DeliveryPartner partner) {
        try {
            order.assignTo(partner.getId());
        } catch (InvalidStateTransitionException e) {
            return false;
        }
        partner.assign(order.getId());
        return true;
    }
}
