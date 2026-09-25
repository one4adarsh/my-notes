package org.example.behavioural.strategy.payment;

public class CreditCardPaymentStrategy implements PaymentStrategy {
    private final String cardType;
    private final String cardNumber;

    public CreditCardPaymentStrategy(String cardType, String cardNumber) {
        this.cardType = cardType;
        this.cardNumber = cardNumber;
    }

    @Override
    public void pay(double amount) {
        String masked = "****" + cardNumber.substring(cardNumber.length() - 4);
        System.out.printf("Paid %.2f using %s card %s%n", amount, cardType, masked);
    }
}
