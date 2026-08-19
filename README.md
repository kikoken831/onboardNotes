# Interview Prep — BofA GMT Post Trade (Java/Spring Boot → Scala)

Study pack for a **Bank of America Global Markets Technology, Post Trade** Software Engineer
role: a Java Spring Boot → Scala migration on event-sourced "strategic systems".

**JD-driven topic coverage:** multi-threading · garbage collection · Scala & functional
programming · event sourcing & CQRS · ATDD/BDD/TDD · securities post-trade domain · DSA
screen · behavioural.

Local toolchain: **JDK 21** (virtual threads GA), **Scala 2.13.15**. Every code file here
compiles and runs — the measured numbers quoted throughout came from running them.

**Process** — ⚠️ **anecdotal, Glassdoor-sourced, and a verification pass found no corroboration.
Ask your recruiter for the real sequence.** Reported shape: recruiter screen → online
assessment / **HireVue** (2 coding problems + recorded behavioural, *including narrating time
complexity on camera*) → 1–2 technical rounds (DSA/SQL, then OOP/system design/project deep
dive) → hiring-manager/behavioural final on teamwork, conflict, and "Responsible Growth".

---

## How to use this

Three passes, roughly 3 sessions:

| Pass | Goal | Files |
|---|---|---|
| 0. Screen | Where are my gaps? (fast, 290 MCQs) | `mcq-quiz/`, or `tools/mcq_track.py drill` |
| 1. Recall | Can you answer out loud, no notes? | `quiz/01`–`07`, `09`–`19` |
| 2. Hands-on | Can you find/fix the bug in code? | `problems/` |
| 3. Empirics | Can you read a GC log and tune a heap? | `problems/gc-labs/` |
| 4. **Write** | Your own examples, in your own words | **`quiz/08`** — do this one with a keyboard, not by reading |
| 5. **Rehearse** | Say it out loud, on camera | `cheatsheets/answer-scripts.md` + `problems/java/P12DsaPractice.java` |

Do the quiz **before** reading answers. Every answer is inside a `<details>` block —
in most markdown previewers it's collapsed; in a plain editor, cover the screen.

---

## Contents

### Quizzes (recall + discussion prompts)

**Core prep — the two topics you were told to study:**
- [`quiz/01-threading-fundamentals.md`](quiz/01-threading-fundamentals.md) — Java Memory Model, `volatile`, `synchronized`, atomics, safe publication, deadlock
- [`quiz/02-executors-and-pools.md`](quiz/02-executors-and-pools.md) — `ThreadPoolExecutor` sizing, queues, rejection, `ForkJoinPool`, `CompletableFuture`
- [`quiz/03-virtual-threads-jdk21.md`](quiz/03-virtual-threads-jdk21.md) — virtual threads, pinning, structured concurrency, what it means for Spring
- [`quiz/04-gc-fundamentals.md`](quiz/04-gc-fundamentals.md) — generations, TLAB, barriers, safepoints, reference types, OOM taxonomy. **Q23 is the consolidated "how does HotSpot GC work" walkthrough.**
- [`quiz/05-gc-collectors-and-tuning.md`](quiz/05-gc-collectors-and-tuning.md) — G1 vs ZGC vs Parallel, flags, containers, reading GC logs

**The migration:**
- [`quiz/06-scala-concurrency.md`](quiz/06-scala-concurrency.md) — `Future`/`ExecutionContext`, Akka/Pekko, Cats Effect, ZIO; Scala-specific GC pressure
- [`quiz/07-spring-to-scala-migration.md`](quiz/07-spring-to-scala-migration.md) — thread-per-request vs async, `@Transactional`, MDC, what breaks in the port

**Broader interview coverage (Citi question bank):**
- [`quiz/08-behavioral-and-delivery.md`](quiz/08-behavioral-and-delivery.md) — screening questions, describing a system, SDLC/testing tiers, sign-offs, rollback, Definition of Done. **Needs your content — write these out.**
- [`quiz/09-collections-and-patterns.md`](quiz/09-collections-and-patterns.md) — Builder pattern, `TreeMap` vs `HashMap`, `Set` vs `List`, `hashCode`/`equals`, exceptions, records
- [`quiz/10-spring-and-layering.md`](quiz/10-spring-and-layering.md) — the "endpoint enriched from another team's service" design question, annotations, `@Transactional`, AOP, testing
- [`quiz/11-sql-performance.md`](quiz/11-sql-performance.md) — slow-query triage, indexes, sargability, `NOT IN`, N+1, connection pools, schema rollback

