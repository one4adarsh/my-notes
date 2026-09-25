# Strategy pattern
- https://github.com/dstar55/100-words-design-patterns-java#strategy

### Technical thought process to implement Strategy pattern
1. Implement a common interface for all the algorithms (strategies) that you want to use.
2. Create concrete classes that implement the common interface for each algorithm (strategy).
3. Create a context class that will use the common interface to call the algorithm (strategy)
4. The client code will create an instance of the context class and set the desired algorithm (strategy) at runtime.
5. The context class will then use the selected algorithm (strategy) to perform the task.

### Example: Payment options in a Shopping Cart
The payment options in a Shopping Cart are an example of a Strategy. A user can choose various payment
options, such as MasterCard, Amex or PayPal. Any of these will pay for the items in the cart, and they can be
used interchangeably. The user picks the Strategy based on their possibilities and preferences.

| Pattern role      | Class                                                             | Step |
|-------------------|-------------------------------------------------------------------|------|
| Strategy          | `payment/PaymentStrategy` — `void pay(double amount)`             | 1    |
| Concrete Strategy | `payment/CreditCardPaymentStrategy` (MasterCard, Amex, ...)       | 2    |
| Concrete Strategy | `payment/PayPalPaymentStrategy`                                   | 2    |
| Context           | `cart/Cart` — holds `Item`s, `setPaymentStrategy()`, `checkout()` | 3    |
| Client            | `Main` — builds the cart and swaps strategies at runtime          | 4, 5 |

`Cart` depends only on the `PaymentStrategy` interface, so new payment methods can be added without
touching the cart. `checkout()` delegates to the selected strategy with the cart total and then empties
the cart; calling it with no strategy set throws `IllegalStateException`.

```java
Cart cart = new Cart();
cart.addItem(new Item("Book", 29.99));
cart.addItem(new Item("Pen", 9.99));

cart.setPaymentStrategy(new CreditCardPaymentStrategy("MasterCard", "5555444433331111"));
cart.checkout();   // Paid 39.98 using MasterCard card ****1111

cart.addItem(new Item("Keyboard", 45.00));
cart.setPaymentStrategy(new PayPalPaymentStrategy("user@example.com"));
cart.checkout();   // Paid 45.00 using PayPal account user@example.com
```

Run the demo:
```bash
./gradlew :lld-design-patterns:run -PmainClass=org.example.behavioural.strategy.Main
```
(or run `Main` directly from the IDE)