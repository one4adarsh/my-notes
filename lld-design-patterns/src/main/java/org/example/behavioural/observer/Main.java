package org.example.behavioural.observer;

import org.example.behavioural.observer.publisher.NewsletterPublisher;
import org.example.behavioural.observer.subscriber.EmailSubscriber;
import org.example.behavioural.observer.subscriber.SmsSubscriber;
import org.example.behavioural.observer.subscriber.Subscriber;

public class Main {
    public static void main(String[] args) {
        NewsletterPublisher techWeekly = new NewsletterPublisher("Tech Weekly");

        Subscriber alice = new EmailSubscriber("alice@example.com");
        Subscriber bob = new SmsSubscriber("+91-9876543210");

        techWeekly.subscribe(alice);
        techWeekly.subscribe(bob);
        techWeekly.publish("Issue #1");

        techWeekly.unsubscribe(bob);
        techWeekly.publish("Issue #2");
    }
}
