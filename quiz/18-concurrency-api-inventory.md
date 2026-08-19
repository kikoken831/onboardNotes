# Quiz 18 — `java.util.concurrent` API Inventory

28 questions. The source list devotes roughly 60 entries to individual `java.util.concurrent` classes
and methods — including **14 separate questions on `CompletableFuture` methods**. Those are grouped
here by family, because "what is `thenAccept`" fourteen times tests recall, not understanding.

**The *why* lives elsewhere:** pool sizing and queue choice in `quiz/02`, the memory model and
synchroniser selection in `quiz/01`. This file is the API surface.

Target: 30 min.

---

## A. Tasks, futures and completion

### 1. `Runnable` vs `Callable` vs `Future`.

<details><summary>Answer</summary>

| | Method | Returns | Throws checked? |
|---|---|---|---|
| `Runnable` | `void run()` | nothing | **no** |
| `Callable<V>` | `V call()` | `V` | **yes** |

So `Callable` exists because `Runnable` can neither **return a value** nor **throw a checked
exception**. Submit either to an `ExecutorService`; both give you a `Future`.

**`Future<V>` methods** — the source list asks about three separately:
- **`get()`** — **blocks** until done, then returns the value. Throws checked `ExecutionException`
  (with the real failure as `getCause()`) and `InterruptedException`. **`get(timeout, unit)`** throws
  `TimeoutException` instead of blocking forever — always prefer it.
- **`isDone()`** — true if completed **for any reason**: success, exception, *or* cancellation. It does
  **not** mean "succeeded", which is the trap.
- **`isCancelled()`** — true only if cancelled before completing.
- **`cancel(boolean mayInterruptIfRunning)`** — returns `false` if the task already completed. With
  `true` it **interrupts** the thread, which only stops a task that actually checks for interruption
  (`quiz/01` Q14).

**The limitation that motivated `CompletableFuture`:** a plain `Future` has **no composition** — no
`map`, no callback, no combining. Every use ends in blocking a thread on `get()`.

**Modern shape:** `ExecutorService` is `AutoCloseable` since **JDK 19**, so
`try (var ex = Executors.newVirtualThreadPerTaskExecutor()) { … }` waits for all tasks on close —
no latch needed (`quiz/03` Q5).
</details>

---

### 2. What is `CompletableFuture`, and what does it add over `Future`?

<details><summary>Answer</summary>

A `Future` you can **compose** and **complete manually**. It implements `Future<T>` and
`CompletionStage<T>`.

Three capabilities `Future` lacks:
1. **Composition** — `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `anyOf`, `exceptionally`,
   `handle`. Callback-driven, so no thread needs to block.
2. **Manual completion** — `complete(v)`, `completeExceptionally(t)`. That's what makes it usable as a
   bridge from a callback API.
3. **Timeouts** — `orTimeout`, `completeOnTimeout` (JDK 9+).

**It is eager.** The work starts when the future is created, which means — and this is the important
limitation — you **cannot retry it** (re-observing a completed future gives the same result), and
`cancel` does **not** stop the underlying work.

**Measured in `problems/java/P05CompletableFuture.java` Part 6:** a 300 ms `orTimeout` returned a
fallback, and the task **ran to completion 1.2 seconds later**, still holding whatever it held. So
every timeout leaks the capacity of the in-flight work. That's the concrete argument for fiber-based
cancellation in Cats Effect/ZIO (`quiz/06` C4).

**The other measured trap, Part 4:** a fire-and-forget `runAsync` that throws produces **no output
anywhere** — no log, no stack trace. Always terminate a chain with
`whenComplete((v, t) -> { if (t != null) log.error("…", t); })`.
</details>

---

### 3. `supplyAsync()` vs `runAsync()`.

<details><summary>Answer</summary>

The two entry points:
```java
CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> load(id));      // Supplier<T> -> CF<T>
CompletableFuture<Void>   b = CompletableFuture.runAsync(() -> audit(id));        // Runnable   -> CF<Void>
```
`supplyAsync` returns a **value**; `runAsync` returns `Void` (a side effect).

**Both have a one-arg and a two-arg form**, and the difference matters more than the value/void
distinction:
```java
supplyAsync(supplier)                 // runs on ForkJoinPool.commonPool()  <-- usually WRONG
supplyAsync(supplier, myExecutor)     // runs on YOUR bounded, named pool   <-- do this
```
**Always pass an explicit executor in production.** The common pool is shared JVM-wide with
parallelism `cores − 1`; blocking in it stalls every `parallelStream` and every other default-pool
`CompletableFuture` in the process (`quiz/02` Q7).

**Measured in `P05` Part 7:** 17 tasks each sleeping 400 ms on a 13-parallelism common pool took
**818 ms** rather than ~400 — the pool saturated, and anything else in the JVM was queued behind it.

Also: **there is no `runAsync` equivalent of `completedFuture`** — for an already-known value use
`CompletableFuture.completedFuture(v)` or `failedFuture(t)` (JDK 9+), which allocate no thread at all.
</details>

---

### 4. `thenApply` vs `thenAccept` vs `thenRun` — and the `Async` suffix.

<details><summary>Answer</summary>

Distinguished by **what the callback receives and returns**:

| Method | Takes | Returns | Analogy |
|---|---|---|---|
| `thenApply(fn)` | the value | `CF<U>` | **map** |
| `thenAccept(consumer)` | the value | `CF<Void>` | side effect on the value |
| `thenRun(runnable)` | **nothing** | `CF<Void>` | "when done, do this" |

```java
cf.thenApply(Trade::notional)          // CF<Money>
  .thenAccept(m -> log.info("{}", m))   // CF<Void>, sees the value
  .thenRun(() -> metrics.increment());  // CF<Void>, sees nothing