**JD-specific for BofA GMT Post Trade:**
- [`quiz/12-scala-language-and-fp.md`](quiz/12-scala-language-and-fp.md) — `val`/`lazy val`, case classes, pattern matching, **monads**, `Future` vs `Promise`, implicits, type classes, tail recursion, variance, tagless final, SOLID. **Q20 sketches a trade domain model — the synthesis question.**
- [`quiz/13-event-sourcing-and-cqrs.md`](quiz/13-event-sourcing-and-cqrs.md) — **core to the JD.** Event log vs state, commands/events/aggregates, eventual consistency, schema evolution, snapshots, sagas, outbox, idempotency, when *not* to use it
- [`quiz/14-atdd-bdd-tdd.md`](quiz/14-atdd-bdd-tdd.md) — TDD vs ATDD vs BDD, **a worked settlement feature file**, what goes wrong with BDD, testing time-dependent logic, CI pipeline
- [`quiz/15-post-trade-domain.md`](quiz/15-post-trade-domain.md) — trade lifecycle, T+1, clearing/CCPs, DvP and fails, netting, corporate actions, reconciliation, AMPS vs Kafka, regulations. **Domain primer — verify specifics with the team**

**Core Java breadth (from the LeetCode "100 important questions" list — 85 questions):**
- [`quiz/16-SOURCE-COVERAGE-MAP.md`](quiz/16-SOURCE-COVERAGE-MAP.md) — **read this first if you're working from that list.** Maps all 130 source questions to where they're answered, flags the ~12 duplicates, and lists the 12 topics the list omits that are asked in 2026 anyway.
- [`quiz/16-java-core-and-oop.md`](quiz/16-java-core-and-oop.md) — `==`/`equals` + `Integer` cache, access modifiers, polymorphism vs hiding, immutability, `transient`/`serialVersionUID`, `clone`, `finalize` → `Cleaner`, classloading, `Proxy`, `MethodHandle`, **generics erasure and PECS**
- [`quiz/17-java8-functional-and-streams.md`](quiz/17-java8-functional-and-streams.md) — lambdas vs anonymous classes, the functional interfaces, `map`/`flatMap`, **`peek` may not run**, laziness, `Optional` done properly, `parallelStream`, collectors, `Comparator`
- [`quiz/18-concurrency-api-inventory.md`](quiz/18-concurrency-api-inventory.md) — the `java.util.concurrent` catalogue: `CompletableFuture` in depth, synchronisers, locks, concurrent collections, `BlockingQueue` family, `Executors` factories, `ThreadLocal`. **Companion to `quiz/01`–`03`, which cover the reasoning rather than the API surface.**
- [`quiz/19-design-patterns.md`](quiz/19-design-patterns.md) — the GoF set that actually comes up, which ones dissolve in a language with lambdas, Spring's pattern map, and "name a pattern you'd argue against"

### Standalone set — the LeetCode list, 1:1
- [`leetcode-set/`](leetcode-set/) — the **129 questions from the LeetCode "100 important questions" post, in the post's own order, one answer each, duplicates intact** (~12 of them, answered from a different angle each time). Short answers — what it is, the idiom, one gotcha — each pointing into the deep treatment. Its own MCQ deck (`leetcode-set/mcq/`, 129 questions) with a **separate progress file**, so this set's coverage is tracked independently:
  ```bash
  python3 tools/mcq_track.py --root leetcode-set/mcq drill -n 15
  ```
  Use this when you want to work *through the list*; use `quiz/16`–`19` when you want to learn the topics. Same material, different shape.

### Tools
- [`tools/`](tools/) — `mcq_shuffle.py` regenerates randomised practice sheets (question order *and* A/B/C/D positions) so the MCQs stay reusable; `mcq_track.py` is an auto-graded drill with coverage tracking and Leitner-style review scheduling. Standard library only. See [`tools/README.md`](tools/README.md).

### MCQ drill (rapid recall)
- [`mcq-quiz/`](mcq-quiz/) — **290 multiple-choice questions** across 18 files, mirroring `quiz/01`–`19`. Distractors are built from the real misconceptions, so the wrong answers teach the traps. Use to find gaps fast, then read the full `quiz/` entry for anything missed. **`quiz/08` and the open design questions have no MCQ version on purpose** — see the folder README.

