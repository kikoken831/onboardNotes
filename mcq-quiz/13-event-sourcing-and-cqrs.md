# MCQ 13 — Event Sourcing & CQRS

16 questions. Source: [`quiz/13`](../quiz/13-event-sourcing-and-cqrs.md).
**Core to the JD** — this is the file to know coldest after the concurrency material.

**Note:** `quiz/13` Q16 (design high-volume trade event processing) stays in the source file — it's
the open design question for a system-design round.

---

### Q1. What is the defining characteristic of event sourcing?

- **A.** Publishing events to Kafka whenever state changes
- **B.** Using an event bus instead of direct method calls
- **C.** State is stored as an immutable, append-only sequence of events, and current state is *derived* by replaying them
- **D.** Storing an audit table alongside the current-state table

<details><summary>Answer</summary>

**C.** The log **is** the source of truth, not a side-effect of it.

- **A** and **B** describe **event-driven** architecture, which is a different thing and constantly
  conflated with event sourcing. You can be event-driven with a perfectly ordinary mutable database.
- **D** is the traditional alternative — and the key difference is that a bolt-on audit table **can
  drift** from reality, whereas here the audit trail *is* reality.

The reason to lead with in a bank: **the audit trail is the storage model, not a bolt-on.** You can
answer "what did we believe at 16:00 yesterday, and why" — which regulators do ask.

→ `quiz/13` Q1
</details>

---

### Q2. `SettleTrade` vs `TradeSettled` — which is the command?

- **A.** `SettleTrade` — imperative, an intent, and it **can be rejected**
- **B.** `TradeSettled` — because it names the outcome
- **C.** Both are commands
- **D.** Both are events

<details><summary>Answer</summary>

**A.** A **command** is an intent, imperative, addressed to one aggregate, and rejectable — validation
happens here. An **event** is a **fact, past tense**, and cannot be rejected because it already
happened.

**Interviewers listen for this naming discipline.** Getting it backwards in a design discussion
signals you haven't built one.

→ `quiz/13` Q2
</details>

---

### Q3. What is an aggregate, primarily?

- **A.** A denormalised read model
- **B.** A database table
- **C.** A collection of related events
- **D.** The **consistency boundary** — the unit of atomicity, which enforces its own invariants

<details><summary>Answer</summary>

**D.** One command → one aggregate → zero or more events, committed together.

The design judgement being tested: an aggregate should be **as small as possible** while still
enforcing its invariants. A `Trade` aggregate, **not** a `Portfolio` containing every trade — the
latter serialises all writes for an account and its event stream grows without bound.

The rules: invariants that must hold **immediately** go *inside* one aggregate; anything that can be
eventually consistent goes *across* aggregates via events or a saga; **one command should touch one
aggregate** (if it needs two, either the boundary is wrong or you need a saga); and reference other
aggregates **by id**.

→ `quiz/13` Q2
</details>

---

### Q4. Why does CQRS pair naturally with event sourcing?

- **A.** Both require Kafka
- **B.** An event log is excellent for consistency and auditing and terrible for querying, so a separate read model isn't optional — it's the consequence
- **C.** CQRS is required by event sourcing specifications
- **D.** They both need eventual consistency

<details><summary>Answer</summary>

**B.** "Show me all unsettled trades for account X sorted by value date" can't be answered efficiently
by replaying event streams. So you need projections — and you can have **many**, each shaped for its
query and independently rebuildable.

The important clarification, because it's a common misconception: **CQRS does not require event
sourcing, and event sourcing does not require CQRS.** CQRS over a relational write model with a
denormalised read replica is a normal, much cheaper pattern. Saying this shows you're not
cargo-culting.

→ `quiz/13` Q3
</details>

---

### Q5. A user submits a trade, is redirected to the trade list, and doesn't see it. Best first fix?

- **A.** Return the new state from the command directly, so the client needn't re-query
- **B.** Make the projection update synchronous for all views
- **C.** Add a client-side delay before redirecting
- **D.** Route all reads to the write side

<details><summary>Answer</summary>

**A.** The command handler already knows the resulting state. Cheapest fix, and it often eliminates the
problem entirely.

Next best: a **version token** the command returns and the client passes on the next read, so the read
side either waits until it's caught up or says retry — that's read-your-own-writes done properly.
Then: optimistic UI, or just designing the UX for it ("your instruction has been accepted"), since
most business processes are *already* asynchronous.

