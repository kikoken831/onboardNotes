# Cheatsheet — 60-second spoken answers

**32 questions** you're most likely to be asked, with answers structured for **speaking**,
not reading. Each is: **one-line claim → mechanism → consequence → what you'd do.**

- **Part 1 (Q1–Q15)** — concurrency, GC, and the migration.
- **Part 2 (Q16–Q23)** — SQL, collections, patterns, Spring, and behavioural/process.
- **Part 3 (Q24–Q32)** — the BofA JD topics: monads, event sourcing, CQRS, schema evolution,
  feature files, the trade lifecycle, T+1, AMPS, ATDD.

Read these last, the morning of. Practise saying **at least three out loud**, including one
from Part 3 — Q25 (event sourcing), Q29 (trade lifecycle) and Q30 (T+1) are the ones most
likely to decide this interview.

---

# Part 1 — concurrency, GC & the migration

---

### 1. "What does `volatile` do?"

> Visibility and ordering, not atomicity. A volatile write is a release and a volatile read
> is an acquire, so everything written before the write is visible to a thread that reads
> the new value — that's what makes safe publication work. But `i++` is read-modify-write,
> three operations, so `volatile int i; i++` is still a race. For a counter I'd use
> `AtomicInteger`, or `LongAdder` if it's write-heavy and I only read it for metrics.

*Follow-up they'll ask:* "so when do you use `volatile`?" → **a stop flag, and publishing an
immutable snapshot** (`volatile Map` swapped wholesale for hot-reload).

---

### 2. "How do you size a thread pool?"

> Start from what the threads wait on. CPU-bound: cores + 1. I/O-bound: cores × target
> utilisation × (1 + wait/compute) — so 90 ms blocked and 10 ms computing on 8 cores gives
> about 80.
>
> But that's a starting point, not an answer. Three things matter more. First, **the
> binding constraint is usually downstream** — if the DB pool has 20 connections, 200
> workers just moves the queue somewhere I can't see it. Second, **one pool per dependency**,
> so a slow payment provider can't consume every thread and take down checkout. Third,
> **bounded queue with a real rejection policy**, because `newFixedThreadPool` has an
> unbounded queue and turns overload into memory growth and unbounded latency instead of
> backpressure. Then I'd load-test and find the knee.

*Bonus if you have room:* Little's law — `concurrency = throughput × latency`. And on JDK
21+, you stop sizing threads and start bounding the resource with a semaphore.

---

### 3. "Why are unbounded queues bad?"

> Because you never reject anything, you just accumulate. The heap grows, GC pressure rises,
> and queueing delay dominates latency — while every dashboard says the pool is healthy,
> because all threads are busy and there are zero rejections. Eventually you either OOM or
> you're burning CPU on requests whose clients timed out minutes ago.
>
> `Executors.newFixedThreadPool` uses an unbounded `LinkedBlockingQueue`, which also means
> `maximumPoolSize` is dead configuration — the pool never grows past core, because the
> queue always accepts. I'd construct `ThreadPoolExecutor` explicitly with an
> `ArrayBlockingQueue` and `AbortPolicy`, and export queue depth as a metric — it's the
> earliest overload signal you get.

---

### 4. "Explain generational GC."

> It's built on one observation: **most objects die young.** So you split the heap into a
> small young generation and a larger old one, and collect young frequently with a
> **copying** collector — trace from the roots, copy the few survivors out, then declare the
> whole region free. The cost is proportional to what **survives**, not to the garbage, so a
> young collection over 4 GB of dead objects is nearly free. Objects that survive enough
> collections get promoted to old, which is collected rarely by a different algorithm.
>
> The practical consequence is that **allocation is cheap and survival is expensive**. So
> when someone says "we allocate too much", the real question is what the *survival rate*
> and the *promotion rate* are — that's what drives GC cost.

*This reframe is the highest-value sentence in the whole GC topic. Lead with it.*

---

### 5. "Which garbage collector would you pick?"

> Start with the default, G1, and change it only with a measurement that says why. G1 is
> region-based with a pause target you can actually reason about, it degrades gracefully,
> and it needs almost no tuning.
>
> I'd move to **generational ZGC** if the p99 pause budget is single-digit milliseconds or
> the heap is over about 32 GB — ZGC uses coloured pointers and load barriers to relocate
> objects concurrently, so pauses are sub-millisecond and roughly independent of heap size.
> It costs you compressed oops, some throughput from the load barrier, and more footprint.
> I'd use **Parallel** for batch or Spark work where a two-second pause is irrelevant and
> I want the last 10% of throughput. Serial for tiny containers.
>
> And CMS isn't an option — it was removed in JDK 14.

