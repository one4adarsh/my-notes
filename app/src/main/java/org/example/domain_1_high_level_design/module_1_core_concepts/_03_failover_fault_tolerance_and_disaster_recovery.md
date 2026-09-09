<!-- TOC -->
* [_03_failover_fault_tolerance_and_disaster_recovery](#_03_failover_fault_tolerance_and_disaster_recovery)
  * [Server Failover](#server-failover)
    * [Server Redundancy](#server-redundancy)
    * [Difference between failover and switchover](#difference-between-failover-and-switchover)
    * [How does server failover work?](#how-does-server-failover-work)
  * [Fault Tolerance](#fault-tolerance)
    * [High Availability vs. Fault Tolerance](#high-availability-vs-fault-tolerance)
  * [Disaster Recovery](#disaster-recovery)
    * [What is considered an IT disaster?](#what-is-considered-an-it-disaster)
    * [What are the five steps of disaster recovery?](#what-are-the-five-steps-of-disaster-recovery)
    * [How disaster recovery works](#how-disaster-recovery-works)
    * [Types of disaster recovery](#types-of-disaster-recovery)
    * [Planning a disaster recovery strategy](#planning-a-disaster-recovery-strategy)
<!-- TOC -->

# _03_failover_fault_tolerance_and_disaster_recovery

- https://www.cloudflare.com/learning/performance/what-is-server-failover/
- https://www.cockroachlabs.com/blog/what-is-fault-tolerance/
- https://cloud.google.com/learn/what-is-disaster-recovery

## Server Failover

- Practice of having a backup server/s prepared to automatically take over if the primary server goes offline.
- Servers can fail for many reasons, such as:
    - Power outages
    - Natural disasters
    - Unexpected traffic surgesic surges
    - Cyber attacks (like Distributed Denial of Service (DDoS) attacks)
    - Hardware complications (like cable problems or overheating)
    - Operating system issues

### Server Redundancy

- Measure of how many backup servers are in place to support a primary server.

### Difference between failover and switchover

- In failover, the shift to a redundant server happens automatically.
- In switchover, the shift to a redundant server is done manually.

### How does server failover work?

- For server failover to work, servers must be connected so that they can sense issues and take over when necessary.
- Two ways to stay connected:
    - Physical “heartbeat” cables
    - Network-based heartbeat signals
- Server failover configurations are either active-active or active-standby.

- Active-standby
  In active-standby, one primary server handles traffic while one or more secondary servers wait in standby.  
  In a two-server setup, the secondary server watches the primary but stays inactive.  
  If the primary fails, the secondary takes over and alerts the data center to restore the primary.  
  After restoration, the primary resumes service and the secondary returns to standby.  
  This return of the primary server is called failback.

- Active-active
  In a two-server active-active setup, both servers stay active at the same time.  
  This is commonly used with load balancing, since both servers are configured similarly and share traffic.  
  If one server fails, traffic is redirected to the remaining operational server(s).

## Fault Tolerance

- Fault tolerance describes a system’s ability to handle errors and outages without any loss of functionality.
- It involves designing systems that can automatically recover from failures, ensuring minimal disruption to services.
- Common approaches to achieving fault tolerance:
    - Multiple hardware systems capable of doing the same work
    - Multiple instances of software capable of doing the same work
    - Backup sources of power used in on-premises systems

### High Availability vs. Fault Tolerance

- Fault tolerance implies zero service interruptions. If there is a failure somewhere the system will instantly switch
  to the backup solution and service will continue without interruption.
- High availability implies that services are highly available but not always available. A system can be highly
  available but not fault-tolerant.

- High availability is one of the primary reasons architects look to build fault-tolerant systems.

## Disaster Recovery

- Disaster recovery (DR) is an organization’s ability to restore access and functionality to IT infrastructure after a
  disaster event, whether natural or caused by human action (or error).

### What is considered an IT disaster?

- Cyberattacks (for example, malware, DDoS, and ransomware attacks)
- Technological hazards (for example, power outages, pipeline explosions, and transportation accidents)
- Machine and hardware failure

### What are the five steps of disaster recovery?

- Risk assessment
- Business impact analysis
- DR planning
- Implementation
- Testing and maintenance

### How disaster recovery works

- An effective DR plan addresses three different elements for recovery:
    - Preventive
    - Detective
    - Corrective

### Types of disaster recovery

- Backups: Data is backed up to a secondary offsite system or shipped to a secondary offsite location. Does not include
  IT infrastructure.
    - Use case: Archiving data for long-term retention and compliance purposes.
- Backup as a service (BaaS): A third-party provider offers regular data backups.
    - Use case: Small and medium-sized businesses that lack the resources to manage their own backups.

- Disaster recovery as a service (DRaaS): Data and IT infrastructure are backed up and hosted on a third-party
  provider’s cloud infrastructure. The provider implements and orchestrates the DR plan during a crisis.
    - Use case: Organizations that want to outsource their DR operations to a specialized provider and minimize
      downtime.

- Point-in-time snapshots: Data, files, or an entire database are replicated at a specific point in time. Snapshots can
  be used to restore data as long as the copy is stored in a location unaffected by the event.
    - Use case: Quickly recovering from data corruption or accidental deletion, with some potential for data loss
      depending on the snapshot frequency.

- Virtual DR: Operations and data are backed up, or a complete replica of the IT infrastructure is created and run on
  offsite virtual machines (VMs).
    - Use case: Quickly resuming operations after a disaster by failing over to a virtualized environment, requiring
      frequent data and workload transfers.

- Disaster recovery sites: Locations that organizations can temporarily use after a disaster event, which contain
  backups of data, systems, and other technology infrastructure.
    - Use case: Providing a physical location to recover operations in the event of a complete loss of the primary data
      center, suitable for organizations with strict compliance requirements or those needing to maintain physical
      control over their DR environment.

### Planning a disaster recovery strategy

- When it comes to creating disaster recovery strategies, you should carefully consider the following key metrics:
    - Recovery time objective (RTO): The maximum acceptable length of time that systems and applications can be down
      without causing significant damage to the business. For example, some applications can be offline for an hour,
      while others might need to recover in minutes.
    - Recovery point objective (RPO): The maximum age of data you need to recover to resume operations after a major
      event. RPO helps to define the frequency of backups. 