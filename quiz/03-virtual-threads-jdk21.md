# Quiz 03 — Virtual Threads & Structured Concurrency (JDK 21)

Your local JDK is 21, where virtual threads are **GA** (JEP 444). If the new Scala
service targets 21+, this is a likely discussion topic — especially the question
"do we still need async/reactive?" Target: 12 min.

---

### 1. What is a virtual thread, and how does it differ from a platform thread?

<details><summary>Answer</summary>

A **platform thread** is a thin wrapper over an OS thread: ~1 MB of reserved stack, kernel-scheduled, expensive to create (~microseconds to milliseconds), and you can have thousands, not millions.

A **virtual thread** is a `java.lang.Thread` scheduled by the **JVM**, not the OS. Its stack lives on the heap as a *continuation* (a chunk that grows and shrinks), it costs a few hundred bytes to a few KB, and creation is roughly an object allocation. You can have millions.

Virtual threads run by being **mounted** onto a **carrier** (a platform thread from a dedicated `ForkJoinPool`, default parallelism = `availableProcessors()`). When the virtual thread blocks on something the JDK has instrumented, its continuation is **unmounted** — the stack is copied to the heap and the carrier is freed to run another virtual thread. When the blocking operation completes it's **remounted**, possibly on a different carrier.

The point: `Thread.sleep`, socket reads, and most blocking JDK calls no longer waste an OS thread. **Blocking code becomes scalable again**, so you can write straight-line, debuggable, stack-trace-friendly code and still handle 10⁵–10⁶ concurrent requests.

They're always daemon threads, they have no thread priority, they're not in a `ThreadGroup` in any useful way, and pooling them is an anti-pattern — create one per task.
</details>

---

### 2. What is pinning? What causes it on JDK 21?

<details><summary>Answer</summary>

**Pinning** = a virtual thread blocks while it *cannot* be unmounted, so it holds its carrier hostage. With a default carrier pool of N = cores, N pinned-and-blocked virtual threads deadlock or serialise the entire application.

On **JDK 21** two things pin:
1. **Blocking inside a `synchronized` block or method** — the monitor is tied to the carrier, so the VM can't unmount. This is the big one.
2. **Blocking inside a native frame / JNI call.**

(`Object.wait()` also pinned on 21.)

Mitigations on 21:
- Replace `synchronized` with **`ReentrantLock`** on any path that blocks — `ReentrantLock` is virtual-thread-aware and unmounts cleanly.
- `-Djdk.tracePinnedThreads=full|short` prints a stack trace when a pinned thread blocks. **This is the diagnostic to name.**
- The carrier pool *does* compensate for pinning in some cases (it can temporarily expand, tunable via `jdk.virtualThreadScheduler.maxPoolSize`), but don't rely on it.

Say the version history, because it shows currency: **JEP 491 in JDK 24 made `synchronized` unpin**, so on 24+ monitors no longer pin and `tracePinnedThreads` is obsolete. On 21 you still have to care. Native frames still pin on every version.
</details>

---

### 3. If blocking is cheap now, do you still need a thread pool?

<details><summary>Answer</summary>

You stop needing pools **to limit thread cost**, but you still need to **limit concurrency at scarce resources**.

- Don't pool virtual threads. `Executors.newVirtualThreadPerTaskExecutor()` creates one per task; that's correct. Pooling them reintroduces `ThreadLocal` bleed and queueing for no benefit.
- Do bound access to anything finite: a **`Semaphore`** in front of the database, the downstream API, the file handles. 10 000 virtual threads all trying to grab one of 20 DB connections is fine (they just park cheaply) — but 10 000 all hammering a downstream service that tolerates 200 is a self-inflicted DoS.

The shift in mental model: **you no longer size the thread pool, you size the resource.** A semaphore is the honest expression of "this dependency supports 50 concurrent calls" — whereas a 50-thread pool conflated that limit with "50 units of execution capacity."

CPU-bound work is the exception: for that, a **fixed platform-thread pool sized to cores** is still right. Virtual threads solve waiting, not computing.
</details>

---

### 4. Do virtual threads make reactive programming obsolete?

<details><summary>Answer</summary>

Nuanced answer, which is what's being tested.

They **remove the main reason people adopted reactive**: thread-per-request scalability. You get the concurrency of async with the readability, debuggability (real stack traces!), and profiler-friendliness of blocking code. For a typical request/response service that fans out to a DB and a few HTTP calls, virtual threads + straight-line code beat reactive on maintainability at equivalent throughput. A lot of WebFlux adoption was tax paid for scalability that is now free.

