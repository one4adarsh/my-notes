<!-- TOC -->
* [LLD: Meeting Room Scheduling System](#lld-meeting-room-scheduling-system)
  * [Candidate Prompt](#candidate-prompt)
  * [Step 1 — Requirements](#step-1--requirements)
    * [Clarifying questions](#clarifying-questions)
    * [Functional requirements](#functional-requirements)
      * [1. Add room](#1-add-room)
      * [2. Schedule meeting](#2-schedule-meeting)
      * [3. Prevent overlapping meetings](#3-prevent-overlapping-meetings)
      * [4. Cancel meeting](#4-cancel-meeting)
      * [5. Check room availability](#5-check-room-availability)
      * [6. View room schedule](#6-view-room-schedule)
    * [Constraints](#constraints)
    * [Scope and assumptions](#scope-and-assumptions)
  * [Step 2 — Entities and Relationships](#step-2--entities-and-relationships)
    * [Entities](#entities)
    * [Relationships](#relationships)
  * [Step 3 — Class Design](#step-3--class-design)
    * [Orchestrator](#orchestrator)
    * [Abstractions and strategies](#abstractions-and-strategies)
    * [Domain models and repositories](#domain-models-and-repositories)
    * [Where the rules live](#where-the-rules-live)
  * [Step 4 — Implementation](#step-4--implementation)
    * [Happy path: `scheduleMeeting`](#happy-path-schedulemeeting)
    * [Failure modes](#failure-modes)
    * [Availability / conflict detection](#availability--conflict-detection)
    * [Worked example](#worked-example)
  * [Step 5 — Extensibility (LLD follow-ups)](#step-5--extensibility-lld-follow-ups)
    * [Follow-up 1 — Allocation Strategy](#follow-up-1--allocation-strategy)
    * [Follow-up 2 — Room Features](#follow-up-2--room-features)
    * [Follow-up 3 — Reschedule a Meeting](#follow-up-3--reschedule-a-meeting)
    * [Follow-up 4 — Recurring Meetings](#follow-up-4--recurring-meetings)
  * [Beyond LLD — HLD Follow-ups](#beyond-lld--hld-follow-ups)
    * [Follow-up 5 — Scale](#follow-up-5--scale)
    * [Follow-up 6 — Distributed Deployment](#follow-up-6--distributed-deployment)
    * [Follow-up 7 — Race Condition](#follow-up-7--race-condition)
    * [Follow-up 8 — Failure Handling](#follow-up-8--failure-handling)
    * [Follow-up 9 — Notifications](#follow-up-9--notifications)
    * [Follow-up 10 — Service Decomposition](#follow-up-10--service-decomposition)
  * [What You're Evaluating](#what-youre-evaluating)
    * [One particularly useful interviewer technique](#one-particularly-useful-interviewer-technique)
<!-- TOC -->

# LLD: Meeting Room Scheduling System

## Candidate Prompt

You are building a Meeting Room Scheduling System for a company.

The company has multiple meeting rooms, and employees should be able to schedule meetings in those rooms.

Design and implement a system that allows users to:

1. Add/register meeting rooms.
2. Schedule a meeting for a given time period and number of attendees.
3. Find an appropriate available room for the meeting.
4. Prevent two meetings from being scheduled in the same room at overlapping times.
5. Cancel an existing meeting.
6. Query the schedule/availability of a room.

---

## Step 1 — Requirements

> Read the problem at least three times: understand → discuss and decode → verify and finalize.

### Clarifying questions

Lock down scope before touching code. Questions are grouped by the four framework buckets, with the  answers agreed for this exercise.

**Primary capabilities**

| Question                                                | Agreed answer                                        |
|---------------------------------------------------------|------------------------------------------------------|
| Does the user pick a room, or does the system pick one? | The system picks one, using an allocation strategy   |
| Can a user request a *specific* room?                   | Not in v1 (possible extension)                       |
| Do we need to reschedule/update a meeting?              | Not in v1 — cancel + schedule again (see follow-ups) |
| Recurring meetings?                                     | Out of scope for v1                                  |
| What time granularity do we support?                    | Any `LocalDateTime`; no slot rounding                |

**Rules and completion**

| Question                                                        | Agreed answer                                          |
|-----------------------------------------------------------------|--------------------------------------------------------|
| What makes a room *suitable*?                                   | `capacity >= attendees` and free during the interval   |
| When two rooms are suitable, which wins?                        | Smallest suitable room (default strategy)              |
| Do back-to-back meetings (11:00–12:00 after 10:00–11:00) clash? | No — intervals are half-open `[start, end)`            |
| Is `attendees` inclusive of the organizer?                      | Yes; it is the total head-count                        |

**Error handling**

| Question                                                        | Agreed answer                                          |
|-----------------------------------------------------------------|--------------------------------------------------------|
| `startTime >= endTime`?                                         | Reject with a validation error                         |
| No room large enough for the attendee count?                    | Reject — "no suitable room"                            |
| All suitable rooms busy?                                        | Reject — "no available room"                           |
| Cancel an unknown or already-cancelled meeting?                 | Reject — "meeting not found"                           |
| Time in the past?                                               | Not validated in v1 (keep it simple, call it out)      |

**Scope boundaries**

- **In scope:** core scheduling logic and business rules, data model, in-memory storage, Java API.
- **Out of scope:** UI, persistence, networking, auth, deployment, monitoring, notifications.

### Functional requirements

#### 1. Add room

```java
void addRoom(Room room);
```

Registers a new room. Rejects a duplicate `roomId`.

#### 2. Schedule meeting

```java
Meeting scheduleMeeting(
    LocalDateTime startTime,
    LocalDateTime endTime,
    int attendees,
    String organizerId
);
```

The system should:

- validate the request,
- find a suitable room via the allocation strategy,
- ensure the room is available for the interval,
- create and persist the meeting,
- return the scheduled meeting.

If no room is available, the operation fails with a domain exception.

#### 3. Prevent overlapping meetings

A room cannot host two meetings whose intervals overlap.

```text
Room B

10:00 -------- 11:00
        Meeting 1
```

This request must fail:

```text
10:30 -------- 11:30
       Meeting 2
```

This request must succeed (half-open intervals, boundary touch is not an overlap):

```text
11:00 -------- 12:00
       Meeting 2
```

#### 4. Cancel meeting

```java
void cancelMeeting(String meetingId);
```

Removes the meeting; the room becomes available for that interval again.
Throws `MeetingNotFoundException` for an unknown or already-canceled id.

#### 5. Check room availability

```java
boolean isAvailable(String roomId, LocalDateTime startTime, LocalDateTime endTime);
```

Returns whether the room has no meeting overlapping the requested interval.

#### 6. View room schedule

```java
List<Meeting> getSchedule(String roomId);
```

Returns the meetings scheduled for a room, ordered by start time.

### Constraints

| Constraint    | Rule                                                                           |
|---------------|--------------------------------------------------------------------------------|
| Time validity | `startTime < endTime`                                                          |
| Capacity      | `room.capacity >= meeting.attendees`                                           |
| Overlap       | For any two meetings in the same room: `!(a.start < b.end && b.start < a.end)` |

### Scope and assumptions

For the first version, assume:

- The system runs on a single machine (no concurrency concerns yet).
- Data is stored in memory.
- No authentication/authorization.
- No UI.
- Focus on clean, maintainable object-oriented Java.

---

## Step 2 — Entities and Relationships

> Entities are the nouns that maintain changing state or enforce rules; everything else is an attribute.

### Entities

| Candidate noun | Entity or attribute?   | Reasoning                                                                    |
|----------------|------------------------|------------------------------------------------------------------------------|
| `Room`         | Entity                 | Registered and managed by the system; owns `capacity` rule                   |
| `Meeting`      | Entity                 | Has a lifecycle (scheduled → cancelled); owns `attendees`, references a room |
| `TimeSlot`     | Entity (value object)  | Enforces `start < end` and owns the overlap rule — more than plain data      |
| `capacity`     | Attribute of `Room`    | Just a number describing the room                                            |
| `organizerId`  | Attribute of `Meeting` | Identity only; no behaviour in v1                                            |
| `Scheduler`    | Orchestrator           | Drives the schedule/cancel workflow across rooms and meetings                |

**Room**

- `roomId`
- `name`
- `capacity`

**Meeting**

- `meetingId`
- `roomId`
- `timeSlot` (`startTime`, `endTime`)
- `attendees`
- `organizerId`

**TimeSlot**

- `start`
- `end`

### Relationships

```text
                 ┌──────────────────────┐
                 │   MeetingScheduler   │  ← orchestrator: owns the workflow
                 └──────────┬───────────┘
    uses-a                  │ uses-a                          uses-a
   ┌────────────────────────┼──────────────────────────────────────┐
   ▼                        ▼                                      ▼
RoomRepository       MeetingRepository                   RoomAllocationStrategy
   │ contains-a             │ contains-a                           ▲ is-a
   ▼                        ▼                                      │
 Room ◄──── refers-to ──── Meeting ── has-a ─► TimeSlot    SmallestFitStrategy
(roomId)                  (roomId)
```

- **Orchestrator:** `MeetingScheduler` drives the main workflow.
- **Durable state:** `RoomRepository` owns rooms; `MeetingRepository` owns meetings.
- **Data rules:** `TimeSlot` owns interval validity and overlap; `Room` owns the capacity check.
- **Workflow rules:** "find a room, verify availability, book it" lives in the orchestrator.

---

## Step 3 — Class Design

Working top-down: orchestrator → abstractions/strategies → domain models, repositories, enums.
(This is the *explanation* order; enums and value objects are usually coded first.)

### Orchestrator

```java
public class MeetingScheduler {
    private final RoomRepository rooms;
    private final MeetingRepository meetings;
    private final RoomAllocationStrategy allocationStrategy;

    public void addRoom(Room room);
    public Meeting scheduleMeeting(LocalDateTime start, LocalDateTime end, int attendees, String organizerId);
    public void cancelMeeting(String meetingId);
    public boolean isAvailable(String roomId, LocalDateTime start, LocalDateTime end);
    public List<Meeting> getSchedule(String roomId);
}
```

- **State:** the two repositories and the strategy (injected via constructor).
- **Behavior:** the six public operations from the functional requirements.

### Abstractions and strategies

```java
public interface RoomAllocationStrategy {
    Optional<Room> select(List<Room> candidates, MeetingRequest request);
}

public class SmallestFitStrategy implements RoomAllocationStrategy {}
```

`candidates` are rooms that already pass capacity & availability; the strategy only decides *which* suitable room wins.
This keeps the strategy free of scheduling rules.

### Domain models and repositories

```java
public record TimeSlot(LocalDateTime start, LocalDateTime end) {
    public TimeSlot {                       // validation in the compact constructor
        if (!start.isBefore(end)) throw new InvalidTimeSlotException(start, end);
    }
    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}

public record Room(String roomId, String name, int capacity) {
    public boolean canAccommodate(int attendees) { return capacity >= attendees; }
}

public record Meeting(String meetingId, String roomId, TimeSlot slot, int attendees, String organizerId) {}

public record MeetingRequest(TimeSlot slot, int attendees, String organizerId) {}

public interface RoomRepository {
    void save(Room room);
    Optional<Room> findById(String roomId);
    List<Room> findAll();
}

public interface MeetingRepository {
    void save(Meeting meeting);
    Optional<Meeting> findById(String meetingId);
    List<Meeting> findByRoom(String roomId);
    void delete(String meetingId);
}
```

Exceptions: `InvalidTimeSlotException`, `NoSuitableRoomException`, `MeetingNotFoundException`,
`DuplicateRoomException` — all extending a common `SchedulingException`.

### Where the rules live

| Rule                                | Type           | Lives in                 |
|-------------------------------------|----------------|--------------------------|
| `start < end`                       | data rule      | `TimeSlot` constructor   |
| Two intervals overlap               | data rule      | `TimeSlot.overlaps()`    |
| Room fits attendee count            | data rule      | `Room.canAccommodate()`  |
| Which suitable room to pick         | policy         | `RoomAllocationStrategy` |
| Find → check availability → persist | workflow rule  | `MeetingScheduler`       |
| Cancel only an existing meeting     | lifecycle rule | `MeetingScheduler`       |

> data-specific rules belong in the entity that owns the data
> workflow and lifecycle rules belong in the orchestrator

---

## Step 4 — Implementation

> Ask the interviewer whether they want pseudocode, complete code, or a talk-through.

### Happy path: `scheduleMeeting`

```text
scheduleMeeting(start, end, attendees, organizerId)
  1. slot    = new TimeSlot(start, end)                 // throws if start >= end
  2. request = new MeetingRequest(slot, attendees, organizerId)
  3. candidates = rooms.findAll()
                       .filter(r -> r.canAccommodate(attendees))
                       .filter(r -> isAvailable(r.roomId, slot))
  4. room = allocationStrategy.select(candidates, request)
                              .orElseThrow(NoSuitableRoomException::new)
  5. meeting = new Meeting(newId(), room.roomId, slot, attendees, organizerId)
  6. meetings.save(meeting)
  7. return meeting
```

```text
isAvailable(roomId, slot)
  return meetings.findByRoom(roomId).stream().noneMatch(m -> m.slot().overlaps(slot))
```

```text
cancelMeeting(meetingId)
  meetings.findById(meetingId).orElseThrow(MeetingNotFoundException::new)
  meetings.delete(meetingId)
```

### Failure modes

| Scenario                                      | Handling                            |
|-----------------------------------------------|-------------------------------------|
| `start >= end`                                | `InvalidTimeSlotException` (step 1) |
| `attendees <= 0`                              | `IllegalArgumentException` (step 2) |
| No room with `capacity >= attendees`          | `NoSuitableRoomException` (step 4)  |
| All large-enough rooms are busy               | `NoSuitableRoomException` (step 4)  |
| Cancel unknown / already-cancelled meeting    | `MeetingNotFoundException`          |
| `addRoom` with duplicate `roomId`             | `DuplicateRoomException`            |
| `getSchedule` / `isAvailable` on unknown room | `RoomNotFoundException`             |

### Availability / conflict detection

| Approach                                              | Check cost per room | Notes                                                               |
|-------------------------------------------------------|---------------------|---------------------------------------------------------------------|
| Linear scan of the room's meetings                    | O(m)                | Fine for v1; simplest to reason about                               |
| `TreeMap<LocalDateTime, Meeting>` keyed by start time | O(log m)            | Check `floorEntry(start)` and `ceilingEntry(start)` neighbours only |
| Interval tree                                         | O(log m)            | Overkill unless intervals are queried in bulk                       |

Start with the linear scan and mention the `TreeMap` upgrade — this hits the "efficient conflict detection" signal without premature complexity.

### Worked example

Rooms registered:

```text
Room A → capacity 4
Room B → capacity 10
Room C → capacity 20
```

**Request 1** — `10:00–11:00`, 8 attendees

```text
candidates (capacity ≥ 8, free)  → [B, C]
SmallestFitStrategy              → B
result                           → Meeting m1 in Room B, 10:00–11:00
```

**Request 2** — `10:30–11:30`, 6 attendees

```text
capacity ≥ 6                     → [B, C]
B: m1 [10:00,11:00) overlaps [10:30,11:30) → busy
candidates                       → [C]
result                           → Meeting m2 in Room C, 10:30–11:30
```

**Request 3** — `10:00–12:00`, 15 attendees

```text
capacity ≥ 15                    → [C]
C: m2 overlaps                   → busy
candidates                       → []
result                           → NoSuitableRoomException
```

**Request 4** — `11:00–12:00`, 8 attendees

```text
capacity ≥ 8                     → [B, C]
B: m1 ends 11:00, half-open → no overlap → free
C: m2 [10:30,11:30) overlaps     → busy
result                           → Meeting m4 in Room B, 11:00–12:00
```

**Cancel** `m1` → `getSchedule("B")` returns `[m4]`; `isAvailable("B", 10:00, 11:00)` → `true`.

---

## Step 5 — Extensibility (LLD follow-ups)

> The interviewer proposes a change; show how the design absorbs it without restructuring.
> Identify the seam → show the abstraction → say what stays unchanged.

### Follow-up 1 — Allocation Strategy

> Currently, we select the smallest available room. Tomorrow the business may want different strategies:

```text
Smallest suitable room
First available room
Largest room
Room closest to the organizer
```

**Question:** How would you add a new strategy without modifying the core scheduling logic?

**Seam:** `RoomAllocationStrategy`. Add `ClosestToOrganizerStrategy` (needs `organizerId` → location lookup, 
hence the field on `MeetingRequest`). `MeetingScheduler` is unchanged — **Strategy pattern**, Open/Closed principle.

### Follow-up 2 — Room Features

> Some meetings need a projector or video-conferencing.

**Seam:** add `Set<Feature> features` to `Room` and `Set<Feature> requiredFeatures` to `MeetingRequest`;
extend the candidate filter in step 3 of the happy path (`room.hasAll(required)`). Strategy and repositories unchanged.

### Follow-up 3 — Reschedule a Meeting

> Users want to move a meeting to a new time.

**Seam:** new orchestrator method `rescheduleMeeting(meetingId, newStart, newEnd)`. 
Prefer to keep the same room if it is free (excluding the meeting itself from the overlap check), 
otherwise re-run allocation. 
`Meeting` gains a `status` enum (`SCHEDULED`, `CANCELLED`) rather than being deleted, so history is preserved.

### Follow-up 4 — Recurring Meetings

> Weekly team stand-ups.

**Seam:** introduce a `RecurrenceRule` (value object) that expands into a list of `TimeSlot`s; 
the scheduler books each occurrence atomically or rolls back. 
Discuss trade-off: expand eagerly (simple, storage grows) vs. lazily (complex overlap check).

---

## Beyond LLD — HLD Follow-ups

These follow-ups move from class design to system design. Use them once the LLD phase is complete.

### Follow-up 5 — Scale

> The company has 100,000 meeting rooms and approximately 50,000 scheduling requests per second.

**Ask:** Would your current implementation scale? What would you change?

Look for discussion around:

- Database instead of in-memory storage
- Indexing (`room_id, start_time`)
- Efficient availability queries
- Horizontal scaling and stateless application servers
- Caching
- Partitioning/sharding (by room, by building)
- Read/write patterns

### Follow-up 6 — Distributed Deployment

> The service is now deployed on 20 application servers behind a load balancer.

```text
                  Load Balancer
                /      |       \
               /       |        \
            App 1    App 2     App 3
               \       |        /
                \      |       /
                    Database
```

**Ask:** Two requests for the same room can arrive at two different servers at exactly the same time. 
How do you prevent double booking?

This is an important senior-level discussion.

### Follow-up 7 — Race Condition

Give this scenario explicitly:

```text
Alice → Room A, 10:00 - 11:00
Bob   → Room A, 10:00 - 11:00
```

Both application servers perform:

```text
Check availability
        ↓
Room available
        ↓
Create meeting
```

**Ask:** How do you guarantee that only one meeting succeeds?

Good candidates may discuss:

- Database transactions
- Optimistic locking (version column on room schedule)
- Pessimistic locking (`SELECT ... FOR UPDATE` on the room row)
- Serializable isolation
- Unique / exclusion constraints where supported (e.g. PostgreSQL `EXCLUDE USING gist`)
- Distributed locks
- Atomic operations

The important thing is that they understand **why a simple "check then insert" is unsafe**.

### Follow-up 8 — Failure Handling

**Ask:** What happens if the application crashes immediately after creating the meeting but before
returning the response to the user?

```text
Client
  |
  | POST /meetings
  ↓
Server
  |
  | INSERT meeting
  ↓
Database
  |
  X
Server crashes
```

The client doesn't know whether the meeting was created.

**Ask:** What would you do to make this API safe to retry?

This opens discussion around:

- Idempotency keys
- Request IDs
- Transaction boundaries
- Exactly-once vs at-least-once semantics

### Follow-up 9 — Notifications

> Whenever a meeting is successfully scheduled or canceled, users should receive notifications.

**Ask:** Would you send the notification synchronously as part of the scheduling request?

A strong candidate may propose:

```text
Scheduling Service
      |
      | MeetingScheduled / MeetingCancelled
      ↓
 Message Queue
      |
      +----> Email Service
      |
      +----> Calendar Service
      |
      +----> Notification Service
```

This allows you to explore **asynchronous processing and event-driven architecture**
(and, at the LLD level, the Observer pattern as an in-process precursor).

### Follow-up 10 — Service Decomposition

**Ask:** If you were building this as a production system, how would you break it into components?

A possible answer could evolve toward:

```text
                    API Gateway
                         |
                  Scheduling Service
                  /       |        \
                 /        |         \
          Room Service  Meeting    Availability
                        Service        |
                           |           |
                           +-----------+
                                 |
                              Database
                                 |
                            Message Queue
                                 |
                      +----------+----------+
                      |                     |
                 Notification           Calendar
                   Service              Service
```

But don't require microservices automatically. 
A good candidate should explain **why** they would split components rather than simply saying "microservices."

---

## What You're Evaluating

For a senior candidate, evaluate across these dimensions:

| Area                  | What you're looking for                          | Framework step |
|-----------------------|--------------------------------------------------|----------------|
| Problem understanding | Clarifies requirements and ambiguities           | 1              |
| OOP                   | Good entities, responsibilities and abstractions | 2, 3           |
| SOLID                 | Low coupling, extensibility                      | 3, 5           |
| Design patterns       | Uses patterns where they solve a real problem    | 3, 5           |
| Code quality          | Readable, testable, maintainable                 | 4              |
| Algorithms            | Efficient availability/conflict detection        | 4              |
| Concurrency           | Understands race conditions                      | HLD            |
| Distributed systems   | Understands multiple application instances       | HLD            |
| Data consistency      | Knows how double booking can occur               | HLD            |
| Scalability           | Can reason about bottlenecks                     | HLD            |
| Reliability           | Handles retries/failures                         | HLD            |
| Communication         | Explains trade-offs clearly                      | all            |

### One particularly useful interviewer technique

**Don't reveal all requirements upfront.**

Start with only:

> "Build a meeting room scheduling system."

Let the candidate ask questions.

Then progressively introduce:

```text
Phase 1 → OOP implementation            (Steps 1–4)
Phase 2 → Allocation strategy / features (Step 5)
Phase 3 → Scale
Phase 4 → Multiple servers
Phase 5 → Concurrent bookings
Phase 6 → Failure/retries
Phase 7 → Notifications/events
```

This gives you a very good signal on whether the candidate can design incrementally rather than prematurely over-engineering the solution.
