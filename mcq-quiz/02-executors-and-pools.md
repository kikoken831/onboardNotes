# MCQ 02 — Executors, Thread Pools & CompletableFuture

16 questions. Source: [`quiz/02`](../quiz/02-executors-and-pools.md).

---

### Q1. In `ThreadPoolExecutor`, when does the pool create a thread beyond `corePoolSize`?

- **A.** As soon as all core threads are busy
- **B.** When the queue is more than half full
- **C.** Only when the work queue rejects the task and `poolSize < maximumPoolSize`
- **D.** Never — `maximumPoolSize` only applies to `newCachedThreadPool`

<details><summary>Answer</summary>

**C.** The admission order is: below core → new thread; otherwise → **try to enqueue**; only if the
queue *refuses* → new thread up to max; else → rejection handler.

**A** is the intuitive-but-wrong model, and getting it right is what explains the next question:
because the queue is preferred over new threads, **an unbounded queue makes `maximumPoolSize` dead
configuration** — the pool never grows past core.

→ `quiz/02` Q1
</details>

---

### Q2. What's the specific danger of `Executors.newFixedThreadPool(n)`?

- **A.** Its queue is unbounded, so overload becomes memory growth and unbounded queueing delay rather than backpressure
- **B.** It creates unbounded threads under load
- **C.** Core threads time out and die, causing latency spikes
- **D.** It uses a `SynchronousQueue`, so tasks are rejected immediately

<details><summary>Answer</summary>

**A.** It uses a `LinkedBlockingQueue` with `Integer.MAX_VALUE` capacity. Nothing is ever
rejected — you accumulate. Symptoms: heap grows, GC pressure rises, latency is dominated by
queueing, and **every dashboard says the pool is healthy** (all threads busy, zero rejections).
Eventually OOM, or you burn CPU on requests whose clients timed out minutes ago.

- **B** describes `newCachedThreadPool` (unbounded *threads*) — also dangerous, different failure.
- **D** describes `newCachedThreadPool`'s queue.

→ `quiz/02` Q2
</details>

---

### Q3. Which rejection policy gives you crude backpressure by throttling the producer?

- **A.** `AbortPolicy`
- **B.** `DiscardPolicy`
- **C.** `DiscardOldestPolicy`
- **D.** `CallerRunsPolicy`

<details><summary>Answer</summary>

**D.** The submitting thread runs the task itself, so it's too busy to submit more — the producer
is throttled by construction. Great for bounded batch pipelines; in a web server it blocks a
request thread, which is sometimes right and sometimes a self-inflicted stall.

- **A** (the default) fails fast — correct for a server, mapped to a 503.
- **B** silently drops — almost always wrong.
- **C** drops the head — only for "freshest value wins" data.

→ `quiz/02` Q3
</details>

---

### Q4. For an I/O-bound task spending 90 ms blocked and 10 ms computing, on 8 cores at full target utilisation, the classic formula suggests roughly how many threads?

- **A.** 9
- **B.** 80
- **C.** 16
- **D.** 800

<details><summary>Answer</summary>

**B.** `N_cpu × utilisation × (1 + W/C)` = `8 × 1 × (1 + 9)` = **80**.

But the formula is a starting point, not an answer. What matters more: the **binding constraint is
usually downstream** (200 workers against a 20-connection DB pool just moves the queue somewhere
less visible); **one pool per dependency** (bulkheads); and a **bounded queue with a real rejection
policy**. Then load-test for the knee.

→ `quiz/02` Q4
</details>

---

### Q5. Which queue is bounded, single-locked, and pre-allocates its storage?

- **A.** `ArrayBlockingQueue`
- **B.** `LinkedBlockingQueue`
- **C.** `SynchronousQueue`
- **D.** `PriorityBlockingQueue`

<details><summary>Answer</summary>

**A.** A pre-allocated ring buffer, one lock, predictable footprint, no per-node garbage. The sane
default for a bounded server pool.

- **B** is optionally bounded (**unbounded by default** — the trap) and uses two locks, so higher
  throughput under simultaneous producer/consumer contention at the cost of a node allocation per
  element.
- **C** has capacity zero — a direct handoff.
- **D** is **unbounded** and starves low-priority tasks.

→ `quiz/02` Q5
</details>

---

### Q6. A task submitted with `scheduleAtFixedRate` throws an unchecked exception. What happens?

- **A.** It's logged and the schedule continues
- **B.** The whole executor shuts down
- **C.** The task is silently cancelled and never runs again
- **D.** The task retries immediately

<details><summary>Answer</summary>