- **B** and **D** are legitimate for the *few* views that genuinely need it, but applied broadly they
  throw away CQRS's benefits and rebuild a monolith.
- **C** is a race with extra steps.

And the operational non-negotiable: **monitor projection lag and alert on it.** If lag is growing,
your read models are silently wrong.

→ `quiz/13` Q4
</details>

---

### Q6. Which event schema change is safe?

- **A.** Renaming a field
- **B.** Changing a field's type from `int` to `string`
- **C.** Adding an optional field with a default
- **D.** Removing a field that a consumer still reads

<details><summary>Answer</summary>

**C.** **Additive changes only.** Events are immutable and permanent — you'll be reading five-year-old
payloads written by people who have left.

- **A** and **B** are *not* compatible; treat them as add-new + deprecate-old, the same expand/contract
  discipline as a database migration.
- Never change the **meaning** of an existing field — that's the one that causes silent data
  corruption, because nothing fails and the numbers are just wrong.

The controls: a **schema registry with compatibility enforced in CI** (Avro or Protobuf), version the
event type, and **upcasting** — a chain transforming old versions into the current shape on read, so
**the domain model only ever sees the latest version** and the historical mess is quarantined.

→ `quiz/13` Q5
</details>

---

### Q7. What is upcasting?

- **A.** Casting a subtype to a supertype
- **B.** Transforming old event versions into the current shape on read, before the domain sees them
- **C.** Promoting an event to a snapshot
- **D.** Increasing an aggregate's version number

<details><summary>Answer</summary>

**B.** `raw bytes → deserialize → upcast V1 → V2 → V3 → domain event`. Axon calls it upcasting;
Akka/Pekko Persistence uses an `EventAdapter`.

The value is keeping the **aggregate clean**: all the legacy shapes live in one dedicated place rather
than as `if (version == 1)` branches scattered through your business logic.

And the test that makes this safe, which very few candidates mention: **keep a corpus of real
historical serialised events as fixtures** and assert they still deserialise into the current model.
That's the regression test for schema evolution, and it's the one that actually saves you.

→ `quiz/13` Q5, Q15
</details>

---

### Q8. When should you introduce snapshots?

- **A.** Always, from day one
- **B.** Never — they break the event-sourcing model
- **C.** Whenever the aggregate has more than 10 events
- **D.** When an aggregate's stream is long enough that load latency on the command path is measurable

<details><summary>Answer</summary>

**D.** Measure first. Most aggregates never need one, and snapshots add real complexity — schema
evolution now applies to them too, and they can be *wrong*.

Typical triggers: a `Position` accumulating every trade for years; streams past a few hundred to a few
thousand events; a p99 load time you can see.

**The senior points:** a snapshot is a **derived cache, never the source of truth** — you must be able
to delete every snapshot and rebuild from events, and if you can't, you've lost the property you
adopted event sourcing for. Snapshots need versioning (or discard-and-rebuild on deploy, which is
often simpler *because* of that). They can **hide bugs** baked in by a faulty `apply`, so a periodic
**rebuild-from-zero-and-compare** job is a good control.

And the better fix when available: **close the stream instead.** A `Trade` ends at settlement, so its
stream is bounded and never needs a snapshot. Fixing the aggregate boundary often removes the need
entirely.

→ `quiz/13` Q6
</details>

---

### Q9. How do you enforce optimistic concurrency on the write side?

- **A.** A pessimistic row lock on the aggregate
- **B.** A distributed lock in Redis
- **C.** A unique constraint on `(aggregateId, version)`, appending with an expected version
- **D.** Serialising all commands through a single global thread

<details><summary>Answer</summary>

**C.** Load, note the version, decide, then append with `expectedVersion`. The store enforces
uniqueness; a conflict means someone got there first, so you **reload, re-validate, and retry** —
re-validating rather than blindly re-appending is what makes it correct.

It fits event sourcing perfectly because the version is already part of the log's structure — you get
it free from a primary key.

**The common alternative worth naming:** route all commands for an aggregate to a **single writer** —
an actor with cluster sharding, or a Kafka consumer partitioned by aggregate id. Then there *is* no
concurrency for that aggregate and no conflicts to resolve. The trade is cluster membership, sharding
and rebalancing.

