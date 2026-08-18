# Quiz 13 — Event Sourcing & CQRS

**Core to this JD's "strategic systems".** 16 questions. This is the file to know coldest
after the concurrency material, and the one where your Apple event-driven/Cassandra
experience maps most directly.

Target: 30 min.

---

### 1. Explain event sourcing. Why persist an event log instead of current-state rows?

<details><summary>Answer</summary>

**The idea:** instead of storing the *current state* of an entity and mutating it in place,
you store the **immutable, append-only sequence of events** that produced it. Current state is
**derived** by replaying those events.

```
Traditional (state-oriented):
  UPDATE trades SET status='SETTLED', settled_on='2026-08-20' WHERE id=123;
  -- the previous status is GONE. You know what is; you don't know how or when.

Event-sourced (append-only):
  append(TradeExecuted(123, qty=1000, px=99.5, at=T1))
  append(TradeConfirmed(123, at=T2))
  append(TradeCleared(123, ccp=LCH, at=T3))
  append(TradeSettled(123, on=2026-08-20, at=T4))
  -- current state = fold(events)
```

**Why — and lead with the reasons that matter in a bank:**

1. **A complete, immutable audit trail is the storage model, not a bolt-on.** You can answer
   "what did we believe at 16:00 yesterday, and why?" — regulators ask exactly that. In a
   state-oriented system you bolt on an audit table and hope it's consistent with reality;
   here the audit trail *is* the source of truth, so it cannot drift.
2. **No lost information.** `UPDATE` destroys history. Business questions you haven't thought
   of yet ("how often do trades get amended before settlement?") are answerable from data you
   already have, because the events record *intent and causation*, not just outcome.
3. **Temporal queries / time travel.** Rebuild the state as of any point in time by replaying
   to that offset. Invaluable for reconciliation, dispute resolution, and debugging.
4. **Debugging and reproduction.** You can replay a production event stream into a test
   environment and reproduce a bug exactly. This is a genuinely huge operational win.
5. **New projections from old data.** A new read model can be built by replaying the whole
   history — you get the new report retroactively covering all time, not just from deployment.
6. **Natural fit for event-driven integration.** The events you persist are the events you
   publish, so downstream systems and your own store agree by construction (see Q13 on the
   outbox).
7. **Append-only writes are cheap and contention-free** — no read-modify-write, no row locks
   on the write path, which matters at high volume.

**And say the costs unprompted**, because an answer without them sounds like you've only read
about it — see Q14. The short version: **eventual consistency, schema evolution forever,
replay cost, and a much steeper learning curve.** It is not a default architecture; it earns
its place where auditability and temporal accuracy are requirements. Post-trade processing is
one of the clearest legitimate cases.
</details>

---

### 2. Commands vs events vs aggregates — get the vocabulary right.

<details><summary>Answer</summary>

- **Command** — an *intent*, imperative, **can be rejected**. `SettleTrade`, `ConfirmTrade`.
  Addressed to one aggregate. Validation happens here.
- **Event** — a *fact*, **past tense**, and **cannot be rejected** because it already
  happened. `TradeSettled`, `TradeConfirmed`. Immutable forever.
- **Aggregate** — the **consistency boundary**. A cluster of objects treated as one unit, with
  an id, that guarantees its own invariants. All state changes go through it, and **it's the
  unit of atomicity**: one command → one aggregate → zero or more events, committed together.
- **Aggregate root** — the entity you address from outside; nothing reaches inside.
- **Projection / read model** — a denormalised view built by consuming events.
- **Event store** — the append-only log, usually keyed by `(aggregateId, version)`.

**The naming discipline matters and interviewers listen for it:** `Settle` is a command,
`Settled` is an event. Getting this backwards in a design discussion signals you haven't
built one.

**Choosing aggregate boundaries is the hard part**, and it's the design judgement being
tested:
- The aggregate is your **transactional boundary**, so it should be as **small as possible**
  while still enforcing its invariants. A `Trade` aggregate, not a `Portfolio` aggregate
  containing every trade — the latter serialises all writes for an account and its event
  stream grows without bound.
- **Invariants that must hold immediately** go *inside* one aggregate. Anything that can be
  eventually consistent goes *across* aggregates, coordinated by events or a saga (Q11).
- **One command should touch one aggregate.** If a command needs two, you either have the
  boundary wrong or you need a saga.
- Reference other aggregates **by id**, never by object reference.

For post-trade, plausible aggregates: `Trade` (lifecycle from execution to settlement),
`SettlementInstruction`, `Position` (per account/instrument), `NettingSet`. A `Trade`'s
invariant — "you cannot settle an unconfirmed trade" — must be immediate, so it lives inside
the aggregate. "Position equals the sum of settled trades" is a **projection**, and eventual.
</details>

---

### 3. What is CQRS, and why does it pair naturally with event sourcing?

<details><summary>Answer</summary>

**CQRS — Command Query Responsibility Segregation.** Separate the model that **writes** from
the model(s) that **read**. Not just different methods: different models, often different
storage, scaled and deployed independently.

```
             COMMAND SIDE (write)                    QUERY SIDE (read)
  Command --> validate against invariants      Event --> update projection
          --> append event(s) to the log              --> denormalised, query-shaped
          --> optimised for CONSISTENCY               --> optimised for READS
```

