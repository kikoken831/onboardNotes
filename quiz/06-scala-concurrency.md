# Quiz 06 — Scala Concurrency & Scala-Specific GC Pressure

The stack is TBC, so this covers all four realistic choices. Sections:
**A.** `scala.concurrent.Future` (relevant no matter what) ·
**B.** Akka/Pekko · **C.** Cats Effect & ZIO · **D.** Scala GC pressure.

Target: 25 min.

---

## A. `scala.concurrent.Future` and `ExecutionContext`

### A1. What is an `ExecutionContext`, and what is `ExecutionContext.global`?

<details><summary>Answer</summary>

`ExecutionContext` is Scala's `Executor` abstraction — a single method, `execute(runnable: Runnable): Unit`, plus `reportFailure`. Every `Future` operation (`map`, `flatMap`, `onComplete`, `Future.apply`) needs one, which is why they all take an `implicit ec: ExecutionContext`.

`ExecutionContext.global` is a **`ForkJoinPool`** with parallelism = `availableProcessors()` by default (tunable via `scala.concurrent.context.minThreads` / `numThreads` / `maxThreads`, where `numThreads` accepts `xN` multipliers like `x1`). It's a **compute pool**, sized for CPU-bound work.

The critical consequence: **there are only ~N threads.** If you run blocking I/O on `global`, you starve it. With 8 cores, 8 concurrent JDBC calls consume the entire application's capacity to make progress on *anything* — including the callbacks that would have completed those very futures, which is how you get a **deadlock**, not just slowness.

`import scala.concurrent.ExecutionContext.Implicits.global` is the single most common source of production incidents in Scala `Future` codebases. It's convenient, it compiles, and it's usually wrong outside a REPL or a test.
</details>

---

### A2. What does `scala.concurrent.blocking { … }` do?

<details><summary>Answer</summary>

It signals to the `ExecutionContext` that the enclosed code will block, so the pool can compensate. Concretely, on a `ForkJoinPool`-backed context it wraps the body in a **`ForkJoinPool.ManagedBlocker`**, which lets the pool **spawn a compensation thread** to maintain its target parallelism while your thread is parked.

```scala
Future {
  blocking { jdbcTemplate.query(sql) }   // pool grows to compensate
}
```

Caveats that matter, and this is the interesting part:
- It only works on a context that understands it. On a **fixed thread pool** it's a **no-op** — no compensation is possible, so you still starve.
- The compensation is **unbounded-ish**: enough concurrent `blocking` calls and the FJP creates a lot of threads (up to `maxThreads`, default 256 for the global EC). You've converted thread starvation into thread explosion. It is a mitigation, not a design.
- It does nothing about the *real* problem, which is that you have no bound on concurrent access to the downstream resource.