```

**The `Async` suffix is the part that matters.** Every one has three forms:
- `thenApply(fn)` — runs on **whichever thread completed the previous stage**, or on the **calling**
  thread if the stage is already complete when you attach.
- `thenApplyAsync(fn)` — resubmitted to the **common pool**.
- `thenApplyAsync(fn, executor)` — resubmitted to **your** executor.

**Measured in `P05` Parts 1–2:** attaching to an already-completed future ran the callback on
**`main`**. So a non-`Async` stage can execute on your HTTP request thread, a Netty event loop, or a
common-pool worker — **you do not control it**.

**Therefore: never block or do long work in a non-`Async` stage**, and pass an explicit executor to
the `*Async` variants. That's the single most useful rule in this whole family.
</details>

---

### 5. `thenApply` vs `thenCompose`.

<details><summary>Answer</summary>

**`thenApply` is `map`; `thenCompose` is `flatMap`.**

If your function itself returns a `CompletableFuture`, `thenApply` gives you a **nested** future:
```java
CompletableFuture<CompletableFuture<Trade>> nested =
    cf.thenApply(id -> loadAsync(id));      // WRONG — you must join twice

CompletableFuture<Trade> flat =
    cf.thenCompose(id -> loadAsync(id));    // RIGHT — flattened
```
**Verified in `P05` Part 5:** `thenApply` returned a `CompletableFuture` as its *value*;
`thenCompose` returned `42`.

**This is the most common `CompletableFuture` bug**, and it's the same map/flatMap distinction you need
for Scala's `Future`, `IO` and `ZIO` (`quiz/12` Q13) — so it's worth naming the connection: any monadic
type has both, and reaching for `map` when you needed `flatMap` produces a nested container in every
one of them.

Mnemonic: **if the lambda returns the same wrapper type, you need `compose`/`flatMap`.**
</details>

---

### 6. `exceptionally` vs `handle` vs `whenComplete`.

<details><summary>Answer</summary>

| Method | Runs on | Receives | Can recover? |
|---|---|---|---|
| `exceptionally(fn)` | **failure only** | the throwable | **yes** — returns a replacement value |
| `handle(fn)` | **both** paths | `(value, throwable)` | **yes** — returns a new value |
| `whenComplete(action)` | **both** paths | `(value, throwable)` | **no** — passes the original through |

```java
cf.exceptionally(t -> fallback())                    // recover
  .handle((v, t) -> t != null ? fallback() : v)      // recover, sees both
  .whenComplete((v, t) -> log(v, t));                // observe only, result unchanged
```

**The distinction that matters:** `whenComplete` is for **side effects** (logging, metrics, cleanup) and
does **not** change the outcome — a failure stays a failure. `handle` and `exceptionally` **transform**
it. People reach for `whenComplete` expecting recovery and don't get it.

Also **`exceptionallyCompose`** (JDK 12+) recovers with another *future*, which is the `flatMap` of the
error channel.

**Exception wrapping, which is a real wart:** a failure in a stage propagates wrapped in
**`CompletionException`** from `join()`, or **`ExecutionException`** from `get()`. So the throwable your
handler sees may be a wrapper — **always unwrap `getCause()`**, and be aware the wrapping is
inconsistent across the API.

**And the rule from `P05` Part 4:** if nothing observes the chain, the exception is **silently
swallowed**. Terminate every chain with `whenComplete` logging, or hand it to something that observes it.
</details>

---

### 7. `allOf`, `anyOf`, `thenCombine`, `thenAcceptBoth`.

<details><summary>Answer</summary>

**Combining two futures:**
- **`thenCombine(other, biFn)`** → `CF<V>` — waits for **both**, applies a function to both values.
  This is **zip**.
- **`thenAcceptBoth(other, biConsumer)`** → `CF<Void>` — same wait, side effect instead of a value.
- **`runAfterBoth(other, runnable)`** → `CF<Void>` — waits for both, ignores both values.
- And the `*Either` family: `applyToEither`, `acceptEither`, `runAfterEither` — first one wins.

**Combining N futures:**
- **`allOf(cf...)`** → **`CF<Void>`** — completes when all complete. **It does not give you the
  results**, which is the trap. You collect them afterwards, and since they're all complete the joins
  don't block:
  ```java
  CompletableFuture.allOf(a, b, c)
      .thenApply(v -> List.of(a.join(), b.join(), c.join()));
  ```
- **`anyOf(cf...)`** → **`CF<Object>`** — completes with the first result. Note the **`Object`** return
  type, because the futures may have different types; you cast. Useful for **hedged requests**.

**Failure semantics:** `allOf` completes exceptionally if **any** input fails — but the others **keep
running**, exactly as measured for Scala's `Future.sequence`
(`problems/scala/S02FutureTraps.scala` Part 5: gave up at 50 ms, the 1-second tasks all ran to
completion). Nothing is cancelled.

**The fan-out pattern this is for** — and remember to pass an executor and a timeout (`quiz/02` Q12):
```java
var a = supplyAsync(() -> svcA.get(id), ex);
var b = supplyAsync(() -> svcB.get(id), ex);
return a.thenCombine(b, this::merge).orTimeout(500, MILLISECONDS)
        .exceptionally(t -> degraded());
