# _01_sequence_of_steps

## introduction

1. problem navigation 
   - break problem into smaller pieces
   - prioritize the most important ones
   - navigate to a solution
2. solution design 
   - core concepts
   - building blocks
3. technical excellence
   - common_patterns
   - key technologies
   - tradeoffs
4. communication and collaboration
   - ability to communicate complex concepts
   - response to feedback and questions

## delivery framework

1. requirements
   - **functional**: core features of your system and should be the first thing to be discussed
   - **non-functional**: statements about system qualities that are important to users
     - **scalability**
       - all systems need to scale, do we have any unique scaling requirements?
         - bursty traffic at any moment
         - events like holidays, sales, etc.
         - read vs. write ratio
         - need to scale read or write more
     - **latency**
       - how quickly does the system need to respond? 
         - low latency while searching
     - **durability**
       - how important is it that the data in the system is not lost?
         - social network can accept some data loss
         - banking system cannot 
     - **fault tolerance**
       - how to handle failures?
         - redundancy, failover and recovery mechanisms
     - **CAP theorem**
       - should your system prioritize consistency or availability?
     - **environmental constraints** 
       - e.g., a mobile device with limited battery, memory, bandwidth, etc.
     - **security**
       - how secure the system needs to be?
         - data protection, access control, compliance with regulations
     - **compliance**
       - any legal or regulatory requirements that the system needs to meet?
         - industry standards, data protection laws, other regulations 
   - **capacity estimation**
     - perform calculations only if they directly influence the system design
     - in most scenarios, we're dealing with a large, distributed system
2. core entities
   - understand the data central to the design
   - core entities names that will be exchanged
   - system will persist these in a Data Model
3. API or system interface
   - contract between your system and its users
     - REST - perform CRUD operations on resources
     - GraphQL - query only the data you need, avoid over-fetching or under-fetching
     - gRPC - high-performance service-to-service communication between internal APIs
4. (optional) data flow
   - especially applicable for data-processing systems
     - high-level sequence of actions or processes that the system performs on the inputs to produce desired outputs
5. high-level design
   - it consists of drawing boxes and arrows to represent the different components of your system and how they interact
   - primary goal is to design an architecture that satisfies the API design and requirements
   - in most cases, go one-by-one through API endpoints and build up your design sequentially to satisfy each one
6. deep dives
   - addressing edge cases
   - ensuring it meets all of your non-functional requirements
   - identifying and addressing issues and bottlenecks
   - improving the design based on probes from your interviewer.