Implementation note relevant to Cassandra: uniqueness needs a **lightweight transaction** (Paxos),
which is substantially more expensive than a normal write — so many Cassandra designs use the
single-writer approach instead.

→ `quiz/13` Q7
</details>

---

### Q10. Where does business validation belong?

- **A.** In the command handler (`decide`), against state loaded inside the aggregate
- **B.** In the event handler (`apply`)
- **C.** In the projection
- **D.** In the event store

<details><summary>Answer</summary>

**A.** And this is **the single most important rule in the whole topic.**

`apply(state, event)` must be **total and side-effect-free**, because it also runs during **replay**.
If `apply` can reject, replaying history can fail — and then **you can't load your own aggregate.**

The pattern: `decide(state, command): Either[Error, List[Event]]` may reject;
`apply(state, event): State` may not. Both pure.

So: **validation happens on the command, never on the event.** Expect this as a follow-up.

Layering: structural validation at the edge (returning 400, accumulating errors with `Validated`);
invariants inside the aggregate; cross-aggregate rules via a saga, reserve-then-confirm, or
detect-and-compensate.

And: **no I/O in the aggregate.** State + command → events. That's what makes it fast, replayable and
trivially testable.

→ `quiz/13` Q8
</details>

---

### Q11. Why must event handlers be idempotent?

- **A.** To support multi-threaded projection
- **B.** To allow rollback
- **C.** Because events can arrive out of order
- **D.** Because you *will* process the same event more than once — at-least-once delivery, consumer restarts, rebalancing, and deliberate replay

<details><summary>Answer</summary>

**D.** Guaranteed by several routes. Exactly-once end-to-end doesn't exist in general; you get
at-least-once **plus idempotency**.

So `balance = balance + amount` will **double-count**, and the error is silent and cumulative — an
incident in a settlement or position-keeping system.

How to achieve it, best first: **make the operation naturally idempotent** (prefer absolute over
relative — `SET status = 'SETTLED'` and upserts are idempotent); **track the sequence number in the
read model itself** and apply only if `event.seq > row.last_seq` (one conditional update, no dedupe
table, and it handles out-of-order too — usually the most elegant); deduplicate on event id in the
same transaction; or **checkpoint atomically with the update**, which closes the window entirely.

For genuinely non-idempotent side effects (send a payment) you need an **idempotency key honoured by
the downstream** — and if it doesn't support one, say so plainly rather than pretend it's solved.

→ `quiz/13` Q10
</details>

---

### Q12. A business process spans four aggregates over two days, with failure possible at each step. What do you use?

- **A.** A distributed transaction (2PC)
- **B.** A saga / process manager with compensating actions and timeouts
- **C.** One large aggregate covering all four
- **D.** A database trigger chain

<details><summary>Answer</summary>

**B.** Post-trade is full of these — confirm, clear, net, fund, settle.

**Compensation, not rollback**: you can't roll back a committed transaction in another service, so each
step has a compensating action. Not "un-settle" but "book a reversal" — a **new business fact**, which
is exactly why an event log suits it.

Prefer **orchestration** (an explicit process manager owning the state machine) over
**choreography** (each service reacting to the previous event) for anything beyond about three steps —
choreography makes the process implicit and impossible to see in one place, and in a regulated domain
you generally want to be able to *see* it.

What makes the answer sound experienced: **timeouts are first-class** (a saga without them hangs
silently forever — the most common real defect); sagas must be idempotent; **compensation can fail
too**, so you escalate to a human exception queue; and intermediate states are **visible**, so
"cleared but not funded" must be a legitimate representable state.

- **A** blocks, holds locks, and has a coordinator as a single point of failure.
- **C** is the wrong boundary (Q3).

→ `quiz/13` Q11
</details>

---

### Q13. What is the dual-write problem?

- **A.** Changing state and publishing an event as two separate operations with no transaction across them, so a crash between them leaves the system inconsistent
- **B.** Writing the same event to two topics
- **C.** Two aggregates writing to the same stream
- **D.** Writing to both a primary and a replica

<details><summary>Answer</summary>

**A.** `db.save(trade)` succeeds, `kafka.publish(...)` crashes — state changed and nobody was told;
every projection and downstream is now silently and permanently wrong. Reversing the order is no
better: you announce something that didn't happen. **There is no ordering that fixes it**, which is
the insight.

