# Quiz 07 — Spring Boot → Scala: What Actually Breaks

12 questions framed as migration problems rather than trivia. This is the file most
likely to match what your team actually asks, because it's what they're living through.

Target: 15 min.

---

### 1. Contrast the threading model of Spring MVC, Spring WebFlux, and a Scala `Future`/effect-based service.

<details><summary>Answer</summary>

**Spring MVC (thread-per-request).** Tomcat's acceptor hands each request to a worker from a pool (`server.tomcat.threads.max`, default 200). That thread runs the *entire* request: filters, controller, service, JDBC call, serialisation. Blocking is fine because the thread is dedicated. Concurrency ceiling = pool size. Context lives in `ThreadLocal`s, which works precisely *because* one thread owns the request. Simple, debuggable, real stack traces — and it dies at high concurrency with slow downstreams, since 200 threads all waiting on a 2-second call means 100 rps.

**Spring WebFlux (event loop).** A small number of Netty event-loop threads (≈ cores) never block; work is expressed as `Mono`/`Flux` and handed off via callbacks. Concurrency ceiling is thousands of in-flight requests on a handful of threads. Costs: **everything in the stack must be non-blocking** (R2DBC not JDBC — one blocking call on an event loop stalls every request that loop is serving), stack traces are useless, debugging is hard, and `ThreadLocal` context is gone (you use the Reactor `Context` instead).

**Scala `Future`/`IO`/`ZIO`.** Structurally like WebFlux: work hops between threads on an `ExecutionContext`/fiber runtime, so no thread is tied to a request. Two differences worth stating: (a) the effect libraries have **explicit compute/blocking pool separation** (`IO.blocking`, `ZIO.blocking`) so mixing blocking code in is a supported, typed operation rather than a landmine; (b) `Future` has **no backpressure or cancellation**, while `IO`/`ZIO` have both.

**Virtual threads (JDK 21) are the fourth option**, and worth raising: you get MVC's programming model with WebFlux's scalability. If the target JDK is 21+, "why not just virtual threads?" is a legitimate question to ask about the new architecture.

The point to land: **the migration changes who owns a request's thread**, and everything below follows from that.
</details>

---

### 2. `@Transactional` on a method that returns a `Future` — what happens?

<details><summary>Answer</summary>

**It's broken, and it fails silently**, which is why this is the most dangerous item in the migration.

Spring's `@Transactional` proxy: open a connection, bind it to the current thread via `TransactionSynchronizationManager` (a `ThreadLocal`), invoke the method, then commit or roll back **when the method returns**.

With a `Future`-returning method:
1. The method returns the `Future` **immediately** — usually before any database work has happened.
2. The proxy sees a normal return and **commits right then**, on an empty or partial transaction.
3. The actual DB work runs later, on an **EC thread with no bound connection**, so it either gets a fresh connection **outside any transaction** or fails.
4. A failure inside the `Future` completes it as `Failure` — the proxy never sees an exception, so **no rollback**.

Net effect: you have `@Transactional` in the source, code review sees "it's transactional", and there is no transaction. Partial writes, no atomicity, no rollback.

What to do:
- **Keep the transactional unit synchronous.** Do the whole transaction inside one blocking call, and make *that call* the thing you run on a blocking EC: `Future { blocking { txTemplate.execute(status => …) } }(blockingEc)`. The transaction is confined to one thread; the asynchrony is outside it. This is the pragmatic answer during an incremental migration.
- **Or move to a transaction abstraction that's a value, not a thread-local**: **doobie**'s `ConnectionIO` with `.transact(xa)`, **Slick**'s `DBIO` with `db.run`, or **ZIO Quill**. In all three, the transaction is part of the *description* of the work, composed explicitly, and committed where you say. No thread-locals, no proxies, no magic — and no silent failure. This is the target state.
- If the team keeps Spring's `PlatformTransactionManager`, at minimum **write a test that asserts rollback happens** on a failure path. That test will catch the whole class of bug.