**C.** No log, no warning — the exception is captured in a `ScheduledFuture` nobody inspects.
Countless "our cleanup job just stopped working three weeks ago" incidents come from exactly
this. Always wrap the body in `try { … } catch (Throwable t) { log.error(…); }`.

Related: prefer **`scheduleWithFixedDelay`** as the default — it waits after each *completion*, so
it self-throttles instead of running late and back-to-back to catch up.

→ `quiz/02` Q6
</details>

---

### Q7. Why is blocking inside a `ForkJoinPool` toxic?

- **A.** `ForkJoinPool` throws an exception if a worker blocks
- **B.** The pool is sized to cores and doesn't know you've blocked, so parallelism collapses — and the common pool is shared JVM-wide
- **C.** Work stealing requires all tasks to be non-blocking to remain correct
- **D.** Blocked workers spin, burning CPU

<details><summary>Answer</summary>

**B.** Parallelism is `cores − 1` on the common pool. Blocked workers aren't compensated for, so
throughput collapses toward zero — and because `parallelStream()` and default
`CompletableFuture.supplyAsync` both use the **common** pool, one library blocking in a parallel
stream can stall the whole application's async work.

The sanctioned escape hatch is `ForkJoinPool.ManagedBlocker`, which is exactly what Scala's
`scala.concurrent.blocking { }` uses. Better: never block in a compute pool.

→ `quiz/02` Q7
</details>

---

### Q8. Where does a non-`Async` stage like `thenApply` execute?

- **A.** Always on the common pool
- **B.** Always on the thread that created the future
- **C.** On a dedicated `CompletableFuture` internal thread
- **D.** On whichever thread completes the previous stage — or on the calling thread if that stage is already complete

<details><summary>Answer</summary>

**D.** You do not control it. It may run on a common-pool worker, a Netty event loop, or your HTTP
request thread, depending on a race. Verified: attaching to an already-completed future ran the
callback on **`main`**.

Consequences: never block or do long work in a non-`Async` stage, and **always pass an explicit
`Executor`** to the `*Async` variants in production.

→ `quiz/02` Q8, measured in `problems/java/P05CompletableFuture.java` Parts 1–2
</details>

---

### Q9. Your function returns `CompletableFuture<U>`. Which combinator avoids a nested future?

- **A.** `thenApply`
- **B.** `thenCombine`
- **C.** `thenCompose`
- **D.** `thenAccept`

<details><summary>Answer</summary>

**C.** `thenCompose` is **flatMap** — it flattens. `thenApply` is map, so it gives you
`CompletableFuture<CompletableFuture<U>>` and you must join twice.

`thenCombine` is **zip** (two independent futures in parallel); `thenAccept` consumes and returns
`Void`.

Using `thenApply` where you meant `thenCompose` is the most common `CompletableFuture` bug — and
it's the exact map/flatMap distinction you need for Scala's `Future`, `IO` and `ZIO`.

→ `quiz/02` Q9, verified in `P05` Part 5
</details>

---

### Q10. What happens to an exception in `CompletableFuture.runAsync(() -> { throw new IllegalStateException(); })` if nothing observes the future?

- **A.** It's silently swallowed — no log, no stack trace anywhere
- **B.** It's printed by the default `UncaughtExceptionHandler`
- **C.** It's rethrown on the calling thread
- **D.** The common pool shuts down

<details><summary>Answer</summary>

**A.** Verified: **no output at all.** The throwable is stored in a future nobody inspects.
Contrast a plain `Thread`, whose uncaught exception *does* reach the default handler and print —
which is what makes this so surprising.

Minimum fix: terminate every chain with
`whenComplete((v, t) -> { if (t != null) log.error("...", t); })`.

Also: `join()` wraps in `CompletionException`, `get()` in `ExecutionException` — different
wrappers for the same failure, so always unwrap `getCause()`.

→ `quiz/02` Q10, measured in `P05` Part 4
</details>

---

### Q11. Which is **not** a good reason to avoid `parallelStream()`?

- **A.** The lambda body performs blocking I/O
- **B.** The source is a `LinkedList` or an unsized iterator
- **C.** You're already in a web server handling concurrent requests
- **D.** The collection has 10 million elements and the per-element work is expensive

<details><summary>Answer</summary>

**D.** That's the case where `parallelStream` genuinely *helps* — a large, well-splittable source
with real per-element work.

- **A** stalls the shared common pool for the whole JVM.
- **B** can't split evenly, so you get no benefit and pay the overhead.
- **C** already has request-level parallelism; adding intra-request parallelism usually costs
  throughput and makes latency less predictable.

→ `quiz/02` Q11
</details>

---