### Code problems
- [`problems/java/`](problems/java/) — 12 runnable problems (`java P01Counter.java`). **P12 is the DSA screen practice set with a test harness and the on-camera complexity-narration script.**
- [`problems/scala/`](problems/scala/) — 5 Scala problems (`scala S01BlockingGlobalEC.scala`)
- [`problems/gc-labs/`](problems/gc-labs/) — 4 GC experiments with real flags and logs
- Solutions live in `SOLUTIONS.md` next to each set.

### Cheatsheets (skim the morning of)
- [`cheatsheets/jmm-happens-before.md`](cheatsheets/jmm-happens-before.md)
- [`cheatsheets/gc-flags-and-logs.md`](cheatsheets/gc-flags-and-logs.md)
- [`cheatsheets/answer-scripts.md`](cheatsheets/answer-scripts.md) — 60-second spoken answers to the 15 most likely questions

---

## Quick start

```bash
cd /Users/kendrickkee/onboardNotes

# Java problems run as single files, no build tool needed
java problems/java/P01Counter.java

# Some need JVM flags — the header comment of each file tells you
java -Xmx64m -Xlog:gc problems/gc-labs/L01AllocationRate.java

# Scala
scala problems/scala/S01BlockingGlobalEC.scala
```

---

## The 8 things to be able to say cold

If you only retain eight things, make it these. Details and framing in
[`cheatsheets/answer-scripts.md`](cheatsheets/answer-scripts.md).

1. **`volatile` gives visibility + ordering, not atomicity.** `volatile int i; i++` still races.
2. **Happens-before is the whole JMM.** Unlock→lock, volatile write→read, `Thread.start()`, `Thread.join()`, final-field freeze.
3. **Unbounded queues turn a thread pool into a latency bomb.** `Executors.newFixedThreadPool` uses an unbounded `LinkedBlockingQueue`; load sheds as memory growth and p99, never as backpressure.
4. **Virtual threads make blocking cheap again, but only if nothing pins.** On JDK 21 `synchronized` around blocking I/O pins the carrier; `ReentrantLock` doesn't.
5. **Allocation is cheap; *surviving* is expensive.** GC cost tracks the live set and promotion rate, not the allocation count.
6. **Pick the collector from the SLO, not from vibes.** Throughput/batch → Parallel. General service, heap ≤ ~32 GB → G1. Tail-latency-sensitive or large heap → Generational ZGC.
7. **Scala's default `ExecutionContext` is a `ForkJoinPool` sized to your CPUs.** One blocking JDBC call inside it and you starve the whole app.
8. **Scala allocates more than Java for the same logic** — boxing, tuples, closures, `Option`, intermediate collections. That's a young-gen pressure story, not usually a leak story.

---

## Suggested order for this interview

1. **`quiz/13` (event sourcing/CQRS)** and **`quiz/12` (Scala/FP)** — the two JD-critical files.
2. **`quiz/01`, `02`, `04`** — the multi-threading and GC you were told to prep.
3. **`problems/java/P12`** — implement the DSA set, and record yourself narrating one.
4. **`quiz/15`** — the domain vocabulary, and Q8 (mapping ES/CQRS onto post-trade).
5. **`quiz/14`** — write the settlement feature file (Q3) by hand.
6. **`quiz/08`** — write out your system, delivery process, DoD, and the four stories.
7. **`cheatsheets/answer-scripts.md`** — the morning of.

---

## Open question worth asking them

The Scala stack isn't stated in the JD, and it changes almost every concurrency answer:

- **`scala.concurrent.Future` + Spring/Play** — closest to what you know; `ExecutionContext` discipline is the whole game.
- **Akka / Pekko** — actors, mailboxes, supervision; ask *Pekko* specifically, since Akka's licence change in 2022 pushed most teams to the Apache fork.
- **Cats Effect 3** — `IO`, fibers, `Resource`; typically with http4s + doobie.
- **ZIO 2** — `ZIO[R,E,A]`, fibers, built-in structured concurrency.

Cats Effect and ZIO replace the JDK's whole concurrency vocabulary with their own
runtime. If the team has picked one, that's where the interview weight will sit —
`quiz/06` covers all four so you're not caught flat, and `quiz/12` covers the language
regardless.

More questions to ask, per topic, in [`quiz/15` Q13](quiz/15-post-trade-domain.md).
