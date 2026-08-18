# MCQ 06 — Scala Concurrency

18 questions. Source: [`quiz/06`](../quiz/06-scala-concurrency.md).
Covers `Future`/`ExecutionContext`, Pekko, Cats Effect/ZIO, and Scala GC pressure.

---

### Q1. What is `ExecutionContext.global`?

- **A.** An unbounded cached thread pool
- **B.** A single-threaded event loop
- **C.** A `ForkJoinPool` with parallelism equal to `availableProcessors()`
- **D.** A pool sized to the number of in-flight futures

<details><summary>Answer</summary>

**C.** A **compute pool**, sized for CPU-bound work. That's the whole problem: there are only ~N
threads, so N concurrent blocking JDBC calls consume the entire application's capacity to make
progress on *anything* — including the callbacks that would complete those very futures.

`import scala.concurrent.ExecutionContext.Implicits.global` is the single most common source of
production incidents in Scala `Future` codebases. It belongs in tests and the REPL.

→ `quiz/06` A1
</details>

---

### Q2. What does `scala.concurrent.blocking { }` do, and where does it *not* work?

- **A.** Wraps the body in a `ForkJoinPool.ManagedBlocker` so the pool spawns compensation threads — a no-op on a fixed thread pool
- **B.** Moves the body to a dedicated blocking pool; works on any `ExecutionContext`
- **C.** Suspends the fiber without blocking a thread
- **D.** Marks the code so the compiler warns about blocking calls

<details><summary>Answer</summary>

**A.** Measured: 64 blocking calls on the global EC took **1038 ms** without the hint and **218 ms**
with it — because the pool grew.

But note what it did: **it grew the pool** (up to `maxThreads`, default 256). You converted thread
starvation into thread growth. It's a mitigation, not a design.

And the critical limitation: on a **`ThreadPoolExecutor` it's a no-op**, because there's no
compensation mechanism — `ManagedBlocker` is a `ForkJoinPool` feature. This is the most
misunderstood thing about `blocking`.

→ `quiz/06` A2, measured in `problems/scala/S01BlockingGlobalEC.scala` Parts 1–2
</details>

---

### Q3. `for { a <- callA(); b <- callB(); c <- callC() } yield a+b+c`, each call taking 300 ms. Total?

- **A.** ~300 ms — they run in parallel
- **B.** Non-deterministic between 300 and 900 ms
- **C.** ~300 ms only if the EC has 3+ threads
- **D.** ~900 ms — they run sequentially

<details><summary>Answer</summary>

**D.** Measured: **920 ms**. The comprehension desugars to nested `flatMap`s, so `callB()` isn't
even **constructed** until `a` arrives — and because `Future` is **eager**, construction *is*
starting.

Fix: bind them to `val`s *first*, then compose — measured **311 ms**.

**The only difference between 920 ms and 311 ms is where you put a `val`.** No type signal, no
compiler warning, and a code review won't catch it. That's exactly the class of bug laziness fixes:
Cats Effect's `parTupled` makes parallelism explicit in the API.

→ `quiz/06` A4, measured in `problems/scala/S02FutureTraps.scala` Part 1
</details>

---

### Q4. `Future.traverse(items)(fetch)` over 5000 items against a service tolerating 20 concurrent calls. What happens?

- **A.** It processes 20 at a time automatically
- **B.** It launches all 5000 concurrently — a self-inflicted denial of service
- **C.** It fails fast with a rejection
- **D.** It batches according to the EC's thread count

<details><summary>Answer</summary>

**B.** Measured with genuinely async calls: **peak 500 of 500 in flight.**

- **D** is the subtle trap: with *blocking* calls the EC's thread count caps concurrency *by
  accident*, so you'd wrongly conclude `traverse` is safe. A real non-blocking HTTP client removes
  that accidental protection.

There is **no built-in throttled variant** for `scala.concurrent.Future` — a real API gap. You
batch with `grouped(n)` + `foldLeft`, or use `parTraverseN(20)` (Cats Effect),
`ZIO.foreachPar(...).withParallelism(20)`, or `mapAsync(20)` (Pekko Streams).

→ `quiz/06` A5, measured in `S02FutureTraps.scala` Part 3
</details>

---

### Q5. Can you retry a `scala.concurrent.Future`?

- **A.** No — it's eager, so it holds a completed result; retry needs a `() => Future[A]` thunk
- **B.** Yes — `f.recoverWith { case _ => f }`
- **C.** Yes, but only within 30 seconds of failure
- **D.** Yes — `Future.retry(n)` is in the standard library