---

### 6. "How would you debug a memory leak?"

> Four steps. **First, confirm it's a leak**: plot the **post-full-GC live set** over time.
> Monotonically rising means a leak; a sawtooth returning to the same floor is just
> allocation. That step alone kills most false alarms.
>
> **Second, characterise it** — per request, per connection, or per deploy? Per deploy
> usually means a classloader leak.
>
> **Third, get evidence cheapest-first**: `jcmd GC.class_histogram` is nearly free and often
> enough — "three million `LinkedHashMap$Entry`" points straight at a cache. Then JFR, which
> is about 1% overhead and gives you allocation stacks. Only then a heap dump, and from a
> depooled canary, because it pauses the JVM and writes a file the size of your live set.
>
> **Fourth, analyse** in MAT: dominator tree by **retained** size, then path to GC roots
> excluding weak and soft references.
>
> In practice it's almost always one of three things: an unbounded static map used as a
> cache, a `ThreadLocal` never removed on a pooled thread, or listeners registered and never
> unregistered.

---

### 7. "What kinds of `OutOfMemoryError` are there?"

> The message is the diagnosis, and **only two of them are fixed by raising `-Xmx`.**
>
> `Java heap space` and `GC overhead limit exceeded` are the heap — same root cause, same
> fix. `Metaspace` means too many **classes**, not too many objects — classloader leaks from
> proxies or repeated redeploys, and it's unbounded by default so you should always cap it.
> `Direct buffer memory` is off-heap — Netty and NIO — and you can hit it with a nearly
> empty heap, because direct buffers are freed via `Cleaner` only after a GC notices them.
> `unable to create native thread` means the OS refused, and counterintuitively a **bigger**
> heap makes it *more* likely, because stacks and heap compete. `Requested array size
> exceeds VM limit` is a VM limit, not a memory limit.
>
> And exit code 137 isn't a Java OOM at all — that's the kernel killing you on RSS, with no
> dump and no stack trace, which means the problem is non-heap memory.

---

### 8. "Why do we get long pauses even though `-Xmx` looks fine?"

> Several candidates, and I'd check them in order. **Time to safepoint** — that's the
> interval before all threads stop, it's not counted in GC pause time, and `-Xlog:safepoint`
> is the only way to see it. **Evacuation failure or to-space exhaustion** in G1, meaning
> there was no room to copy into, which triggers a full GC — and the fix there is usually
> *more* heap, not less. **Humongous allocations** if you're allocating multi-megabyte
> arrays. **Metaspace** pressure triggering full GCs.
>
> And then the non-GC ones that look identical in dashboards: **CFS throttling** in a
> container — a 500-millicore limit freezes every thread once you burn your 50 ms of a 100 ms
> period — and swapping, which shows up as `real` time far exceeding `user` in `-Xlog:gc+cpu`.

---

### 9. "What are virtual threads, and do they replace reactive?"

> A virtual thread is a `Thread` scheduled by the JVM instead of the OS, with its stack on
> the heap as a continuation. When it blocks on something the JDK instrumented, it unmounts
> from its carrier thread, so the carrier goes and runs something else. They cost about two
> kilobytes and a microsecond, so blocking code becomes scalable again — I measured 10,000
> tasks each blocking 200 ms finishing in 250 ms, versus 10 seconds on a 200-thread pool.
>
> They remove the main reason people adopted reactive: thread-per-request scalability. For a
> normal request/response service, virtual threads plus straight-line code beat WebFlux on
> readability, debuggability, and real stack traces at the same throughput.
>
> What reactive still gives you is **backpressure as a protocol** and **streaming
> operators** — windowing, debouncing, merging infinite streams. So: request/response →
> virtual threads; streaming with flow control → reactive or fs2/ZIO Streams.
>
> The caveat on JDK 21 specifically is **pinning**: blocking inside `synchronized` holds the
> carrier. I measured a 34× slowdown with completely uncontended locks. Use `ReentrantLock`,
> and run with `-Djdk.tracePinnedThreads`. JDK 24 fixed monitors.

---

### 10. "What's the biggest concurrency risk in this migration?"

