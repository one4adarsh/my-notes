# LLD: Flipkart Minutes

## Candidate Prompt

![Quick Commerce System - part 1](Quick%20Commerce%20System%20-%20part%201.png)
![Quick Commerce System - part 2](Quick%20Commerce%20System%20-%20part%202.png)

## Step 1 - Requirements

> Read the problem at least three times: understand → discuss and decode → verify and finalize.

**Primary capabilities**

- `onboardCustomer(id, name)`
- `onboardDeliveryPartner(id, name)`
- `placeOrder(customerId, itemName)` → returns `Order` (system-generated id)
- `cancelOrder(orderId)`
- `pickupOrder(partnerId, orderId)`
- `deliverOrder(partnerId, orderId)`
- `getOrderStatus(orderId)`
- `getPartnerStatus(partnerId)`

```text
Order:   PENDING ──assign──▶ ASSIGNED ──pickup──▶ PICKED_UP ──deliver──▶ DELIVERED
            │                   │
            └──cancel──▶ CANCELLED ◀──cancel──┘        (no cancel after PICKED_UP)


Partner: AVAILABLE ──assign──▶ BUSY ──(deliver | order cancelled)──▶ AVAILABLE
```

**Rules and Completion**

- Delivery partner holds exactly one order.
- No partner free → Order waits in FIFO queue.
- Partner becomes free (deliver or cancel-before-pickup) → immediately assign next queued order.
- Canceled order must not be dequeued and assigned.

**Error Handling**

- duplicate id on onboard
- unknown ids
- cancel after pickup
- pickup by a partner who isn't the assignee
- deliver before pickup
- double cancel

**Scope boundaries**

- in-memory only
- no UI/HTTP
- single JVM

- I'm reading the requirements as two state machines plus a dispatch workflow.
- The hard part is the dispatcher under concurrency, so that's where I'll focus design effort.

## Step 2 - Entities and Relationships

> Entities are the nouns that maintain changing state or enforce rules; everything else is an attribute.

### Entities

- `Customer` -> entity
- `DeliveryPartner` -> entity
- `Order` -> entity
- `Item` -> attribute of Order (just a name, no stock or rules)
- `OrderStatus`, `PartnerStatus` enums

### Relationships

- QuickCommerceService (facade / entrypoint)
    - uses CustomerRepository
    - uses DeliveryPartnerRepository
    - uses OrderRepository
    - uses OrderDispatcher (owns pending-order queue; the real orchestrator)
        - uses PartnerAssignmentStrategy (picks a partner from available ones)

```text
1 Customer <-> 1..* Order
1 DeliveryPartner <-> 0..1 Order
```

## Step 3 - Class Design

### Orchestrators

```java
class QuickCommerceService {
    Customer onboardCustomer(String id, String name);

    DeliveryPartner onboardDeliveryPartner(String id, String name);

    Order placeOrder(String customerId, String itemName);

    void cancelOrder(String orderId);

    void pickupOrder(String partnerId, String orderId);

    void deliverOrder(String partnerId, String orderId);

    OrderStatus getOrderStatus(String orderId);

    PartnerStatus getPartnerStatus(String partnerId);
}
```

```java
class OrderDispatcher {
    // Queue<Order> pendingOrders, ReentrantLock lock, DeliveryPartnerRepository, PartnerAssignmentStrategy
    void submit(Order order);                    // try assign now, else enqueue

    void releasePartner(DeliveryPartner p);      // after deliver: release + assign next queued order

    void onOrderCancelled(Order order);          // after cancel: dequeue, or release partner + assign next

    private void assignNextPending(DeliveryPartner p);   // poll queue, skip cancelled

    private boolean tryAssign(Order o, DeliveryPartner p); // order first, then partner
}
```

### Abstractions/Strategies

```java
interface PartnerAssignmentStrategy {
    Optional<DeliveryPartner> select(Collection<DeliveryPartner> available);
}

class FirstAvailablePartnerStrategy implements PartnerAssignmentStrategy {
}
```

### Repositories

```text
Repository<T>                         ← interface (contract)
    ▲
InMemoryRepository<T>                 ← abstract class (shared ConcurrentHashMap logic)
    ▲
    ├── CustomerRepository            ← extends InMemoryRepository<Customer>
    ├── DeliveryPartnerRepository     ← extends InMemoryRepository<DeliveryPartner> + findAvailable()
    └── OrderRepository               ← extends InMemoryRepository<Order>
```