<details><summary>Answer</summary>

**A.** Measured: retrying the *value* made **1 attempt** (re-observing the same failure forever);
retrying a **thunk** made 3 and succeeded.

- **B** is exactly the futile pattern — `recoverWith` on the same value just re-reads it.
- **D** doesn't exist.

Same root cause as the referential-transparency break: `val f = Future(effect); (f, f)` is one
effect, but `(Future(effect), Future(effect))` is two — so you can't substitute a value for its
definition. `IO`/`ZIO` are *descriptions*, so `io.retry(policy)` is well-defined and built in.

→ `quiz/06` A3, measured in `S02FutureTraps.scala` Part 4
</details>

---

### Q6. `Future.sequence` fails fast on the first failure. What happens to the other in-flight futures?

- **A.** They're cancelled and their threads interrupted
- **B.** They're cancelled at the next `flatMap` boundary
- **C.** They run to completion, holding whatever resources they hold
- **D.** They complete but their results are queued for later

<details><summary>Answer</summary>

**C.** Measured: `sequence` returned the failure at ~50 ms; the three 1-second tasks **all ran to
completion** afterwards, holding connections for a result nobody will read.

`scala.concurrent.Future` has **no cancellation API at all** — and that isn't fixable by a wrapper.
It's the single biggest limitation of the type.

To collect all outcomes instead: `Future.sequence(fs.map(_.transform(Success(_))))` gives you
`Future[List[Try[T]]]`.

→ `quiz/06` A5, measured in `S02FutureTraps.scala` Part 5
</details>

---

### Q7. Is `class Counter { private var count = 0; def increment() = count += 1 }` thread-safe in Scala?

- **A.** Yes — Scala's `var` is synchronised
- **B.** No — it compiles to a plain mutable JVM field; `count += 1` is a non-atomic read-modify-write
- **C.** Yes, if the class is a `case class`
- **D.** Only on a single-core machine

<details><summary>Answer</summary>

**B.** Identical to Java's `count++`. Scala's syntax is prettier; the JMM is unchanged. Measured:
987 of 1000 increments survived, and a `ListBuffer` lost 5 of 1000 appends.

The idiomatic fix isn't a lock — it's not having shared mutable state: return a new value, fold the
results, or use `Ref[IO, Int]` / ZIO `Ref` if it genuinely must be shared.

And note the related trap: **`val` does not mean immutable.** `val buf = mutable.ListBuffer()` is a
`val` holding a mutable, unsynchronised structure.

→ `quiz/06` A6, measured in `S02FutureTraps.scala` Part 2
</details>

---

### Q8. Where is `Await.result` acceptable?

- **A.** Anywhere, as long as you pass a timeout
- **B.** Never, under any circumstances
- **C.** Inside `Future` callbacks, to simplify composition
- **D.** In `main`, in tests, and at a synchronous framework boundary you can't change

<details><summary>Answer</summary>

**D.** Never on a pool thread: a thread holding a slot while waiting for work that needs a slot is
how you **deadlock** a bounded EC. Verified — 2 threads, 2 outer tasks each awaiting an inner task
that needs a thread: permanent deadlock, presenting as "the service hangs at 5% CPU" with no error.

And `blocking` can't save you there, because a fixed pool can't create compensation threads.

The rule: **be async all the way to the edge.** Return `Future[Result]` up to the framework and let
*it* complete the response.

→ `quiz/06` A7, measured in `S01BlockingGlobalEC.scala` Part 4
</details>

---

### Q9. An exception thrown *outside* the `Future { }` block but inside a `Future`-returning method — what happens?

- **A.** It's captured into a failed `Future`
- **B.** It's sent to the EC's `reportFailure`
- **C.** It propagates synchronously to the caller, so `.recover` never sees it
- **D.** It's silently swallowed

<details><summary>Answer</summary>

**C.**
```scala
def f(): Future[Int] = { require(x > 0); Future { compute() } }   // require throws SYNCHRONOUSLY
```
Callers doing `f().recover { … }` won't catch it. Push all effects inside the block, or use
`Future.fromTry` / `Future.failed`.

Two related gotchas: **fatal errors** (`OutOfMemoryError`, `InterruptedException`, anything not
`NonFatal`) are **not** captured into the `Future`, so `recover` never sees them; and a
fire-and-forget future whose failure nobody inspects logs nothing useful — same trap as
`CompletableFuture`.