> The `ExecutionContext`. The Java model was **forgiving** because every request owned a
> thread: blocking was normal, one slow call cost you one thread out of 200, and the failure
> was gradual and legible. In Scala, one blocking JDBC call on a cores-sized compute pool can
> **wedge the entire application at 5% CPU** — no compile error, no warning, and the symptom
> is just "everything is slow".
>
> So the discipline I'd want is: **two pools** — compute sized to cores that never blocks,
> and a bounded blocking pool sized to the downstream — with blocking quarantined and
> reviewed. Ban `ExecutionContext.Implicits.global` with a lint rule. No `Await` outside
> `main`. And an explicit bound on every queue and every `traverse`.
>
> The other two I'd worry about are `@Transactional` on a `Future`-returning method, which
> **silently doesn't transact** — the proxy commits before the work happens and never sees
> the failure, so there's no rollback — and `MDC`/`SecurityContext` disappearing across async
> boundaries, because they're `ThreadLocal`-backed.
>
> All three share a shape: **the Java version's implicit bound is gone and nothing replaced
> it.**

---

### 11. "What's the biggest *win* of moving to Scala?"

> Immutability by default. In Java, thread-safety is opt-in — every field on every shared
> Spring singleton is a potential race you have to reason about, and that's the bug that
> actually ships. In Scala, `val` and immutable collections are the default, case classes
> are safely publishable by construction, and shared mutation is something you have to
> deliberately reach for. That deletes a whole category of bug and changes the review
> question from "is this thread-safe?" to "why is this mutable?"
>
> If they've picked Cats Effect or ZIO, the second win is **real cancellation and resource
> safety** — Java genuinely has no correct answer for "release this resource if the async
> operation is cancelled". `CompletableFuture.cancel` doesn't interrupt the work, and I
> measured that: a 300 ms `orTimeout` returned a fallback while the task kept running for
> another 1.2 seconds, holding whatever it held.

---

### 12. "Why is Scala slower / why does it allocate more?"

> Same logic allocates more in Scala for specific, enumerable reasons: **boxing** —
> `List[Int]` is a cons cell plus a boxed `Integer` per element, about ten times the
> footprint of an `Array[Int]`, and scattered across the heap so traversal is
> pointer-chasing. **Tuples** — a `Tuple2` of two `Int`s is roughly 56 bytes to carry 8
> bytes. **Intermediate collections** — `map.filter.map` builds three of them. **Closures**,
> `Some`, and `copy` all allocate.
>
> But I'd be careful with that framing, because nearly all of it **dies in eden** and the
> copying collector never touches it. The cost shows up as GC *frequency*, not per-object
> overhead. So the process is: profile with async-profiler `-e alloc`, fix the top three to
> five sites, re-measure. In practice the wins are boringly repetitive — **eager string
> interpolation in log statements is usually number one**, and it's a one-line fix, because
> `s"..."` evaluates even when the level is off, unlike SLF4J's placeholders.
>
> What I wouldn't do is de-idiomatise the codebase on principle. You'd make it harder to
> change and probably not faster.

---

### 13. "Walk me through `for { a <- fa; b <- fb }` on futures."

> That depends entirely on where the futures were **constructed**, which is the trap. The
> comprehension desugars to nested `flatMap`s, so if the calls are inline —
> `for { a <- svcA(); b <- svcB() }` — then `svcB()` isn't even constructed until `a`
> arrives. And `Future` is **eager**, so construction is starting. Three 300 ms calls take
> 900 ms. I measured 920.
>
> To get parallelism you bind them to `val`s first, or use `Future.sequence`. The difference
> between 900 ms and 300 ms is **where you put a `val`** — no type signal, no warning, and a
> code review won't catch it.
>
> That's the clearest argument for laziness: in Cats Effect, `parTupled` makes parallelism
> explicit in the API and a `for`-comprehension is *always* sequential and always obviously
> so.

---

### 14. "How do you test concurrent code?"

> Mostly by **making it deterministic instead**. I inject the executor and the clock so the
> test can run everything on the calling thread — most so-called concurrency tests should be
> determinism tests.
>
> Where I do need real concurrency, I run N threads × M operations and assert an invariant
> at the end. That reliably catches lost updates. It catches ordering bugs only sometimes —
> in my own exercises a buggy `wait`/`notify` queue **passed** a 600-item stress test on a
> fast idle machine, and I had to force the interleaving to expose it. So a passing
> concurrency test proves very little, and for actual memory-model claims the right tool is
> **jcstress**.
>
> On the Scala side this gets much better: ZIO's `TestClock` and Cats Effect's `TestControl`
> give you **virtual time** — you `adjust(1.hour)` and it happens instantly and
> deterministically. Testing an exponential-backoff retry becomes a fast unit test instead
> of a 30-second sleep. That's one of the genuinely best arguments for those libraries.
>
> And always put a timeout on every test, so a deadlock fails instead of hanging CI.

---

### 15. "Anything you'd want to know / ask us?"

Have two or three real questions. These are good because the answers change your work:

> **Which Scala stack did you land on — bare `Future`, Pekko, Cats Effect, or ZIO?** That
> changes almost every concurrency answer. And if it's Akka, did you move to Pekko after the
> licence change?
>
> **What's driving the migration** — expressiveness and types, or specific operational pain
> like cancellation and resource leaks? Because if it's the latter, that's a direct argument
> for an effect type over `Future`, and it changes what "done" looks like.
>
> **How are you handling transactions and request context during the transition?** Those are
> the two things I'd expect to break silently in a port — `@Transactional` on an
> async-returning method, and `ThreadLocal`-backed MDC and security context. Have you hit
> that yet, or is there a pattern already agreed?
>
> **What's the target JDK?** If it's 21+, I'd want to know whether virtual threads are on the
> table, because that changes the "do we need to be async at all" conversation.

---

# Part 2 — the broader bank (Citi-style questions)

Same format. These are the non-concurrency ones most likely to come up.

---

### 16. "A query is running slow — what would you check?"

> First I'd establish whether it's actually the query. Slow *always* or slow *sometimes*?
> Intermittent means load, locking, or plan instability, not the SQL text. And time spent
> waiting for a **connection** isn't query time — I'd check the pool's pending count before
> blaming the database.
>
> Then I'd get the **execution plan** — `EXPLAIN (ANALYZE, BUFFERS)` — before changing
> anything. The most valuable thing in it is **estimated versus actual row counts**: if
> they diverge badly, the statistics are stale and the optimiser picked a plan for the wrong
> data shape, which is a completely different fix from adding an index. I'd also look for a
> sequential scan where I expected a seek, a nested loop over a large input, and sorts
> spilling to disk.
>
> Third, contention: is the whole database slow, or is this query **waiting on a lock**?
> `pg_stat_activity` and `pg_locks` tell you, and again that's a different fix.
>
> Only then the SQL itself: is there a usable index, and is the query written so it *can*
> use one — no function on the indexed column, no leading wildcard, no implicit cast. Am I
> pulling columns I don't need. Is there a correlated subquery running per row. Is it
> `NOT IN` where a NULL will silently return zero rows. And is the application doing **N+1**,
> which the slow-query log won't show you at all because each query is fast.

*Detail: quiz 11. The N+1 point is the one most likely to earn a follow-up.*

---

### 17. "`TreeMap` vs `HashMap` — implementation and when?"

> `HashMap` is a hash table: a contiguous array of buckets, index derived from a spread
> `hashCode`, collisions chained in a list, and since Java 8 a bucket with eight or more
> entries becomes a red-black tree so the worst case is O(log n) rather than O(n). It
> resizes and rehashes at a 0.75 load factor. **O(1) average**, iteration order unspecified.
>
> `TreeMap` is a red-black tree ordered by `Comparable` or a `Comparator`. **O(log n)
> guaranteed**, iteration always sorted, and it implements `NavigableMap` — `floorKey`,
> `ceilingKey`, `subMap`, `headMap`. **That's the actual reason to choose it**: sorted
> iteration and range queries.
>
> So I default to `HashMap` and reach for `TreeMap` when ordering or range access is a
> requirement. If I want *insertion* order rather than sorted order, that's `LinkedHashMap`,
> which is O(1) and also the basis of an LRU cache via `removeEldestEntry`.

**If they offer the "TreeMap is good for frequent add/remove" line, correct it politely:**
> That's true only in a specific sense. `TreeMap`'s add and remove are O(log n) where
> `HashMap`'s are O(1) — I measured about 8× slower on 200 000 entries. What's true is that
> if you need the data **kept in sorted order** while it's being modified, `TreeMap`
> maintains that incrementally, which beats re-sorting a list on every change. The advantage
> is the ordering, not the mutation speed.

Also worth being precise about: **`TreeMap` rejects a null *key*, but null values are
fine**, and `HashMap` allows one null key.

*Measured in `problems/java/P11Collections.java`; detail in quiz 09 Q2.*

---

### 18. "Explain the Builder pattern."

> It solves construction when you have many parameters, several optional. The alternative
> is a telescoping constructor — `new Order(id, customer, items, null, null, discount,
> null, true)` — where you can't tell what the nulls are and two adjacent `String`
> parameters can be swapped and still compile.
>
> A builder gives you named steps, validation in one place in `build()` so an invalid
> object can never exist, and — the part I care about most — **immutability**: all fields
> final, no setters, so the object is safely publishable and shareable across threads with
> no synchronisation.
>
> I'd use it at about four-plus parameters or when several are optional. Not for two or
> three. The trade-off is more code, which Lombok's `@Builder` handles, and that missing
> *required* fields become a runtime error instead of a compile error — a plain constructor
> catches that at compile time.
>
> And in modern Java a **`record`** with a compact constructor for validation covers a lot
> of what builders were for. In Scala you rarely need it at all: `case class` with named and
> default parameters plus `copy` gives you the same thing natively — **the pattern is
> largely a workaround for a language without named parameters.**