**Why they pair so naturally:**
- Event sourcing gives you a write model that is **excellent for consistency and auditing and
  terrible for querying**. "Show me all unsettled trades for account X sorted by value date"
  is impossible to answer efficiently by replaying event streams. So you *need* a separate
  read model — CQRS isn't an optional addition, it's the consequence.
- The **event log is the natural integration point**: the write side appends, the read side
  subscribes and projects. The events are already there.
- **You can have many read models** from one event stream, each shaped for its query — a
  settlement dashboard, a regulatory report, a position keeper, a client API. Each is
  independently rebuildable and independently deployable.

**The important clarification, because it's a common misconception:** **CQRS does not require
event sourcing, and event sourcing does not require CQRS.** CQRS with a plain relational
write model and a denormalised read replica is a perfectly normal, much cheaper pattern.
Saying this shows you're not cargo-culting.

**What CQRS buys you independently of event sourcing:**
- **Independent scaling.** Read traffic is usually 10–1000× write traffic; now you scale them
  separately.
- **Different storage per side.** Append-optimised for writes (Cassandra, Kafka, an event
  store); query-optimised for reads (Postgres with indexes, Elasticsearch for search, Redis
  for hot lookups).
- **Simpler models.** The write model carries no query concerns; the read model carries no
  invariants. Both get smaller.
- **Different teams / different release cadence.**

**The cost is the whole of Q4: eventual consistency.** That's the trade, and it's the first
thing an interviewer will push on.
</details>

---

### 4. How do you handle eventual consistency between the write and read sides?

<details><summary>Answer</summary>

**Acknowledge the problem precisely first:** the command succeeds, the event is appended, but
the projection hasn't caught up. A user submits a trade, is redirected to the trade list, and
**doesn't see it**. That's a real UX and correctness problem, not a theoretical one.

**Strategies, roughly in order of how often they're the right answer:**

1. **Return what you know from the command side.** The command handler knows the resulting
   state, so return the id and the new state directly rather than making the client re-query.
   Often eliminates the problem entirely, and it's the cheapest fix.
2. **Read-your-own-writes via a version token.** The command returns the aggregate version (or
   log offset); the client passes it on the next read; the read side either waits until it has
   caught up to that version or tells the client to retry. This is the principled answer, and
   it's what "consistent prefix / monotonic reads" means in practice.
3. **Client-side optimistic update.** The UI shows the expected result immediately and
   reconciles when the projection catches up. Good UX, but you must handle the case where the
   command is later rejected.
4. **Design the UX for it.** "Your instruction has been accepted and is being processed" is
   both honest and usually fine. Most business processes are *already* asynchronous —
   settlement takes two days; nobody expects it to be instant.
5. **Synchronous projection for the few views that need it.** Update one critical read model in
   the same transaction as the append. You've given up some of CQRS's benefits for that view;
   sometimes that's the correct trade.
6. **Route reads that must be strongly consistent to the write side.** The aggregate can
   answer "what's the state of trade 123" authoritatively. Use sparingly — that's how you
   accidentally rebuild a monolith.

**What you must do operationally, regardless:**
- **Monitor projection lag** — events behind the log head, and time behind. This is the single
  most important metric in a CQRS system, and it should be alerted on. If lag is growing, your
  read models are silently wrong.
- **Make projections idempotent** (Q10), so retries and replays are safe.
- **Track the last-processed offset/version per projection**, durably, so a restart resumes.
- **Have a rebuild procedure** and know how long it takes (Q6/Q12).

**The framing that lands:** *"Eventual consistency isn't a bug you fix, it's a property you
design for — and the important question is what the consistency window is and whether the
business process can tolerate it. For settlement, where the real-world process takes T+2, a
200 ms projection lag is irrelevant. For a pre-trade risk check, it isn't — and that check
belongs on the write side inside the aggregate."*

That last sentence — **knowing which decisions must be strongly consistent and putting those
inside an aggregate** — is the real skill being probed.
</details>

---

### 5. How do you evolve event schemas without breaking consumers?

<details><summary>Answer</summary>

**This is the hardest ongoing cost of event sourcing, and interviewers know it.** Events are
**immutable and permanent** — you have five years of events serialised in a format you chose
five years ago, and you can never rewrite them. Get this answer right.

**The rules:**

1. **Only make additive, backward-compatible changes.**
   - **Adding** an optional field with a default: safe.
   - **Removing** a field: only if no consumer reads it. Deprecate first, verify, then remove
     from *new* events (old events still have it).
   - **Renaming** or **changing a type**: **not** compatible. Treat as add-new + deprecate-old,
     the same expand/contract discipline as a database migration (quiz 11 Q14).
   - **Never** change the meaning of an existing field. That's the one that causes silent
     data corruption, because nothing fails — the numbers are just wrong.