→ `quiz/06` A8
</details>

---

### Q10. Which actor guarantee lets you use a plain `var` for actor state?

- **A.** An actor processes one message at a time, so it is a unit of thread confinement
- **B.** Akka synchronises all field access automatically
- **C.** Actor state is stored in a `ThreadLocal`
- **D.** Actors run on a single global thread

<details><summary>Answer</summary>

**A.** Serial mailbox processing means actor-internal state needs no synchronisation. That's the
central win of the model.

- **D** is wrong — actors are multiplexed across a dispatcher's threads; each *actor* is serial, the
  *system* is not.

Costs worth naming: no compile-time protocol checking in classic untyped Akka (`receive` is
`PartialFunction[Any, Unit]`, so a typo'd message is a silent `DeadLetter` — **Typed** fixes this);
no call stack across a message send, so debugging needs correlation ids; and **mailboxes are
unbounded by default**, making a slow actor a memory leak under load.

→ `quiz/06` B1
</details>

---

### Q11. What's wrong with calling `sender() ! Done` inside a `Future` callback in an actor?

- **A.** Nothing — `sender()` is captured when `receive` is entered
- **B.** Replies must always use `context.parent`
- **C.** `sender()` isn't thread-safe and throws
- **D.** `sender()` reads mutable actor state, so by completion time it returns whoever sent the *latest* message, or `deadLetters`

<details><summary>Answer</summary>

**D.** The reply goes to the wrong actor, non-deterministically. **Fix: capture it first** —
`val replyTo = sender()` outside the callback.

The companion bug in the same code shape: mutating a `var` inside the callback runs on the future's
thread, escaping the single-threaded illusion entirely. Fix with **`pipeTo self`** so state changes
only happen in `receive`.

**The general rule: never close over `sender()`, `context`, or any `var` inside an async callback.
Capture values, and route results back through the mailbox.** (In Akka Typed this bug is impossible
— `replyTo` is a field of the message.)

→ `quiz/06` B2
</details>

---

### Q12. Why configure a separate dispatcher for a blocking actor?

- **A.** Blocking actors need a higher thread priority
- **B.** Otherwise it consumes threads from the pool shared by every actor — including Akka HTTP internals and cluster heartbeats
- **C.** Dispatchers are per-actor by default anyway
- **D.** To enable message ordering guarantees

<details><summary>Answer</summary>

**B.** This is *the* most common Akka misconfiguration. Starving the default dispatcher causes
cluster heartbeats to miss their schedule, so **nodes get falsely marked unreachable** — "blocking
on the default dispatcher caused a cluster split" is a real and instructive war story.

Configure a `thread-pool-executor` with a fixed size, `throughput = 1`, and attach it with
`.withDispatcher("blocking-io-dispatcher")`.

Akka's own docs have a page titled "Blocking needs careful management" for exactly this.

→ `quiz/06` B3
</details>

---

### Q13. How do Pekko/Akka Streams provide backpressure?

- **A.** Demand-driven: the downstream sends `request(n)` upstream, and the upstream may emit at most `n`
- **B.** By dropping messages when buffers fill
- **C.** By blocking the producer thread
- **D.** By buffering to disk

<details><summary>Answer</summary>

**A.** Reactive Streams — nothing is pushed unless it was pulled. That propagates all the way to the
source, including a TCP socket, where it becomes actual TCP window backpressure on the wire.

Key API: `mapAsync(parallelism)` (bounded concurrent async work, order-preserving),
`buffer(size, OverflowStrategy)` where you choose the policy **explicitly**, `throttle`,
`groupedWithin` for batching.

The standard advice: **actors for state, streams for flow** — because raw actors have *no*
backpressure (unbounded mailbox by default).

→ `quiz/06` B4
</details>

---

### Q14. What is a fiber, and what does it give you that a JDK thread doesn't?

- **A.** Faster context switching only
- **B.** Access to more CPU cores
- **C.** Cancellation that is safe, composable, and runs finalizers — plus structured concurrency and typed errors
- **D.** Guaranteed fairness between tasks

<details><summary>Answer</summary>

**C.** Fibers are conceptually close to virtual threads (both continuations on carriers), but the
scheduling is the *library's*, and it integrates with the effect type's cancellation and error model.

What they give you that JDK threads still don't: `fiber.cancel` is an **effect you can await** that
runs finalizers and respects `uncancelable`/`bracket` regions — versus `Thread.interrupt`, which is
advisory and which most code ignores. Plus structured concurrency by default (`parTupled`, `race`,
`Supervisor`) and, in ZIO, typed errors.

→ `quiz/06` C1
</details>

---

### Q15. What does "semantic blocking" mean?

- **A.** Blocking that the compiler can detect
- **B.** An operation that blocks the fiber but not the thread — the runtime deschedules the fiber and reuses the carrier
- **C.** Blocking with a timeout attached
- **D.** Blocking only on the blocking pool

<details><summary>Answer</summary>

**B.** `Deferred.get`, `Queue.take` on empty, `Semaphore.acquire` with no permits — they *look*
blocking but cost only a continuation. So you can write straightforward sequential coordination code
with **millions** of fibers and still only N carrier threads.

The contrast: **`IO.blocking { }` is for *actual* thread-blocking code** (JDBC, file I/O, a legacy
Java library) and shifts to a separate pool. Failing to wrap a real blocking call is the Cats Effect
equivalent of the global-EC mistake, with an identical symptom.

→ `quiz/06` C3
</details>

---

### Q16. What does `Resource[F, A]` give you over try-with-resources?

- **A.** Better performance
- **B.** It automatically retries acquisition
- **C.** It works with checked exceptions
- **D.** It's a composable *value* — release runs on success, error **and cancellation**, and N resources compose into one `use`

<details><summary>Answer</summary>

**D.** try-with-resources is lexical and synchronous only, so it can't express "this connection is
held for the lifetime of that fiber". `Resource` is a value you can pass around, combine, and
acquire in parallel — and N resources release in reverse order correctly even if acquisition fails
halfway or the whole thing is cancelled.

The comparison to make: **Java has no correct answer for "release this resource if the async
operation is cancelled."** `CompletableFuture.cancel` doesn't interrupt, `Thread.interrupt` is
advisory, `finalize` is dead, `Cleaner` is non-deterministic. This is a genuine, concrete advantage.

→ `quiz/06` C4
</details>

---

### Q17. Which Scala construct allocates the **most** for 1 million `Int`s?

- **A.** `Array[Int]`
- **B.** `Vector[Int]`
- **C.** `Map[Int, Int]`
- **D.** `List[Int]`

<details><summary>Answer</summary>

**C.** Measured: `Array[Int]` **3.8 MB**, `Array[Integer]` 19.1 MB, `Vector[Int]` 19.7 MB,
`List[Int]` **38.1 MB**, `Set[Int]` 131.4 MB, `Map[Int,Int]` **245.7 MB** — 65× the primitive array.

`List[Int]` breakdown: a cons cell is 16 bytes of header + 2 compressed references = 24 bytes, plus
a 16-byte boxed `Integer` = 40 bytes/element. And the cells are **scattered across the heap**, so
traversal is pointer-chasing with poor cache locality — often a bigger cost than the allocation.

Use `ArraySeq.ofInt` for an immutable, unboxed, contiguous sequence.

→ `quiz/06` D1, D2, measured in `problems/scala/S03AllocationPressure.scala` Part 2
</details>

---

### Q18. Which is typically the **top** allocation hotspot in a real Scala service?

- **A.** Eager string interpolation in log statements
- **B.** Case class construction
- **C.** `Option` allocation
- **D.** `for`-comprehension desugaring

<details><summary>Answer</summary>

**A.** `s"..."` is **eager**: the `StringBuilder` runs, values box for the interpolator, and the
`String` is built — *then discarded* because the level is off. Java's SLF4J
`log.debug("order {}", id)` defers formatting, so **idiomatic Java didn't have this problem and the
direct Scala port does.**

It's usually the number-one hit in an allocation profile and it's a one-line fix: use
`scala-logging`, whose macros rewrite `logger.debug(s"...")` into
`if (logger.isDebugEnabled) ...` — zero cost when off.

**Look for this first.** **B**, **C** and **D** are all real but secondary.

→ `quiz/06` D1, D3, measured in `S03AllocationPressure.scala` Part 4
</details>

---

## Score yourself

- **15+** — good.
- **Priority if short on time:** Q1–Q6 (the `Future`/EC traps, relevant on every stack) and Q17/Q18
  (allocation — where concurrency and GC prep meet). Then Q10–Q13 if the answer to "which stack?" is
  Pekko.

The framing to carry: **all four `Future` traps are consequences of one design decision — `Future`
is eager.** Every one is either impossible or explicit in Cats Effect/ZIO.