What reactive still gives you that virtual threads do not:
- **Backpressure as a first-class protocol** (Reactive Streams `request(n)`). Virtual threads have no equivalent; you get blocking as implicit backpressure, which is coarser.
- **Streaming and time-based operators** — `window`, `buffer`, `debounce`, `zipWithLatest`, merging infinite streams. Modelling a stream of events is genuinely different from modelling a request.
- **Declarative composition** of retry/timeout/circuit-breaking over an async graph.

So: **request/response → virtual threads. Streaming/event-processing with flow control → reactive (or fs2/ZIO Streams on the Scala side).**

The Scala-specific point worth making: Cats Effect and ZIO were *already* doing user-space green threads (fibers) with a work-stealing runtime, plus cancellation and resource safety that the JDK still doesn't match. Virtual threads are less of a revolution there — they're an alternative backend (both runtimes can run blocking work on virtual threads via `Blocking`/`ZIO.blocking`).
</details>

---

### 5. How do you create and use virtual threads? Three ways.

<details><summary>Answer</summary>

```java
// 1. One-off
Thread t = Thread.ofVirtual().name("worker-1").start(() -> work());
t.join();

// 2. Unstarted, for a factory
ThreadFactory f = Thread.ofVirtual().name("req-", 0).factory();

// 3. Executor — the usual production shape
try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Result>> futures = ex.invokeAll(tasks);   // AutoCloseable: close() awaits all
}
```

`ExecutorService` is `AutoCloseable` as of JDK 19, so try-with-resources gives you "submit everything, wait for all" without a latch. Note `newVirtualThreadPerTaskExecutor()` is *not* a pool — it's a task-to-thread factory with no queue and no bound.

Checks: `Thread.isVirtual()`. In a thread dump, `jcmd <pid> Thread.dump_to_file -format=json <file>` includes virtual threads and their structured-concurrency nesting; plain `jstack` does not show them.
</details>

---

### 6. What is structured concurrency and what problem does it solve?

<details><summary>Answer</summary>

The rule: **if a task splits into concurrent subtasks, they all complete before the enclosing block exits** — concurrency follows the lexical block structure the way `try`/`finally` does for resources.

The problems it fixes in unstructured concurrency:
- **Leaks** — you submit to a pool and the task outlives the request; nothing owns it.
- **No cancellation propagation** — one leg fails, the other four keep burning capacity and money for a response that will be discarded.
- **Lost stack context** — the pool worker's stack trace tells you nothing about who submitted the work, so failures are unattributable.
- Hand-rolled `allOf` + timeout + cancel logic that's subtly wrong in every codebase.

JDK 21 (preview, `--enable-preview`, JEP 453):
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<User>  u = scope.fork(() -> fetchUser(id));
    Subtask<Order> o = scope.fork(() -> fetchOrders(id));
    scope.joinUntil(Instant.now().plusMillis(300));
    scope.throwIfFailed(HttpException::new);
    return new Page(u.get(), o.get());
}
```
`ShutdownOnFailure` = "all must succeed" (interrupts siblings on first error); `ShutdownOnSuccess` = "first wins" (hedged requests). The scope's `close()` guarantees no subtask outlives the block.

Say it's **preview in 21** and be specific about what changed, because this is a place where 2023-era
prep material is now actively wrong: the `new StructuredTaskScope.ShutdownOnFailure()` /
`ShutdownOnSuccess` constructors above are the **JDK 21 preview** shape and **do not exist in JDK 25+**.
They were replaced by static `StructuredTaskScope.open(...)` factories plus a **`Joiner`** policy
(`Joiner.allSuccessfulOrThrow()`, `anySuccessfulOrThrow()`), all `@since 25`, with more added in 26 and
27 (`Joiner.timeout()`). And as of **August 2026 it is still a preview API** — every public type carries
`@PreviewFeature`, so it still needs `--enable-preview` and can still change. Virtual threads themselves
are **final since 21** (JEP 444); keep the two claims separate.

Cats Effect's `parTupled`/`Supervisor` and ZIO's fiber-scoping give you this already — ZIO in particular
has had structured concurrency since day one, which is a nice bridge to make.
</details>

---

### 7. What replaces `ThreadLocal` for virtual threads, and why?

<details><summary>Answer</summary>

**`ScopedValue`** (JEP 446, preview in 21).

Why `ThreadLocal` is a poor fit: it's **mutable** (anyone can `set` anywhere, so the value's provenance is untraceable), **unbounded in lifetime** (you must remember to `remove()`), and **expensive at scale** — a million virtual threads each with a `ThreadLocalMap` is real footprint, and inheritable thread-locals get *copied* per child thread.

`ScopedValue` is immutable and lexically scoped:
```java
static final ScopedValue<Principal> USER = ScopedValue.newInstance();