```
</details>

---

### 8. `join()` vs `get()`, `complete()`, `completeExceptionally()`, `cancel()`.

<details><summary>Answer</summary>

**Blocking:**
- **`get()`** — throws **checked** `ExecutionException` + `InterruptedException`.
- **`join()`** — throws **unchecked** `CompletionException`. No checked exceptions, which is why it's
  usable inside lambdas.

Verified in `P05` Part 4: the same failure surfaces as `CompletionException` from `join()` and
`ExecutionException` from `get()`. Either way **unwrap `getCause()`**.

**Manual completion — this is what makes `CompletableFuture` a bridge:**
```java
CompletableFuture<Quote> cf = new CompletableFuture<>();
amps.subscribe("quotes", msg -> cf.complete(parse(msg)));   // callback API -> future
cf.completeExceptionally(new TimeoutException());            // or fail it
```
`complete` returns **`true` if this call transitioned it**, `false` if it was already done. That
boolean is how you win a race safely.

This is the Java analogue of Scala's **`Promise`** — except Scala **splits** the read and write sides
(`Future` vs `Promise`) so a consumer *cannot* complete your future, which is better encapsulation
(`quiz/12` Q8).

**`cancel(mayInterruptIfRunning)`** — and here is the honest answer:
**`CompletableFuture.cancel` does not interrupt the underlying computation.** The flag is effectively
ignored. It completes the future with a `CancellationException`; the task keeps running. Measured in
`P05` Part 6.

Related: `isCompletedExceptionally()`, `getNow(valueIfAbsent)` for a non-blocking peek, and
`obtrudeValue`/`obtrudeException` which forcibly overwrite a completed result (avoid).
</details>

---

## B. Synchronisers and locks

### 9. `CountDownLatch` vs `CyclicBarrier` vs `Semaphore`.

<details><summary>Answer</summary>

> `quiz/01` Q16 covers choosing between them. This is the API.

- **`CountDownLatch(n)`** — **one-shot gate**. `countDown()` decrements; `await()` blocks until zero,
  then **never blocks again**. **Not reusable.** Counters and waiters are different roles.
  *"Wait until N things have happened."*
- **`CyclicBarrier(n)`** — **reusable rendezvous**. N threads each call `await()`; all block until the
  Nth arrives, then all proceed and the barrier **resets**. Optional barrier action runs once per
  generation. *"All workers finish step k before step k+1."*
- **`Semaphore(n)`** — **counting permits**. `acquire()`/`release()`. Bounds **concurrency** rather than
  sequencing it. A permit-holder needn't be the acquirer, and you can `release()` more than you
  acquired — it is **not** a lock.

**The distinctions to state:** the latch is one-shot and asymmetric; the barrier is cyclic and
symmetric; the semaphore counts rather than gates.

**And the one people conflate:** a `Semaphore` limits **concurrency**, not **rate**. Ten permits could
be 10/sec or 10,000/sec. Rate limiting needs a token bucket (`quiz/02` Q16; both implemented and
verified in `problems/java/P09Exercises.java`).

**With virtual threads (JDK 21), the semaphore becomes the primary tool** — you stop sizing a pool to
protect a downstream and instead bound the resource directly. Measured: 2000 virtual threads through a
`Semaphore(50)` completed in 554 ms with max concurrency exactly 50 (`quiz/03` Q3).
</details>

---

### 10. `Phaser`, and `BrokenBarrierException`.

<details><summary>Answer</summary>

**`Phaser`** is a `CyclicBarrier` with three things it lacks:
1. **Dynamic party registration** — `register()`, `bulkRegister(n)`, `arriveAndDeregister()`. The number
   of participants can change between phases, which `CyclicBarrier` fixes at construction.
2. **Phase numbering** — `arriveAndAwaitAdvance()` returns the phase number, and
   `getPhase()`/`awaitAdvance(phase)` let you reason about progress.
3. **Tiering** — phasers can be arranged in a tree to reduce contention at high party counts.

```java
Phaser phaser = new Phaser(1);                  // register self
for (Task t : tasks) {
    phaser.register();
    exec.submit(() -> { work(t); phaser.arriveAndDeregister(); });
}
phaser.arriveAndAwaitAdvance();                  // wait for everyone
```
Also `onAdvance(phase, parties)` — override it to run an action per phase and to signal termination.

**Reach for `Phaser` when** the participant count isn't known up front, or you need multiple phases
with changing membership. Otherwise `CyclicBarrier` is simpler and `CountDownLatch` simpler still.

**`BrokenBarrierException`** — thrown by `CyclicBarrier.await()` when the barrier is **broken**, which
happens if: another waiting thread is **interrupted**, another thread **times out**, the barrier is
**`reset()`**, or the **barrier action throws**. The point is that a barrier is all-or-nothing — if one
party can't arrive, everyone else is released with this exception rather than deadlocking. Recovery
means `reset()` and starting the generation again.
</details>

---

### 11. `Lock`, `ReadWriteLock`, `ReentrantReadWriteLock`.

<details><summary>Answer</summary>

**`Lock`** — the interface `synchronized` can't express:
```java
lock.lock();                                  // uninterruptible
lock.lockInterruptibly();                     // responds to interrupt
lock.tryLock();                               // false if unavailable — deadlock avoidance
lock.tryLock(500, MILLISECONDS);              // bounded wait
lock.unlock();                                // MUST be in a finally
Condition c = lock.newCondition();            // multiple wait-sets per lock
```
**Always `try { … } finally { lock.unlock(); }`** — unlike `synchronized`, nothing releases it for you.

**`ReentrantLock`** — the standard implementation. Optional **fairness**
(`new ReentrantLock(true)`): FIFO ordering, lower throughput, bounded waiting.

**`ReadWriteLock` / `ReentrantReadWriteLock`** — a pair of locks: many concurrent **readers** or one
**writer**.
```java
rw.readLock().lock();   …   rw.readLock().unlock();
rw.writeLock().lock();  …   rw.writeLock().unlock();
```
**Only worth it when reads are long.** If the critical section is a few nanoseconds, plain
`synchronized` wins on bookkeeping alone. And **every reader still writes** to the lock's shared state
to register itself, so on read-mostly workloads across many cores that counter becomes the bottleneck —
which is exactly why **`StampedLock`** exists with its optimistic read path (`quiz/01` Q17).

Lock **downgrading** (write → read) is legal; **upgrading** (read → write) **deadlocks**.

**JDK 21 note:** `ReentrantLock` does **not** pin a virtual thread's carrier; `synchronized` does.
Measured 34× (`quiz/03` Q2). On virtual threads, prefer `ReentrantLock` on any blocking path.
</details>

---

### 12. `LockSupport` — what is it for?

<details><summary>Answer</summary>

The low-level **thread parking** primitive that every other synchroniser is built on.

```java
LockSupport.park();                 // block until unparked/interrupted/spurious
LockSupport.park(blocker);          // with a blocker object for diagnostics
LockSupport.parkNanos(1_000_000);
LockSupport.parkUntil(deadline);
LockSupport.unpark(thread);         // give that thread a permit
```

**The key semantic, and the reason it's better than `wait`/`notify`:** each thread has a **binary
permit**. `unpark` makes the permit available; `park` consumes it, blocking only if it isn't there.

**So `unpark` may precede `park`** — the permit is remembered and the subsequent `park` returns
immediately. That eliminates the **lost-wakeup** race that `Object.wait`/`notify` has, where a `notify`
before the `wait` is simply lost (`quiz/01` Q10; reproduced deterministically in
`problems/java/P06ProducerConsumer.java`).

The permit does **not** accumulate: two `unpark`s then two `park`s will block on the second.

**`park` can return spuriously** — no exception, no indication why. So callers **must** loop on their
condition, exactly as with `wait`. It also returns on interrupt **without throwing** and without
clearing the interrupt status, so you check `Thread.interrupted()` yourself.

**Who uses it:** `AbstractQueuedSynchronizer` (AQS), and therefore `ReentrantLock`, `Semaphore`,
`CountDownLatch`, `ReentrantReadWriteLock`, `FutureTask` — essentially all of `j.u.c.`.

**Should you use it directly?** Almost never. It's the answer to "how do the locks work underneath",
not a tool for application code. Reach for it only when building a custom synchroniser, and then
prefer subclassing **AQS**.
</details>

---

## C. Concurrent collections

### 13. `ConcurrentHashMap` vs `HashMap` vs `Collections.synchronizedMap`.

<details><summary>Answer</summary>

- **`HashMap`** — not thread-safe. Concurrent modification can lose entries or leave a corrupt table.
  (Java 8 restructured resizing so the famous *infinite loop in `get()`* is gone, but it is **still not
  thread-safe** — say it that way.)
- **`Collections.synchronizedMap`** — **one lock** around every method. Correct but serialised, and
  **iteration is not safe** unless you synchronise externally for the whole loop.
- **`ConcurrentHashMap`** — lock-free reads, **per-bin locking** for writes, so writers to different
  bins proceed concurrently.

**What CHM gives you:**
- Atomic compound operations: `putIfAbsent`, `computeIfAbsent`, `computeIfPresent`, `compute`, `merge`,
  `replace(k, old, new)`.
- **Weakly consistent** iterators — no `ConcurrentModificationException`, but you may not see
  concurrent updates.
- **No null keys or values** — deliberately, because `get() == null` would be ambiguous between
  "absent" and "mapped to null" in a map where you can't check-then-act (`quiz/16` Q19).
- `size()` is **weakly consistent** — it sums striped counters without locking. Fine for metrics;
  never for a correctness decision.

**The rule to state:** each *operation* is atomic; **combinations are not**. `containsKey` + `put` is
still a race — measured in `problems/java/P04ComputeIfAbsentDeadlock.java`: 8 threads, `expensive()`
ran **8 times**, 7 results silently discarded.
</details>

---

### 14. `computeIfAbsent` and `computeIfPresent`.

<details><summary>Answer</summary>

```java
map.computeIfAbsent(k, key -> load(key));          // Function — no existing value to pass
map.computeIfPresent(k, (key, old) -> old + 1);     // BiFunction
map.compute(k, (key, old) -> old == null ? 1 : old + 1);   // both cases
map.merge(k, 1, Integer::sum);                      // often the cleanest counter
```
- **`computeIfAbsent`** — computes and inserts **only if** absent (or mapped to null). Returns the
  existing or new value. Atomic, and the function runs **at most once per key** — verified in `P04`:
  8 concurrent threads, **1** invocation.
- **`computeIfPresent`** — remaps **only if** present. Returning `null` **removes** the entry.

**Three caveats that make this a good question:**

1. **The mapping function runs while holding the bin lock.** Keep it short, pure and non-blocking.
   Doing I/O there blocks every other writer hashing to that bin. Measured in `P04` Part C: 4 threads ×
   4 distinct keys × 300 ms ran in **310 ms** (different bins → parallel) — but colliding keys would
   serialise to ~1200 ms, and **nothing in your source tells you which case you're in**.
2. **Recursive update on the same map throws `IllegalStateException("Recursive update")`** (JDK 9+).
   Verified in `P04` Part B with a memoised-fibonacci — the realistic version of this bug. And the
   dangerous variant: with keys in *different* bins it sometimes **works by luck**, so the bug is
   latent.
3. On JDK 8 the same shape could **deadlock** or corrupt the table instead of throwing.

**For async loading with per-key coalescing**, don't hand-roll this — use Caffeine's
`AsyncLoadingCache`, or the `CompletableFuture`-as-value trick (implemented and verified in
`P09Exercises` EX4: loader called exactly once for 16 concurrent gets).
</details>

---

### 15. `ConcurrentHashMap.KeySetView` and `newKeySet()`.

<details><summary>Answer</summary>

There is **no `ConcurrentHashSet`** in the JDK. `KeySetView` is how you get one.

```java
Set<String> set = ConcurrentHashMap.newKeySet();              // a concurrent Set
Set<String> s2  = ConcurrentHashMap.<String>newKeySet(64);     // with initial capacity
```
`newKeySet()` returns a `KeySetView` backed by a `CHM` whose values are all `Boolean.TRUE`. Fully
usable as a `Set` — `add`, `remove`, `contains` — with CHM's concurrency properties.

**The two flavours, and the distinction is the actual question:**
- **`map.keySet()`** — a **view** of an existing map's keys. **`add()` throws
  `UnsupportedOperationException`**, because there's no value to associate.
- **`map.keySet(mappedValue)`** — the same view, but `add()` **is** supported: it inserts the key with
  that fixed `mappedValue`.

```java
ConcurrentHashMap<String, Boolean> m = new ConcurrentHashMap<>();
m.keySet().add("x");              // throws UnsupportedOperationException
m.keySet(Boolean.TRUE).add("x");  // works — inserts x -> TRUE
```

**Views are live**, both ways: removing from the key set removes the mapping. That applies to
`keySet()`, `values()` and `entrySet()` on any `Map`, and it's a common surprise.

Prefer **`newKeySet()`** when you want a set. Use `keySet(v)` only when you genuinely have a map and
want add-through.
</details>

---

### 16. The `BlockingQueue` family: `LinkedBlockingQueue`, `ArrayBlockingQueue`, `SynchronousQueue`, `LinkedTransferQueue`.

<details><summary>Answer</summary>

> Choice guidance is `quiz/02` Q5. This is what each one *is*.

| Queue | Bounded? | Notes |
|---|---|---|
| `ArrayBlockingQueue(n)` | **always** | pre-allocated ring buffer, **one lock**, no per-node garbage. Optional fairness |
| `LinkedBlockingQueue` | optional — **unbounded by default** | **two locks** (head/tail), so higher throughput under simultaneous producer/consumer; allocates a node per element |
| `SynchronousQueue` | **capacity zero** | a direct **handoff** — `put` blocks until a taker arrives |
| `LinkedTransferQueue` | **unbounded** | adds `transfer()`; the fastest unbounded queue |
| `PriorityBlockingQueue` | **unbounded** | heap-ordered; can starve low priority |
| `DelayQueue` | **unbounded** | elements available only after their delay |

**`BlockingQueue`'s four behaviours per operation** — worth knowing the table:

| | Throws | Returns special | Blocks | Times out |
|---|---|---|---|---|
| Insert | `add` | `offer` | **`put`** | `offer(e, t, u)` |
| Remove | `remove` | `poll` | **`take`** | `poll(t, u)` |
| Examine | `element` | `peek` | — | — |

So **`take()`** blocks until an element is available, and **`put()`** blocks until there's space. Those
are the two you use in a producer/consumer, and they're why you don't hand-roll `wait`/`notify`.

**`TransferQueue`** (interface, JDK 7) adds the semantic the others lack: **`transfer(e)` blocks until a
consumer actually *receives* the element**, not merely until it's enqueued. Plus `tryTransfer`,
`hasWaitingConsumer()`, `getWaitingConsumerCount()`. `LinkedTransferQueue` is the implementation — think
of it as `SynchronousQueue` (handoff) *and* `LinkedBlockingQueue` (buffering) in one.

**The recurring warning:** **unbounded is the default in several of these**, and an unbounded queue
turns overload into memory growth and unbounded latency instead of backpressure (`quiz/02` Q2).
</details>

---

### 17. `ConcurrentSkipListMap` vs `TreeMap`.

<details><summary>Answer</summary>

Both are **sorted** maps implementing `NavigableMap`. The difference is the data structure and the
concurrency.

| | `TreeMap` | `ConcurrentSkipListMap` |
|---|---|---|
| Structure | **red-black tree** | **skip list** — layered linked lists |
| Thread-safe | **no** | **yes**, lock-free (CAS-based) |
| Complexity | O(log n) | O(log n) *expected* — probabilistic |
| `size()` | O(1) | **O(n)** — must traverse, and weakly consistent |
| Iterators | fail-fast | weakly consistent |
| Nulls | null values ok, **null keys throw** | **no nulls at all** |

**Why a skip list rather than a concurrent tree** — this is the interesting part: a balanced tree
requires **rotations**, which restructure multiple nodes at once and are extremely hard to make
lock-free. A skip list needs only **local CAS operations** on individual `next` pointers, so it
parallelises without locks. That's the whole design rationale.

**The `size()` cost is the practical gotcha** — it's O(n) and not a snapshot, so never use it in a hot
path or a correctness check.

**Choose:** `TreeMap` for single-threaded sorted access (`quiz/09` Q2/Q3 — measured ~8× slower on `put`
than `HashMap`, so only for the ordering). `ConcurrentSkipListMap` when you need sorted **and**
concurrent — a time-ordered event index, an order book by price, a priority structure with range
queries. `ConcurrentSkipListSet` is the set counterpart.
</details>

---

### 18. `ConcurrentLinkedDeque`, `ConcurrentLinkedQueue`, `LinkedBlockingDeque`, `BlockingDeque`.

<details><summary>Answer</summary>

The distinction is **blocking vs non-blocking**, which is the thing to get right:

- **`ConcurrentLinkedQueue` / `ConcurrentLinkedDeque`** — **non-blocking**, lock-free, **unbounded**.
  They are **not** `BlockingQueue`s: `poll()` returns `null` immediately when empty rather than
  waiting. Use when you want a lock-free handoff and will poll or drain yourself.
- **`LinkedBlockingDeque`** — a **blocking**, optionally-bounded deque. Implements **`BlockingDeque`**,
  so it has the blocking operations at **both ends**: `putFirst`/`putLast`, `takeFirst`/`takeLast`.
- **`BlockingDeque`** — the interface: `BlockingQueue` + `Deque`.

**Why a *deque* concurrently:** **work stealing**. A worker pushes and pops its own tasks at one end
(LIFO, cache-hot) while thieves steal from the other end (FIFO, biggest task). That's exactly how
`ForkJoinPool` works (Q23), and it's the best answer to "why would you want a concurrent deque".

**The trap:** `size()` on `ConcurrentLinkedQueue`/`Deque` is **O(n)** and not atomic — it traverses.
`isEmpty()` is O(1), so prefer it.

**Choosing between them:** if consumers should **wait** for work, you want a blocking queue —
`ArrayBlockingQueue` for bounded, `LinkedBlockingQueue`/`Deque` otherwise. If consumers poll on their
own schedule, or you need lock-free, use the `ConcurrentLinked*` family. Note the `ConcurrentLinked*`
ones are **unbounded**, so they give you no backpressure.
</details>

---

### 19. `CopyOnWriteArrayList` and `CopyOnWriteArraySet`.

<details><summary>Answer</summary>

> `quiz/01` Q18 covers the list. Adding the set, which the source list asks separately.

**`CopyOnWriteArrayList`** — every mutation **copies the whole backing array**. Reads are lock-free,
and iteration is over an **immutable snapshot**: no `ConcurrentModificationException`, the iterator
never sees later writes, and `Iterator.remove()` is **unsupported**.

**`CopyOnWriteArraySet`** — **backed by a `CopyOnWriteArrayList`**. Which has a direct consequence
worth stating: uniqueness is checked by **scanning the array**, so **`add` is O(n)** and `contains`
is O(n) too. It is *not* hash-based.

**So the performance profile:**
- Reads: **O(1)** indexed (list) / **O(n)** membership (set), lock-free.
- Writes: **O(n)** plus a full-array allocation, every time.

**Use when:** many readers, **very few** writers, and a small collection. **Listener/observer
registries are the canonical fit** — registered once at startup, read on every event.

**Never** for a collection you append to in a loop: that's O(n²) with an allocation per step, and a
young-gen firehose.

**If you need a concurrent set with O(1) membership**, use **`ConcurrentHashMap.newKeySet()`** (Q15),
not `CopyOnWriteArraySet`. That's the practical takeaway.
</details>

---

### 20. `Exchanger`.

<details><summary>Answer</summary>

A **two-party rendezvous that swaps objects**. Each thread calls `exchange(myObject)` and receives the
*other* thread's object. Both block until both have arrived.

```java
Exchanger<ByteBuffer> ex = new Exchanger<>();