```java
interface Repository<T> {
    void save(T entity);              // throws DuplicateEntityException

    Optional<T> findById(String id);

    Collection<T> findAll();
}

abstract class InMemoryRepository<T> implements Repository<T> {
    ConcurrentHashMap<String, T> store;
    Function<T, String> idExtractor;  // subclass passes Customer::getId etc.
    // save uses putIfAbsent -> single atomic duplicate guard
}

class DeliveryPartnerRepository extends InMemoryRepository<DeliveryPartner> {
    List<DeliveryPartner> findAvailable();   // snapshot filtered by isAvailable()
}
```

### Domain models

```java
class Order {
    // id, customerId, itemName, status, partnerId, createdAt
    synchronized void assignTo(partnerId);   // PENDING→ASSIGNED

    synchronized void markPickedUp();        // ASSIGNED→PICKED_UP

    synchronized void markDelivered();       // PICKED_UP→DELIVERED

    synchronized void cancel();              // PENDING|ASSIGNED→CANCELLED else throw
}

class DeliveryPartner {
    // id, name, status, currentOrderId
    synchronized void assign(orderId);       // AVAILABLE→BUSY else throw

    synchronized void release();             // BUSY→AVAILABLE else throw

    synchronized boolean isAvailable();
}

class Customer {
    // id, name
}

enum OrderStatus {
    PENDING, ASSIGNED, PICKED_UP, DELIVERED, CANCELLED
}

enum PartnerStatus {
    AVAILABLE, BUSY
}
```

### Exception

```java
class QuickCommerceException extends RuntimeException {
} // (base, runtime)

class EntityNotFoundException extends QuickCommerceException {
}

class DuplicateEntityException extends QuickCommerceException {
}

class InvalidStateTransitionException extends QuickCommerceException {
} // order/partner transitions + wrong-partner pickup/deliver
```

## Step 4 - Implementation

