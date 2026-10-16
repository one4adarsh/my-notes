package org.example.repository;

import org.example.model.Order;

public class OrderRepository extends InMemoryRepository<Order> {

    public OrderRepository() {
        super(Order::getId);
    }
}
