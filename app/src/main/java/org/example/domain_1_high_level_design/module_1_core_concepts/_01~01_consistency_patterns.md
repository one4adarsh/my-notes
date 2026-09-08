<!-- TOC -->
* [_01~01_consistency_patterns](#_0101_consistency_patterns)
  * [What is Consistency?](#what-is-consistency)
  * [Types of Consistency](#types-of-consistency)
    * [Strong Consistency](#strong-consistency)
      * [Replication](#replication)
      * [Characteristics](#characteristics)
      * [Pros](#pros)
      * [Cons](#cons)
      * [Use Cases](#use-cases)
      * [Examples](#examples)
    * [Eventual Consistency](#eventual-consistency)
      * [Replication](#replication-1)
      * [Characteristics](#characteristics-1)
      * [Pros](#pros-1)
      * [Cons](#cons-1)
      * [Use Cases](#use-cases-1)
      * [Examples](#examples-1)
    * [Weak Consistency](#weak-consistency)
      * [Replication](#replication-2)
      * [Characteristics](#characteristics-2)
      * [Pros](#pros-2)
      * [Cons](#cons-2)
      * [Use Cases](#use-cases-2)
      * [Examples](#examples-2)
  * [Strong vs Eventual vs Weak](#strong-vs-eventual-vs-weak)
  * [Linearizability](#linearizability)
      * [Implementation](#implementation)
      * [Pros](#pros-3)
      * [Cons](#cons-3)
      * [Use Cases](#use-cases-3)
  * [Causal Consistency](#causal-consistency)
      * [Implementation](#implementation-1)
      * [Pros](#pros-4)
      * [Cons](#cons-4)
      * [Use Cases](#use-cases-4)
  * [Strong vs Eventual (Interview Favorite)](#strong-vs-eventual-interview-favorite)
  * [Real-world Mapping](#real-world-mapping)
<!-- TOC -->

# _01~01_consistency_patterns

https://systemdesign.one/consistency-patterns/
https://hackernoon.com/eventual-vs-strong-consistency-in-distributed-databases-282fdad37cf7

## What is Consistency?

Consistency defines when all replicas observe the same data after write.

It determines:

- Data freshness
- Read correctness
- Replication behavior
- User experience

## Types of Consistency

### Strong Consistency

Every read returns the latest committed write.

No stale reads are allowed.

#### Replication

- Synchronous

#### Characteristics

- Latest data always
- Higher latency
- Lower availability during synchronization
- Simpler application logic

#### Pros

- Correct data
- No stale reads
- High durability

#### Cons

- Higher latency
- Lower throughput
- Resource intensive

#### Use Cases

- Banking
- Payments
- Airline booking
- Inventory
- Healthcare
- Relational databases

#### Examples

- Google Spanner
- Google BigTable
- Paxos
- Raft
- 2PC

### Eventual Consistency

Replicas may temporarily return stale data but eventually converge to the latest state.

#### Replication

- Asynchronous

#### Characteristics

- Temporary stale reads
- Low latency
- High availability
- Highly scalable

#### Pros

- Fast writes
- High throughput
- Better scalability
- Better fault tolerance

#### Cons

- Data conflicts
- Stale reads
- Conflict resolution required

#### Use Cases

- Social media feeds
- DNS
- Search indexing
- URL Shortener
- Product catalog
- Shopping cart
- Amazon S3
- Live comments

#### Examples

- Cassandra
- Amazon Dynamo
- DynamoDB
- Couchbase

### Weak Consistency

Reads may or may not return the latest data.

#### Replication

- Best effort

#### Characteristics

- Lowest latency
- Highest availability
- Possible data loss

#### Pros

- Extremely fast
- Highly available

#### Cons

- Data loss
- Data inconsistency
- No freshness guarantee

#### Use Cases

- Cache (Write-back)
- Live streaming
- VoIP
- Multiplayer games
- Data backups

#### Examples

- Redis
- Memcached
- CDN

## Strong vs Eventual vs Weak

| Feature      | Strong | Eventual   | Weak           |
|--------------|--------|------------|----------------|
| Latest Read  | Always | Eventually | Not guaranteed |
| Replication  | Sync   | Async      | Best effort    |
| Latency      | High   | Low        | Very Low       |
| Availability | Lower  | High       | Highest        |
| Scalability  | Lower  | High       | Highest        |
| Stale Reads  | Never  | Temporary  | Common         |
| Data Loss    | No     | Possible   | Possible       |

## Linearizability

A stronger form of **Strong Consistency**.

Once a write completes,
every future read immediately sees that value.

The system behaves like a **single machine**.

#### Implementation

- Single Leader
- Quorum
- Paxos
- Raft

#### Pros

- Simple programming model
- Immediate visibility

#### Cons

- High latency
- Lower scalability
- Lower availability

#### Use Cases

- Distributed locks
- Unique username generation
- Banking

## Causal Consistency

Guarantees **cause-effect ordering**.

Related operations appear in order.

Unrelated operations may appear in any order.

Example:

```
User A posts

↓

User B replies

Everyone sees:

Post
Reply
```

#### Implementation

- Vector clocks
- Version vectors

#### Pros

- Low latency
- High availability
- Stronger than eventual consistency

#### Cons

- Doesn't totally order concurrent events
- More complex

#### Use Cases

- Chat applications
- Slack
- Reddit comments
- Collaborative editing

## Strong vs Eventual (Interview Favorite)

| Strong                   | Eventual                     |
|--------------------------|------------------------------|
| Latest data              | May return stale data        |
| Sync replication         | Async replication            |
| High latency             | Low latency                  |
| Lower availability       | Higher availability          |
| Easier application logic | Conflict resolution required |
| e.g. - Banking           | e.g. - Social media          |

---

## Real-world Mapping

| System            | Consistency |
|-------------------|-------------|
| Banking           | Strong      |
| Airline Booking   | Strong      |
| Inventory         | Strong      |
| Social Media Feed | Eventual    |
| Product Catalog   | Eventual    |
| DNS               | Eventual    |
| Search Engine     | Eventual    |
| Multiplayer Game  | Weak        |
| Live Streaming    | Weak        |
| Write-back Cache  | Weak        |
