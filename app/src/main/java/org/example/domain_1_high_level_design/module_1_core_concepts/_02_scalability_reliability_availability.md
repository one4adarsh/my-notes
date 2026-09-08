<!-- TOC -->
* [_02_scalability_reliability_availability](#_02_scalability_reliability_availability)
  * [Scalability](#scalability)
    * [How Systems Grow](#how-systems-grow)
    * [Scaling Techniques](#scaling-techniques)
      * [Vertical vs Horizontal Scaling](#vertical-vs-horizontal-scaling)
  * [Reliability](#reliability)
    * [Questions Answered](#questions-answered)
    * [Reliability Metrics](#reliability-metrics)
      * [MTBF (Mean Time Between Failures)](#mtbf-mean-time-between-failures)
      * [Failure Rate](#failure-rate)
    * [Improve Reliability](#improve-reliability)
  * [Availability](#availability)
    * [Questions Answered](#questions-answered-1)
  * [Availability Metrics](#availability-metrics)
      * [Availability](#availability-1)
      * [MTTR (Mean Time To Repair)](#mttr-mean-time-to-repair)
    * [Improve Availability](#improve-availability)
  * [Reliability vs Availability](#reliability-vs-availability)
    * [Common Scenarios](#common-scenarios)
      * [High Availability, Low Reliability](#high-availability-low-reliability)
      * [High Reliability, Lower Availability](#high-reliability-lower-availability)
      * [Ideal System](#ideal-system)
  * [Scalability vs Reliability vs Availability](#scalability-vs-reliability-vs-availability)
  * [Real-world Mapping](#real-world-mapping)
<!-- TOC -->

# _02_scalability_reliability_availability

- https://blog.algomaster.io/p/scalability
- https://www.atlassian.com/incident-management/kpis/reliability-vs-availability

## Scalability

Scalability is the ability of a system to handle increasing load by adding resources while maintaining acceptable
performance.

A scalable system grows without requiring major redesign.

### How Systems Grow

- User Growth → More concurrent users / RPS
- Feature Growth → More APIs and business logic
- Data Growth → More records, logs, files
- Complexity Growth → Monolith → Microservices
- Geographic Growth → Multi-region users

### Scaling Techniques

1. Vertical Scaling (Scale Up)
2. Horizontal Scaling (Scale Out)
3. Load Balancer
4. Caching
5. CDN
6. Database Sharding
7. Asynchronous Processing
8. Microservices
9. Auto Scaling
10. Multi-region Deployment

#### Vertical vs Horizontal Scaling

| Vertical Scaling        | Horizontal Scaling        |
|-------------------------|---------------------------|
| Bigger machine          | More machines             |
| Easy                    | More complex              |
| Hardware limit          | Nearly unlimited          |
| Single point of failure | Better fault tolerance    |
| Small systems           | Large distributed systems |

## Reliability

Reliability is the probability that a system performs its intended function without failure over time.

Focus:

- Correctness
- Consistency
- Failure-free operation

### Questions Answered

- Does the system work correctly?
- How often does it fail?

### Reliability Metrics

#### MTBF (Mean Time Between Failures)

Average operating time between failures.

```text
MTBF = Total Operating Time / Number of Failures
```

Higher MTBF = Better reliability

#### Failure Rate

Failures per unit time.

```text
Failure Rate = Number of Failures / Total Operating Time
```

Lower Failure Rate = Better reliability

### Improve Reliability

- Better testing
- Fault tolerance
- Redundancy
- Monitoring
- Root Cause Analysis (RCA)
- Preventive maintenance
- Automated recovery

## Availability

Availability is the percentage of time a system is operational and accessible.

Focus:

- Uptime
- Accessibility
- Service continuity

### Questions Answered

- Can users access the service?
- Is it online?

## Availability Metrics

#### Availability

```text
Availability =
Uptime
-------------------
Uptime + Downtime
```

or

```text
Availability =
MTBF
----------------
MTBF + MTTR
```

#### MTTR (Mean Time To Repair)

Average recovery time after failure.

```text
MTTR = Total Repair Time / Number of Failures
```

Lower MTTR = Higher availability

### Improve Availability

- Load balancing
- Redundant servers
- Failover
- Auto scaling
- Multi-region deployment
- Fast incident response
- Reduce MTTR

## Reliability vs Availability

| Reliability            | Availability         |
|------------------------|----------------------|
| Correctness over time  | Uptime               |
| Failure-free operation | Accessibility        |
| Measures failures      | Measures downtime    |
| MTBF, Failure Rate     | Availability %, MTTR |

### Common Scenarios

#### High Availability, Low Reliability

- Service is online
- Frequently returns errors

Example:

- Website always responds but returns HTTP 500 often

#### High Reliability, Lower Availability

- Works correctly
- Occasionally unavailable for maintenance

Example:

- Banking system during scheduled downtime

#### Ideal System

- Rarely fails
- Quickly recovers
- Always accessible


- [x] High Reliability
- [x] High Availability

## Scalability vs Reliability vs Availability

| Property     | Measures               | Goal          |
|--------------|------------------------|---------------|
| Scalability  | Handle increasing load | Growth        |
| Reliability  | Failure-free operation | Correctness   |
| Availability | Uptime                 | Accessibility |

## Real-world Mapping

| System          | Priority                                 |
|-----------------|------------------------------------------|
| Banking         | Reliability                              |
| Airline Booking | Reliability                              |
| Healthcare      | Reliability                              |
| Netflix         | Scalability + Availability               |
| WhatsApp        | Availability                             |
| Google Search   | Scalability + Reliability + Availability |
| E-commerce      | Scalability + Reliability + Availability |

