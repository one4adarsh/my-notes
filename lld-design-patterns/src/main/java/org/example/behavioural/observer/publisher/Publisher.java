package org.example.behavioural.observer.publisher;

import org.example.behavioural.observer.subscriber.Subscriber;

public interface Publisher {
    void subscribe(Subscriber subscriber);
    void unsubscribe(Subscriber subscriber);
    void notifySubscribers();
}
