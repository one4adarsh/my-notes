# Factory Method
- https://github.com/dstar55/100-words-design-patterns-java#factory-method

### Technical thought process to implement Factory Method pattern
1. Create a product interface that declares the operations common to every object the factory can create.
2. Create concrete product classes that implement the product interface, one for each variant.
3. Create a factory class with a creation method that takes an input (e.g. a type) and decides which concrete product to instantiate.
4. Return the created object typed as the product interface, and fail fast (e.g. `IllegalArgumentException`) for unsupported inputs.
5. The client code asks the factory for an object instead of calling `new` on a concrete class, and works with it only through the product interface.

### Example: Notification service
A notification service can deliver a message over different channels, such as email or SMS. The client
only knows it wants to send a notification of a given type; the factory hides which concrete class is
created and how. Supporting a new channel (push, Slack, ...) means adding a new product class and a
branch in the factory, while client code stays unchanged.

| Pattern role     | Class                                                                          | Step |
|------------------|--------------------------------------------------------------------------------|------|
| Product          | `notification/Notification` — `void send(String message)`                      | 1    |
| Concrete Product | `notification/EmailNotification`                                               | 2    |
| Concrete Product | `notification/SMSNotification`                                                 | 2    |
| Factory          | `notification/NotificationFactory` — `static Notification create(String type)` | 3, 4 |
| Client           | `Main` — requests notifications by type and sends messages                     | 5    |

`NotificationFactory.create()` maps `"email"` to `EmailNotification` and `"sms"` to `SMSNotification`,
and throws `IllegalArgumentException` for any other type. The client depends only on the `Notification`
interface, so it is decoupled from the concrete classes and their construction.

> Note: this example uses a single static creation method (often called a *Simple Factory*). In the
> classic GoF *Factory Method*, the creation method is declared in a base creator class and each
> subclass overrides it to decide which product to create.

Usage:
```java
Notification notif = NotificationFactory.create("email");
notif.send("Hello");   // sent via EmailNotification

Notification sms = NotificationFactory.create("sms");
sms.send("Hello");     // sent via SMSNotification

NotificationFactory.create("fax");   // throws IllegalArgumentException
```

Run the demo:
```bash
./gradlew :lld-design-patterns:run -PmainClass=org.example.creational.factory.Main
```
(or run `Main` directly from the IDE)