2. **A schema registry with enforced compatibility.** Confluent Schema Registry with **Avro**
   or **Protobuf**, set to `BACKWARD` (new schema reads old data) or `FULL` compatibility, and
   **enforced in CI** so an incompatible change fails the build rather than production. This
   is the single highest-leverage control, and naming it is important.
   - **Avro** — compact, schema-on-read with writer/reader schema resolution, excellent
     evolution story. Common in event-sourced systems.
   - **Protobuf** — field numbers are the contract, so renaming is free and removing means
     reserving the number. Very good evolution discipline.
   - **JSON** — human-readable and easy to debug, but no enforced schema unless you add JSON
     Schema. Verbose at volume. Fine for lower-volume domains; usually the pragmatic default.

3. **Version the event type.** Two approaches:
   - **In the type name** — `TradeSettledV1`, `TradeSettledV2`, both in the codebase.
     Explicit, verbose, and impossible to get wrong.
   - **A version field** in a stable envelope. Less type-safe but tidier.

4. **Upcasting** — the key technique to name. On read, transform old event versions into the
   current shape before the domain sees them, in one dedicated place:
   ```
   raw bytes -> deserialize -> upcast V1 -> V2 -> V3 -> domain event
   ```
   The **domain model only ever knows the latest version**; all the historical mess is
   quarantined in an upcaster chain. That keeps the aggregate clean, which is the whole point.
   Axon calls it upcasting; Akka/Pekko Persistence uses an `EventAdapter`.

5. **A stable envelope.** Keep metadata separate from payload: event id, type, version,
   aggregate id, sequence number, timestamp, correlation and causation ids, and the actor. The
   envelope should essentially never change, so infrastructure can route and store events
   without understanding the payload.

6. **Weak schema / tolerant reader.** Consumers ignore unknown fields rather than failing.
   Combined with additive-only changes, most evolution becomes a non-event.

**The nuclear options, and when they're justified:**
- **A new stream + copy-transform** — replay the old stream through a transformer into a new
  one. This is effectively a data migration, and it **breaks immutability of the log**, so
  it's a last resort requiring careful reconciliation.
- **Never** edit events in place. In a regulated environment that's likely to be an actual
  compliance issue, not just bad practice.

**The line to deliver:** *"You have to treat the event schema as a public API from day one,
because it is one — it has consumers you don't control, including your own future self
replaying five-year-old data. So: additive changes only, compatibility enforced in CI, and
upcasting so the domain model never sees a legacy shape."*
</details>

---

### 6. When and why would you introduce snapshots?

<details><summary>Answer</summary>

**The problem:** current state is `fold(events)`. An aggregate with 10 events replays
instantly; one with 500 000 events does not. Loading it becomes the latency of reading and
folding half a million rows — on **every command**.

**A snapshot** is a **persisted materialisation of the aggregate state at a known version**.
On load: read the latest snapshot, then replay **only the events after it**.

```
load(aggregateId):
  snapshot = snapshotStore.latest(aggregateId)          // state @ version 4800
  events   = eventStore.readFrom(aggregateId, 4801)     // just 12 events
  state    = events.foldLeft(snapshot.state)(apply)
```

**When to introduce one:**
- **Long-lived aggregates with large streams** — a `Position` that accumulates every trade
  for years, an account, a long-running instrument. Exactly the post-trade shape.
- **When load latency becomes measurable** on the command path. Typical triggers: streams past
  a few hundred to a few thousand events, or a p99 load time you can see.
- **Never prematurely.** Snapshots add real complexity (Q5's schema-evolution problem now
  applies to snapshots too, and they can be *wrong*). Most aggregates never need one. Measure
  first, and say that.

**Snapshotting strategies:** every N events (simple and predictable — the usual choice); on a
time schedule; on load if the gap exceeds a threshold; or on a business milestone (end of day,
after settlement).

**The things that make this a senior answer:**

1. **A snapshot is a derived cache, never the source of truth.** You must be able to **delete
   every snapshot and rebuild from events**. If you can't, you've accidentally made the
   snapshot authoritative and lost the property you adopted event sourcing for. That's the
   most important sentence here.
2. **Snapshots need versioning too.** Change the aggregate's internal state shape and old
   snapshots become unreadable — so either version them and upcast, or **discard and rebuild**
   on deploy, which is often simpler and perfectly safe *because* of point 1.
3. **They can hide bugs.** If `apply` had a bug that a snapshot has baked in, replaying from
   zero gives a different answer. A periodic **"rebuild from zero and compare"** reconciliation
   job catches that, and it's a genuinely good practice to propose.
4. **An alternative worth mentioning: close the stream instead.** For naturally-terminating
   lifecycles, end the aggregate. A `Trade` ends at settlement — so its stream is bounded and
   never needs a snapshot. **Fixing the aggregate boundary (Q2) often removes the need for
   snapshots entirely**, and that's the better fix when it's available.
5. Snapshots are for the **write side**. The read side's equivalent is the projection itself,
   plus checkpointing its offset.
</details>

---

### 7. How do you enforce optimistic concurrency on the write side?

<details><summary>Answer</summary>

Two commands for the same aggregate arrive concurrently. Both load state at version 42, both
validate against it, both append. Now you have two version-43 events built on the same
premise — and one of them made its decision against state that no longer holds. In a trade
system that's a double settlement.

**The mechanism: a unique constraint on `(aggregateId, version)`.**

