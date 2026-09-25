package org.example.behavioural.observer.subscriber;

public class EmailSubscriber implements Subscriber {
    private final String email;

    public EmailSubscriber(String email) {
        this.email = email;
    }

    @Override
    public void update(String newsletterName, String issue) {
        System.out.printf("[email -> %s] New issue of '%s': %s%n", email, newsletterName, issue);
    }
}