ScopedValue.where(USER, principal).run(() -> handle(request));   // bound only in here
// deeper down:
Principal p = USER.get();
```
The binding is visible to that block and everything it calls, including subtasks forked in a `StructuredTaskScope` — **inheritance is by structure, not by copying** — and it's automatically unbound when the block exits. No leak, no bleed, no `finally`, cheap for children.

Both preview in 21. The practical answer for production on 21 is still `ThreadLocal` with disciplined `remove()`, or explicit parameter passing. Which is, notably, what idiomatic Scala does anyway: pass the context as a value (`Reader`, ZIO's `R`, or just an argument). Good place to note that the JDK is converging on what the functional Scala ecosystem already does.
</details>

---

### 8. Predict: how long does this take, and how many OS threads does it use?

```java
try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++)
        ex.submit(() -> { Thread.sleep(1000); return null; });
}
```
And with `Executors.newFixedThreadPool(200)`?

<details><summary>Answer</summary>

**Virtual:** ~1 second total. 10 000 virtual threads all sleep concurrently; each unmounts immediately, so the carrier pool (≈ number of cores) is nearly idle. OS threads used: roughly `availableProcessors()` carriers plus JVM housekeeping. Memory: a few MB.

**Fixed(200):** 10 000 / 200 = **50 batches × 1 s ≈ 50 seconds**, using 200 OS threads and ~200 MB of reserved stack. The pool is the bottleneck, and 200 threads sit blocked doing nothing.

This is the whole value proposition in one measurement, and it's exactly the "10 000 concurrent requests each waiting on a downstream" shape of a real service. `problems/java/P10VirtualThreads.java` runs both and times them.

Follow-up to be ready for: **swap `Thread.sleep(1000)` for a 1-second busy loop and the two are identical** (both bounded by cores), because virtual threads add zero parallelism. And swap it for `synchronized { Thread.sleep(1000); }` on JDK 21 and the virtual version collapses to ~`10_000/cores` seconds — pinning.
</details>

---

### 9. What breaks or needs care when adopting virtual threads in an existing Spring Boot app?

<details><summary>Answer</summary>

- **`spring.threads.virtual.enabled=true`** (Boot 3.2+) switches Tomcat's request executor and `@Async`/scheduling to virtual threads. That's the easy part.
- **`synchronized` on blocking paths** anywhere in your code *or your dependencies* → pinning on JDK 21. Old JDBC drivers, connection pools, and logging appenders are frequent offenders. Run with `-Djdk.tracePinnedThreads=full` in staging.
- **Connection pool becomes the real limit.** Removing the 200-thread ceiling means 10 000 requests now queue at HikariCP instead. You must set an explicit bound and tune `connectionTimeout`, or you convert a thread-starvation failure into a connection-timeout failure. Nothing got faster by itself.
- **You've removed your accidental admission control.** The old 200-thread pool was rejecting/queueing load implicitly. Now you accept everything and can overload downstreams. Add explicit rate limits / semaphores / bulkheads.
- **`ThreadLocal`-based context still works** (each request has its own thread — actually *cleaner* than pooling), but **thread-local caching as an optimisation stops working** — a per-thread `SimpleDateFormat` or buffer cache now allocates per request. Look for `ThreadLocal` used as a pooling trick.
- **Monitoring** — thread-count metrics and `jstack` become useless/misleading; you need `Thread.dump_to_file`. Micrometer's thread metrics won't show virtual threads.
- **Logging**: synchronous appenders with locks are a pinning and contention hotspot at high concurrency.
- **Don't** run CPU-bound work on virtual threads, and don't pool them.
</details>

---

## Score yourself

Q2 (pinning), Q3 (semaphore not pool), and Q4 (the reactive question) are the three
that separate "read a blog post" from "thought about it." Be able to give the JDK 21
vs 24 pinning history — it's a cheap credibility signal.