The solution is the **transactional outbox**: in **one local transaction** write the state change *and*
insert the event into an `outbox` table; a relay publishes and marks them sent. At-least-once, so
consumers must be idempotent (Q11).

Variants: **CDC/Debezium** (tail the WAL — no outbox table or relay to write, and it can't miss a
change), usually combined *with* an outbox table so your event schema isn't coupled to your table
schema.

**And event sourcing largely dissolves the problem** — if the event log *is* the state, there's only
one write. That's one of the strongest arguments for it in an integration-heavy system.

→ `quiz/13` Q13
</details>

---

### Q14. When would you argue **against** event sourcing?

- **A.** When the system has high write volume
- **B.** When you need an audit trail
- **C.** Simple CRUD with no audit or temporal requirement, or a team without the experience
- **D.** When there are many different read models

<details><summary>Answer</summary>

**C.** Most applications. Use a database.

**Being able to argue against it is what distinguishes experience from having read the book** — and
you should volunteer this even if not asked.

The costs: eventual consistency everywhere; **schema evolution forever**; you can't fix data with an
`UPDATE` (a correction is a compensating event — more honest, much more work, and no quick production
data fix); every query needs a projection built in advance, so ad-hoc support investigation gets hard;
replay takes hours at scale; **storage grows monotonically**; and **GDPR right-to-erasure sits awkwardly
with an immutable log** (the answer is crypto-shredding — encrypt per subject, destroy the key).

**The most common reason these projects fail is team unfamiliarity, not architecture.**

**B** and **D** are reasons *for* it. **A** is actually a point in its favour — append-only writes are
contention-free.

→ `quiz/13` Q14
</details>

---

### Q15. How do you unit-test an aggregate?

- **A.** Mock the repository and the event bus, then verify the interactions
- **B.** Given a list of past events, when a command is handled, then assert on the events returned — no mocks, no database
- **C.** Spin up the full application with Testcontainers
- **D.** Replay production events and diff the output

<details><summary>Answer</summary>

**B.** Because `decide(state, command): Either[Error, List[Event]]` is a **pure function**, setup is
constructing a list and the assertion is on the returned events. **Milliseconds, no mocks at all.**

This is the cleanest unit-testing story of any architecture — and it maps **exactly** onto a BDD
feature file's Given/When/Then, which is presumably why this JD asks for event sourcing *and* BDD
together.

Also test: that `apply` is **total** (property-test it — any state, any event, never throws);
round-trip serialisation plus **upcasting against real historical payloads** (Q7); saga timeout and
compensation paths; and projection **idempotency** (apply the same event twice, assert unchanged).

→ `quiz/13` Q15, `quiz/14` Q4
</details>

---

### Q16. Why is post-trade securities processing a genuinely good fit for event sourcing?

- **A.** It has high transaction volume
- **B.** Trades are immutable by nature
- **C.** It uses Kafka already
- **D.** Regulatory audit is a hard requirement, reconciliation and entitlements are inherently temporal, the trade lifecycle is a real state machine, and many consumers need different views of the same facts

<details><summary>Answer</summary>

**D.** Five of the "when to use it" criteria hit at once — which is why the JD asks about it, and
saying so shows you've connected the architecture to the domain rather than learned it abstractly.

Concretely: **regulators require** proving what you knew and when; **as-of reconstruction** is needed
for reconciliation and corporate-action record dates; the lifecycle (executed → confirmed → cleared →
settled, with fails, amendments and cancellations) is a genuine state machine; and settlement views,
position keeping and regulatory reporting are all different projections of the same events.

**And the honest caveat that makes it a senior answer:** the eventual-consistency window must be
checked against each use. A **settlement instruction cut-off is a hard deadline** — if projection lag
means you miss it, that's a real financial loss, not a UX annoyance. So lag must be alerted on, and
anything on the critical path to a cut-off should read authoritatively.

→ `quiz/13` Q14, `quiz/15` Q8
</details>

---

## Score yourself

- **14+** — you can hold this conversation, which for this JD matters most.
- **Must be automatic:** Q1 (vs event-driven), Q2 (naming), Q4, Q5, Q6, Q8.
- **The ones that show you've built one:** **Q10** (validation on commands, never events), **Q11**
  (idempotency), **Q13** (dual writes), **Q14** (the costs).
- **Q16 is the synthesis question** — practise it out loud, along with the last paragraph of Q14.