**The correct design is a dedicated, bounded `ExecutionContext` for blocking work**, passed explicitly:
```scala
val blockingEc: ExecutionContext = ExecutionContext.fromExecutorService(
  new ThreadPoolExecutor(20, 20, 60L, SECONDS, new ArrayBlockingQueue(200),
    namedThreadFactory("jdbc"), new AbortPolicy))
```
Then `Future { … }(blockingEc)`. Two pools: one compute (cores-sized), one blocking (bounded, sized to the downstream's capacity). That's the answer they want. `problems/scala/S01BlockingGlobalEC.scala` demonstrates the starvation.
</details>

---

### A3. Scala `Future` vs Java `CompletableFuture` vs `IO` — eager or lazy?

<details><summary>Answer</summary>

**Scala `Future` is eager.** `Future { work }` **starts running immediately** — it's submitted to the EC at the moment of construction. Consequences:
- You cannot **retry** a `Future`; it holds a completed result, so "re-running" it just re-observes the same value. Retry requires a `() => Future[A]` thunk.
- You cannot meaningfully **cancel** it. `scala.concurrent.Future` has **no cancellation API at all**. A `Future` that's stuck on a socket read runs to completion, always. This is its single biggest design limitation.
- **Referential transparency is broken**: `val f = Future(sideEffect); (f, f)` differs from `(Future(sideEffect), Future(sideEffect))` — one effect vs two. So you can't refactor by substituting values, which is the whole basis of equational reasoning.
- Ordering and parallelism depend on *where you construct* the futures, not where you compose them (see A5).

`CompletableFuture` is also eager, with the same retry/substitution problems, but it does have `cancel`.

**`cats.effect.IO` / `ZIO` are lazy** — a value **describing** a computation. Nothing runs until `unsafeRunSync()`/the runtime interprets it at the "end of the world" (usually `IOApp.run`). That buys you: free retries (`io.retry(policy)`), real **cancellation** with resource safety, referential transparency (so `val x = io; x *> x` runs it twice, as you'd expect from substitution), and composable timeouts.

This eager/lazy distinction is the single most important conceptual difference if the team picks Cats Effect or ZIO, and it's a very likely interview question.
</details>

---

### A4. Predict: does this run in parallel or sequentially?

```scala
val result = for {
  a <- callServiceA()   // each returns Future[Int], takes 1s
  b <- callServiceB()
  c <- callServiceC()
} yield a + b + c
```

<details><summary>Answer</summary>

**Sequentially — 3 seconds.** This is the number-one Scala `Future` gotcha.

The `for`-comprehension desugars to nested `flatMap`s:
```scala
callServiceA().flatMap(a => callServiceB().flatMap(b => callServiceC().map(c => a+b+c)))
```
`callServiceB()` is *inside* the lambda passed to `flatMap`, so it isn't even **constructed** until `a` has arrived. Since `Future` is eager, construction *is* starting — so nothing starts until its predecessor finishes.

Parallel version — construct all the futures **before** composing:
```scala
val fa = callServiceA(); val fb = callServiceB(); val fc = callServiceC()  // all started
val result = for { a <- fa; b <- fb; c <- fc } yield a + b + c             // 1 second
```
Or `Future.sequence(List(fa, fb, fc))`, or `(fa zip fb zip fc).map(...)`.

Note how **fragile** this is: the difference between 1 s and 3 s is where you put a `val`, with no type-level signal and no compiler warning. This is exactly the class of bug that laziness fixes — in Cats Effect you write `(ioA, ioB, ioC).parTupled` and the parallelism is **explicit in the type/API**, while `for { a <- ioA; b <- ioB }` is *always* sequential and *always* obvious. Great point to volunteer.
</details>

---

### A5. `Future.sequence` vs `Future.traverse` vs `foldLeft` — what's the difference in concurrency?

<details><summary>Answer</summary>

```scala
// 1. All at once — N concurrent, unbounded
Future.sequence(items.map(fetch))              // futures already started by .map
Future.traverse(items)(fetch)                   // same, one less intermediate collection

// 2. Strictly sequential — 1 at a time
items.foldLeft(Future.successful(List.empty[R])) { (acc, i) =>
  acc.flatMap(rs => fetch(i).map(_ :: rs))      // fetch(i) constructed inside flatMap
}
```

`sequence`/`traverse` fire **everything simultaneously**. With 10 000 items that's 10 000 concurrent in-flight requests, which will exhaust your connection pool, blow up the downstream, or OOM. There is **no built-in throttled variant** for `scala.concurrent.Future` — that's a real gap.

Bounded concurrency requires doing it yourself:
```scala
items.grouped(20).foldLeft(Future.successful(Vector.empty[R])) { (acc, batch) =>
  acc.flatMap(rs => Future.traverse(batch)(fetch).map(rs ++ _))   // 20 at a time
}
```
…or a `Semaphore`-gated wrapper, or Akka Streams' `mapAsync(parallelism)`.

Contrast, and say this: **Cats Effect gives you `parTraverseN(20)(fetch)` and ZIO gives you `ZIO.foreachPar(items)(fetch).withParallelism(20)`** — bounded concurrency as a first-class, one-argument concern. Akka/Pekko Streams gives `Source(items).mapAsync(20)(fetch)` with backpressure end-to-end. The fact that this is *awkward* with bare `Future` and *trivial* with the effect libraries is one of the better arguments for adopting one.

Also: `Future.sequence` **fails fast on the first failure** but does **not cancel** the others (there's no cancellation) — they run to completion, wasting resources, and their failures are silently dropped. Use `Future.sequence(fs.map(_.transform(Success(_))))` to collect all results including failures.
</details>

---

### A6. Is this Scala code thread-safe?

```scala
class Counter {
  private var count = 0
  def increment(): Unit = count += 1
  def get: Int = count
}
```

<details><summary>Answer</summary>

**No.** Scala's `var` compiles to a plain mutable JVM field. `count += 1` is `count = count + 1` — a non-atomic read-modify-write with no visibility guarantee, identical to Java's `count++`. Scala's syntax makes it look more innocuous, but the JMM applies unchanged.

Fixes, in idiomatic order:
1. **Don't have mutable shared state.** Return a new value: `case class Counter(count: Int) { def increment = copy(count + 1) }`. This is the Scala answer, and it's the migration's real concurrency win — `val` and immutable collections are the *default*, so the whole class of bug becomes something you have to opt into.
2. `private val count = new AtomicInteger` / `LongAdder`.
3. `synchronized { count += 1 }` (Scala's `synchronized` is on `AnyRef`).
4. Confine the mutation to a single thread — an actor's `receive`, or `Ref[IO, Int]` in Cats Effect / `Ref` in ZIO (a purely functional, `AtomicReference`-backed cell with `update`/`modify`).

Point to make: **`val` and immutability are not automatic thread safety** — `val xs = mutable.ListBuffer[Int]()` is a `val` holding a mutable, unsynchronised structure, and `var` in a `case class` field or in an actor is still a `var`. Also note **`lazy val` is thread-safe** (it compiles to a double-checked lock on the instance, or a bitmap + `synchronized`) but it can **deadlock** if two threads initialise interdependent lazy vals in opposite orders — a genuinely famous Scala footgun.
</details>

---

### A7. What does `Await.result` do and when is it acceptable?

<details><summary>Answer</summary>

`Await.result(f, timeout)` **blocks the calling thread** until the future completes, then returns the value or throws. `Await.ready` blocks but returns the `Future` itself.

Acceptable in:
- `main`, at the very top of the application, to keep the JVM alive.
- **Tests** (though `ScalaFutures`/`whenReady` is nicer).
- A synchronous framework boundary you cannot change — e.g. a Servlet API method that must return a value, if you're not using async servlets.

Never acceptable inside a `Future` callback or on a compute pool: **`Await` on a pool thread is exactly how you deadlock a bounded EC.** Thread holds a slot while waiting for work that needs a slot to run. On the global EC with N cores, N nested `Await`s and you're wedged permanently. (`Await` does internally use `blocking`, so on the global FJP it *may* compensate — which makes it worse, because it papers over the design flaw until the day it doesn't.)

The rule: **be async all the way to the edge.** Return `Future[Result]` up to the framework and let *it* complete the response. Spring MVC accepts `CompletableFuture`/`DeferredResult`; Play, http4s, Akka HTTP, and Tapir all natively return the effect type. If you find yourself calling `Await` mid-stack, you've found a place where the port from Java is incomplete.
</details>

---

### A8. How do you handle failure in `Future`? What's the difference between a failed `Future` and a thrown exception?

<details><summary>Answer</summary>

A `Future[A]` completes with `Success(a)` or `Failure(t)`. Combinators:
- `recover { case e: TimeoutException => default }` — like `map` on the failure (returns a value).
- `recoverWith { case e => otherFuture }` — like `flatMap` on the failure.
- `transform` / `transformWith` — handle both paths.
- `fallbackTo(other)` — use `other` if this fails (keeps *this* failure if both fail).
- `zip`, `sequence` — fail fast on the first failure.
- `f.failed` — an inverted `Future[Throwable]`, occasionally handy in tests.

**The distinction:** an exception thrown **inside** `Future { … }` or inside a `map`/`flatMap` body is **captured** into a failed `Future` — it does not propagate to the caller's stack. But an exception thrown **while constructing** the future, outside the block, propagates normally:
```scala
def f(): Future[Int] = { require(x > 0); Future { compute() } }   // require throws synchronously!
```
Callers doing `f().recover { … }` won't catch it. Push all effects inside the block, or use `Future.fromTry`/`Future.failed`.

Two more gotchas worth naming:
- **Fatal errors** (`OutOfMemoryError`, `StackOverflowError`, `InterruptedException`, `ControlThrowable` — see `NonFatal`) are **not** captured into the `Future`; they're rethrown to the EC's `reportFailure`. Reasonable, but it means `recover` will never see them.
- **`foreach`/`onComplete` swallow**: if a callback throws, the exception goes to `reportFailure` (which by default just prints to stderr) and is otherwise lost. A fire-and-forget `Future` whose failure nobody inspects logs nothing useful. Same trap as `CompletableFuture` (quiz 02 Q10).
</details>

---

## B. Akka / Pekko

> Ask which one. **Akka relicensed from Apache 2.0 to BSL 1.1 on 7 September 2022**
> (PR akka/akka#31561), and **Apache Pekko** is the open-source fork — taken from Akka 2.6.x at
> the last pre-BSL commit that same day, though the fork work began late October 2022 and
> today's Pekko also carries some Apache-2.0 Akka 2.7.0 features. APIs are near-identical; the
> package is `org.apache.pekko`. Current stable line is **Pekko 1.6.x** (1.7.0-RC1 as of
> mid-Aug 2026; a 2.0.0 line exists only as milestones). Saying this unprompted shows you're
> current.

### B1. What guarantees does the actor model give you, and what does it cost?

<details><summary>Answer</summary>

Guarantees:
- **One message at a time.** An actor processes its mailbox serially, so **actor-internal `var` state needs no synchronisation** — the actor is a unit of thread confinement. This is the central win.
- **Location transparency** — an `ActorRef` may point at a local or remote actor; the code doesn't change. Basis for clustering and sharding.
- **Supervision** — a parent decides what happens when a child fails (resume/restart/stop/escalate). "Let it crash" plus a restart strategy replaces defensive error handling.
- **At-most-once delivery** by default, with **ordering preserved per sender–receiver pair** (not globally).

Costs:
- **You lose the type system's help at boundaries** in classic (untyped) Akka — `receive` is `PartialFunction[Any, Unit]`, so a typo'd message is silently an unhandled `DeadLetter`. **Akka/Pekko Typed** (`Behavior[T]`) fixes this and is what you should use for anything new.
- **No compile-time protocol checking** of conversation sequences even in Typed.
- **`ask` introduces timeouts and failure modes** where a direct call had none; every ask needs a timeout policy and a decision about what a timeout *means*.
- **Debugging is harder** — no call stack across a message send, so stack traces don't tell you who caused what. You need correlation ids.
- **Mailboxes are unbounded by default** → a slow actor is a memory leak under load. Configure a **bounded mailbox** with a `MailboxSelector`/dispatcher config, or use Streams for anything flow-shaped.
- It's a big conceptual and operational commitment. If your problem is "handle HTTP requests statelessly", actors are overkill — that's what `Future`/`IO` and Akka HTTP's routing are for. Actors earn their keep for **stateful** entities (sessions, devices, game rooms, aggregates) and sharding.
</details>

---

### B2. Spot the bug.

```scala
class OrderActor(repo: Repo) extends Actor {
  var pending = 0
  def receive = {
    case Submit(order) =>
      pending += 1
      repo.save(order).onComplete {          // returns Future
        case Success(_) => pending -= 1; sender() ! Done
        case Failure(e) => pending -= 1; sender() ! Failed(e)
      }
  }
}
```

<details><summary>Answer</summary>

Two classic Akka bugs, both about **closing over actor context inside a `Future` callback**:

1. **`sender()` is invalid inside the callback.** `sender()` reads *mutable actor state* — the sender of the message **currently** being processed. By the time the future completes, the actor has moved on and `sender()` returns whoever sent the latest message, or `deadLetters`. The reply goes to the wrong actor, non-deterministically. **Fix: capture it first** — `val replyTo = sender()` outside the callback. (In Akka Typed this bug is impossible: `replyTo` is a field of the message.)

2. **`pending -= 1` runs on the future's thread, not the actor's thread.** It escapes the single-threaded illusion entirely — it's now an unsynchronised `var` mutated concurrently from arbitrary pool threads, racing with the actor's own `pending += 1`. **Fix: `pipeTo self`** and mutate state only in `receive`:
   ```scala
   case Submit(order) =>
     pending += 1
     val replyTo = sender()
     repo.save(order).map(_ => Saved(replyTo)).recover{case e => SaveFailed(replyTo,e)}.pipeTo(self)
   case Saved(replyTo)       => pending -= 1; replyTo ! Done
   case SaveFailed(replyTo,e)=> pending -= 1; replyTo ! Failed(e)
   ```

Also worth flagging: nothing bounds in-flight saves, and the actor's `receive` never blocks (good) but `repo.save` presumably uses some EC — make sure it isn't the actor's dispatcher.

**The general rule to state: never close over `sender()`, `context`, or any `var` inside an asynchronous callback. Capture values, and route results back through the mailbox.**
</details>

---

### B3. What's a dispatcher, and why would you configure more than one?

<details><summary>Answer</summary>

A **dispatcher** is the `ExecutionContext` that runs actors' message processing (and can be used as an EC directly). Types: `Dispatcher` (a FJP or thread-pool, event-based, the default), `PinnedDispatcher` (one dedicated thread per actor — for a genuinely blocking or latency-critical actor), and the deprecated `BalancingDispatcher`.

Why more than one — **bulkheading**, the same argument as quiz 02 Q4:
```hocon
blocking-io-dispatcher {
  type = Dispatcher
  executor = "thread-pool-executor"
  thread-pool-executor { fixed-pool-size = 20 }
  throughput = 1
}
```
```scala
context.actorOf(Props[LegacyJdbcActor]().withDispatcher("blocking-io-dispatcher"))
```
If a blocking actor runs on the **default dispatcher**, it consumes threads from the pool shared by *every* actor in the system — including Akka HTTP's internals and cluster heartbeat actors. Cluster nodes get falsely marked unreachable because heartbeats couldn't be scheduled. That failure mode ("blocking on the default dispatcher caused a cluster split") is a great war story to be able to describe.

`throughput` = how many messages an actor processes before yielding the thread (default 5). Higher = better throughput and locality, worse fairness. Set to 1 for blocking dispatchers.

This is *the* most common Akka misconfiguration, and Akka's own docs have a page titled "Blocking needs careful management" for exactly this reason.
</details>

---

### B4. What is backpressure, and how do Akka Streams provide it?

<details><summary>Answer</summary>

**Backpressure** = a slow consumer's ability to *tell the producer to slow down*, rather than being buffered into oblivion. Without it, an unbounded queue somewhere absorbs the mismatch until it OOMs (quiz 02 Q2) or latency becomes meaningless.

Akka/Pekko Streams implements **Reactive Streams**: it's **demand-driven**. The downstream sends `request(n)` upstream, and the upstream may emit at most `n` elements. Nothing is pushed unless it was pulled. That propagates all the way to the source — including a TCP socket, where it translates into not reading from the socket, which becomes actual TCP window backpressure on the wire. End-to-end flow control with no configuration.

Key API:
- `Source`/`Flow`/`Sink`, materialised into a running stream.
- `mapAsync(parallelism)(f)` — bounded concurrent async work, **preserving order**; `mapAsyncUnordered` for higher throughput.
- `buffer(size, OverflowStrategy)` — an *explicit, bounded* buffer where you choose the policy: `backpressure`, `dropHead`, `dropTail`, `dropBuffer`, `dropNew`, `fail`. Making the overflow decision explicit is the whole point.
- `throttle(n, per)` — rate limiting.
- `groupedWithin(n, d)` — batching for DB writes.
- Raw actors have **no backpressure** (unbounded mailbox by default), so if the flow is stream-shaped, use Streams. This is the standard advice: **actors for state, streams for flow.**

Contrast with the alternatives: **fs2** (Cats Effect) and **ZIO Streams** give you the same guarantees; both are generally considered nicer to write than Akka Streams' graph DSL, and neither needs a materialiser.
</details>

---

## C. Cats Effect 3 & ZIO 2

> Current stable as of Aug 2026: **Cats Effect 3.7.0** and **ZIO 2.1.26** — no 4.x or 3.x lines
> exist, so "3" and "2" are still the right major versions to name.

### C1. What is a fiber, and how does it differ from a thread?

<details><summary>Answer</summary>

A **fiber** is a lightweight, user-space unit of concurrency scheduled by the effect library's runtime onto a small pool of carrier threads. Creating one is an allocation; you can have millions. Suspension points are the `flatMap` boundaries of your effect — the runtime runs a chunk of your `IO`, then may **yield** and reschedule, so cooperative multitasking falls out of the interpreter loop for free (with **auto-yielding** every N flatMaps so a long chain can't hog a thread).

vs a **platform thread**: ~1 MB stack, kernel-scheduled, thousands max, preemptive.
vs a **virtual thread**: conceptually very similar (both are continuations on carriers). Differences: fibers are scheduled by the *library* and integrate with the effect type's cancellation and error model; virtual threads are scheduled by the *JVM* and work with all existing blocking JDK code. Fibers predate virtual threads by years.

What fibers give you that JDK threads (still) don't:
- **Cancellation that's safe and composable.** `fiber.cancel` is an effect you can await; cancellation runs finalizers, respects `uncancelable`/`bracket` regions, and is checked at every suspension point. Compare `Thread.interrupt`, which is advisory and which most code ignores.
- **Structured concurrency by default** — `parTupled`, `race`, `Supervisor`, ZIO's fiber scoping: children are guaranteed not to outlive the parent scope, and a failure cancels siblings.
- **Typed errors** (ZIO's `E` channel).

Both libraries also run blocking work on a *separate* pool: `IO.blocking { … }` (Cats Effect) / `ZIO.blocking` — the same compute/blocking split as A2, but built into the type so it's hard to get wrong.
</details>

---

### C2. What are `Ref`, `Deferred`, and `Semaphore` in Cats Effect (or ZIO)?

<details><summary>Answer</summary>

The purely-functional concurrency primitives — each returns effects, so nothing happens until run:

- **`Ref[F, A]`** — a mutable cell backed by an `AtomicReference`. `get`, `set`, `update(f)`, `modify(a => (a, b))` (atomic update + return a value). This replaces `var`, `AtomicInteger`, and most `synchronized` blocks. Crucially: `update` retries on CAS failure, and **the function must be pure** — a side-effecting `f` may run multiple times.
- **`Deferred[F, A]`** — a write-once, read-many promise. `get` semantically blocks (parks the fiber, doesn't block a thread) until `complete(a)`. This is your building block for "wait until something happens" — replaces `CountDownLatch(1)`, `CompletableFuture` as a signal, and most `wait`/`notify`.
- **`Semaphore[F]`** — permits, with `acquire`/`release` and, more usefully, the bracketed `permit: Resource[F, Unit]` / `withPermit(fa)` which releases even on cancellation. This is how you bound concurrency (quiz 03 Q3).

Plus: `Queue` (bounded/unbounded/dropping/circular — with `offer` semantically blocking when bounded, i.e. real backpressure), `Supervisor` (spawn fibers with a lifecycle tied to a scope), `Dispatcher` (run effects from non-effect code — your bridge to Java callbacks), `Resource` (acquire/release, cancellation-safe, composable — the answer to try-with-resources), `Hotswap`, `AtomicCell`.

ZIO's equivalents: `Ref`/`Ref.Synchronized`, `Promise`, `Semaphore`, `Queue`, `Hub`, `Scope`, `FiberRef` (a fiber-local, the fiber analogue of `ThreadLocal`).

The framing: **these are the j.u.c. toolkit, redone so that (a) everything is a value you can compose and (b) cancellation and resource release are correct by construction.** If you can name `Ref`, `Deferred`, `Semaphore`, and `Resource`, you can hold this conversation.
</details>

---

### C3. What does "semantic blocking" mean?

<details><summary>Answer</summary>

An operation that **blocks the fiber but not the thread**. `Deferred.get`, `Queue.take` on an empty queue, and `Semaphore.acquire` with no permits all *appear* to block in your code, but under the hood the runtime **descheduled the fiber** and put the carrier thread back to work on other fibers. Nothing is parked at the OS level.

Why it matters: it means you can write straightforward, sequential-looking coordination code — "take from the queue, wait for the signal, grab a permit" — with **millions** of fibers doing so, and still only N carrier threads. In Java, `BlockingQueue.take()` parks a real OS thread; in Cats Effect, `Queue.take` costs you nothing but a continuation.

The contrast to draw: **`IO.blocking { … }` is for *actual* thread-blocking code** (JDBC, a file read, a legacy Java library). It shifts execution to a separate unbounded cached pool so the compute pool isn't starved — the type system nudges you to declare which kind you have. Failing to wrap a real blocking call is the Cats Effect equivalent of A2's global-EC mistake, and the symptom is identical: the compute pool starves and everything stalls.

Virtual threads are the JDK's version of the same idea: make blocking cheap so straight-line code scales.
</details>

---

### C4. How does cancellation work, and what is `bracket`/`Resource`?

<details><summary>Answer</summary>

Cancellation in Cats Effect/ZIO is **checked at every asynchronous/suspension boundary**. `fiber.cancel` marks the fiber; at its next `flatMap` boundary it stops, runs all registered finalizers **in reverse order**, and completes as `Canceled`. `cancel` itself is an effect that **semantically blocks until finalization is done**, so "cancelled" means "cleaned up", not "abandoned".

Regions:
- **`uncancelable { poll => … }`** — a critical section that cannot be interrupted, with `poll` to punch cancellable holes in it. Needed for correctness in acquire/release.
- **`bracket(acquire)(use)(release)`** — `release` runs on success, error, **and cancellation**. The primitive behind everything.
- **`Resource[F, A]`** — bracket as a **composable value**. `for { db <- dbPool; http <- httpClient } yield App(db, http)` builds a single `Resource` whose `use` acquires in order and releases in reverse, correctly, even if acquisition of the second fails halfway or the whole thing is cancelled.

Why this beats try-with-resources: `Resource` is a **value** — you can pass it around, combine it, put it in a `Map`, parallelise acquisition (`parZip`), and hold it across asynchronous boundaries. `try`-with-resources is lexical and synchronous only, so it can't express "this connection is held for the lifetime of that fiber". And it composes: N resources is still one `use` block, not N levels of nesting.

The comparison to make: **Java has no correct answer for "release this resource if the async operation is cancelled."** `CompletableFuture.cancel` doesn't interrupt the underlying work, `Thread.interrupt` is advisory, `finalize` is dead, and `Cleaner` is non-deterministic. This is a genuine, concrete advantage of the Scala effect libraries, and a good thing to be able to argue.
</details>

---

### C5. ZIO's `ZIO[R, E, A]` — what do the three parameters buy you?

<details><summary>Answer</summary>

- **`R` — environment/requirements.** The dependencies this effect needs. `ZIO[Database & Logger, ...]` says so *in the type*, and `provide`/`ZLayer` supplies them. This is compile-time dependency injection: **the Spring `@Autowired` + component-scan runtime graph becomes a type-checked, compile-time one.** No "no qualifying bean of type" at startup — it won't compile. That's the headline for someone coming from Spring, so lead with it.
- **`E` — typed errors.** `ZIO[R, DbError, User]` says exactly what can go wrong. The compiler forces you to handle or propagate `DbError`, and `catchAll` narrows `E` to `Nothing` so the type proves you've handled everything. Contrast Java's unchecked exceptions (invisible) or checked ones (uncomposable). `Cause` additionally tracks defects and multiple parallel failures.
- **`A` — success value.** As usual.

`Task[A]` = `ZIO[Any, Throwable, A]`, `UIO[A]` = `ZIO[Any, Nothing, A]` (can't fail), `RIO`, `IO[E,A]` are aliases.

Cats Effect's `IO[A]` has neither `R` nor `E` — you get `Throwable` and you thread dependencies by hand (constructor injection, or `Kleisli`/`ReaderT` if you want them in the type). Both approaches are respectable: ZIO is more powerful and more opinionated; Cats Effect is simpler and composes with the wider Typelevel ecosystem (http4s, doobie, fs2) very naturally.

Don't take a side unprompted — say what each buys, and note that **the ecosystem choice usually decides it** (http4s+doobie+fs2 → Cats Effect; ZIO HTTP + ZIO Quill → ZIO).
</details>

---

## D. Scala-specific GC & performance pressure

### D1. Where does Scala allocate more than equivalent Java? Name as many as you can.

<details><summary>Answer</summary>

This is the highest-value question in this file, because it's the intersection of both prep topics.

1. **Boxing.** `List[Int]`, `Option[Int]`, `Map[String, Int]`, `Future[Int]` — generics erase to `Object`, so every `Int` becomes a **16-byte `java.lang.Integer`** to carry 4 bytes. `Array[Int]` is a genuine primitive `int[]`; `List[Int]` is not. Only `-128..127` are cached by `Integer.valueOf`.
2. **Tuples.** `(Int, String)` is a `Tuple2` object with **two boxed fields** — so `(1, 2)` costs a `Tuple2` (24 bytes) + two `Integer`s (32) = **~56 bytes for 8 bytes of data**. Very common in `map`/`groupBy`/`zip` pipelines.
3. **`Option`.** Every `Some(x)` is an allocation (16 bytes). `None` is a singleton, so that's free. `Option[Int]` boxes twice.
4. **Closures.** Every lambda is an object; if it **captures** variables, they're fields on it. A closure created inside a loop allocates per iteration. (Java 8+ lambdas that capture nothing are cached singletons; Scala 2.12+ uses `invokedynamic`/`LambdaMetafactory` so this got much better, but capturing closures still allocate.)
5. **Intermediate collections.** `xs.map(f).filter(p).map(g)` builds **three full collections**. On a 1 M-element `List` that's 3 M cons cells plus 3 M boxed `Int`s ≈ 120 MB of garbage for one pipeline. (`problems/scala/S03AllocationPressure.scala` measures this.)
6. **`for`-comprehensions** desugar to `map`/`flatMap`/`withFilter` chains — same intermediate allocations, plus a closure per stage, plus a `Tuple` if you pattern-match multiple bindings.
7. **Immutable collection "updates".** `map + (k -> v)` copies path nodes (a HAMT is O(log₃₂ n) nodes, not O(1)) and allocates a `Tuple2` for the arrow. In a fold over 1 M items that's real.
8. **`String` interpolation** — `s"..."` builds a `StringBuilder` + intermediate `String`s. Worse: it's evaluated **eagerly**, so `logger.debug(s"expensive $x")` pays even when debug is off (unlike SLF4J's `{}` placeholders). A frequent hot-path find.
9. **Case class `copy`** — a whole new object per update. Correct and cheap individually, expensive in a tight loop.
10. **Implicit conversions and value-class boxing.** A `value class` (`extends AnyVal`) is unboxed *usually* — but it **boxes** when used as a type parameter, stored in an array, pattern-matched, or in an `Option`. So the optimisation silently evaporates in exactly the generic contexts you wanted it.
11. **`Future` machinery** — every `map`/`flatMap` allocates a `Promise`, a callback object, and a `Try` (`Success`/`Failure` wrapper). Fine per request; a lot in a 10 k/s hot loop.
12. **`Range.foreach`** is specialised and fine, but `(1 to n).map(...)` produces a boxed `IndexedSeq`.
13. **Varargs / `Seq` literals** in method calls, and `ArraySeq` wrappers.

Mitigations to name: `Array` + `while` loops for numeric hot paths; `.view` or `.iterator` to fuse pipeline stages into a single pass with no intermediates; `foldLeft` instead of `map.sum`; `@specialized` (limited, generates code explosion) or a manual primitive specialisation; **Scala 3 `opaque type`** instead of value classes (compiles to the underlying type, no boxing surprises); `mutable.ArrayBuffer`/`Builder` locally (mutation confined to a method is perfectly idiomatic); `cats.data.Chain` for O(1) append; and `.mkString`/`StringBuilder` over repeated `+`.

Framing to deliver: **this is a young-gen allocation-rate story, not a leak story.** Most of it dies in eden and the copying collector never touches it — which is why idiomatic Scala performs fine in the 99% of code that isn't a hot loop. But it raises GC *frequency* (quiz 05 Q10) and therefore promotion, so the top few allocation sites are worth profiling. **Measure with async-profiler `-e alloc` before rewriting anything into ugly `while` loops.**
</details>

---

### D2. Why can `List` be a performance problem, and what should you use instead?

<details><summary>Answer</summary>

`List` is a **singly-linked immutable cons list**. Per element: a `::` object (16 bytes of header + 2 references = **24 bytes**) plus the boxed element if it's a primitive (another 16). So a `List[Int]` of 1 M elements ≈ **40 MB** where an `Array[Int]` is **4 MB** — a 10× difference. And the cells are scattered across the heap, so traversal is **pointer-chasing with terrible cache locality** — often the bigger cost than the allocation.

Operation costs: prepend O(1), head/tail O(1), **`apply(i)`/`length`/`last`/append O(n)**. `xs :+ x` in a loop is **quadratic** — a classic accidental O(n²).

Choose instead:
- **`Vector`** — O(log₃₂ n) ≈ effectively constant indexed access, append, and update. The default general-purpose immutable sequence.
- **`ArraySeq`** — immutable, array-backed, contiguous, and there's a specialised `ArraySeq.ofInt` etc. that **doesn't box**. Best immutable choice for numeric data.
- **`Array`** — raw JVM array, no boxing, best locality. Mutable and invariant, so it's awkward in generic code, but correct for hot numeric paths.
- **`mutable.ArrayBuffer`** / a `Builder` — for accumulating locally before freezing. Local mutation is idiomatic Scala, not a sin.
- **`cats.data.Chain`** / `Vector` — O(1) append *and* prepend, for accumulating in a fold.
- **`.iterator` / `.view`** — lazy, fuses stages, single pass, no intermediates. `xs.view.map(f).filter(p).toList` allocates one output collection instead of three.

`List` is still the right choice for small collections, recursive head/tail algorithms, and pattern matching (`case h :: t`) — which is most code. The point is knowing when it isn't.
</details>

---

### D3. Your Scala service has 3× the GC overhead of the Java one it replaced. How do you investigate?

<details><summary>Answer</summary>

Same method as quiz 05 Q16, with Scala-specific hypotheses. And **first, confirm the framing**: is it 3× the *GC CPU %*, 3× the *pause time*, or 3× the *allocation rate*? They imply different causes.

1. **Measure allocation rate** for both services (GC log or JFR). If the Scala one allocates 5 GB/s vs 1 GB/s, it's a D1 problem and you have your answer.
2. **`async-profiler -e alloc`** → flame graph of allocation by call site. This is the whole investigation, usually. Look for: `scala.collection.immutable.$colon$colon` (List cells), `Tuple2`, `java.lang.Integer`/`Long` (boxing), `Some`, `StringBuilder`, `scala.concurrent.impl.Promise` (Future churn), and any `copy$default$N`.
3. **Check the top 5 sites** against D1's list. In practice the wins are boringly repetitive: eager string interpolation in log statements; a `map.filter.map` chain over a large collection in a request path; boxing in a numeric fold; `groupBy` materialising tuples; `List :+` in a loop.
4. **Check retention separately** — post-GC live set trend. If *that's* also 3×, it's not allocation churn, it's data structure footprint (a `List`/`Map` where Java had an `int[]`, or memoisation that Java didn't have) or a genuine leak.
5. **Check the effect-runtime overhead**: is the compute pool sized correctly? Are blocking calls wrapped (`IO.blocking`/`blocking`/a separate EC)? A starved pool looks like a latency problem, not a GC problem, but they get conflated.
6. **Check for accidental sequentiality/parallelism** (A4, A5) — a `Future.sequence` over 10 000 items creates 10 000 concurrent in-flight objects, which is both a GC and a downstream problem.
7. **Then fix, in order:** the top 3 allocation sites → `.view`/`iterator` fusion → primitive arrays in the genuinely hot loop → more eden. Re-measure after each. **Don't de-idiomatise the whole codebase**; the top few sites usually account for most of it.

Say the reframe out loud: **"3× the GC overhead" may be entirely fine** if GC was 1.5% and is now 4.5%, and the Scala version is more maintainable. Get the absolute number and the SLO impact before optimising. Then note the flip side worth raising: Scala's immutability-by-default *removes* whole classes of concurrency bug, and trading a few percent of CPU for that is usually a good deal — but only if you know the number.
</details>

---

### D4. What is `@tailrec`, and why does it matter for the JVM?

<details><summary>Answer</summary>

The JVM has **no tail-call optimisation** — every call pushes a frame, so deep recursion gives `StackOverflowError` (default stack ~512 KB–1 MB, so roughly 10 000–100 000 frames depending on frame size).

`scalac` performs **self-tail-call elimination**: a method whose recursive call is in tail position and which calls **itself** (not an override — so it must be `private`, `final`, or in an `object`) is rewritten into a `while` loop. Zero frames, zero allocation, same speed as the loop you'd have written.

**`@scala.annotation.tailrec` makes it a compile error if the optimisation cannot be applied.** Always annotate — otherwise you get a silently stack-consuming method, and the failure appears in production on the one input that's 50 000 elements long.

What it can't do: **mutual recursion** (`isEven`/`isOdd`) and non-self tail calls. For those you need **trampolining** — `scala.util.control.TailCalls` (`TailRec`), or `cats.free.Trampoline`, or `Eval.defer`. Notably, this is exactly what `IO`/`ZIO` give you for free: `IO`'s `flatMap` chains are interpreted by a **heap-allocated loop**, not the JVM stack, so arbitrarily deep recursion in `IO` is safe and stack-overflow-proof. That's a genuinely underrated benefit of effect types and a nice thing to mention.

GC angle for this file: recursion that isn't tail-eliminated builds a deep stack, and **deep stacks make GC root scanning more expensive** (quiz 04 Q5) and lengthen time-to-safepoint. Minor, but it connects the two topics.
</details>

---

### D5. Where should mutable state live in idiomatic Scala, if anywhere?

<details><summary>Answer</summary>

The hierarchy, best to worst:

1. **Nowhere — thread it through as a value.** `foldLeft`, a state parameter, or a recursive function's accumulator. If the state is per-request, it's just a local. This covers a surprising majority of cases.
2. **Confined to a method.** `var` and `mutable.ArrayBuffer` inside a function body are **perfectly idiomatic** when the mutation doesn't escape — a `Builder` in a tight loop is faster and no less safe than a fold. The rule is "no observable mutation", not "no `var`".
3. **In an explicit concurrency primitive** for genuinely shared state: `Ref[IO, S]` / ZIO `Ref` / `AtomicReference` with a pure `update`, or `Ref.Synchronized`/`AtomicCell` when the update needs to be effectful. This makes the sharing visible in the type.
4. **Confined to a single fiber/actor.** An actor's `var` (accessed only from `receive` — see B2) or a `StateT`/recursive-`Behavior` loop. Single-writer by construction.
5. **In an external store** — the database, Redis, a cache with a defined consistency model. The honest answer for anything that must survive a restart or be shared across instances.
6. **A `var` on a shared singleton object with a lock.** Works; the Java answer; the thing you're migrating away from. If you find yourself here, you've probably skipped 1–5.

What to avoid: `var` in a `case class`, mutable collections in `val` fields of a shared object, and `object`-level `var`s (Scala's `object` is a singleton — its `var`s are exactly Java's `static` mutable state, with all the same problems plus a lazy-init lock).

The line to deliver: **the migration's biggest concurrency win isn't a library, it's that the default flipped.** In Java, thread-safety is opt-in and every field is a potential race you have to reason about. In Scala, immutability is the default and shared mutation is something you have to deliberately reach for — so the reviewer's question changes from "is this safe?" to "why is this mutable?"
</details>

---

## Score yourself

Priority if short on time: **A1, A2, A4, A5** (the `Future`/EC traps — relevant on every
stack), **D1** (the allocation list — where both prep topics meet), **C1** and **C3**
(fibers, semantic blocking). Then B2 if the answer to "which stack?" is Akka/Pekko.