// producer                                    // consumer
ByteBuffer empty = ex.exchange(full);          ByteBuffer full = ex.exchange(empty);
```

**The classic use is double-buffering:** the producer fills a buffer and swaps it for an empty one; the
consumer drains it and swaps back. No allocation per cycle, no queue, and the two threads are
naturally paced to each other.

`exchange(x, timeout, unit)` throws `TimeoutException`; `exchange` throws `InterruptedException`.

**Be honest about its rarity.** `Exchanger` is genuinely uncommon in application code — a
`BlockingQueue` handles nearly every producer/consumer case with fewer constraints (it supports N
parties, buffering, and doesn't require pairing). `Exchanger` is strictly **two** parties, both of
which must participate on every cycle.

So the good answer names the niche: **buffer recycling where you want zero allocation and symmetric
pacing**, or a genetic-algorithm/simulation pattern where two workers swap state each generation. If
asked "would you use it?", the credible answer is "I know what it does; I'd reach for a
`BlockingQueue` first."
</details>

---

### 21. `PriorityBlockingQueue` and `DelayQueue`.

<details><summary>Answer</summary>

- **`PriorityBlockingQueue`** — a blocking priority queue, heap-ordered by natural ordering or a
  `Comparator`. **Unbounded**, so `put` never blocks.
- **`DelayQueue`** — holds elements implementing **`Delayed`** (`getDelay(TimeUnit)` + `Comparable`).
  An element becomes available only when its delay **expires**; `take()` blocks until then. Also
  **unbounded**.

**The shared, important caveat: both are unbounded**, so neither can provide backpressure. Using a
`PriorityBlockingQueue` as a thread-pool work queue means `maximumPoolSize` is ignored and overload
becomes memory growth (`quiz/02` Q1/Q5).

**`PriorityBlockingQueue`'s other traps:**
- **Low-priority tasks can starve indefinitely** if high-priority work keeps arriving.
- **Iteration and `toString` are not in priority order** — only `poll`/`take` respect it. People are
  surprised by this.
- No FIFO guarantee among equal-priority elements (add a sequence number to the comparator if you need
  one — a genuinely useful trick).

**`DelayQueue`'s role:** it is the mechanism behind **`ScheduledThreadPoolExecutor`**, whose internal
`DelayedWorkQueue` is the same idea. So the honest answer to "how would you use `DelayQueue`" is often
"I'd use `ScheduledExecutorService`, which is built on it" — plus genuine direct uses like cache
expiry, session timeouts, and retry backoff scheduling.
</details>

---

## D. Executors and the rest

### 22. The `Executors` factory methods — and why to avoid most of them.

<details><summary>Answer</summary>

> The full argument is `quiz/02` Q2. This is what each one actually constructs.

| Factory | core | max | keepAlive | queue |
|---|---|---|---|---|
| `newFixedThreadPool(n)` | n | n | 0 | **unbounded `LinkedBlockingQueue`** |
| `newSingleThreadExecutor()` | 1 | 1 | 0 | **unbounded `LinkedBlockingQueue`** |
| `newCachedThreadPool()` | 0 | **`Integer.MAX_VALUE`** | 60 s | `SynchronousQueue` |
| `newScheduledThreadPool(n)` | n | `Integer.MAX_VALUE` | 0 | **unbounded `DelayedWorkQueue`** |
| `newSingleThreadScheduledExecutor()` | 1 | as above | 0 | as above |
| `newVirtualThreadPerTaskExecutor()` | — | not a pool | — | none |
| `newWorkStealingPool()` | — | a `ForkJoinPool` | — | per-worker deques |

**The two failure modes:**
- **Unbounded queue** (`fixed`, `single`, `scheduled`): nothing is ever rejected, so overload becomes
  heap growth and unbounded queueing delay — while every dashboard says the pool is healthy. It also
  makes **`maximumPoolSize` dead configuration**, because the queue always accepts.
- **Unbounded threads** (`cached`): a burst creates thousands of platform threads at ~1 MB of stack
  each, then `OutOfMemoryError: unable to create native thread`.

**So: construct `ThreadPoolExecutor` explicitly** with a bounded `ArrayBlockingQueue`, a deliberate
`RejectedExecutionHandler`, and a **named** `ThreadFactory` (so thread dumps are readable). Then export
`getQueue().size()`, `getActiveCount()` and the rejection count — queue depth is your earliest overload
signal.

**`newSingleThreadExecutor`'s legitimate use:** guaranteeing **sequential execution** with
thread-confined state — a single-writer pattern. That's a real design tool, not just a degenerate pool.

**Scheduled-executor gotcha:** if a periodic task throws an unchecked exception, it is **silently
cancelled and never runs again** (`quiz/02` Q6). Always wrap the body in `try/catch(Throwable)`.
</details>

---

### 23. `ForkJoinPool`, work stealing, and `availableProcessors()` vs `getCommonPoolParallelism()`.

<details><summary>Answer</summary>

**Work stealing:** each worker has its own **double-ended queue**. It pushes and pops its own subtasks
**LIFO** (the freshest subtask is cache-hot); idle workers **steal from the other end, FIFO** (the
oldest task is likely the biggest, so a steal yields the most work per synchronisation). Near-zero
contention in the common case, automatic load balancing.

Designed for **divide-and-conquer**: `RecursiveTask<V>` (returns a value) / `RecursiveAction` (doesn't),
with `fork()`/`join()` and a sequential threshold.

**`Runtime.getRuntime().availableProcessors()` vs `ForkJoinPool.getCommonPoolParallelism()`** — the
source list asks this directly:
- `availableProcessors()` — how many processors the JVM believes it has. In a container this is derived
  from the **cgroup CPU quota**, so a `cpu: 500m` limit reports **1** (`quiz/04` Q12).
- `getCommonPoolParallelism()` — the common pool's target parallelism, which by default is
  **`availableProcessors() − 1`**. It's one fewer because the **submitting thread also participates** in
  the work, so total concurrency is still about `availableProcessors()`.

**The edge case worth naming — and stating carefully:** on a single-processor machine (or a
heavily-limited container) the common pool **stops using workers and gives each task a fresh thread**.
Measured on JDK 21.0.11 with `-XX:ActiveProcessorCount=1`: `supplyAsync` ran on `Thread-0`, `Thread-1`,
`Thread-2` rather than any `commonPool-worker-N`.

But do **not** say "parallelism is 0", because that's checkable and wrong:
`ForkJoinPool.getCommonPoolParallelism()` still returns **1** there — the getter floors the value at 1
(`Math.max(…, 1)`), so the metric hides the fallback. Only `commonPool().toString()` exposes the
underlying `parallelism = 0`, and only when the property was set explicitly. The precise claim is
**"the pool degrades to a thread per task while the reported parallelism stays at 1."**

Also common-pool-specific: the **−1 applies only to the common pool.** Measured on 14 cores,
`new ForkJoinPool()` → **14** and `Executors.newWorkStealingPool()` → **14**, versus **13** for
`commonPool()`. Verified in `problems/java/P05CompletableFuture.java`, which prints both values.

Override with `-Djava.util.concurrent.ForkJoinPool.common.parallelism=N`, or
`-XX:ActiveProcessorCount=N` for the underlying count.

**And the recurring warning:** **never block in a compute pool.** The pool is sized to cores and has
no idea you've blocked, so parallelism collapses. The sanctioned escape hatch is
`ForkJoinPool.ManagedBlocker` — which is exactly what Scala's `scala.concurrent.blocking { }` uses
(`quiz/06` A2, measured: 1038 ms → 218 ms).
</details>

---

### 24. `ThreadLocal` — API and the two hazards.

<details><summary>Answer</summary>

> `quiz/01` Q13 has the full treatment; measured in `problems/java/P08ThreadLocalLeak.java`.

```java
static final ThreadLocal<Principal> CURRENT = new ThreadLocal<>();
static final ThreadLocal<Map<String,String>> MDC = ThreadLocal.withInitial(HashMap::new);
CURRENT.set(p);  CURRENT.get();  CURRENT.remove();
```
Each thread gets its own value; storage lives in the **thread's** `ThreadLocalMap`, not in the
`ThreadLocal` object.

**Hazard 1 — context bleed on a pooled thread.** Pool threads are **reused and never die**, so a value
set by task A is visible to unrelated task B. Verified: an *unauthenticated* request read the previous
request's principal. In a multi-tenant service that is a **security incident**, not a bug report — and
the trace id is wrong too, so the logs lie about which request did what.

**Hazard 2 — memory leak.** `ThreadLocalMap.Entry extends WeakReference<ThreadLocal<?>>`: the **key is
weak, the value is strong**. So dropping your reference to the `ThreadLocal` lets the *key* be
collected but leaves a stale entry whose **value is still strongly reachable** until the slot is reused
or the thread dies. Measured: 22 MB retained across two pool threads, dropping to 4 MB after
`remove()`. This is the classic webapp classloader leak.

**So: always `remove()` in a `finally`.** To propagate deliberately, wrap the executor — that's what
Spring's `TaskDecorator` does — capturing at submit, installing for the task, clearing after.

**`InheritableThreadLocal`** copies to child threads at creation — which is worse at scale, because
it's a copy per thread.

**JDK 21+:** each virtual thread is fresh, so bleed disappears (verified: `before` was always `null`).
But footprint at a million threads becomes the concern, and `ThreadLocal`-as-a-cache stops working.
Hence **`ScopedValue`** (preview in 21) — immutable, lexically scoped, inherited by structure
(`quiz/03` Q8).
</details>

---

### 25. `ThreadLocalRandom` — why not `Random`?

<details><summary>Answer</summary>

`java.util.Random` is **thread-safe but contended**: its seed is a single `AtomicLong` updated by CAS on
every `next()`. Under concurrency every thread fights over that one word — the same cache-line
ping-pong that makes a contended `AtomicLong` slow (measured: `LongAdder` **36× faster** in
`problems/java/P01Counter.java`).

**`ThreadLocalRandom`** gives each thread its **own** seed, so there is no sharing and no contention.

```java
int n = ThreadLocalRandom.current().nextInt(1, 101);      // [1, 101)
double d = ThreadLocalRandom.current().nextDouble();
```

**`current()` is the whole API surface** — and the rules are:
- It returns **this thread's** instance. You **must not** share the returned reference between threads,
  and you must not store it in a field. Call `current()` at the point of use, every time; it's cheap.
- You **cannot set the seed** — `setSeed` throws `UnsupportedOperationException`. So it is **not
  reproducible**, which matters for tests: use a seeded `Random` (or `SplittableRandom`) where you need
  determinism.
- It is **not cryptographically secure**. For tokens, keys or anything security-relevant use
  `SecureRandom`.

**Also useful:** `nextInt(origin, bound)` — a bounded form `Random` lacked until later; and
`ints()`/`doubles()` stream methods.

**Related:** `SplittableRandom` (JDK 8) for parallel/splittable generation, and the
`RandomGenerator` interface family (JDK 17, JEP 356) which unified all of these behind
`RandomGenerator.of("L64X128MixRandom")` — a good currency detail.
</details>

---

### 26. `Thread.UncaughtExceptionHandler`.

<details><summary>Answer</summary>

Where an exception goes when it escapes `run()`. The lookup order:
1. The thread's own handler (`thread.setUncaughtExceptionHandler(h)`).
2. Otherwise the thread's `ThreadGroup` handler.
3. Otherwise the **default** (`Thread.setDefaultUncaughtExceptionHandler(h)`).
4. Otherwise: print the stack trace to `System.err` and the thread dies.

```java
Thread.setDefaultUncaughtExceptionHandler((t, e) ->
    log.error("uncaught in {}", t.getName(), e));
