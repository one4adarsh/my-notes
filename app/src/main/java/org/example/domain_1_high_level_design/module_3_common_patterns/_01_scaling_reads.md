<!-- TOC -->
* [_01_scaling_reads](#_01_scaling_reads)
  * [Problem](#problem)
  * [Architectural Strategies](#architectural-strategies)
    * [Optimize Read Performance Within Your Database](#optimize-read-performance-within-your-database)
      * [Indexing](#indexing)
      * [Scaling Hardware](#scaling-hardware)
      * [Denormalization Strategies](#denormalization-strategies)
    * [Scale Your Database Horizontally](#scale-your-database-horizontally)
      * [Read Replicas](#read-replicas)
      * [Database Sharding](#database-sharding)
    * [Add External Caching Layers](#add-external-caching-layers)
      * [Application-Level Caching](#application-level-caching)
      * [CDN and Edge Caching](#cdn-and-edge-caching)
  * [When to use](#when-to-use)
  * [When NOT to use](#when-not-to-use)
<!-- TOC -->

# _01_scaling_reads

- address the challenge of serving high-volume read requests when application grows from hundreds to millions of users
- read traffic grows faster than write traffic

## Problem

- As the number of reads increase, your database will struggle under the load.
- Physical constraints -
    - CPU cores can execute only a limited instruction per second
    - Memory can hold only a limited amount of data
    - Disk I/O is bounded by speed of spinning platters or SSD write cycles
    - Network bandwidth is limited by the speed of the network interface and the underlying infrastructure

## Architectural Strategies

- Optimize read performance within your database
- Scale your database horizontally
- Add external caching layers

### Optimize Read Performance Within Your Database

- Most read scaling problems can be solved with proper tuning and smart data modeling.

#### Indexing

- The first thing to consider when addressing a read scaling problem is to add indexes to the columns you frequently
  query, join on, or sort by.
- An index is essentially a sorted lookup table that points to rows in your actual data.
- When you query without an index, the database performs what's called a full table scan, it reads every single row to
  find matches.
- With an index, it can jump directly to the relevant rows.
- This turns a linear O(n) operation into a logarithmic O(log n).
- There are different types of indexes -
    - B-tree being the most common for general queries
    - Hash indexes work well for exact matches
    - specialized indexes handle full-text search or geographic queries

#### Scaling Hardware

- Swapping spinning disks for SSDs can give you 10-100x faster random I/O.
- Adding more RAM means more of your dataset sits in memory instead of on disk.
- Faster CPUs and more cores mean you can handle more concurrent queries without breaking a sweat.

#### Denormalization Strategies

- Normalization is the process of structuring data to reduce redundancy by splitting information across multiple tables
  to avoid storing duplicate data.
- While this saves storage space, it makes queries more complex because you need joins to bring related data back
  together.

- Denormalization is the process of combining tables to reduce the number of joins and improve read performance, at the
  cost of some redundancy.

- For read-heavy systems
    - denormalization trades storage for speed
    - optimizing for read performance makes sense even if it complicates writes

- Materialized views take this further by precomputing expensive aggregations.

### Scale Your Database Horizontally

- When a single database server hits its limits, add more servers. This is where things get interesting - and complex.

#### Read Replicas

- Read replicas copy data from your primary database to additional servers.
- All writes go to the primary, but reads can go to any replica.
- Beyond solving the throughput problem, read replicas also provide redundancy as a nice added benefit.
    - If your primary database fails, you can promote a replica to become the new primary, minimizing downtime.
- Leader-follower replication is the standard approach.
    - one primary (leader) handles writes
    - multiple secondaries (followers) handle reads
    - Replication can be -
        - synchronous (slower but consistent)
        - asynchronous (faster but potentially stale)

- The key challenge is replication lag. When you write to the primary, it takes time to propagate to replicas.
    - Synchronous replication ensures data consistency but introduces latency.
    - Asynchronous replication is faster but introduces potential data inconsistencies.

#### Database Sharding

- Read replicas distribute load but don't reduce the dataset size that each database needs to handle.
- If dataset becomes so large that even well-indexed queries are slow, database sharding can help by splitting data
  across multiple databases.

- For read scaling, sharding helps in two main ways:
    - smaller datasets mean faster individual queries
    - distribute read load across multiple databases

- Functional sharding splits data by business domain or feature rather than by records.
- Geographic sharding is particularly effective for global read scaling.

- Sharding is primarily a write scaling technique.

### Add External Caching Layers

- when you've optimized your database but still need more read performance
    - add read replicas
    - add caching layers
- Both distribute read load, but caches typically provide better performance for read-heavy workloads.

#### Application-Level Caching

- In-memory caches like Redis sit between your application and database.
- Cache invalidation remains the primary challenge. When data changes, you need to ensure caches don't serve stale data.
    - TTL expiration - Set a fixed lifetime for cached entries.
    - Write-through invalidation - Update or delete cache entries immediately when writing to the database.
    - Write-behind invalidation - Update the cache asynchronously after writing to the database.
    - Tagged invalidation - Invalidate cache entries based on tags or categories.
    - Versioned keys - Use version numbers in cache keys to manage updates and invalidation.

#### CDN and Edge Caching

- While originally designed for static assets, modern CDNs cache dynamic content including API responses and database
  query results.
- For read-heavy applications, CDN caching can reduce origin load by 90% or more.
- Product pages, user profiles, search results - anything that multiple users request becomes a candidate for edge
  caching.

## When to use

- URL Shortener
- Ticketmaster
- News Feed Systems
- Youtube/Video Platforms

## When NOT to use

- Small scale applications
- Write-heavy systems (like Uber's location tracking)
- Strongly consistent systems (like financial, inventory management)
- Real-time collaborative systems (like Google Docs)