*Detail: quiz 09 Q1.*

---

### 19. "How would you organise a Spring endpoint that serves DB data enriched from another team's HTTP service?"

> Four components, one responsibility each. A `@RestController` that does nothing but
> translate HTTP to a domain call and map the result to a response DTO. A `@Service` with
> the orchestration and the transaction boundary, which knows nothing about HTTP. A
> `@Repository` or Spring Data interface for persistence. And a component for the other
> team's service — but I'd define **my own interface** for it and put the Feign client or
> `WebClient` behind that, so their DTOs and their failure modes stay at the boundary and I
> can swap the implementation or mock it in a unit test.
>
> Then the things that make it actually work in production. **Timeouts** on that HTTP
> client, connect and read — an HTTP client with no timeout is the most common cause of
> thread-pool exhaustion I've seen. A **circuit breaker**, and an explicit decision about
> whether enrichment failing fails the request or degrades to the un-enriched response —
> that's a product question. **Caching** the reference data if it's slow-changing.
> If the endpoint returns a list, **batch the enrichment** rather than calling per element,
> or you've built an N+1.
>
> And the one I'd flag in review: **the transaction must not span the HTTP call.** Holding a
> database connection while you wait on another team's latency is how you exhaust the
> connection pool.

*Detail: quiz 10 Q1.*

---

### 20. "Tell us about a system you worked on."

**This is the highest-value question in the interview and you should prepare it in writing.
Structure, ~4 minutes:**

1. **Purpose and scale** (20s) — business function, then numbers: rps, data volume,
   instances, p99. **Numbers are the credibility signal.**
2. **Upstream and downstream** (45s) — who you consumed, who consumed you, over what, and
   **who broke if you broke**.
3. **Components and stack** (45s) — grouped by responsibility, not a dependency list.
4. **Redundancy and failover** (45s) — replicas, health checks, replication, region
   strategy, graceful degradation. **And name what was *not* redundant** — every real system
   has a single point of failure, and saying yours is a strength, not a weakness.
5. **Protocols** (20s) — be specific, and say *why* for at least one.
6. **Architectural patterns** (30s) — name them, then give **one trade-off you actually
   lived with**: "it gave us X, the cost was Y, in hindsight I'd do Z."

Also unprompted: **your** specific contribution ("I owned…", not just "we…"), one incident,
and how it was observed.

*Full skeleton with all the probe areas: quiz 08 Q2.*

---

### 21. "What was your Definition of Done?"

> A checklist, then an example. Acceptance criteria met and demonstrated; code reviewed;
> unit and integration tests passing and the regression suite green; static analysis clean;
> docs and runbook updated; **observability added** — metrics and alerts for the new path,
> because "done" without monitoring isn't done; deployed to pre-prod and validated; UAT
> sign-off; rollback plan documented.
>
> Concretely, for [small specific story]: the acceptance criteria were [two or three
> testable conditions], written with the product owner before we started. Done meant [what
> testing], [who reviewed], [who signed off], and it was behind a feature flag until [X].
>
> On BDD — we wrote acceptance criteria as Given/When/Then in the ticket, and ordinary unit
> tests underneath. We didn't maintain an executable Cucumber suite. The value was really
> the **conversation**: getting business, QA and dev to agree on behaviour before code. The
> tooling is optional; the shared understanding is the deliverable.

**The closing line that lands:** *"The DoD only mattered because it was enforced — it was in
the PR template and the pipeline blocked on most of it. When it was just a wiki page, it got
skipped."*

*Full checklist: quiz 08 Q4.*

---

### 22. "How was work delivered, and what was your rollback plan?"

> **Walk one real change end to end** rather than describing the process abstractly.

Cover: intake and prioritisation (sprints or Kanban, how production issues were triaged);
branching and PR checks; **every testing tier you had** — unit, integration, QA, UAT,
pre-prod, automated regression — and honestly which ones you *didn't*; sign-offs and change
control (CAB, segregation of duties, release windows); release documentation; and deployment
mechanics (build once and promote the same artifact, rolling or blue-green, feature flags).

