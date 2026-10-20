<!-- TOC -->
* [_02_machine_coding_round](#_02_machine_coding_round)
  * [1. Primary Capabilities (Must-Have Features)](#1-primary-capabilities-must-have-features)
  * [2. Rules and Completion](#2-rules-and-completion)
  * [3. Entity, Relationship and Class design (attributes and methods)](#3-entity-relationship-and-class-design-attributes-and-methods)
  * [4. Implementation](#4-implementation)
  * [5. Bonus Features (Good-to-Have Enhancements)](#5-bonus-features-good-to-have-enhancements)
<!-- TOC -->

# _02_machine_coding_round

> Read the problem at least three times: understand → discuss and decode → verify and finalize.

## 1. Primary Capabilities (Must-Have Features)

- what operations the system must support? → function signatures, return types

## 2. Rules and Completion

- what conditions define success, failure or when the system stops or transitions state?

## 3. Entity, Relationship and Class design (attributes and methods)

> Top-down approach: Start with the Orchestrator class and its methods, then define the supporting classes and enums.

```text
1. orchestrator
2. services(abstractions, strategies, observer, factories)
3. repositories
4. domain models (models, dto, enums)
5. exceptions
```

- entities are nouns that maintain changing state or enforce rules in a system
- identify how entities interact with each other
- class design: for each entity, ask:
    - **state** (i.e. attributes) - what information does this class need to remember?
    - **behavior** (i.e. methods) - what operations does the outside world need?

## 4. Implementation

> Bottom-up approach: Implement the classes and methods first, then integrate them into the Services and Orchestrator.

```text
1. enums, models, dto
2. exceptions
3. repository
4. strategy, dispatcher, service
5. orchestrator 
6. main
```

## 5. Bonus Features (Good-to-Have Enhancements)

- the interviewer proposes a change, and you show how your design handles it without major restructuring