- Collect thoughts: top-down
- Implementation: bottom-up (don't write anything that calls something which is still a stub)

> Writing order (each compiles before the next):
> enums → exceptions → models → repository → strategy → dispatcher → service → main

| #   | File                                                   | Why now?                                                                             |
|-----|--------------------------------------------------------|--------------------------------------------------------------------------------------|
| 1   | `model/Order.java`                                     | Zero dependencies. Pure state machine; locks in the rules everything else relies on. |
| 2   | `model/DeliveryPartner.java`                           | Same shape as Order, simpler (2 transitions + `isAvailable()`).                      |
| 3   | `repository/InMemoryRepository`                        | Trivial `ConcurrentHashMap` ops; unblocks service and dispatcher.                    |
| 3.1 | `repository/DeliveryPartnerRepository.findAvailable()` | Find a free partner when assigning an order; needed by dispatcher.                   |
| 4   | `strategy/FirstAvailablePartnerStrategy`               | One line -> `available.stream().findFirst()`; needed by dispatcher.                  |
| 5   | `service/OrderDispatcher`                              | `submit` → `releasePartner` → `onOrderCancelled`; write under lock from the start.   |
| 6   | `service/QuickCommerceService`                         | Thin facade; after dispatcher so `cancel`/`deliver` know the dispatcher contract.    |
| 7   | `LldFlipkartMinutes` scenarios 1–7                     | Sequential demo; run after every 2 scenarios.                                        |
| 8   | `LldFlipkartMinutes` scenario 8                        | Concurrency demo, only after 1–7 are green.                                          |

**Concurrency summary**

| Race                                          | Closed by                                                             |
|-----------------------------------------------|-----------------------------------------------------------------------|
| Two `placeOrder`s grab the same free partner  | dispatcher `ReentrantLock` around select + assign                     |
| `cancel` vs dispatcher `assign` on same order | `Order` methods `synchronized`; `tryAssign` order-first, catch → skip |
| `deliver` frees partner while order arrives   | both paths go through dispatcher lock                                 |
| Duplicate onboarding                          | `ConcurrentHashMap.putIfAbsent` in `save`                             |
| Lock ordering                                 | always dispatcher → order/partner monitor, never reverse              |

## Step 5 - Extensibility

> Start bonus features only after P0 is green (demo + tests). Order is by **value ÷ time** and **dependencies**:
> each item builds on the one before, and each one can be demoed on its own.

### Bonus Features (explicit, in implementation order)

| #  | Feature              | Requirement (from prompt)                                                        | Design                                                                                                                                                                                                                                                                | Pattern                     | Why this order                                                                                                                                                             |
|----|----------------------|----------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| E1 | Notifications        | Notify customers and partners (simulated logs) on order status changes           | `OrderEventListener` interface + `LoggingNotifier`. `QuickCommerceService` / `OrderDispatcher` publish `(order, oldStatus, newStatus)` after each transition. Listeners are kept in a `CopyOnWriteArrayList`                                                          | Observer                    | Smallest change, touches every transition, shows up right away in demo output. B2–B4 events reuse it for free                                                              |
| E2 | Auto-cancel (30 min) | If no partner picks up within 30 min of creation, cancel whether assigned or not | `ScheduledExecutorService` schedules a task in `placeOrder` using `Order.createdAt`. The task calls `service.cancelOrder(id)`, not `order.cancel()`, so the dispatcher frees the partner and reassigns. Make the timeout configurable (`Duration`)                    | Scheduler / Command         | Reuses the existing cancel path and state guard, so it is mostly wiring. The timer thread vs. `pickupOrder` race is already closed by `Order`'s `synchronized` transitions |
| E3 | Ratings              | Customers rate partners after successful delivery                                | `RatingService.rate(customerId, orderId, stars)`. Guards: order is `DELIVERED`, `order.customerId == customerId`, one rating per order, `1 ≤ stars ≤ 5`. Partner keeps `deliveryCount` (`AtomicInteger`) and a rating sum and count, so the average stays thread-safe | New service + facade guards | Dashboard (B4) needs this data                                                                                                                                             |
| E4 | Dashboard            | Top partners by number of deliveries and ratings                                 | `DashboardService.topPartners(n)` sorts a snapshot of `findAll()` with a `PartnerRankingStrategy` (default: `avgRating` desc, then `deliveryCount` desc)                                                                                                              | Strategy / Comparator       | Read-only query; depends on B3                                                                                                                                             |

### Probes the interviewer will likely invent (implicit, in likely order)

> Usually you only explain these. Implement one only if the interviewer asks. Each one should be a local change, which proves the design.

| #  | Probe                                      | Change required                                                                                                      | Untouched                                                           |
|----|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| I1 | Assign the nearest / highest-rated partner | New `PartnerAssignmentStrategy` impl (+ `location` on `Order`/`DeliveryPartner` for nearest)                         | Dispatcher, service, models' state machines                         |
| I2 | Partner goes offline / off-shift           | Add `OFFLINE` to `PartnerStatus`; `goOnline()`/`goOffline()` on partner; `isAvailable()` excludes it                 | `findAvailable()`, strategy, dispatcher work automatically          |
| I3 | Priority orders                            | Swap `ConcurrentLinkedQueue` → `PriorityBlockingQueue<Order>` with a comparator in `OrderDispatcher`                 | Everything outside the dispatcher                                   |
| I4 | Partner can carry N orders (batching)      | `DeliveryPartner`: `currentOrderId` → bounded `Set<String>`; `isAvailable()` = `size < capacity`; `release(orderId)` | Strategy, repository; dispatcher only passes `orderId` to `release` |
| I5 | New order state (e.g. `OUT_FOR_DELIVERY`)  | Add enum value + one guarded transition in `Order`; one facade method                                                | Dispatcher, repositories                                            |
| I6 | Multiple items per order                   | `itemName` → `List<String> items` on `Order`                                                                         | Whole workflow                                                      |
| I7 | Swap in-memory for a real DB               | New `Repository<T>` impl; change wiring in `bootstrap()`                                                             | Service, dispatcher (constructor injection)                         |

### Patterns to name

- Already in P0:
  - Facade (`QuickCommerceService`)
  - Strategy (`PartnerAssignmentStrategy`)
  - Repository
  - Template (abstract `InMemoryRepository` + `idExtractor`)
  - State machine (guarded transitions in models)
- Added in Step 5:
  - Observer (B1)
  - Scheduler/Command (B2)
  - Strategy/Comparator (B4)