**On rollback, the thing that separates candidates:**
> Code rolls back trivially. **A migration that dropped a column cannot be undone.** So the
> rule is that every migration is backward-compatible — during a rolling deploy both
> versions are live against the same schema simultaneously — and destructive changes are
> deferred to a later release. To rename a column you do **expand/contract**: add the new
> one, dual-write, backfill in batches, cut over reads, stop writing the old, and only then
> drop it. Each step is independently revertible. The back-out plan for a genuinely
> destructive change is restore-from-backup, which means data loss — which is why you design
> so that rolling back the code alone is always sufficient.

*Detail: quiz 08 Q3 and quiz 11 Q14.*

---

### 23. "Why are you leaving?"

> **Pull, not push. 30–45 seconds. Forward-looking and specific.**

> "I've spent [N] years on [X] and I'm looking for [specific thing this role has].
> [One concrete reason that matters to me.] What drew me to this one is [the migration /
> the scale / the domain]."

**Don't:** criticise anyone or the codebase (it tells them how you'll describe *theirs*),
lead with money, or stack three reasons. **If pushed** — "was nothing keeping you?" — that's
a probe for badmouthing: give genuine credit, then return to the pull.

*Detail: quiz 08 Q1.*

---

# Part 3 — BofA GMT Post Trade (the JD topics)

---

### 24. "What is a monad?"

> A monad is a type with a `flatMap` and a way to lift a plain value in, obeying three laws.
> Practically: **it's a context you can sequence computations inside, where the context's
> plumbing is handled for you.**
>
> `Option` handles absence — `flatMap` short-circuits on `None`, so no null checks.
> `Either` handles failure with a reason — it short-circuits on `Left` and carries the error.
> `Try` handles thrown exceptions. `Future` handles asynchrony. `List` handles multiplicity.
> **The uniformity is the value**: one syntax — a `for`-comprehension — for absence, error,
> async, and multiplicity, so you write the happy path and the context propagates.
>
> The laws — left identity, right identity, associativity — are what make that safe to
> refactor: you can re-nest and factor out steps without changing behaviour.

**Two things to add that show you've used them rather than read about them:**
> `Future` is arguably not a *lawful* monad, because it's eager and side-effecting — you can't
> substitute `pure(a).flatMap(f)` for `f(a)` once effects are involved, and it memoises. It
> composes monadically, which is why the syntax works, but referential transparency is broken.
> `IO` and `ZIO` are lawful.
>
> And monads don't mix in one comprehension — you can't `flatMap` an `Option` into a `Future`.
> That's what monad transformers like `EitherT` are for, or you convert first.

*Don't lead with category theory. Detail: quiz 12 Q6.*

---

### 25. "Explain event sourcing."

> Instead of storing current state and mutating it in place, you store the **immutable,
> append-only sequence of events** that produced it, and derive current state by replaying
> them. An `UPDATE` destroys history; an append doesn't.
>
> For a bank the headline reason is that **the audit trail becomes the storage model rather
> than a bolt-on**. You can answer "what did we believe at 16:00 yesterday and why" — which
> regulators do ask — and it can't drift from reality the way a separate audit table can. You
> also get temporal queries for reconciliation, the ability to replay production events into a
> test environment to reproduce a bug exactly, and new projections built retroactively over
> all of history.
>
> The costs are real, though: **eventual consistency everywhere**, schema evolution as a
> permanent commitment because you'll be reading five-year-old events, you can't fix data with
> an `UPDATE` — you append a compensating event — and it's a steep learning curve, which is
> the most common reason these projects fail.
>
> So it's not a default architecture. It earns its place where auditability and temporal
> correctness are requirements — **and post-trade is one of the clearest legitimate cases.**

*Detail: quiz 13 Q1 and Q14.*

---

### 26. "What's CQRS, and how do you handle eventual consistency?"

> CQRS separates the write model from the read model — different models, often different
> storage, scaled independently. It pairs naturally with event sourcing because an event log is
> excellent for consistency and auditing and **terrible for querying**: "all unsettled trades
> for this account sorted by value date" can't be answered by replaying streams. So you build
> projections — and you can have many, each shaped for its query.
>
> Worth saying: **CQRS doesn't require event sourcing and vice versa.** CQRS over a relational
> write model with a denormalised read replica is a normal, much cheaper pattern.
>
> On eventual consistency, the options in order: return the new state from the command
> directly, so the client needn't re-query; a **version token** the client passes on the next
> read for read-your-own-writes; optimistic UI; or just design the UX for it — most business
> processes are already asynchronous. For the few views that must be immediate, update one
> projection in the same transaction, or route that read to the write side.
>
> Operationally the non-negotiable is **monitoring projection lag and alerting on it** — if lag
> grows, your read models are silently wrong.

