# _01_sequence_of_steps

## difference between high-level design and low-level design interviews

### HLD

- Architecture at scale
- The goal is to show how you think about large systems that serve real users
- Talk through
    - functional requirements
    - non-functional requirements
        - storage
        - caching
        - sharding
        - trade-offs
        - API or system interface
            - REST, gRPC, GraphQL, etc
        - CAP theorem
        - scalability
        - latency
        - durability
        - security
        - fault tolerance
        - compliance
        - environmental constraints
    - capacity estimation
        - traffic

### LLD

- Implementation at the code/class level
- The goal is to show how you think about building a system that serves real users
- Talk through
    - modeling data into entities
        - class
            - attributes
            - methods
    - interactions between entities
        - relationships
        - state transitions
    - design patterns
        - creational, structural, behavioral

## interview assessment

1. problem analysis
    - extract key entities and relationships
    - ask questions to lock down scope
    - frame the problem before touching code
2. class design
    - design classes and their interactions
        - choosing right responsibilities for each class
        - shaping method signatures and return types
        - clear ownership of data and behavior
        - keeping boundaries between classes clear
3. code quality
    - object-oriented design principles
        - encapsulation
        - well-managed state
        - sensible use of composition or inheritance
        - separation of concerns
    - hygiene
        - naming
        - dependency direction
4. extensibility and maintainability
    - check whether design can absorb new functionality without being rewritten
        - flexible structures with clean boundaries
5. communication
    - clear narrative
    - thoughtful reasoning
    - ability to adjust when interviewer probes

## delivery framework

1. requirements
    - turn requirements into a spec which can be designed
    - ask clarifying questions to lock down scope based on these thoughts:
        - **primary capabilities**
            - what operations the system must support?
        - **rules and completion**
            - what conditions define success, failure or when the system stops or transitions state?
        - **error handling**
            - how should the system respond when inputs or actions are invalid?
        - **scope boundaries**
            - In-scope: core logic and business rules, data model, and API or system interface
            - Out-of-scope: UI, storage, networking, infrastructure, deployment, monitoring etc.
    - KIM: read the problem at least 3 times
        1. understand
        2. discuss and decode
        3. verify and finalize
2. entities and relationships
    - **identify entities**
        - entities are nouns that clearly need to exist in system
        - these are the things that the system will manage and operate on
        - how to figure out if something is an entity or an attribute?
            - if something maintains changing state or enforces rules, it is likely an entity
            - if it's just information attached to something else, it is likely an attribute
    - **define relationship**
        - identify how entities interact with each other
        - establish system's shape
            - which is orchestrator - the one driving the main workflow?
            - how entities depend on each other?
                - has-a, uses-a, is-a, contains-a
            - which entity owns a durable state?
            - where should rules logically live?
    - KIM:
        1. represent list of entities and arrows showing relationship between various entities
3. class design
    - turn entity outline into an actual class design
    - start entity-by-entity, working top-down through the system
        1. orchestrator, services, repositories
        2. abstractions, strategies, factories
        3. DTOs, domain models, enums
    - for each entity, ask:
        - **state** (i.e. attributes) - what information does this class need to remember?
        - **behavior** (i.e. methods) - what operations does the outside world need?
    - KIM:
        1. keep functions within the entity that owns the state
        2. keep functions that operate on multiple entities in a separate orchestrator class
    - KIM (in other words):
        1. data specific rules belong in the entity that owns the data
        2. workflow and lifecycle rules belong in the orchestrator class
4. implementation
    - KIM: always ask your interviewer what they prefer before you start writing
        1. pseudo-code
        2. complete code
        3. talk through logic
    - start implementation with happy path (normal flow when everything goes right) in a linear way
        - input it receives
        - sequence of steps it performs
        - internal calls it makes to other classes
        - what it returns or how it changes state
    - then enumerate failure modes and error handling
        - invalid inputs
        - illegal operations
        - out-of-range values
        - calls that violate current system state
        - anything that must be rejected or handled gracefully
    - finally walk through a specific scenario with concrete values to verify the implementation
5. extensibility
    - the interviewer proposes a change, and you show how your design handles it without major restructuring