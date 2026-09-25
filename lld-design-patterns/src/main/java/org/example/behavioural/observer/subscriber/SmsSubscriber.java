package org.example.behavioural.observer.subscriber;

public class SmsSubscriber implements Subscriber {
    private final String phoneNumber;

    public SmsSubscriber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void update(String newsletterName, String issue) {
        System.out.printf("[sms -> %s] New issue of '%s': %s%n", phoneNumber, newsletterName, issue);
    }
}