Same argument applies to `@Async` + `@Transactional` on the same method, and to `@Transactional` self-invocation (the proxy is bypassed, so it does nothing at all).
</details>

---

### 3. Trace ids and MDC vanish from your logs after the port. Why, and how do you fix it?

<details><summary>Answer</summary>

**Why:** SLF4J's `MDC` is a `ThreadLocal<Map<String,String>>`. It's populated by a filter on the request thread. The moment work moves to an `ExecutionContext`/fiber/actor, the executing thread has an empty (or worse, a *previous request's*) MDC. Every log line after the first async hop loses its correlation id, so you can't reconstruct a request from logs — which usually gets discovered during the first production incident after the migration, at the worst possible moment.

**Fixes, worst to best:**

1. **Copy the context at submission.** Wrap the `ExecutionContext` so every `execute` captures the submitter's MDC and installs it on the worker (`MDC.getCopyOfContextMap` → set → run → **clear in a `finally`**). Spring's `TaskDecorator` does this for `@Async`. Works, but it's a leaky patch — one unwrapped EC anywhere and the chain breaks, and you must not forget the cleanup or you get cross-request bleed.
2. **A fiber-local.** ZIO's `FiberRef` and Cats Effect's `IOLocal` propagate along the fiber tree automatically — the right mechanism if you're on those runtimes. Libraries like `otel4s`/`natchez`/`ZIO Telemetry` build tracing on exactly this.
3. **Pass it explicitly as a value.** A `RequestContext` parameter (or an implicit one, or ZIO's `R` environment) carried through the call chain and used to build log messages. Verbose, and completely immune to the problem — the compiler enforces it.
4. **Use a tracing library that already solved it** — OpenTelemetry's Scala/Java agent plus context propagation, `natchez`, or `ZIO Telemetry`. Don't build this yourself.

The general lesson, which is the real answer: **`ThreadLocal` is not a context mechanism in an async world, it's an optimisation that happened to work under thread-per-request.** Every ambient-context feature you inherit from Spring — MDC, `SecurityContextHolder`, `RequestContextHolder`, transaction binding, `LocaleContextHolder` — has the same defect and needs the same treatment. Inventory them early; they're a known, finite list.
</details>

---

### 4. How do you handle a blocking JDBC layer you can't rewrite yet?

<details><summary>Answer</summary>

You quarantine it, and you size it honestly.

```scala
// One bounded pool, sized to the connection pool — not bigger.
private val jdbcEc: ExecutionContext = ExecutionContext.fromExecutorService(
  new ThreadPoolExecutor(
    /* core, max */ 20, 20,
    60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue[Runnable](200),           // bounded! backpressure, not accumulation
    new ThreadFactoryBuilder().setNameFormat("jdbc-%d").build(),
    new ThreadPoolExecutor.AbortPolicy))             // shed load loudly

def findUser(id: Long): Future[User] = Future { blocking { repo.findById(id) } }(jdbcEc)
```

The reasoning to articulate:
- **Never on the compute EC.** Blocking the global/compute pool starves everything (quiz 06 A2).
- **Threads ≈ connections.** More worker threads than DB connections just moves the queue from your executor to HikariCP, where you have less visibility and a worse timeout story. Aim for `threads ≈ maxPoolSize`, and remember the DB has a real concurrency limit too — 500 connections to Postgres is worse than 50.
- **Bounded queue + a rejection policy.** An unbounded queue converts overload into memory growth and unbounded latency (quiz 02 Q2). Rejection surfaces as a 503, which is a *correct* answer to overload.
- **Named threads.** So the thread dump tells you who's stuck.
- **Keep transactions inside one task** (see Q2) — the whole `txTemplate.execute` goes in the `Future`, not around it.
- **Metrics**: queue depth, active count, rejections, plus Hikari's `connections_pending` and `connections_timeout`. Queue depth is your earliest overload signal.
- On the effect libraries this is just `IO.blocking { … }` / `ZIO.blocking` — same idea, provided by the runtime with a sensible default pool, so prefer it if you're on CE/ZIO.
- **On JDK 21+**, virtual threads make the "one thread per blocking call" cost trivial — but the DB connection limit doesn't move, so you still bound with a `Semaphore` (quiz 03 Q3). The bound is a property of the database, not of your thread strategy.

Then say the strategic bit: this is a **strangler-fig** shape. Wrap the blocking layer behind an async interface, migrate callers to the interface, and swap the implementation to doobie/Slick later without touching them.
</details>

---

### 5. How do dependency injection and singleton scope differ between Spring and idiomatic Scala, and why is that a concurrency question?

<details><summary>Answer</summary>

**Spring:** `@Component`/`@Service` beans are **singletons by default**, wired at runtime by type via reflection, and often initialised via field/setter injection after construction. Concurrency consequences:
- Every bean is **shared by all request threads**, so any mutable field is a race (quiz 01 Q24). This is the single most common Spring concurrency bug.
- Field injection means fields are **non-final and set post-construction**, so they aren't safely published by final-field semantics — usually benign in practice because the context is fully initialised before serving traffic, but it's the same unsafe-publication shape as quiz 01 Q3.
- Errors surface **at startup, at runtime**: "No qualifying bean of type X".
- Proxies (for `@Transactional`, `@Async`, `@Cacheable`) mean the object you hold isn't the object you wrote — hence self-invocation silently not working.

**Idiomatic Scala:** constructor injection, by hand or with a layer/module system.
```scala
class UserService(repo: UserRepo, cache: Cache)      // dependencies are constructor params
// wired once at the edge:
val app = new UserService(new PostgresRepo(xa), new CaffeineCache)
```
- Everything is a `val`, set in the constructor → **safely published, immutable, final**. The unsafe-publication class of bug disappears.
- Wiring errors are **compile errors**. With ZIO's `ZLayer` (`ZIO[R,...]`), the dependency graph is checked by the type system — the `R` channel *is* the DI container, verified at compile time.
- No proxies, so no self-invocation surprises and no "why is my annotation not firing".
- Cost: you write the wiring yourself (a `Main` that constructs the graph — often 50–200 lines), and you lose Spring's ecosystem of auto-configuration. `MacWire` generates the wiring at compile time if the manual version gets tedious.

**Why it's a concurrency question:** a singleton is *shared mutable state by default* unless its fields are immutable. Spring makes sharing implicit and safety opt-in; constructor injection with `val`s makes safety the default. During the migration, **every stateful Spring bean is a thing to examine** — audit them for mutable fields, and either make them immutable or make the state explicit (a `Ref`, an actor, or the database).
</details>

---

### 6. What replaces Spring's `@Scheduled`, `@Async`, `@Cacheable`, and `@Retryable`?

<details><summary>Answer</summary>

The pattern: **Spring gives you annotations interpreted by a runtime proxy; Scala gives you values and combinators.** The trade is magic-and-brevity for explicitness-and-composability.

| Spring | Scala equivalent |
|---|---|
| `@Async` | Return `Future`/`IO`/`ZIO` from the method. The asynchrony is **in the type**, not in an annotation you might forget — and no self-invocation trap. |
| `@Scheduled(fixedDelay=…)` | `Source.tick(…)` (Pekko Streams), `fs2.Stream.awakeEvery[IO](1.minute)`, `ZIO.repeat(Schedule.fixed(1.minute))`, or an `IO`/`ZIO` recursive loop with `sleep`. All of these give you a **cancellable, composable, testable** schedule — and unlike `@Scheduled`, a thrown exception doesn't silently kill the job forever (quiz 02 Q6). |
| `@Retryable` | `ZIO.retry(Schedule.exponential(100.millis) && Schedule.recurs(5))`, `cats-retry`'s `retryingOnAllErrors`, or `fs2`'s `retry`. **Retry requires laziness** — you can't retry a `scala.concurrent.Future` (quiz 06 A3), you need `() => Future[A]`. Good place to make the eager/lazy point. |
| `@Cacheable` | An explicit cache: `scaffeine`/Caffeine wrapped in the effect type, or `Ref[IO, Map[K,V]]` for something simple. Explicit means you can see the key, the TTL, and the invalidation — the three things `@Cacheable` hides and everyone gets wrong. |
| `@Transactional` | `doobie`'s `.transact(xa)`, Slick's `db.run(action.transactionally)`, ZIO Quill. See Q2. |
| `@Value` / `@ConfigurationProperties` | `PureConfig`, `Ciris`, or `zio-config` — decoded into a case class, validated at startup, typed. |
| `@RestController` | http4s `HttpRoutes`, Pekko HTTP routing DSL, Tapir (endpoint descriptions as values → server + client + OpenAPI from one definition), ZIO HTTP. |
| `@Valid` / Bean Validation | `Validated`/`ValidatedNel` (accumulating errors), or refined types. Errors accumulate instead of throwing. |

The honest cost, which you should volunteer: **you give up a lot of "it just works."** Spring Boot's auto-configuration, actuator endpoints, metrics, health checks, and the enormous ecosystem are real value, and the Scala equivalents need assembling. Teams that miss this find themselves rebuilding Spring Boot badly. The upside is that everything is a value you can test without a container, and the framework can't surprise you at runtime.

Also worth saying: **you can keep Spring Boot and write Scala inside it.** It's a valid intermediate state for a migration, and it means you only fight one battle at a time.
</details>

---

### 7. What's the migration risk in translating `synchronized` blocks directly into Scala?

<details><summary>Answer</summary>

It compiles and it works — `x.synchronized { … }` is available on every `AnyRef` — so a mechanical port produces correct-looking code. The risks:

1. **You've ported the design, not just the syntax.** A `synchronized` block usually exists because there's shared mutable state. The Scala port is the moment to ask whether the state should be immutable, confined to a fiber/actor, or held in a `Ref`. If you keep the lock, you keep the design and get none of the migration's concurrency benefit.
2. **Pinning on JDK 21 virtual threads.** If the new service uses virtual threads, `synchronized` around anything blocking pins the carrier (quiz 03 Q2). Prefer `ReentrantLock`.
3. **`lazy val` interacts with locking.** `lazy val` compiles to synchronised double-checked initialisation on the enclosing instance. A `lazy val` whose initialiser acquires *your* lock, in a codebase where something else takes those two in the other order, is a deadlock — and it's invisible in the source. Scala's own `lazy val` initialisation deadlocks (two objects with interdependent lazy vals initialised concurrently) are a known footgun.
4. **`synchronized` on a `case class`** or on anything with value-based equality is a smell — you may be locking on a different instance than you think, and `synchronized` on a boxed primitive or interned `String` locks something globally shared.
5. **`object` initialisation** is itself synchronised by the JVM's class-init lock. Two `object`s whose initialisers reference each other from different threads deadlock, and the stack trace is baffling.
6. **Blocking inside a lock inside a `Future`** on a bounded EC is the compound version of every problem in quiz 06 A2.

The rule to give: **treat every `synchronized` in the Java source as a design question in the port, not a translation task.** Make a list of them during the migration; each one is either (a) state that should be immutable, (b) state that should live in one place (`Ref`/actor/DB), or (c) a genuine lock that should probably be a `ReentrantLock` or a `Semaphore`. Very few turn out to be (c).
</details>

---

### 8. Your team is deciding between "port to Scala + Future" and "port to Scala + Cats Effect/ZIO". What do you say?

<details><summary>Answer</summary>

Frame it as a trade, and make clear it partly depends on team experience rather than technical merit alone.

**`Future` (+ Spring or Play):**
- *For:* shallow learning curve for a Java team — it maps almost 1:1 to `CompletableFuture`. Interops with Spring and the whole Java ecosystem trivially. You can migrate incrementally, file by file. Hiring is easier.
- *Against:* **no cancellation** — a stuck request runs to completion, forever, consuming a connection. **No backpressure.** **No retry** without hand-rolled thunks. **Eager**, so it breaks referential transparency and makes the sequential-vs-parallel distinction a positional accident (quiz 06 A4). `ExecutionContext` discipline is a convention you enforce by review, not by the compiler. Error handling via `Try`/`recover` is workable but untyped.

**Cats Effect 3 or ZIO 2:**
- *For:* real **cancellation and resource safety** (`Resource`/`Scope`) — the thing `Future` fundamentally can't do, and the thing that matters most in a service with timeouts. Bounded concurrency as a one-liner (`parTraverseN`). Composable retries/timeouts. Lazy, so referentially transparent and testable without running anything. Structured concurrency. ZIO additionally gives typed errors and compile-time DI.
- *Against:* **a genuinely steep learning curve** for a Java team — this is a different programming model, not a library. Ecosystem lock-in (Typelevel or ZIO; interop between them exists but is friction). Stack traces need the library's tooling to be readable. Hiring is harder. And a team that half-learns it writes worse code than it would have with `Future`.

**What I'd actually say:**
1. **The decision is mostly about the team, not the technology.** Both can build a correct service. A team that adopts ZIO reluctantly will produce something worse than the same team writing careful `Future` code.
2. **Ask what problem the migration is solving.** If it's "we need cancellation, backpressure, and resource safety because we have timeout and connection-leak incidents", that's a direct argument for CE/ZIO. If it's "Java is verbose and we want better types", `Future` plus immutability gets you most of the value at a fraction of the risk.
3. **Whatever you pick, pick one and be consistent.** A codebase with `Future` in some modules, `IO` in others, and Akka in a third is the worst outcome — every boundary is a conversion and a place to get the EC wrong.
4. **The ecosystem often decides it.** If you want http4s + doobie + fs2, you're on Cats Effect. If you want ZIO HTTP, you're on ZIO. If you want to keep Spring, you're on `Future`.
5. **Don't have this argument in an interview as if you know their constraints.** Ask what they picked and why. Then be the person who can work with it.
</details>

---

### 9. How do you test concurrent code? What's different in Scala?

<details><summary>Answer</summary>

**The general problem:** concurrency bugs are non-deterministic, so a passing test proves very little. Strategies:

- **Make it deterministic instead.** Inject the executor/clock so the test can run everything on the calling thread (`ExecutionContext.parasitic`, `same-thread` executor, a virtual/test clock). Most "concurrency tests" should be determinism tests.
- **Stress + assert invariants.** Run N threads × M operations and assert the invariant at the end (`problems/java/P01Counter.java` is exactly this shape). Catches lost updates reliably; catches ordering bugs only sometimes. Run it many times, and on ARM as well as x86 — weak memory ordering surfaces different bugs (quiz 01 Q22).
- **jcstress** for real memory-model claims. It's the OpenJDK harness built for this; it enumerates outcomes and tells you which are permitted. If you're making a JMM argument, this is how you prove it.
- **Thread-safety analysis tools**: `ThreadMXBean.findDeadlockedThreads` in a test teardown, ErrorProne's `@GuardedBy` checking, SpotBugs' concurrency detectors.
- **Timeouts on every test** so a deadlock fails rather than hangs CI.
- **Chaos-ish**: inject latency and failures at boundaries; assert timeouts and cancellation actually work.

**What's better in Scala/effect land:**
- **ZIO's `TestClock`** — you `adjust(1.hour)` and time passes instantly and deterministically. Testing a retry schedule with exponential backoff becomes a fast unit test instead of a 30-second sleep. Cats Effect has `TestControl`/`TestContext` for the same thing, and it's genuinely one of the best arguments for these libraries.
- **`IO`/`ZIO` are values**, so you can assert on the *description* and run it under a controlled scheduler. `TestControl.executeEmbed` runs an `IO` with a deterministic, virtual-time scheduler — the whole concurrent program becomes reproducible.
- **ScalaCheck** property tests over concurrent operations, and `scalacheck-effect` for effectful properties.
- **`munit-cats-effect` / `zio-test`** handle the effect type natively so you're not calling `Await` in tests.
- **Immutability means far less to test** — most of the Java tests you'd write for shared-state races have no Scala equivalent because there's no shared state.

The line: **prefer designs that don't need concurrency tests** (immutable, single-writer, deterministic scheduler injected) over cleverer tests of concurrent designs.
</details>

---

### 10. What would you monitor in the new Scala service that you didn't in the Java one?

<details><summary>Answer</summary>

Carry over everything (latency percentiles, error rate, throughput, GC, heap, thread count, connection pool) and add:

- **`ExecutionContext` / fiber-runtime saturation.** Queue depth and active count per EC, and **rejection counts**. The compute-pool-starvation failure mode (quiz 06 A2) is the signature Scala incident, and it looks like "everything is slow" with low CPU. Cats Effect exposes the work-stealing pool's metrics; ZIO has `Runtime` metrics and `zio-metrics`.
- **Blocking-pool utilisation separately from compute.** The whole point of the split is that they saturate independently.
- **Fiber count** (ZIO/CE) — an unbounded fiber leak is the new unbounded-thread leak, and it's cheaper so it grows further before you notice.
- **Allocation rate**, explicitly. Given quiz 06 D1, this is the metric most likely to be different from the Java service, and it's the leading indicator for GC overhead.
- **Mailbox depth per actor / stream buffer occupancy** if Akka/Pekko — an unbounded mailbox is an invisible memory leak under load.
- **Cancellation and timeout counts.** New capability, so new signal: how often are you cancelling work, and is the cancellation actually releasing resources?
- **`Await`/`unsafeRun` occurrences** — ideally zero outside `main`. Worth a lint rule or an architecture test rather than a metric.
- **Pinned-thread events** if you're on virtual threads (`-Djdk.tracePinnedThreads`, JFR's `VirtualThreadPinned` event).
- **`lazy val` initialisation contention** — obscure, but if you have hot `lazy val`s the double-checked lock shows up in lock profiles.

And the operational shift: **thread count stops being a useful health metric.** In the Java service, "threads climbing" was a reliable alarm. With fibers or virtual threads, the thread count is flat and small no matter how bad things get — the equivalent signal is queue depth, fiber count, and in-flight request count. Teams miss this and lose an alert they were relying on.
</details>

---

### 11. What's the biggest concurrency *win* of the migration, and the biggest new risk?

<details><summary>Answer</summary>

**Biggest win: immutability by default.** In Java, thread safety is opt-in — every field on every shared bean is a race you must reason about, and quiz 01 Q24 is the bug that ships. In Scala, `val` and immutable collections are the default, `case class`es are safely publishable by construction, and shared mutation is something you have to reach for deliberately. That deletes an entire category of bug, and it changes the code-review question from "is this thread-safe?" to "why is this mutable?" Everything else (better types, expressiveness) is secondary to that.

Second-order wins worth naming: composable concurrency (`parTraverseN`, `race`, `timeout` as combinators rather than hand-rolled), and — if you go CE/ZIO — cancellation and resource safety that Java simply has no answer for.

**Biggest new risk: the `ExecutionContext`.** Java's thread-per-request model was *forgiving* — blocking was normal, one slow call cost you one thread out of 200, and the failure was gradual and legible. In Scala, a single blocking call on the compute EC can **deadlock the entire application** with 5% CPU utilisation, and there's no compile error, no warning, and no obvious symptom. It's a footgun that a Java-experienced team walks into precisely *because* their instincts are correct for the old model.

Concretely, the top three migration risks:
1. **Blocking on the compute EC** (quiz 06 A2) — the silent killer.
2. **Silently non-transactional code** (Q2) — data corruption with no error.
3. **Unbounded concurrency** — `Future.sequence` over a big collection, or an unbounded mailbox, taking out a downstream (quiz 06 A5).

All three share a shape: **the Java version's implicit bound is gone, and nothing replaced it.** That's the sentence to say if you get one shot at sounding like you've thought about this.
</details>

---

### 12. If you had to write the migration's concurrency guidelines on one page, what would be on it?

<details><summary>Answer</summary>

1. **Two pools, always.** A compute EC sized to cores, and a bounded blocking EC sized to the downstream's capacity. Never `ExecutionContext.Implicits.global` outside tests. Pass the EC explicitly; ban the wildcard import with a lint rule (Scalafix/WartRemover).
2. **Never block on the compute pool.** Every blocking call is wrapped (`IO.blocking` / `blocking { }` on the blocking EC) and reviewed. Grep for JDBC, `File`, `Thread.sleep`, `Await`, and `.get` on futures.
3. **Bound everything.** Every queue, every mailbox, every `traverse`. Use `parTraverseN` / `mapAsync(n)` / a `Semaphore`, never the unbounded variant. An unbounded queue is a deferred outage.
4. **No `Await` / `unsafeRun*` outside `main` and tests.** Async all the way to the framework edge.
5. **Immutable by default.** `val`, `case class`, immutable collections. Mutation is confined to a method body, a `Ref`, a single actor, or the database — and each of those is a deliberate, reviewed choice.
6. **Transactions stay on one thread and inside one unit of work.** Or move to doobie/Slick where the transaction is a value. Never `@Transactional` on an async-returning method (Q2).
7. **Context is passed, not ambient.** No new `ThreadLocal`s. Trace ids, principal, and tenant travel as values, a `FiberRef`/`IOLocal`, or via an OTel-integrated library.
8. **Every async boundary has a timeout, and every timeout has a defined behaviour.** What does the caller see? Is the work cancelled or leaked? `Future` can't cancel — know that and design around it.
9. **Name your threads**, and export per-pool metrics (queue depth, active, rejected). You cannot debug a thread dump full of `pool-3-thread-7`.
10. **Bulkhead per dependency.** One pool/semaphore per downstream, so a slow one degrades one feature rather than the service.
11. **Fail fast and shed load.** Bounded queue + `AbortPolicy` → 503. Accumulating work you'll never serve is worse than rejecting it.
12. **Measure before optimising.** async-profiler `-e alloc` for GC pressure, `-Xlog:gc*` + JFR always on, and a load test that reproduces the traffic shape. Fix the top three allocation sites, not the style of the whole codebase.

If you can deliver a version of this list in a conversation, you'll sound like someone who has run a service, which is the actual thing being assessed.
</details>

---

## The three sentences to have ready

If they ask one open question — "what do you think the concurrency risks of this migration are?" — this is the answer:

> The Java model was forgiving because every request owned a thread: blocking was safe,
> and `ThreadLocal` worked as ambient context. In Scala none of that holds — one blocking
> call on a cores-sized `ExecutionContext` can wedge the whole service at 5% CPU, and
> anything that relied on thread-confinement (`@Transactional`, MDC, `SecurityContext`)
> silently stops working rather than failing loudly. So the discipline I'd want is: two
> pools with blocking quarantined and bounded, context passed as values instead of
> thread-locals, and an explicit bound on every queue and every `traverse`. In exchange
> we get immutability by default, which deletes the shared-mutable-singleton bug class
> that causes most Java concurrency incidents.