**The sentence that lands:**
> "Eventual consistency isn't a bug you fix, it's a property you design for. The real question
> is what the consistency window is and whether the business process tolerates it — for
> settlement, where the real-world process is T+1 or T+2, a 200 ms projection lag is
> irrelevant. For a pre-trade risk check it isn't, and that check belongs inside an aggregate
> on the write side."

*Detail: quiz 13 Q3, Q4.*

---

### 27. "How do you evolve event schemas?"

> You have to treat the event schema as a **public API from day one**, because it is one — it
> has consumers you don't control, including your own future self replaying five-year-old data.
> And events are immutable, so you can never rewrite them.
>
> So: **additive changes only.** Adding an optional field with a default is safe. Renaming or
> changing a type isn't — you treat it as add-new plus deprecate-old, the same expand/contract
> discipline as a database migration. And you never change the *meaning* of an existing field,
> because nothing fails — the numbers are just quietly wrong.
>
> Mechanically: a **schema registry with compatibility enforced in CI** so an incompatible
> change fails the build, Avro or Protobuf rather than bare JSON, and **upcasting** — a chain
> that transforms old versions into the current shape on read, so the domain model only ever
> sees the latest version and all the historical mess is quarantined in one place.
>
> The test that actually saves you is a **corpus of real historical serialised events kept as
> fixtures**, asserting they still deserialise. That's the regression test for schema
> evolution, and it's the one people forget.

*Detail: quiz 13 Q5.*

---

### 28. "Walk me through a feature file for a settlement scenario."

**Structure it, and narrate your choices while writing.** Full worked example in quiz 14 Q3.

```gherkin
Feature: Trade settlement
  As an operations user
  I want trades to settle on their value date once confirmed and funded
  So that positions and cash are accurate and we meet our settlement obligations

  Scenario: A confirmed and funded trade settles on its value date
    Given a trade "TRD-123" for 1,000 units of "GB00B16KPT44" at 99.50 GBP
    And the trade is confirmed
    And the trade has a value date of 2026-08-20
    And account "ACC-001" has available cash of 100,000.00 GBP
    When the business date becomes 2026-08-20
    And the settlement cycle runs
    Then the trade "TRD-123" is settled
    And the position for "ACC-001" in "GB00B16KPT44" increases by 1,000
    And a "TradeSettled" event is published for "TRD-123"

  Scenario: Settlement fails when the account is underfunded
    ...
    Then the trade is marked as failed with reason "INSUFFICIENT_FUNDS"
    And a settlement failure notification is raised to operations
```

**Say these things while writing:**
> Business language only — `When the settlement cycle runs`, not `When I POST to /settlements`,
> so the scenario survives a re-implementation. Concrete values, not "some units", because
> ambiguity hides in abstraction — that's the point of Specification by Example. One behaviour
> per scenario. And I'd always include the unhappy paths — underfunded, unconfirmed — because
> that's where the real requirements and the disagreements live.

**Then make the architectural connection, which is the strongest thing you can say here:**
> The step definitions should call the domain, not the UI. And in an event-sourced system
> that's almost free, because the write side is already
> `decide(state, command) -> Either[error, events]` — so **`Given` is a list of past events,
> `When` is one command, and `Then` asserts on the events returned.** Gherkin's
> Given/When/Then maps exactly onto the architecture's own shape, with no mocks and no
> database, running in milliseconds. That's presumably why this JD asks for event sourcing and
> BDD in the same breath.

*Detail: quiz 14 Q3, Q4.*

---

### 29. "Walk me through the trade lifecycle."

> Pre-trade is research, pricing and risk; execution is the order matching on a venue, usually
> over FIX. **Post-trade is everything after that**: capture and enrichment, then
> **confirmation** where both counterparties agree the economics, then **clearing** where a CCP
> novates and becomes counterparty to both sides — which mutualises counterparty risk and,
> operationally more importantly, **nets** obligations down, often by an order of magnitude.
> Then **settlement** on the value date: securities against cash, delivery-versus-payment so
> neither leg happens without the other, through CSDs and custodians. Then ongoing **asset
> servicing** — coupons, dividends, corporate actions, tax. And reconciliation and regulatory
> reporting throughout.
>
> The thing I'd emphasise is that it's **not a low-latency problem, it's a correctness,
> auditability and deadline problem** — a settlement fail has real cost, including mandatory
> cash penalties under CSDR. And most of the complexity lives in the exceptions: fails,
> partials, amendments, claims and breaks.

**Then connect it, which is the point:**
> That's also why event sourcing fits here rather than being architectural fashion. It's a
> genuine long-running state machine, the audit trail is a regulatory requirement, and
> reconciliation and corporate-action entitlements are inherently *temporal* — "what was the
> position as of this record date" is a replay, and a current-state-only model can tell you
> that you disagree with the custodian but not why.