```
1. Load events for aggregate, note the current version (say 42).
2. Handle the command against that state, producing new events.
3. Append with expectedVersion = 42.
4. The store enforces uniqueness on (aggregateId, sequenceNumber).
   - Success -> committed at 43.
   - Conflict -> someone else got there first.
```

This is **optimistic** locking: no locks held during validation, conflicts detected at commit.
It fits event sourcing perfectly because the version is already part of the log's structure —
you get it for free from a primary key.

**On conflict, the options:**
- **Retry** — reload, re-validate against the new state, re-apply the command. Correct default
  for most commands, and safe *because* you re-validate rather than blindly re-appending.
- **Reject** and surface a conflict to the caller (an HTTP 409). Right when the caller made a
  decision based on a state that has changed.
- **Merge**, if the domain genuinely commutes. Rare; be suspicious of it.

**Implementation notes worth having:**
- In a relational store: `PRIMARY KEY (aggregate_id, version)`, and the insert fails with a
  duplicate-key violation.
- **In Cassandra** — relevant to your background — you'd use a **lightweight transaction**
  (`INSERT ... IF NOT EXISTS`), which uses Paxos and is **substantially more expensive** than
  a normal write. That's a real design consideration: Cassandra is superb for append-only
  event storage and *not* naturally good at the uniqueness check, so many designs put the
  consistency boundary elsewhere (a single writer per aggregate).
- **The single-writer alternative:** route all commands for an aggregate to one process — an
  actor with cluster sharding (Akka/Pekko), or a partitioned Kafka consumer keyed by
  aggregate id. Then there *is* no concurrency for that aggregate and no conflicts to resolve.
  **This is a very common and elegant answer**, and it connects to the actor model (quiz 06
  B1) and to Kafka partitioning. The trade is that you now need cluster membership, sharding,
  and rebalancing.
- Bound retries and add jitter, or a hot aggregate becomes a livelock (same lesson as quiz 01
  Q8 / P03).
</details>

---

### 8. Where do you put validation and business rules?

<details><summary>Answer</summary>

Distinguish three kinds, because they belong in three places — this distinction is the
substance of the answer:

1. **Syntactic / structural validation** — is this a well-formed command? Required fields,
   formats, ranges, valid enum values. **At the edge**, before the command reaches the domain.
   Returns 400. Use `Validated` to **accumulate** all the errors rather than failing on the
   first (quiz 12 Q8).

