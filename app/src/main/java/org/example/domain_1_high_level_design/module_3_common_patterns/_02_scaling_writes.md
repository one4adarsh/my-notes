<!-- TOC -->
* [_02_scaling_writes](#_02_scaling_writes)
  * [Problem](#problem)
  * [Architectural Strategies](#architectural-strategies)
    * [Vertical Scaling and Database Choices](#vertical-scaling-and-database-choices)
      * [Vertical Scaling](#vertical-scaling)
      * [Database Choices](#database-choices)
    * [Sharding and Partitioning](#sharding-and-partitioning)
      * [Horizontal Sharding](#horizontal-sharding)
      * [Vertical Partitioning](#vertical-partitioning)
    * [Handling bursts with Queues and Load Shedding](#handling-bursts-with-queues-and-load-shedding)
      * [Write Queues for Burst Handling](#write-queues-for-burst-handling)
      * [Load Shedding](#load-shedding)
    * [Batching and Hierarchical Aggregation](#batching-and-hierarchical-aggregation)
      * [Batching](#batching)
      * [Hierarchical Aggregation](#hierarchical-aggregation)
  * [When to use](#when-to-use)
  * [When NOT to use](#when-not-to-use)
<!-- TOC -->

# _02_scaling_writes

- addresses the challenge of handling high-volume write operations when a single db/server becomes the bottleneck

## Problem

- As the number of writes increase, your database will struggle under the load.
- Writes are bottlenecked by disk I/O, CPU, or network bandwidth.

## Architectural Strategies

- Vertical Scaling and Database Choices
- Sharding and Partitioning
- Handling bursts with Queues and Load Shedding
- Batching and Hierarchical Aggregation

### Vertical Scaling and Database Choices

#### Vertical Scaling

- Increasing the capacity of a single database server (more CPU, RAM, or faster disks) to handle higher write loads.

#### Database Choices

- Underlying data stores are optimized for the writes.
- A great example of this is using a write-heavy database like Cassandra.
    - Cassandra achieves superior write throughput through its append-only commit log architecture.
    - Trade-off is Cassandra's read performance isn't great.
- Time-series databases like InfluxDB or TimescaleDB are built for high-volume sequential writes with timestamps.
- Log-structured databases like LevelDB append new data rather than updating in place.
- Column stores like ClickHouse can batch writes efficiently for analytics workloads

- Beyond this, optimize any database for writes:
    - Disable expensive features like foreign key constraints or full-text search indexing during high-write periods.
    - Tune write-ahead logging - databases like PostgreSQL can batch multiple transactions before flushing to disk.
    - Reduce index overhead - fewer indexes mean faster writes

### Sharding and Partitioning

#### Horizontal Sharding

- Distributing data across multiple database instances based on a sharding key.

- Use a consistent hashing scheme to determine which server to write to.
    - Selecting a Good Partitioning Key
        - The principle here is that we want to select a key that minimizes variance in the number of writes per shard.
        - Keep in mind that we need to also consider how the data might be read.

#### Vertical Partitioning

- Splitting a table into multiple tables based on columns, so that writes to one table don't affect others.
- Break table apart based on how the data is actually used.

- Once logically separated data, move each table to a different database instance which can handle unique workloads

### Handling bursts with Queues and Load Shedding

#### Write Queues for Burst Handling

- Use a message queue (like Kafka, RabbitMQ, or SQS) to buffer incoming write requests.
- Burst absorption: the queue acts as a buffer, smoothing out traffic spikes.
- Your database processes writes at a steady rate while the queue handles bursts.
- Tradeoffs are
    - Queues are inherently async, so clients will often need a way to call back to check write was eventually made to
      the database.
    - Use queues when you expect to have bursts that are short-lived, not to patch a database that can't handle the
      steady-state load.

#### Load Shedding

- When your system is overwhelmed, you need to decide which writes to accept and which to reject.
- A simple solution here is to simply drop the least useful writes during times of system overload.

### Batching and Hierarchical Aggregation

#### Batching

- Instead of processing writes one by one, you batch multiple writes together to amortize overheads like network round
  trips, transaction setup, index updates.
- This can be done at the application layer, as an in-between process, or even at the database layer.
- Batching is especially effective for write-heavy workloads where the order of writes isn't critical.

#### Hierarchical Aggregation

- For workloads that involve aggregating data (like metrics, logs, or analytics), you can reduce write load by
  performing aggregation at multiple levels.
- Instead of writing every single event to the database, you can aggregate events in memory or in a cache and write
  summaries periodically.
- This reduces the number of writes and can also improve read performance.

## When to use

- Instagram/Social Media (sharding and vertical partitioning)
- News Feeds
- Search Applications (partitioning and batching)
- Live Comments (hierarchical aggregation)

## When NOT to use

- Each of these strategies comes with tradeoffs, think prior to implementing them.
    - Queues mean eventual consistency and delay
    - Sharding adds complexity to queries and transactions
    - Partitioning means the read path may be compromised
    - Batching adds latency and moving pieces