### Q12. `orTimeout(300, MILLISECONDS)` fires. What happened to the underlying work?

- **A.** It was cancelled and its thread interrupted
- **B.** It ran to completion, still holding whatever resources it held
- **C.** It was cancelled but the thread finished its current statement
- **D.** It was moved to a lower-priority queue

<details><summary>Answer</summary>

**B.** Verified: the timeout returned a fallback at 300 ms; the task completed **1.2 seconds
later**. `orTimeout` only completes *your* future exceptionally — and `CompletableFuture.cancel`
doesn't interrupt the work either.

So every timeout **leaks the capacity** of the in-flight work, including its DB connection. This
is the sharpest argument for fiber-based cancellation in Cats Effect/ZIO, where `timeout` really
does interrupt and run finalizers.

→ `quiz/02` Q12, measured in `P05` Part 6
</details>

---

### Q13. Which Spring feature does **not** break when work moves to another thread?

- **A.** Constructor-injected `final` dependencies
- **B.** SLF4J `MDC`
- **C.** `SecurityContextHolder`
- **D.** `@Transactional`

<details><summary>Answer</summary>

**A.** Those are just fields on the object — they travel with it, safely published by final-field
semantics.

**D**, **B** and **C** are all `ThreadLocal`-backed and all break. `@Transactional` is the worst because it
fails **silently**: the proxy commits before the async work happens and never sees the failure, so
there's no rollback.

→ `quiz/02` Q13, `quiz/07` Q2
</details>

---

### Q14. What's the correct executor shutdown sequence?

- **A.** `shutdownNow()`, then `awaitTermination`
- **B.** `awaitTermination`, then `shutdown()`
- **C.** `shutdown()`, `awaitTermination(timeout)`, then `shutdownNow()` if it didn't finish
- **D.** `shutdown()` alone — it blocks until tasks complete

<details><summary>Answer</summary>

**C.** Graceful first (stop accepting, let queued tasks run), wait with a bound, then force.

- **D** is a common misconception: **neither shutdown method waits.** Both return immediately;
  `awaitTermination` is what blocks.
- **A** skips the graceful phase and interrupts running work unnecessarily.

Operational note: non-daemon pool threads keep the JVM alive after `main` returns — a frequent
"why won't my app exit" bug. And make your grace period shorter than Kubernetes'
`terminationGracePeriodSeconds`.

→ `quiz/02` Q14
</details>

---

### Q15. p99 is 3 s, p50 is 20 ms, CPU is 30%. What's the most likely cause?

- **A.** CPU-bound computation in the hot path
- **B.** Queueing at a bounded resource — most often connection-pool exhaustion
- **C.** An inefficient algorithm with poor constant factors
- **D.** Insufficient heap causing constant minor GC

<details><summary>Answer</summary>

**B.** Low CPU with a terrible tail means **waiting**, not computing. Connection-pool exhaustion is
the number-one cause and shows exactly this shape — check `hikaricp_connections_pending`, executor
queue depth, and per-pool active counts.

Then: GC pauses, **time-to-safepoint** (invisible in GC pause times), lock contention, a slow
downstream with retry amplification, and CFS throttling in a container.

- **A**/**C** would show high CPU. **D** would show up in GC logs and also burn CPU.

Method to state: **USE — utilisation/saturation/errors per resource.** At 30% CPU, look at
*saturation* of every queue in the path.

→ `quiz/02` Q15
</details>

---

### Q16. `Semaphore` vs a token bucket — what's the distinction?

- **A.** They're equivalent; a semaphore is just a simpler token bucket
- **B.** A semaphore is for threads, a token bucket for processes
- **C.** A semaphore limits rate; a token bucket limits concurrency
- **D.** A semaphore limits concurrency (how many at once); a token bucket limits rate (how many per second)

<details><summary>Answer</summary>

**D.** Ten permits means ten in flight — which could be 10 requests/sec or 10,000/sec depending on
duration. A token bucket refills at a fixed rate and caps burst, which is a different constraint.

People conflate these constantly, and choosing wrong means you either fail to protect the
downstream or throttle it unnecessarily. Often you want **both**: a semaphore for the connection
limit, a rate limiter for the API quota.

→ `quiz/02` Q16, Q18; both implemented and verified in `problems/java/P09Exercises.java`
</details>

---

## Score yourself

- **13+** — good.
- **<10** — Q1, Q2, Q7, Q8 are the mechanical core; reread those and run
  `problems/java/P05CompletableFuture.java`.

**Q4, Q13 and Q15 are the ones to be able to riff on for five minutes** — they're where the
interview goes after the factual answer.
