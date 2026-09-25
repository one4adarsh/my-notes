package org.example.behavioural.strategy.cart;

import org.example.behavioural.strategy.item.Item;
import org.example.behavioural.strategy.payment.PaymentStrategy;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<Item> items = new ArrayList<>();
    private PaymentStrategy paymentStrategy;

    public void addItem(Item item) {
        items.add(item);
    }

    public double total() {
        return items.stream().mapToDouble(Item::price).sum();
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout() {
        if (paymentStrategy == null) {
            throw new IllegalStateException("No payment strategy selected");
        }
        paymentStrategy.pay(total());
        items.clear();
    }
}