2. **Invariant validation — inside the aggregate.** Rules that depend on the aggregate's own
   state and **must never be violated**: "cannot settle an unconfirmed trade", "cannot cancel
   a settled trade", "quantity cannot go negative". These are checked against state loaded
   inside the consistency boundary, so they're **strongly consistent** and cannot be raced
   (given Q7's version check). This is what an aggregate is *for*.

3. **Cross-aggregate / eventually-consistent rules** — anything needing data the aggregate
   doesn't own: "the account has sufficient credit", "the instrument is tradeable", "total
   exposure is within limits". The aggregate **cannot** check these atomically without
   swallowing the other aggregate. Options:
   - **Check on the read side before issuing the command** — fast, but it's advisory, since
     the state can change between check and command.
   - **A saga / process manager** (Q11) that coordinates and **compensates** if a later step
     fails.
   - **Reserve-then-confirm** — a two-phase reservation against the other aggregate
     (reserve credit, then settle, then confirm or release). Turns a distributed invariant
     into two local ones.
   - **Detect and compensate after the fact.** Sometimes the honest answer: allow it, detect
     the breach, and raise an exception for a human. Many real financial controls work this
     way, which is worth saying.

**Two things not to do:**
- **Don't validate in the event handler.** By the time you have an event, the fact has
  happened; `apply(state, event)` must be **total and side-effect-free** because it also runs
  during **replay**. If `apply` can reject, replaying history can fail — and then you can't
  load your own aggregate. **This is the single most important rule in the whole file**, and a
  very likely follow-up: *validation happens on the command, never on the event.*
- **Don't do I/O in the aggregate.** No database calls, no HTTP. The aggregate takes state and
  a command and returns events. That's what makes it fast, replayable, and trivially testable
  with no mocks (quiz 14 Q4).

**The pattern:** `decide(state, command): Either[Error, List[Event]]` and
`apply(state, event): State`. `decide` may reject; `apply` may not. Both pure. That pair is
the whole write side, and it's the shape sketched in quiz 12 Q20.
</details>

---

### 9. How do you build and rebuild projections?

<details><summary>Answer</summary>

**A projection is a fold over the event stream into a query-shaped view.**

```scala
// The projection is just: (readModel, event) => readModel
def project(event: TradeEvent): DbAction = event match {
  case Executed(id, qty, px, _)  => insert(TradeRow(id, qty, px, status = "NEW"))
  case Confirmed(id, _)          => update(id, status = "CONFIRMED")
  case Settled(id, on, _)        => update(id, status = "SETTLED", settledOn = on)
}
```

**The mechanics that matter:**

1. **Checkpointing.** Store the last-processed offset/sequence **durably**, and ideally **in
   the same transaction as the projection update** — that gives you exactly-once *effect* even
   with at-least-once *delivery*. If you can't do that (different stores), make the update
   **idempotent** (Q10) and accept at-least-once.
2. **Ordering.** Most projections need per-aggregate ordering at minimum. Partition by
   aggregate id (Kafka keys by aggregate id) so a single consumer sees one aggregate's events
   in order. **Global ordering across aggregates is expensive and usually unnecessary** —
   knowing you don't need it is the useful insight.
3. **Rebuild is a first-class operation, not an emergency procedure.** You will rebuild
   projections: to fix a bug, to add a field, to add a whole new view. So:
   - Build the new version into a **new table/index**, from offset 0.
   - Run it alongside the old one until it catches up.
   - **Atomically switch** reads (an alias, a view, or a config flag).
   - Keep the old one briefly, then drop it.
   That's **blue-green for projections**, and describing it that way is a strong signal.
4. **Know how long a rebuild takes.** "We have 400 million events; a full rebuild takes six
   hours" is a fact you must have, because it bounds your recovery time. If it's too slow:
   parallelise by partition, batch the writes, or snapshot the projection.
5. **Idempotency on replay** — Q10.
6. **Monitor lag** (Q4) and alert on it.

**The mindset to convey:** *"Projections are disposable. The event log is the source of truth,
and every read model is a cache I can throw away and rebuild. That's what makes it safe to
change them aggressively — and it's why I'd never let a projection become the only place a
piece of information exists."*

That last clause is the failure mode to watch for: a projection that **generates** data (a
random id, a timestamp, an externally-fetched value) is no longer reproducible, so a rebuild
changes it. Any such value must come **from the event**, not from the projector. This is a
subtle and important point, and a good one to raise unprompted.
</details>

---

### 10. Why must event handlers be idempotent, and how do you achieve it?

<details><summary>Answer</summary>

**Why: you will process the same event more than once.** Guaranteed, by several routes:
- **At-least-once delivery** from any real message bus. Exactly-once end-to-end doesn't exist
  in general; you get at-least-once plus idempotency, or you get at-most-once and lose things.
- **Consumer restart** after a crash between "processed" and "checkpointed".
- **Rebalancing** — a partition moves and the new owner reprocesses from the last checkpoint.
- **Deliberate replay** — the whole point of event sourcing.

So a projection that does `balance = balance + amount` will **double-count**, and the resulting
error is silent and cumulative. In a settlement or position-keeping system that's an incident.

**How to achieve it:**

1. **Make the operation naturally idempotent.** Prefer **absolute over relative**: `SET
   status = 'SETTLED'` is idempotent; `SET count = count + 1` is not. An **upsert** keyed on
   the aggregate id is idempotent. **Reshape the write and the problem disappears** — this is
   the best answer where it's available.
2. **Deduplicate on event id.** Keep a `processed_events` table, or the last-seen sequence
   number per aggregate, and check-then-skip **inside the same transaction** as the update.
3. **Track the sequence number in the read model itself.** Store `last_seq` on the row and
   apply only if `event.seq > row.last_seq` — a single conditional update, no separate dedupe
   table, and it also handles out-of-order delivery. **Elegant, and my usual recommendation.**
4. **Checkpoint atomically with the update** — same transaction, same store. Then the
   at-least-once window closes entirely.
5. **For genuinely non-idempotent side effects** (send an email, submit a payment), you can't
   make the *effect* idempotent, so you need an **idempotency key** carried to the downstream
   system and honoured by *it*. If the downstream doesn't support that, you need
   at-most-once semantics and a reconciliation process — and you should say so plainly rather
   than pretend the problem is solved.

**Related and worth volunteering:** the same discipline applies to the **command** side — a
client retrying `SettleTrade` because it didn't see the response must not settle twice. That's
a **command idempotency key** (client-supplied request id, deduplicated by the aggregate). And
the aggregate's own optimistic version check (Q7) gives you natural protection against
duplicate *appends*.
</details>

---

### 11. What's a saga / process manager, and when do you need one?

<details><summary>Answer</summary>

**When you need one:** a business process spans **multiple aggregates** (or services), so it
cannot be one transaction — but it still needs to reach a consistent outcome.

Post-trade is full of these: a trade must be confirmed, cleared, netted, funded, and settled,
across systems, over days, with failure at any step.

**Two flavours, and knowing the difference is the point:**

- **Choreography** — no coordinator. Each service reacts to the previous event and emits the
  next. Loosely coupled and simple to start; the **process becomes implicit and impossible to
  see** in any one place, which makes it hard to debug and to change. Fine for 2–3 steps.
- **Orchestration** (a **process manager**) — an explicit component owning the workflow: it
  listens for events, decides the next command, and tracks the process state as its own
  state machine. **The process is visible, queryable, and testable in one place**, at the cost
  of a component that knows about several services. **For anything with more than about three
  steps, or with timeouts and compensation, prefer orchestration** — and in a regulated domain
  you generally want to be able to *see* the process, which is a strong argument for it.

**Compensation, not rollback — the essential concept.** You cannot roll back a committed
transaction in another service, so each step has a **compensating action** that
semantically undoes it. Not "un-settle", but "book a reversal". Compensation is a **new
business fact**, which is exactly why an event log suits it — the reversal is another event,
and the audit trail shows what happened and what corrected it.

**What makes a saga answer sound experienced:**
- **Timeouts are first-class.** "Waiting for clearing confirmation" needs a deadline and an
  action when it expires. A saga without timeouts hangs silently forever, and that's the most
  common real defect.
- **Sagas must be idempotent** (Q10) — they're event consumers.
- **Compensation can fail too.** At some point you escalate to a human. Say this: mature
  systems have an exception queue and an ops process, not infinite automated retry.
- **It's not ACID and shouldn't pretend to be.** Intermediate states are *visible* to other
  parts of the system, so "trade is cleared but not funded" must be a legitimate,
  representable state — not an inconsistency. That's a modelling requirement, and it's why
  quiz 12 Q20 makes states explicit.
- **Prefer avoiding it.** If two aggregates are *always* changed together, that's evidence the
  boundary is wrong (Q2). Fix the boundary before reaching for a saga.

**Contrast with two-phase commit:** 2PC gives you atomicity across resources but blocks, holds
locks, and has a coordinator as a single point of failure — which is why it's largely avoided
in distributed systems. Sagas trade atomicity for availability: you get eventual consistency
plus visible intermediate states.
</details>

---

### 12. What technology would you use for the event store?

<details><summary>Answer</summary>

Frame it by requirement, then name options — and be honest about where your experience is.

**What an event store must do:** append-only writes; read a single aggregate's stream in
order; a uniqueness guarantee on `(aggregateId, version)` for optimistic concurrency (Q7);
subscribe to new events for projections; and retain everything, durably, forever.

| Option | Strengths | Watch out for |
|---|---|---|
| **Relational (Postgres)** | transactional append **plus** projection in one commit; `PRIMARY KEY (agg_id, version)` gives you concurrency control free; easy to query and operate; everyone knows it | scaling writes past a single node; you build subscriptions yourself (`LISTEN/NOTIFY`, or polling an ordered sequence) |
| **Cassandra** | superb append throughput, linear scale, multi-DC replication, `(agg_id, version)` is a natural partition+clustering key | **uniqueness needs a lightweight transaction (Paxos) — expensive**; no cross-partition ordering; no built-in subscription; eventual consistency needs care on read-your-writes |
| **Kafka** | the log *is* the model; excellent throughput; built-in partitioned consumption and consumer groups; natural projection feed | **not a database** — no random access to one aggregate's stream without a compacted topic or an external index; retention and compaction need thought; no uniqueness check |
| **EventStoreDB** | purpose-built: streams, `expectedVersion`, subscriptions, projections | another specialised system to operate; smaller talent pool |
| **DynamoDB** | conditional writes give you optimistic concurrency cleanly; streams for projections | item-size limits; cost model |

**The pragmatic recommendation to give:** for most teams, **Postgres until you've proven you
need more**. Being able to append the event and update the critical projection in **one
transaction** removes an entire category of problem (Q9/Q10), and a single Postgres handles a
very large number of events. Then, if write volume genuinely demands it, move to Cassandra or
a dedicated store and accept the extra machinery.

**A very common production shape worth describing:** **Postgres (or Cassandra) as the
authoritative store, Kafka as the distribution mechanism** — append to the store, publish to
Kafka via the **transactional outbox** or change-data-capture (Debezium). You get
transactional integrity *and* a scalable fan-out. This is the design I'd propose if asked, and
it maps directly onto Kafka + Cassandra experience.

**On AMPS**, since the JD lists it: it's a low-latency pub-sub bus with a "State of the World"
cache, so it's a **distribution and current-state-query layer, not a durable event store** —
you'd use it where Kafka sits in the diagram above, for low-latency fan-out and last-value
queries, not as your system of record. See quiz 15 Q9.
</details>

---

### 13. What is the dual-write problem, and how does the outbox pattern solve it?

<details><summary>Answer</summary>

**The problem:** you need to change state **and** publish an event. If those are two separate
systems, there's no transaction across them:

```
db.save(trade)             // succeeds
kafka.publish(TradeSettled) // CRASHES
// -> state changed, nobody was told. The projection and every downstream is now WRONG,
//    silently, forever.
```
Reversing the order is no better — you publish, then fail to save, and you've announced
something that didn't happen. **There is no ordering that fixes it**, which is the insight.

**The transactional outbox** — the standard solution:

1. In **one local database transaction**, write the state change **and** insert the event into
   an `outbox` table.
2. A separate **relay** process reads unpublished outbox rows, publishes them to Kafka, and
   marks them sent.
3. Delivery is **at-least-once**, so consumers must be **idempotent** (Q10).

Because both writes are in one transaction against one database, they succeed or fail
together. The only remaining failure mode is publishing twice, which idempotency handles.

**Variants worth naming:**
- **Change Data Capture (Debezium)** — tail the database's write-ahead log and publish
  changes. No outbox table and no relay to write, and it can't miss a change. Costs: another
  moving part, and your event schema becomes coupled to your table schema unless you use an
  outbox table *with* CDC — which is the common combination and the best of both.
- **Event sourcing largely dissolves the problem**, and this is the good point to make: if
  the **event log is the state**, there's only one write. Publishing is then a matter of
  reading the log, which is replayable by construction. **This is one of the strongest
  arguments for event sourcing in an integration-heavy system** and connects directly to Q1
  and Q12.
- **Listen-to-yourself** — publish first, and treat your own consumer as the thing that
  updates state. Now there's one write, but you've made your own state eventually consistent.

**The inverse problem to mention if it comes up:** the **inbox** pattern — deduplicating
inbound messages by id on the consumer side, in the same transaction as the effect. Same idea,
other direction, and it's how you get exactly-once *effects* from at-least-once delivery.
</details>

---

### 14. What are the real costs of event sourcing? When would you *not* use it?

<details><summary>Answer</summary>

**Being able to argue against it is what distinguishes someone who has run one.** Volunteer
this even if not asked.

**The costs:**
1. **Eventual consistency everywhere** (Q4). Every "save then show" flow needs thought, and
   the team must internalise it. This is the biggest day-to-day tax.
2. **Schema evolution is forever** (Q5). You will maintain upcasters for events written years
   ago by people who have left.
3. **You cannot fix data with an `UPDATE`.** Correcting a mistake means appending a
   compensating event, which is more honest and much more work. No quick production data fix.
4. **Query difficulty.** Every query needs a projection built and maintained in advance. Ad-hoc
   "just check this in the database" becomes hard, which slows down support and analysis.
5. **Replay cost and time** (Q6/Q9). Rebuilds take hours at scale, and that's your RTO.
6. **Steep learning curve.** Commands vs events, aggregates, idempotency, sagas — a team
   unfamiliar with it will produce something worse than a well-built CRUD service. **This is
   the most common reason event-sourced projects fail**, and saying so is credible.
7. **Storage growth**, monotonic and unbounded. Manageable, but real.
8. **Debugging is different** — distributed, asynchronous, and needing correlation ids
   everywhere rather than a stack trace.
9. **GDPR / right-to-erasure sits awkwardly with an immutable log.** The usual answer is
   **crypto-shredding** — store personal data encrypted per subject and destroy the key. Worth
   knowing; it's a genuine and frequently-asked wrinkle.

**When *not* to use it:**
- **Simple CRUD** with no audit or temporal requirement. Most applications. Use a database.
- **When the team doesn't have the experience** and there's no time to build it.
- **When the domain has no meaningful events** — if state changes aren't business-significant
  facts, you're just writing a diff log.
- **Reporting/analytics-heavy** workloads with no transactional core — a warehouse is a better
  tool.
- **When strong read-after-write consistency is a hard requirement** across the board.

**When it genuinely earns its place:**
- **Regulatory audit requirements** — you must prove what you knew and when.
- **Temporal correctness matters** — reconciliation, dispute resolution, as-of reporting.
- **Complex domain with meaningful state transitions** — a trade lifecycle is a textbook fit.
- **Many consumers needing different views** of the same underlying facts.
- **Integration-heavy** systems where the log is the natural contract (Q13).

**Post-trade securities processing hits five of those**, which is a good closing observation:
regulators require the audit trail, reconciliation is temporal by nature, the trade lifecycle
is a genuine state machine, and settlement/position/regulatory reporting are all different
views of the same events. **That's why this JD asks about it** — and saying that shows you've
connected the architecture to the domain rather than learned it abstractly.
</details>

---

### 15. How would you test the write side and the read side?

<details><summary>Answer</summary>

The lovely property of this architecture is that **both sides are pure functions**, so both
test without mocks, containers, or a running system. Lead with that.

**Write side — Given-When-Then over events and commands.** The aggregate is
`decide(state, command): Either[Error, List[Event]]` (Q8), so:

```
GIVEN  a history of events        (the setup — no database, no mocks)
WHEN   a command is handled
THEN   these events are produced (or this error is returned)
```
```scala
"reject settlement of an unconfirmed trade" in {
  val given = List(Executed(tradeId, qty = 1000, px, at = t1))   // NOT confirmed
  val when  = SettleTrade(tradeId, valueDate)
  val then_ = decide(replay(given), when)
  then_ shouldBe Left(InvalidTransition(TradeState.New, when))
}
```
**No mocks at all.** Setup is a list of events; the assertion is on the returned events. This
is the cleanest unit-testing story of any architecture, and it maps **exactly** onto a BDD
feature file's Given/When/Then (quiz 14 Q4) — which is presumably why this JD asks for both.

**Read side — feed known event sequences, assert the view.**
```
GIVEN  this sequence of events
WHEN   projected
THEN   the read model contains these rows
```
Test the projection function in isolation, then against a real database with Testcontainers
(not H2 — quiz 10 Q8). **And explicitly test idempotency**: apply the same event twice and
assert the result is unchanged (Q10). Also test **out-of-order** and **replay-from-zero**.

**The other tests worth naming:**
- **`apply` must be total.** Property-test it: for *any* state and *any* event, `apply` returns
  a state and never throws — because it runs during replay and a failure there means you can't
  load your aggregate (Q8).
- **Round-trip serialisation** for every event type, and **upcasting tests with real historical
  payloads** — keep a corpus of old serialised events as fixtures and assert they still
  deserialise into the current model. **This is the test that stops Q5's schema evolution
  becoming an outage**, and very few candidates think to mention it.
- **Saga tests** — drive the state machine with event sequences, including the timeout and
  compensation paths. The compensation path is the one that's never tested and always broken.
- **Contract tests** on published events, so a consumer learns at build time when you change
  a schema.
- **Integration** — a thin slice end to end: command → event → projection → query.
- **Property-based testing** fits unusually well here: generate random valid command sequences
  and assert invariants hold (a position never goes negative; replaying to any prefix gives a
  valid state).

**The summary line:** *"Event sourcing makes the business logic a pure function of state and
input, so the valuable tests need no infrastructure. What I'd add beyond the obvious is a
corpus of real historical event payloads as deserialisation fixtures — that's the regression
test for schema evolution, and it's the one that actually saves you."*
</details>

---

### 16. Design it: high-volume, time-critical trade event processing.

<details><summary>Answer</summary>

An open design question — likely the centrepiece of a system-design round for this role.
**Ask about requirements first**, then sketch, then name the trade-offs.

**Questions to ask before designing** (asking these *is* part of the answer):
- Volume and shape — events/sec, peak vs average, burst profile? Market-open spikes?
- Latency requirement — is this microseconds (front office) or seconds (post-trade)? **Post-
  trade is usually throughput- and correctness-critical rather than microsecond-latency
  critical**, and getting that distinction right matters.
- Ordering — per trade, per account, or global?
- Delivery — can we tolerate duplicates (then idempotency) or must we not lose any (then
  durability first)?
- Retention and audit requirements — how far back, and queryable how?
- Recovery objectives — RTO/RPO, and what a replay costs.

**The sketch:**
```
  Upstream (execution venues, OMS)
        |  FIX / Kafka / AMPS
        v
  [ Ingest / gateway ]  -- validate structurally, assign correlation id, idempotency key
        |
        v
  [ Command side ]  -- partitioned by tradeId (single writer per aggregate, Q7)
        |              load aggregate (snapshot + tail, Q6)
        |              decide(state, command) -> events
        v
  [ Event store ]  -- append (aggId, version), Postgres or Cassandra
        |
        +--> [ outbox / CDC ] --> Kafka (partitioned by tradeId)   (Q13)
                                     |
        +----------------------------+-------------------+-----------------+
        v                            v                   v                 v
  [ Position projection ]   [ Settlement view ]   [ Regulatory report ]  [ Client API view ]
   (Postgres/Cassandra)      (Postgres)            (warehouse)           (Redis/ES)
```

**The decisions to justify:**
1. **Partition by aggregate id** (trade id). Gives you per-aggregate ordering and a single
   writer, which removes write contention *and* makes optimistic concurrency almost free.
   Scale by adding partitions. **This is the most important design choice** — say it first.
2. **Append-only writes** — no read-modify-write, no row locks, so write throughput scales.
3. **Snapshots** for long-lived aggregates (positions), not for bounded ones (trades close at
   settlement) — Q6.
4. **Multiple independent projections**, each checkpointed and rebuildable, each with **lag
   monitoring and alerting** (Q4).
5. **Outbox or CDC** for publication so state and events can't diverge (Q13).
6. **Idempotent consumers everywhere** (Q10).
7. **Backpressure**, not unbounded queues. Kafka gives you a durable buffer; within a service,
   bound every queue and every fan-out — an unbounded queue converts overload into an OOM
   (quiz 02 Q2, and the measured fan-out in `S02FutureTraps.scala` Part 3).
8. **Bulkheads per downstream** so a slow custodian or CCP connection degrades one flow, not
   the service (quiz 02 Q4).

**The operational answers, which is where most candidates stop short:**
- **Replay** — how long does a full rebuild take, and can you do it without downtime?
  (Blue-green projections, Q9.)
- **Reconciliation** — an independent check that projections agree with a replay from zero, and
  that your positions agree with the custodian's. In a bank this is not optional.
- **Poison messages** — a dead-letter queue plus an ops process, not an infinite retry loop.
- **Observability** — correlation and causation ids on every event, projection lag, consumer
  group lag, per-stage latency histograms, and GC/pool metrics from the rest of this pack.
- **End-of-day and cut-off handling** — a real post-trade concern: value dates, batch windows,
  and the fact that "now" is a business date, not a wall clock.

**Latency-specific notes if they push on "time-critical":** avoid allocation on the hot path
(quiz 06 D1), keep the GC young-gen work small and consider **Generational ZGC** if pause
p99 matters (quiz 05 Q4), pre-touch the heap (quiz 04 Q20), avoid `synchronized` on hot
paths, and measure with JFR rather than guessing. **That's where this quiz connects back to
the concurrency and GC material** — and being able to join those up is the strongest thing
you can demonstrate in a final round.
</details>

---

## Score yourself

- **Must be automatic:** Q1, Q2 (vocabulary), Q3, Q4 (eventual consistency), Q5 (schema
  evolution), Q6 (snapshots) — those are the ones in your source list.
- **The ones that show you've built one:** Q8 (validation on commands, never events), Q10
  (idempotency), Q13 (dual writes), Q14 (the costs).
- **Practise out loud:** Q16, and the last paragraph of Q14 that ties event sourcing to why
  post-trade specifically wants it.