*Detail: quiz 15 Q1, Q8.*

---

### 30. "What's T+1, and why does it matter?"

> T+n is the settlement cycle in business days. The US, Canada and Mexico moved to **T+1 in
> May 2024**; the EU and UK are **targeted for 2027**. It's the dominant post-trade technology
> programme in the industry right now.

⚠️ **Verify the dates before you say them.** A fact-check pass could not corroborate the T+1
dates or the EU/UK target month. Saying **"targeted for 2027"** is safer than naming October, and
if pushed, *"I'd want to check the exact date"* is a perfectly good answer that a domain expert
will respect more than a confident wrong month.
>
> It matters because compressing the cycle **removes the slack that manual exception handling
> relied on**. Affirmation and confirmation have to be same-day, funding and FX get squeezed,
> and the overnight batch window largely disappears. So the engineering consequence is a push
> from batch-oriented processing toward **real-time, event-driven architectures with automated
> exception management** — predicting and fixing fails *before* the value date rather than
> chasing them after.
>
> Which is, I assume, a large part of why this role is framed around strategic event-driven
> systems.

**This doubles as the answer to "what trends are facing this business?"** — it's specific,
current, verifiable, and it connects the industry to the architecture in one move. Have one
broader one ready too (rates and net interest income, Basel III endgame, or operational
resilience/DORA), and be honest that you follow it from the engineering side.

*Detail: quiz 15 Q2, Q14; quiz 08 Q9.*

---

### 31. "Have you used AMPS?"

**If you haven't, say so — and then be useful.**

> I haven't used AMPS directly. Conceptually it maps onto the same problem I've solved with
> Kafka: distributing state changes to many consumers with different interests. The
> distinctive parts as I understand them are **server-side content filtering**, so subscribers
> register a filter and the broker evaluates it rather than the client, and the **State of the
> World cache** — a last-value-per-key store you can query with SQL-92-style syntax, so a new
> subscriber gets current state plus a live stream in one operation.
>
> Where I'd still want Kafka is **durable, replayable history**, which is what an event store
> needs. Where AMPS looks stronger is low-latency fan-out and last-value queries. So in an
> event-sourced design I'd expect them to be complementary rather than competing — a durable
> store as the system of record, AMPS as the distribution and current-state query layer.
> **The SOW is essentially a materialised projection maintained by the messaging layer**, so it
> fits the CQRS read-side role directly.

Then ask the question, which is a good one: *"Are you using AMPS as distribution over a durable
store, or is the SOW acting as the read model?"*

*Detail: quiz 15 Q9.*

---

### 32. "How does ATDD differ from TDD?"

> Different scope, audience, and point in time. **TDD** is a developer practice at the unit
> level, during implementation, answering *"am I building it right?"*. **ATDD** is a whole-team
> practice at the feature level, **before** implementation, answering *"are we building the
> right thing?"* — the acceptance criteria are agreed with business and QA as concrete examples
> and automated, and they become the definition of done.
>
> They nest: the **outer loop** is a failing acceptance test that tells you when the feature is
> finished; the **inner loop** is red-green-refactor on units, many times, until it passes.
> That's the double-loop model.
>
> And I'd say the point of all of it is **the conversation, not the tooling.** Getting business,
> QA and developers to agree on concrete examples before code is cheaper than discovering the
> misunderstanding in UAT. Gherkin is optional; the shared understanding is the deliverable.

**Be ready for "how rigorously do you do TDD?"** — the honest, defensible answer:
> Tests first for logic — domain rules, state machines, calculations, error handling. For
> exploratory work I sometimes spike and then write tests before committing. What I hold to is
> that nothing merges without tests, and **a bug fix always starts with a failing test that
> reproduces it** — otherwise you don't know you fixed it.

*Detail: quiz 14 Q1, Q2, Q5.*

---

## Delivery notes

- **Lead with the claim, then the mechanism.** Interviewers are checking whether you have a
  model, not whether you can recite.
- **Volunteer one number.** "I measured 34× for pinning with uncontended locks" is worth ten
  sentences of theory, and you have real numbers from these labs.
- **Say what you'd *do*.** Every answer above ends in an action. That's the difference
  between knowing the topic and being useful on the team.
- **Name the limits of your answer.** "`GC overhead limit exceeded` is genuinely hard to
  reproduce — I tried" reads as credible. Overclaiming reads as unreliable.
- **When you don't know:** say so, then reason out loud from what you do know. That's
  usually what's being tested anyway.
