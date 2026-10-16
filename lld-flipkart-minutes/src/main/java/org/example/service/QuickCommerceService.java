package org.example.service;

import org.example.exception.EntityNotFoundException;
import org.example.exception.InvalidStateTransitionException;
import org.example.model.Customer;
import org.example.model.DeliveryPartner;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.PartnerStatus;
import org.example.repository.CustomerRepository;
import org.example.repository.DeliveryPartnerRepository;
import org.example.repository.OrderRepository;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Facade / single entry point for the driver program.
 * <p>
 * Responsibilities: validate ids, create entities, delegate assignment to the
 * dispatcher. Lifecycle rules (who may act on which order) live here; state
 * transition rules live in the models.
 */
public class QuickCommerceService {

    private final CustomerRepository customerRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final OrderDispatcher dispatcher;
    private final AtomicLong orderIdGenerator = new AtomicLong();

    public QuickCommerceService(CustomerRepository customerRepository,
                                DeliveryPartnerRepository partnerRepository,
                                OrderRepository orderRepository,
                                OrderDispatcher dispatcher) {
        this.customerRepository = customerRepository;
        this.partnerRepository = partnerRepository;
        this.orderRepository = orderRepository;
        this.dispatcher = dispatcher;
    }

    // ---- Onboarding ----

    public Customer onboardCustomer(String id, String name) {
        Customer customer = new Customer(id, name);
        customerRepository.save(customer); // throws DuplicateEntityException
        return customer;
    }

    public DeliveryPartner onboardDeliveryPartner(String id, String name) {
        DeliveryPartner partner = new DeliveryPartner(id, name);
        partnerRepository.save(partner);
        return partner;
    }

    // ---- Order placement & cancellation ----

    public Order placeOrder(String customerId, String itemName) {
        requireCustomer(customerId);
        Order order = new Order("ORD-" + orderIdGenerator.incrementAndGet(), customerId, itemName);
        orderRepository.save(order);
        dispatcher.submit(order);
        return order;
    }

    public void cancelOrder(String orderId) {
        Order order = requireOrder(orderId);
        order.cancel();                      // rejects PICKED_UP / DELIVERED / CANCELLED
        dispatcher.onOrderCancelled(order);  // dequeue, or free the partner and reassign
    }

    // ---- Fulfillment ----

    public void pickupOrder(String partnerId, String orderId) {
        requirePartner(partnerId);
        Order order = requireOrder(orderId);
        requireAssignedTo(order, partnerId);
        order.markPickedUp();
    }

    public void deliverOrder(String partnerId, String orderId) {
        DeliveryPartner partner = requirePartner(partnerId);
        Order order = requireOrder(orderId);
        requireAssignedTo(order, partnerId);
        order.markDelivered();
        dispatcher.releasePartner(partner);
    }

    // ---- Status tracking ----

    public OrderStatus getOrderStatus(String orderId) {
        return requireOrder(orderId).getStatus();
    }

    public PartnerStatus getPartnerStatus(String partnerId) {
        return requirePartner(partnerId).getStatus();
    }

    // ---- Lookups & guards ----

    private Customer requireCustomer(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    private DeliveryPartner requirePartner(String id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery partner not found: " + id));
    }

    private Order requireOrder(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    /** Only the assigned partner may pick up / deliver an order. */
    private void requireAssignedTo(Order order, String partnerId) {
        if (!partnerId.equals(order.getPartnerId())) {
            throw new InvalidStateTransitionException(
                    "Order " + order.getId() + " is not assigned to partner " + partnerId
                            + " (assigned: " + order.getPartnerId() + ")");
        }
    }
}
