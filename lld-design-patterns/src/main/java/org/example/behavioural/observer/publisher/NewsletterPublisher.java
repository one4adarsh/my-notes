package org.example.behavioural.observer.publisher;

import org.example.behavioural.observer.subscriber.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class NewsletterPublisher implements Publisher {
    private final String name;
    private final List<Subscriber> subscribers = new ArrayList<>();
    private String latestIssue;


    public NewsletterPublisher(String name) {
        this.name = name;
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    @Override
    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void notifySubscribers() {
        // Iterate over a copy so a subscriber may unsubscribe during update()
        for (Subscriber s : List.copyOf(subscribers)) {
            s.update(name, latestIssue);
        }
    }

    // State change that triggers notification
    public void publish(String issue) {
        this.latestIssue = issue;
        notifySubscribers();
    }
}
