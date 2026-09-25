package org.example.behavioural.strategy;

import org.example.behavioural.strategy.cart.Cart;
import org.example.behavioural.strategy.item.Item;
import org.example.behavioural.strategy.payment.CreditCardPaymentStrategy;
import org.example.behavioural.strategy.payment.PayPalPaymentStrategy;

public class Main {
    public static void main(String[] args) {
        Cart cart = new Cart();
        cart.addItem(new Item("Book", 29.99));
        cart.addItem(new Item("Pen", 9.99));

        cart.setPaymentStrategy(new CreditCardPaymentStrategy("MasterCard", "5555444433331111"));

        cart.checkout();

        cart.addItem(new Item("Keyboard", 45.00));
        cart.setPaymentStrategy(new PayPalPaymentStrategy("user@example.com"));
        cart.checkout();
    }
}
