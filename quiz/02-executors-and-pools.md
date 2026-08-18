# Quiz 02 — Executors, Thread Pools & CompletableFuture

18 questions. This is the most *operationally* relevant file — pool misconfiguration
causes more production incidents than memory-model bugs. Target: 20 min.

---

### 1. Walk through `ThreadPoolExecutor`'s task-admission algorithm.

<details><summary>Answer</summary>

Given `corePoolSize`, `maximumPoolSize`, `keepAliveTime`, `workQueue`, `threadFactory`, `handler`, on `execute(task)`:

1. If `poolSize < corePoolSize` → **start a new thread** for this task. (Doesn't matter that idle threads exist.)
2. Else → **try to enqueue** into `workQueue`.
3. If the queue **rejects** (it's full / it's a `SynchronousQueue` with no waiting taker) and `poolSize < maximumPoolSize` → **start a new thread**.
4. Else → hand to the **`RejectedExecutionHandler`**.

The consequence that trips everyone up: **the queue is preferred over creating threads beyond core.** So if your queue is unbounded, step 2 always succeeds and **`maximumPoolSize` is dead configuration** — the pool never grows past core, no matter what number you put there.

Corollaries:
- `allowCoreThreadTimeOut(true)` lets core threads die when idle.
- `prestartAllCoreThreads()` avoids the cold-start latency cliff on the first N requests.
</details>

---

### 2. What's actually wrong with `Executors.newFixedThreadPool(n)` and `newCachedThreadPool()`?

<details><summary>Answer</summary>

`newFixedThreadPool(n)` → `new ThreadPoolExecutor(n, n, 0L, MS, new LinkedBlockingQueue<>())`. The queue is **unbounded (Integer.MAX_VALUE)**. Under overload you don't reject anything — you accumulate. Symptoms: heap grows, GC pressure rises, and **queueing delay dominates latency** while every dashboard says the pool is "healthy" (all threads busy, no rejections). Eventually `OutOfMemoryError`, or a queue full of requests whose clients timed out minutes ago — you're burning CPU on work nobody wants. Little's law is the framing: with no bound on L, W is unbounded.

`newCachedThreadPool()` → `new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60s, new SynchronousQueue<>())`. **Unbounded threads.** Each platform thread costs ~1 MB of stack reservation plus a kernel thread. Under a burst you get thousands of threads, then either `OutOfMemoryError: unable to create native thread` or a machine spending all its time in the scheduler.

`newSingleThreadExecutor()` — unbounded queue again, plus a single point of head-of-line blocking.

**What to do instead:** construct `ThreadPoolExecutor` explicitly with a **bounded queue** and a **deliberate rejection policy**. Bounded everything. Then export `getQueue().size()`, `getActiveCount()`, and the rejection count as metrics — a rising queue depth is your earliest overload signal.

(Also worth knowing: `Executors.newVirtualThreadPerTaskExecutor()` is *deliberately* unbounded in task count, which is fine because virtual threads are cheap — but it means you must bound concurrency at the resource, e.g. a `Semaphore` around the DB.)
</details>

---

### 3. Name the four built-in rejection policies and when each is right.

<details><summary>Answer</summary>

- **`AbortPolicy`** (default) — throws `RejectedExecutionException`. Correct for a server: fail fast, return 503, let the client back off. Make sure something catches it and maps it to a response rather than a 500.
- **`CallerRunsPolicy`** — the submitting thread executes the task itself. A crude but effective **backpressure** mechanism: the producer is throttled because it's busy doing the work. In a web server this blocks a request thread (or the acceptor), which slows intake — sometimes exactly right, sometimes a self-inflicted stall. Great for bounded batch pipelines.
- **`DiscardPolicy`** — silently drops. Almost always wrong; silent data loss. Acceptable for genuinely optional work (best-effort metrics emission).
- **`DiscardOldestPolicy`** — drops the head of the queue and retries. Reasonable for "only the freshest value matters" (a price tick, a UI refresh). Unsafe with FIFO-ordering assumptions.

Custom handlers are common: increment a metric, log at a sampled rate, then abort. Say that.
</details>

---

### 4. How do you size a thread pool?

<details><summary>Answer</summary>

Start from **what the threads are waiting on**.

**CPU-bound:** `threads ≈ N_cpu + 1`. The +1 keeps a core busy during a page fault. More threads only add context switches and cache pollution.

**I/O-bound:** the classic (Brian Goetz) formula
```
threads = N_cpu × targetUtilisation × (1 + W/C)
```
where `W` = wait time, `C` = compute time per task. So a task that spends 90 ms blocked and 10 ms computing on 8 cores at 100% target → `8 × 1 × (1 + 9) = 80` threads.

But then the real answer, which is what a senior interviewer is fishing for:

1. **The formula gives you a starting point, not an answer.** Measure — load-test and find the knee where throughput plateaus and latency climbs.
2. **The binding constraint is usually downstream, not local.** If the DB connection pool has 20 connections, 200 worker threads just means 180 threads queued on the pool — you've moved the queue, not raised throughput, and you've made latency harder to reason about. **Pool sizes should be aligned across tiers** (Tomcat max-threads ≥ worker threads ≥ ~connection pool size), and the *smallest* pool should be the one at the scarcest resource.
3. **Separate pools for separate concerns (bulkheads).** One pool per downstream dependency, so a slow payment provider can't consume every thread and take down checkout. This is the single highest-value structural point.
4. **Little's law** sanity check: `concurrency = throughput × latency`. 500 rps at 40 ms mean service time needs ~20 concurrent workers. If your pool is 400, you've mostly built a queue.
5. **With virtual threads the question changes** — don't size the threads, size the *resource* with a semaphore.
</details>

---

### 5. Which queue: `ArrayBlockingQueue`, `LinkedBlockingQueue`, `SynchronousQueue`, `PriorityBlockingQueue`, `LinkedTransferQueue`?

<details><summary>Answer</summary>

- **`ArrayBlockingQueue`** — bounded, single lock, pre-allocated ring buffer. Predictable footprint, no per-node garbage. The sane default for a bounded server pool.
- **`LinkedBlockingQueue`** — optionally bounded (unbounded by default — the trap in Q2). **Two locks** (separate head/tail), so higher throughput under simultaneous producer/consumer contention, at the cost of a node allocation per element.
- **`SynchronousQueue`** — capacity zero. A handoff: `put` blocks until a taker arrives. Combined with `maximumPoolSize = ∞` it gives you `newCachedThreadPool` semantics (thread-per-task). Use when you want *no* buffering.
- **`PriorityBlockingQueue`** — unbounded (!), heap-ordered. Enables priority scheduling but **starves low-priority tasks** and can't be bounded. Beware.
- **`LinkedTransferQueue`** — `transfer()` lets a producer block until a consumer *receives* the element; otherwise unbounded and very fast (used heavily inside newer j.u.c.). The most performant unbounded choice, which also makes it the most dangerous unbounded choice.
- **`DelayQueue`** — elements become available only after their delay elapses; the basis for scheduled execution.

Framing: choosing a queue is choosing **where and how you buffer, and whether you can push back**.
</details>

---

### 6. `ScheduledThreadPoolExecutor`: `scheduleAtFixedRate` vs `scheduleWithFixedDelay`, and what happens if a task throws?

<details><summary>Answer</summary>

- **`scheduleAtFixedRate(t, initial, period)`** — fires at `initial, initial+period, initial+2·period, …` measured from the start. If an execution overruns the period, subsequent ones run **late and back-to-back** to catch up (they don't run concurrently — a given periodic task is never run concurrently with itself). Use for "keep to a schedule": cron-like ticks.
- **`scheduleWithFixedDelay(t, initial, delay)`** — waits `delay` **after each completion**. Self-throttling; a slow run just pushes everything later. Use for polling loops and health checks. **This is the safer default.**

If the task throws an unchecked exception, **the task is silently cancelled and never runs again**. No log, no warning — the exception is captured in the (never-inspected) `ScheduledFuture`. Countless "our cleanup job just stopped working three weeks ago" incidents come from this. Always wrap the body in `try { … } catch (Throwable t) { log.error(…); }`.

Also: `ScheduledThreadPoolExecutor` uses an **unbounded** `DelayedWorkQueue`, so `maximumPoolSize` is ignored here too, and it's not clock-adjustment-proof (it uses `System.nanoTime`, which is fine — it's `Timer` that was worse: single-threaded, and one thrown exception killed all tasks).
</details>

---

### 7. `ForkJoinPool` — what's work stealing, and why can't you block in it?

<details><summary>Answer</summary>

Each worker has its own **double-ended queue**. It pushes/pops its own subtasks from one end (LIFO — good for locality, the freshest subtask is likely cache-hot), and idle workers **steal from the other end** (FIFO — the oldest task is likely the biggest, so a steal yields the most work per synchronisation). Result: near-zero contention on the common case, automatic load balancing, designed for **divide-and-conquer** with `RecursiveTask`/`RecursiveAction`.

Why blocking is toxic: the pool is sized to `Runtime.availableProcessors() - 1` (the **common pool**; parallelism = cores − 1 because the submitting thread helps). If workers block on I/O, the pool has no idea and won't compensate — parallelism collapses toward zero and unrelated tasks queue behind you. Because `parallelStream()` and (by default) `CompletableFuture.supplyAsync` both use the **common** pool, one library doing blocking I/O in a parallel stream can stall your entire application's async work. There is exactly one shared common pool per JVM.

If you must block inside FJP, the sanctioned escape hatch is **`ForkJoinPool.ManagedBlocker`**, which tells the pool to spin up a compensation thread. Scala's `scala.concurrent.blocking { … }` is precisely this mechanism, which is why it exists.

Better: **never block in a compute pool.** Have a separate, bounded, elastic pool for blocking I/O and pass it explicitly.
</details>

---

### 8. Predict the thread names printed.

```java
CompletableFuture
    .supplyAsync(() -> { p("supply"); return 1; })
    .thenApply(x -> { p("apply"); return x + 1; })
    .thenApplyAsync(x -> { p("applyAsync"); return x + 1; })
    .thenAccept(x -> p("accept"))
    .join();
// p(s) prints s + " on " + Thread.currentThread().getName()
```

<details><summary>Answer</summary>

- `supply` → **`ForkJoinPool.commonPool-worker-1`** (no executor given → common pool; note: if `availableProcessors() == 1`, common-pool parallelism is 0 and it silently falls back to a **new thread per task**).
- `apply` → whichever thread **completes** the previous stage. In practice the same common-pool worker, because the supplier is still running when `thenApply` is registered.
- `applyAsync` → **a common-pool worker**, resubmitted (possibly a different one).
- `accept` → the thread that completed `applyAsync`.

The rule to state: **non-`Async` methods run on whichever thread completes the previous stage — or on the *calling* thread if the stage is already complete when you attach the callback.** That non-determinism is the whole point of the question. It means a `thenApply` can run on your HTTP request thread, or on a Netty event loop, or on a common-pool worker, depending on a race. So:

- Never do blocking or long work in a non-`Async` stage — you don't know whose thread you're stealing. Stalling a Netty event loop or a `commonPool` worker is a whole-application problem.
- **Always pass an explicit `Executor`** to the `*Async` variants in production code. `thenApplyAsync(f, myExecutor)`.

Run `problems/java/P05CompletableFuture.java` to see it.
</details>

---

### 9. What's the difference between `thenApply`, `thenCompose`, and `thenCombine`?

<details><summary>Answer</summary>

- `thenApply(Function<T,U>)` → `CompletableFuture<U>` — **map**. Sync transform of the value.
- `thenCompose(Function<T,CompletionStage<U>>)` → `CompletableFuture<U>` — **flatMap**. Use when the function itself returns a future; `thenApply` would give you the nested `CompletableFuture<CompletableFuture<U>>`.
- `thenCombine(other, BiFunction)` → **zip** two independent futures, running them in parallel and combining results.

Plus: `allOf(...)` → `CompletableFuture<Void>` that completes when all do (you then collect results by joining each — they're all complete, so it doesn't block); `anyOf(...)` → first to complete, useful for hedging/timeouts; `orTimeout(d, unit)` and `completeOnTimeout(v, d, unit)` (JDK 9+).

The nesting mistake — `thenApply` where you meant `thenCompose` — is the single most common `CompletableFuture` bug, and it's the exact same map/flatMap distinction you'll need for Scala's `Future`, `IO`, and `ZIO`. Good bridge to mention.
</details>

---

### 10. How does exception handling work in `CompletableFuture`? What does `join()` throw vs `get()`?

<details><summary>Answer</summary>

An exception in a stage completes that future **exceptionally**; downstream `thenApply`/`thenAccept` stages are **skipped** and the failure propagates to the end. Handlers:

- `exceptionally(Throwable -> T)` — recover with a value. Only runs on failure.
- `handle((T, Throwable) -> U)` — runs on **both** paths; can transform either.
- `whenComplete((T, Throwable) -> void)` — side-effect on both paths, **passes the original result through** (does not recover).
- `exceptionallyCompose` (JDK 12+) — recover with another future.

Throwing behaviour:
- **`get()`** throws checked `ExecutionException` (cause wrapped) + `InterruptedException`.
- **`join()`** throws unchecked `CompletionException` (cause wrapped). No checked exceptions — that's why it's usable inside lambdas.

Either way **you must unwrap `getCause()`**, and the wrapping is inconsistent across the API — a genuine wart.

**The real trap:** if you never call `join`/`get`/`exceptionally`, an exception is **silently swallowed**. A fire-and-forget `runAsync(...)` that throws produces *no log line anywhere*. Always terminate a chain with `whenComplete` logging the throwable, or hand the future to something that observes it.
</details>

---

### 11. Where does `parallelStream()` run, and when is it a bad idea?

<details><summary>Answer</summary>

On the **`ForkJoinPool.commonPool()`** — shared JVM-wide, parallelism `cores − 1`. (The submitting thread also participates, so `n` cores' worth of work.)

Bad idea when:
- **The body blocks** (I/O, DB, HTTP, or a lock). You stall the common pool for everything else in the JVM, including other libraries' parallel streams and default `CompletableFuture` work.
- **In a web server under load.** You already have request-level parallelism from the N request threads; adding intra-request parallelism just multiplies contention and makes latency less predictable. Usually a throughput *loss*.
- **The source splits badly.** `LinkedList`, `Stream.iterate`, an unsized `Iterator`, or a `BufferedReader.lines()` can't be split evenly. Arrays, `ArrayList`, `IntStream.range`, and `HashMap` split well.
- **N is small or the per-element work is trivial.** Fork/join overhead dominates below roughly 10⁴ cheap elements.
- **The lambda is stateful or the collector isn't concurrent** — `forEach` gives no ordering, and mutating shared state is a race.

The "control the pool" trick: submit the stream terminal operation from inside your own `ForkJoinPool` — `myPool.submit(() -> list.parallelStream()...).get()`. It works (the stream uses the current FJP if it's on one) but it's undocumented behaviour, so mention it as a hack, not a design.
</details>

---

### 12. A request handler needs to call three independent services and combine results. Sketch it.

<details><summary>Answer</summary>

```java
var ex = blockingIoExecutor;                       // bounded, dedicated, named threads
var a = CompletableFuture.supplyAsync(() -> svcA.get(id), ex);
var b = CompletableFuture.supplyAsync(() -> svcB.get(id), ex);
var c = CompletableFuture.supplyAsync(() -> svcC.get(id), ex);

return a.thenCombine(b, Pair::of)
        .thenCombine(c, (ab, cv) -> merge(ab.a(), ab.b(), cv))
        .orTimeout(500, MILLISECONDS)
        .exceptionally(t -> { metrics.inc("fanout.fail"); return fallback(); });
```

Points to make unprompted:
- **Explicit executor**, not the common pool. Ideally one bulkhead pool *per* downstream so a slow C can't exhaust A's capacity.
- **Total timeout** on the combined future, and per-call timeouts in the clients themselves — `orTimeout` doesn't cancel the underlying blocking call, it only completes your future exceptionally. The thread is still stuck. That's a genuine limitation worth naming.
- **Partial failure policy**: fail the whole request, or degrade? Usually degrade — wrap each leg in its own `exceptionally(t -> Optional.empty())`.
- **Context propagation**: MDC/trace ids and `SecurityContext` are `ThreadLocal` and do **not** cross to `ex`. Use a `TaskDecorator` or explicit copying.

Then the punchline: **on JDK 21 you'd rather write this with structured concurrency** —
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var fa = scope.fork(() -> svcA.get(id));
    var fb = scope.fork(() -> svcB.get(id));
    var fc = scope.fork(() -> svcC.get(id));
    scope.joinUntil(deadline);  scope.throwIfFailed();
    return merge(fa.get(), fb.get(), fc.get());
}
```
Straight-line code, real cancellation (a failure interrupts the siblings), no pool to size, and the tree of tasks is visible in thread dumps. It's a preview API in 21 (`--enable-preview`), which you should say rather than pretend it's GA.
</details>

---

### 13. What is thread confinement, and where does Spring rely on it?

<details><summary>Answer</summary>

Thread confinement = state reachable by exactly one thread needs no synchronisation. Three flavours: **stack confinement** (locals — automatically safe), **`ThreadLocal`**, and **ad-hoc confinement** (by convention, e.g. Swing's EDT or Netty's event loop).

Spring MVC's model is **thread-per-request**: the servlet container assigns one thread per request for its whole lifetime, and Spring hangs request-scoped state off it via `ThreadLocal` — `RequestContextHolder`, `SecurityContextHolder`, `TransactionSynchronizationManager` (which holds the JDBC `Connection` bound to `@Transactional`), and SLF4J `MDC`.

Everything breaks the moment work leaves that thread:
- **`@Transactional` does not propagate** to a `@Async` method or a `CompletableFuture` — the new thread has no bound connection, so it silently runs outside the transaction (or opens a second, independent one). Rollback of the outer transaction won't undo it.
- **`MDC` is empty** on the new thread, so log correlation dies mid-request.
- **`SecurityContext` is empty** → authorisation checks fail, or (with a pooled thread) see the *previous* request's principal.
- `@RequestScope` bean lookup throws.

The migration angle, and why they're asking: **Scala's `Future`, Akka actors, Cats Effect fibers, and ZIO all break thread confinement by design.** A direct port that keeps `@Transactional` + `ThreadLocal` context while making the body async is subtly, silently broken. The Scala answer is to pass context **explicitly** — a `Reader`-ish parameter, `FiberRef`/`IOLocal`, or ZIO's `R` environment — rather than smuggling it through the thread.
</details>

---

### 14. What does `shutdown()` vs `shutdownNow()` do, and what's the correct shutdown sequence?

<details><summary>Answer</summary>

- **`shutdown()`** — graceful. Stops accepting new tasks (subsequent submits are rejected); already-queued tasks still run.
- **`shutdownNow()`** — stops accepting, **drains the queue and returns the unrun tasks**, and **interrupts** the running threads. Tasks that ignore interrupts keep going.
- **`awaitTermination(t, u)`** — blocks until done/timeout. Neither shutdown method waits; both return immediately.

Canonical sequence:
```java
pool.shutdown();
try {
    if (!pool.awaitTermination(30, SECONDS)) {
        var dropped = pool.shutdownNow();
        log.warn("forcing shutdown, {} tasks dropped", dropped.size());
        if (!pool.awaitTermination(10, SECONDS)) log.error("pool did not terminate");
    }
} catch (InterruptedException e) {
    pool.shutdownNow();
    Thread.currentThread().interrupt();
}
```

Operationally: a pool whose threads are **non-daemon** keeps the JVM alive after `main` returns — a common "why won't my app exit" bug. In Spring, prefer `ThreadPoolTaskExecutor` with `setWaitForTasksToCompleteOnShutdown(true)` and `setAwaitTerminationSeconds(n)`, and make sure it's shorter than your orchestrator's `terminationGracePeriodSeconds` or Kubernetes SIGKILLs you mid-drain.
</details>

---

### 15. Your service's p99 latency is 3 s but p50 is 20 ms, CPU is at 30%. Where do you look?

<details><summary>Answer</summary>

Low CPU + terrible tail = **waiting**, not computing. Work through:

1. **Queueing at a bounded resource.** Connection pool exhaustion is the number-one cause: `HikariCP` `connectionTimeout` waits show up as exactly this shape. Check `hikaricp_connections_pending`, thread-pool queue depth, and per-pool active counts.
2. **GC pauses.** Check `-Xlog:gc` for pauses in the seconds range, and note that GC *cause* matters (a full GC from allocation failure vs a humongous allocation). Also check **time-to-safepoint** (`-Xlog:safepoint`) — a long TTSP looks like a GC pause but isn't one.
3. **Lock contention / head-of-line blocking.** A `synchronized` singleton method, a hot `ReentrantLock`, or a single-threaded executor with a deep queue. Two `jstack`s 5 s apart, or async-profiler in `-e lock` mode.
4. **A slow downstream** (p99 of *their* latency becomes yours), plus retries amplifying it. Look for retry storms; check whether your timeouts are longer than the client's.
5. **Coordinated omission in the measurement itself** — if your load generator waits for a response before sending the next request, it under-reports the tail. Worth naming; interviewers love it.
6. **Environmental**: CPU throttling under a container quota (`container_cpu_cfs_throttled_seconds` — a 100 ms period quota can pause you even at 30% average utilisation), noisy neighbours, DNS lookups, TLS handshakes, page cache misses.

Method to state: **USE (utilisation/saturation/errors) per resource.** With CPU at 30%, look at *saturation* of every queue in the path. Tail latency is a queueing problem until proven otherwise.
</details>

---

### 16. What's the difference between concurrency and parallelism?

<details><summary>Answer</summary>

**Concurrency** is a structuring property: multiple tasks are *in progress* over overlapping periods, so the program must deal with interleaving. Achievable on one core.

**Parallelism** is an execution property: multiple tasks *literally executing* at the same instant, which requires multiple cores.

Rob Pike's line: "Concurrency is about dealing with lots of things at once; parallelism is about doing lots of things at once." Concurrency is about **composition and correctness**; parallelism is about **throughput**.

Why it matters here: a web server is a **concurrency** problem (10 000 mostly-waiting connections), which is why virtual threads and async I/O help enormously and adding cores helps little. A batch aggregation is a **parallelism** problem, which is what `parallelStream` and fork/join are for. Amdahl's law bounds the parallel case; Little's law bounds the concurrent one.
</details>

---

### 17. Is `HashMap` safe if it's only ever read after being fully populated by one thread?

<details><summary>Answer</summary>

**Only if it's safely published.** Populate it, then hand it over through a happens-before edge (store to a `final` or `volatile` field, put it in a concurrent collection, pass via `Future.get()`) and never mutate it again. Then unsynchronised concurrent reads are correct and fast — this is a standard, legitimate optimisation for lookup tables and config.

Without that edge, readers may see a partially-built table: missing entries, or in the worst case an **infinite loop** in `get()`. (The classic 100%-CPU `HashMap.get` hang came from a concurrent-resize cycle in the Java 7 linked-list implementation. Java 8+ restructured resizing and uses trees for large bins, so the infinite loop is gone — but concurrent modification is still undefined behaviour, and you can still lose entries or see a stale table.) Say "Java 8 removed *that particular* infinite loop, it did not make `HashMap` thread-safe."

Best practice: make it unambiguous with `Map.copyOf(m)` / `Collections.unmodifiableMap` in a `final` field, so nobody can mutate it later. In Scala this is just the default — an immutable `Map` in a `val`.
</details>

---

### 18. Design: a rate limiter allowing 100 requests/second, shared across a pool of worker threads.

<details><summary>Answer</summary>

Talk through the options; the interviewer wants the trade-offs, not one answer.

**Fixed window** — an `AtomicInteger` reset every second. Trivial, but allows a 2× burst at the window boundary (100 at t=0.99 s, 100 at t=1.01 s).

**Sliding window log** — keep timestamps, evict older than 1 s. Exact, O(n) memory per key.

**Token bucket** (usually the right answer) — refill at 100 tokens/s up to a burst capacity; each request takes a token. Smooth, allows a controlled burst, cheap and lock-free:

```java
class TokenBucket {
    private final long capacity, refillPerNano;   // scaled fixed-point
    private final AtomicLong state;               // packs tokens + lastRefillNanos
    boolean tryAcquire() {
        while (true) {
            long s = state.get();
            long now = System.nanoTime();
            long tokens = Math.min(capacity, tokensOf(s) + (now - timeOf(s)) * refillPerNano);
            if (tokens < 1) return false;
            if (state.compareAndSet(s, pack(tokens - 1, now))) return true;
        }
    }
}
```
Single CAS on the happy path. If contention on that one word becomes the bottleneck, **shard** the limiter (N buckets of rate/N, pick by thread probe) — trading exactness for scalability, same idea as `LongAdder`.

**Leaky bucket** — a queue drained at a constant rate. Gives you *shaping* (delay) rather than *rejection*, plus natural backpressure.

Then the systems answer: in a service with M instances, a local limiter enforces `M × 100`. **Distributed limiting needs shared state** — Redis `INCR` with a TTL, or a token lease from a central service, both of which add a network hop to the request path and a failure mode ("fail open or fail closed?"). Common compromise: local token buckets with a periodically rebalanced share of global quota.

Also: `Semaphore` limits **concurrency**, not **rate** — different thing. Say so, since people conflate them. And name-drop Guava's `RateLimiter` / Resilience4j / Bucket4j rather than reinventing it.
</details>

---

## Score yourself

- **14+** — good. Q4, Q13, Q15 are the ones to be able to riff on for five minutes.
- **<10** — Q1, Q2, Q7, Q8 are the mechanical core. Run `problems/java/P05` and `P09`.
