# Observer Pattern
- https://github.com/dstar55/100-words-design-patterns-java#observer

### Technical thought process to implement Observer pattern
1. Create a subject interface that maintains a list of observers and provides methods to attach, detach, and notify observers.
2. Create a concrete subject class that implements the subject interface and maintains the state of the object. When the state changes, it notifies all registered observers.
3. Create an observer interface that defines the update method to be called when the subject's state changes.
4. Create concrete observer classes that implement the observer interface and define the update method to handle the state change notification
5. The client code will create instances of the concrete subject and concrete observers, attach the observers to the subject, and change the state of the subject to trigger notifications.

### Example: Newsletter subscription
A newsletter subscription demonstrates the Observer pattern. A newsletter is a regularly distributed
publication about one main topic of interest to its subscribers. Subscribers can subscribe or unsubscribe
at any time, and every published issue is pushed to whoever is currently subscribed.

| Pattern role      | Class                                                                                            | Step |
|-------------------|--------------------------------------------------------------------------------------------------|------|
| Subject           | `publisher/Publisher` — `subscribe()`, `unsubscribe()`, `notifySubscribers()`                    | 1    |
| Concrete Subject  | `publisher/NewsletterPublisher` — holds subscribers; `publish(issue)` changes state and notifies | 2    |
| Observer          | `subscriber/Subscriber` — `update(newsletterName, issue)`                                        | 3    |
| Concrete Observer | `subscriber/EmailSubscriber`, `subscriber/SmsSubscriber`                                         | 4    |
| Client            | `Main` — subscribes two readers, publishes, unsubscribes one, publishes again                    | 5    |

`NewsletterPublisher` knows subscribers only through the `Subscriber` interface, so new delivery
channels (push, Slack, ...) can be added without touching the publisher. `notifySubscribers()`
iterates over a copy of the list so a subscriber may safely unsubscribe from inside `update()`.
Duplicate `subscribe()` calls are ignored.

```java
NewsletterPublisher techWeekly = new NewsletterPublisher("Tech Weekly");

Subscriber alice = new EmailSubscriber("alice@example.com");
Subscriber bob   = new SmsSubscriber("+91-9876543210");

techWeekly.subscribe(alice);
techWeekly.subscribe(bob);
techWeekly.publish("Issue #1");   // alice and bob are notified

techWeekly.unsubscribe(bob);
techWeekly.publish("Issue #2");   // only alice is notified
```

Run the demo:
```bash
./gradlew :lld-design-patterns:run -PmainClass=org.example.behavioural.observer.Main
```
(or run `Main` directly from the IDE)

