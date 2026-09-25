package org.example.behavioural.strategy.payment;

public class PayPalPaymentStrategy implements PaymentStrategy {
    private final String email;

    public PayPalPaymentStrategy(String email) {
        this.email = email;
    }

    @Override
    public void pay(double amount) {
        System.out.printf("Paid %.2f using PayPal account %s%n", amount, email);
    }
}
