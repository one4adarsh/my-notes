<!-- TOC -->
* [_03_dealing_with_contention](#_03_dealing_with_contention)
  * [Problem - The Race Condition](#problem---the-race-condition)
  * [Solution](#solution)
    * [Conditional Writes](#conditional-writes)
    * [Pessimistic Locking](#pessimistic-locking)
    * [Optimistic Concurrency Control (OCC)](#optimistic-concurrency-control-occ)
    * [Isolation Levels](#isolation-levels)
    * [Distributed Locks](#distributed-locks)
  * [Choosing the Right Approach](#choosing-the-right-approach)
  * [When to use](#when-to-use)
  * [When NOT to use](#when-not-to-use)
  * [Common Deep Dives](#common-deep-dives)
    * [How to prevent deadlocks with pessimistic locking ?](#how-to-prevent-deadlocks-with-pessimistic-locking-)
    * [How to handle retries with optimistic concurrency control ?](#how-to-handle-retries-with-optimistic-concurrency-control-)
    * [How to choose between pessimistic and optimistic locking ?](#how-to-choose-between-pessimistic-and-optimistic-locking-)
    * [How do you handle ABA problem in optimistic concurrency control?](#how-do-you-handle-aba-problem-in-optimistic-concurrency-control)
    * [What about performance when everyone wants the same resource?](#what-about-performance-when-everyone-wants-the-same-resource)
<!-- TOC -->

# _03_dealing_with_contention

- contention occurs when multiple processes compete for the same resource at the same time.
- without proper handling, you get race conditions, double-bookings, and inconsistent state.

## Problem - The Race Condition

- A race condition occurs when the outcome of a process depends on the sequence of other uncontrollable events.
- The race exists because the read and write aren't atomic.
- Reading the count and writing the new value back are two separate steps, and in between them the world can change.
- This race condition is a lost update, the classic failure of a read-modify-write cycle.

## Solution

- There are several ways to make the read-modify-write safe.
- Each relies on the same compare-and-set technique, a read-modify-write where you make the write conditional on the
  value not having changed.

### Conditional Writes

> Using WHERE clauses to guard updates is the simplest way to avoid contention.
> Transactions are a more general solution, but they are heavier weight and slower than a single conditional write.

- When your rule is an if about the current data, the database can check it and make the change in a single statement,
  no locks or version numbers required.
- The reason is that the database won't let two updates change the same row at the same time.
- DB makes updates take turns, and the one that has to wait doesn't get to act on the value it first read.

- A real purchase is more than a counter, though.
- This is what transactions are for. You wrap the operations in BEGIN and COMMIT.
- Database treats them as one unit that either fully happens or fully rolls back.
- The fix is to guard the resource people are actually fighting over.


- As long as the database can evaluate your guard as part of write, you're good, and you don't need an explicit lock.
- The database can only enforce rules it can read in a WHERE clause.

### Pessimistic Locking

> SELECT ... FOR UPDATE is a pessimistic lock, and it works by locking the row for exclusive access until the
> transaction commits or rolls back.

- Pessimistic locking is a strategy where you assume that conflicts will happen, and you lock the resource for exclusive
  access.
- Pessimistic locking prevents conflicts by acquiring locks upfront (explicit row lock).


- The catch is that you've locked every open seat in the section just to claim four.
- The lock does its job in protecting resources from contention, but it's expensive.
- Common mistakes
    - Locking too much, for too long.
    - Deadlocks from inconsistent ordering.
- The main downside of pessimistic locking is that every transaction pays the locking cost, even when there is no
  conflict, including the overwhelming majority that were never going to collide with anyone.

### Optimistic Concurrency Control (OCC)

> WHERE version = ... is an optimistic concurrency control, and it works by checking the version before committing.

- Optimistic concurrency control is a strategy where you assume that conflicts are rare, and you check for conflicts
  before committing.
- OCC requires a value in the row that changes every time the row is written.


- When collisions are common, lock upfront, because constant retries cost more than the lock ever did.
- When collisions are rare, which describes most e-commerce traffic, take the optimistic path and eat the occasional
  retry.

### Isolation Levels

> SERIALIZABLE is the only isolation level that prevents write skew, but it is expensive. Pessimistic locking or OCC is
> often a better choice.

- Isolation levels are a way to control how transactions interact with each other.
- The higher the isolation level, the more protection you get from other transactions, but the slower your transaction
  will be.
- The lower the isolation level, the faster your transaction will be, but the more likely you are to see anomalies from
  other transactions.


- Sometimes two transactions each read an overlapping set of rows, each makes a decision that's perfectly valid on its
  own, and only together do they break a rule. This is called write skew.

- Most databases offer four standard isolation levels, each with different trade-offs between performance and
  consistency.
    - Read Uncommitted → Can see uncommitted changes from other transactions (rarely used)
    - Read Committed → Can only see committed changes (default in PostgreSQL)
    - Repeatable Read → Same data read multiple times within a transaction stays consistent (default in MySQL)
    - Serializable → Strongest isolation, transactions appear to run one after another

- None of the weaker levels catch write skew, REPEATABLE READ included. SERIALIZABLE is the one that does.

- SERIALIZABLE is expensive option, pessimistic locking or OCC is often a better choice.
- The database can only protect a conflict it can see, and your job in all of this is to make sure the contended thing
  exists as a single addressable cell, a row, a key, or an item the store can check as it writes.

### Distributed Locks

> Redis, Database columns, ZooKeeper and etcd are all options for distributed locks to coordinate access to shared
> resources, each with different trade-offs.

- Sometimes you need to hold exclusive access across something a transaction can't span.
- The fix is to stop holding the lock as a transaction and start holding it as data.

- You have a few options for where that lock lives, and each trades off speed, infrastructure, and how gracefully it
  handles failure:
    - Redis with TTL → atomic operations with automatic expiration
    - Database columns → a lock is just two columns on the row, one for who holds it and one for when the hold expires
    - ZooKeeper/etcd → purpose-built coordination services designed specifically for distributed systems with strong
      consistency guarantees

## Choosing the Right Approach

1. Your check is a predicate on the row you're writing
    - use a conditional `UPDATE` and gate any follow-up work on the affected row count
    - no explicit lock needed
2. You've to read, decide in application code, then write
    - Pessimistic locking with `SELECT ... FOR UPDATE` holds the rows.
    - default when contention is high
3. Same read-decide-write, but collisions are rare
    - Optimistic concurrency control with a version column and `WHERE version = ...` on the `UPDATE`
    - retry when write fails due to version mismatch
    - better performance when conflicts are rare
4. Invariant spans rows that never collide i.e. write skew
    - Use `SERIALIZABLE` isolation level so the database catches the conflict
    - materialize onto a single row if possible, so you can use lock or OCC instead
5. The lock has to outlive a single transaction
    - Use a distributed lock when exclusivity has to span a wait

## When to use

- Online Auction System → Optimistic Concurrency Control
- Ticketmaster → Pessimistic Locking, 10 min TTL using distributed locks for UX
- Banking/Payment → Pessimistic Locking or Optimistic Concurrency Control, depending on the operation
- Ride Sharing → Distributed lock, cache with TTL 10 seconds for automatic cleanup
- Flash Sale / Inventory Systems → Mix of approaches, OCC + Distributed Lock with TTL for high contention items
- Yelp → Optimistic Concurrency Control for reviews, likes and votes

## When NOT to use

- Low contention scenarios
- Single-user operations
- Read-heavy workloads

## Common Deep Dives

### How to prevent deadlocks with pessimistic locking ?

- Deadlocks occur when two or more transactions are waiting for each other to release locks, creating a cycle of
  dependencies.
- To prevent deadlocks, you can:
    - Acquire locks in a consistent order across transactions.
    - Use a timeout mechanism to abort transactions that are waiting too long for a lock.
    - Use a deadlock detection algorithm provided by the database, which can automatically detect and resolve deadlocks
      by aborting one of the transactions involved.

### How to handle retries with optimistic concurrency control ?

- When using optimistic concurrency control, you need to handle retries when a write fails due to a version mismatch.
- Implement a retry mechanism that:
    - Catches the version mismatch error.
    - Re-reads the current state of the resource.
    - Applies the intended changes based on the new state.
    - Retries the write operation, possibly with a limit on the number of retries to avoid infinite loops.
- Consider using exponential backoff or jitter to reduce contention during retries.

### How to choose between pessimistic and optimistic locking ?

- Choice between pessimistic & optimistic locking depends on the expected contention level:
    - Use pessimistic locking when:
        - Contention is high, and conflicts are likely.
        - Operations are long-running or involve multiple steps that need to be atomic.
        - The cost of retries is higher than the cost of acquiring locks.
    - Use optimistic locking when:
        - Contention is low, and conflicts are rare.
        - Operations are short-lived and can be retried easily.
        - You want to maximize throughput and minimize locking overhead.

### How do you handle ABA problem in optimistic concurrency control?

- The ABA problem occurs when a resource changes from state A to state B and back to state A, making it appear unchanged
  to a process that is checking for changes.
- To handle the ABA problem in optimistic concurrency control:
    - Use version numbers or timestamps
    - Use unique identifiers for each state change

### What about performance when everyone wants the same resource?

- When everyone wants the same resource, contention can lead to performance degradation.
- The fundamental issue is that normal scaling strategies break down when the contention lands on one specific item.
    - Sharding splits load across rows, but everyone here wants the same row, so there's nothing to split.
    - Load balancing just spreads the requests across servers that then queue up on that same row.
    - Read replicas take read load off the primary, but the fight here is over writes, so they don't touch it.
- To mitigate this, consider:
    - Your first strategy should be questioning whether you can change the problem itself rather than throwing more
      infrastructure at it.
    - For cases where you need strong consistency on a hot resource, implement queue-based serialization.
        - Queue acts as a buffer that can absorb traffic spikes while the worker processes requests at a sustainable
          rate.
    - If you can't change the problem, consider using a distributed lock with a TTL to serialize access to the hot
      resource.