```

**The critical subtlety, and it's the reason to ask this:** **it does not fire for tasks submitted to
an `ExecutorService`.**

- `submit(task)` **captures** the exception into the `Future`. If nobody calls `get()`, it is
  **silently swallowed** — no log, no handler, nothing. Measured in `P05` Part 4: a
  fire-and-forget `runAsync` that throws produces **no output at all**.
- `execute(task)` **does** route to the handler, because there's no `Future` to hold it.

So `submit` vs `execute` changes your error visibility, which almost nobody knows.

**What to do instead in a pool:**
- Always **observe the `Future`**, or wrap the task body in `try/catch(Throwable)` and log there.
- Override `ThreadPoolExecutor.afterExecute(Runnable, Throwable)` — but note that for `submit` the
  throwable argument is `null` and you must unwrap the `FutureTask` yourself.
- Set a `ThreadFactory` that installs a handler — useful for threads you `execute` on, and it makes
  thread names meaningful at the same time.

**And the scheduled-task version:** a periodic task that throws is **cancelled forever, silently**
(`quiz/02` Q6). Same class of problem, different mechanism.
</details>

---

### 27. `CompletionService`.

<details><summary>Answer</summary>

Solves a specific problem: you submit N tasks and want results **in completion order**, not submission
order — so you can process the fast ones while the slow ones are still running.

```java
var cs = new ExecutorCompletionService<Result>(executor);
for (Task t : tasks) cs.submit(() -> handle(t));
for (int i = 0; i < tasks.size(); i++) {
    Result r = cs.take().get();        // BLOCKS until the NEXT one finishes — any one
    process(r);
}
```

**Compare with the naive alternative:**
```java
List<Future<Result>> fs = executor.invokeAll(tasks);   // blocks until ALL are done
for (var f : fs) process(f.get());                      // then processes in SUBMISSION order
```
`invokeAll` makes you wait for the slowest before you touch the first result. With
`CompletionService`, if task 5 finishes first you process it first.

**API:** `submit(Callable)` / `submit(Runnable, result)`, then `take()` (blocking),
`poll()` (immediate, null if none), `poll(timeout, unit)`.

**Internally** it wraps each task and pushes the completed `Future` onto a `BlockingQueue` — so it's a
thin, honest abstraction you could write yourself.

**When to use it:** N independent calls where per-result latency matters — a scatter-gather across
several downstreams, or a search where you can stream partial results.

**The modern alternative worth naming:** `CompletableFuture.anyOf` in a loop, or better,
**`StructuredTaskScope`** on JDK 21+ (preview), which gives you the same "first result wins" or "all
must succeed" semantics **with real cancellation** of the siblings — which `CompletionService` does not
provide (`quiz/03` Q6).
</details>

---

### 28. `TimeUnit`, and the timeout-related exceptions.

<details><summary>Answer</summary>

**`TimeUnit`** is an enum — `NANOSECONDS`, `MICROSECONDS`, `MILLISECONDS`, `SECONDS`, `MINUTES`,
`HOURS`, `DAYS` — and it does three jobs:

1. **Self-documenting timeouts.** `lock.tryLock(500, MILLISECONDS)` instead of a bare `500` whose unit
   you have to guess. Every timed method in `j.u.c.` takes one.
2. **Conversion.** `TimeUnit.SECONDS.toMillis(5)`, `MILLISECONDS.convert(1, HOURS)`. Conversions
   **truncate** rather than round, and **saturate** at `Long.MAX_VALUE`/`MIN_VALUE` rather than
   overflowing — a nice detail.
3. **Sleeping.** `TimeUnit.SECONDS.sleep(2)` is clearer than `Thread.sleep(2000)`.

**The exceptions that go with timeouts:**
- **`TimeoutException`** — **checked**. Thrown by `Future.get(t,u)`, `CompletableFuture.orTimeout`,
  `Exchanger.exchange(t,u)`, `CyclicBarrier.await(t,u)`. It means *"not finished yet"* — the task is
  **still running**, and (for `CompletableFuture`) is **not cancelled** (Q8, measured in `P05` Part 6).
- **`InterruptedException`** — checked. The thread was interrupted while waiting. Propagate it, or
  restore the flag with `Thread.currentThread().interrupt()` (`quiz/01` Q14).
- **`ExecutionException`** — checked. The task itself threw; the real cause is `getCause()`.
- **`CancellationException`** — **unchecked**. `get()` on a cancelled future.
- **`RejectedExecutionException`** — unchecked. The pool refused the task (`AbortPolicy`); in a server
  this should map to a 503.

**And note the modern alternative for durations:** `java.time.Duration` is generally nicer for API
design (`Duration.ofSeconds(5)`), and newer JDK APIs increasingly accept it instead of the
`(long, TimeUnit)` pair.
</details>

---

## Score yourself

- **22+** — you can hold the `j.u.c.` API conversation.
- **The five that matter most**, because they're where behaviour surprises people:
  **Q4** (non-`Async` stages run on an unpredictable thread), **Q8** (`cancel` doesn't cancel),
  **Q14** (`computeIfAbsent` holds a bin lock and forbids recursion), **Q22** (the unbounded-queue
  defaults), **Q26** (`submit` swallows exceptions, `execute` doesn't).
- **Then go read `quiz/01` and `quiz/02`.** This file is inventory; those two are the reasoning, and
  interviews are decided on reasoning. Every measured number quoted here comes from
  `problems/java/P01`–`P12` — run them.
