<!-- TOC -->
* [_01_cap_theorem](#_01_cap_theorem)
  * [What is CAP Theorem?](#what-is-cap-theorem)
  * [CAP Components](#cap-components)
    * [Consistency (C)](#consistency-c)
    * [Availability (A)](#availability-a)
    * [Partition Tolerance (P)](#partition-tolerance-p)
    * [CP Systems](#cp-systems)
    * [AP Systems](#ap-systems)
    * [CAP vs ACID](#cap-vs-acid)
    * [Eventual Consistency](#eventual-consistency)
    * [Real-world Examples](#real-world-examples)
    * [Designing with CAP](#designing-with-cap)
<!-- TOC -->

# _01_cap_theorem

- https://www.hellointerview.com/learn/system-design/core-concepts/cap-theorem
- https://www.splunk.com/en_us/blog/learn/cap-theorem.html
- https://www.bmc.com/blogs/cap-theorem/

## What is CAP Theorem?

- A distributed system cannot guarantee Consistency (C), Availability (A) & Partition Tolerance (P) simultaneously
  during a network partition.
- In normal operation → You can have C + A + P.
    - Only during a network partition, you must choose between Consistency and Availability.

## CAP Components

### Consistency (C)

- Every read returns:
    - Latest committed write, or
    - An error (if latest data cannot be guaranteed)
- Guarantees
    - No stale reads
    - All replicas agree on the same value
- Trade-off
    - Higher latency
    - May reject requests during failures

### Availability (A)

- Every request receives a response. The response:
    - Maybe latest data
    - Maybe stale data
- Guarantees
    - System remains operational
    - No request failures (unless the node itself crashes)
- Trade-off
    - Temporary stale reads

### Partition Tolerance (P)

- System continues operating despite:
    - Network failures
    - Packet loss
    - Broken communication between nodes

### CP Systems

Consistency + Partition Tolerance

- Prioritize:
    - Correct data
    - Strong consistency

- Sacrifice:
    - Availability

- If replicas cannot synchronize:
    - Reject request
    - Wait
    - Timeout

- Suitable for
    - Banking
    - Airline booking
    - Inventory
    - Payments
    - Healthcare
    - Payroll

- Examples
    - MongoDB (default replica set behavior)
    - HBase
    - Google BigTable
    - Redis (depending on deployment)

### AP Systems

Availability + Partition Tolerance

- Prioritize:
    - Always respond
    - High uptime

- Sacrifice:
    - Immediate consistency

- Clients may temporarily see stale data.
- Eventually replicas synchronize (Eventual Consistency).

- Suitable for
    - Social media
    - Product catalogs
    - Shopping carts
    - Recommendation systems
    - DNS

- Examples
    - Cassandra
    - DynamoDB
    - Cosmos DB

### CAP vs ACID

These are different concepts.

- CAP Consistency

Every read sees the latest write.

- ACID Consistency

Every transaction preserves database integrity and constraints.

### Eventual Consistency

All replicas eventually converge to the same value if no new writes occur.

- Common in:
    - Cassandra
    - DynamoDB
    - Couchbase

Good enough when temporary stale data is acceptable.

### Real-world Examples

| System                 | Choice | Reason                        |
|------------------------|--------|-------------------------------|
| Banking                | CP     | Wrong balance is unacceptable |
| Airline Seat Booking   | CP     | Prevent double booking        |
| Inventory              | CP     | Avoid overselling             |
| Healthcare Records     | CP     | Data correctness is critical  |
| WhatsApp               | AP     | Messages sync eventually      |
| Instagram Likes        | AP     | Slight delay is acceptable    |
| Amazon Product Catalog | AP     | Availability matters more     |
| Shopping Cart          | AP     | User experience first         |

### Designing with CAP

Ask these questions:

- Is stale data acceptable?
    - Yes → AP
    - No → CP
- Is downtime acceptable?
    - Yes → CP
    - No → AP
- Will network partitions happen?
    - Always Yes.

Design assuming partitions are inevitable.