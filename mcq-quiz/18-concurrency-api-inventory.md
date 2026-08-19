# MCQ 18 — Concurrency API Inventory

26 questions. Source: [`quiz/18`](../quiz/18-concurrency-api-inventory.md).

This is the API-recall half of concurrency. The *reasoning* half is `mcq-quiz/01`–`04`.

---

### Q1. `Runnable` vs `Callable`?

- **A.** `Callable` runs on a pooled thread, `Runnable` on a new thread
- **B.** `Callable` **returns a value and may throw a checked exception**; `Runnable` does neither
- **C.** `Runnable` is a functional interface, `Callable` isn't
- **D.** They're interchangeable — `Callable` is just the newer name

<details><summary>Answer</summary>

**B.** `Runnable.run()` → `void`, `throws` nothing. `Callable.call()` → `V`, `throws Exception`.

That difference dictates error handling. A `Runnable`'s exception has nowhere to go — it propagates to
the `UncaughtExceptionHandler` (with `execute`) or is **captured in the `Future`** (with `submit`), which
is why an exception in a `submit`ed task is silently invisible unless you call `get()`.

And that's the trap in `Executors`: `submit(Runnable)` returns `Future<?>` and **wraps** it in a
`FutureTask`, so the handler is *not* called. Both **A** and **C** are wrong — both are functional
interfaces, and where a task runs is the executor's business, not the interface's.

`Future`: `get()`, `get(timeout)`, `cancel(mayInterruptIfRunning)`, `isDone()`, `isCancelled()`. Its
limits are the reason `CompletableFuture` exists — `get()` blocks, there is no callback, and no
composition.

→ `quiz/18` Q1
</details>

---

### Q2. What does `CompletableFuture` add over `Future`?

- **A.** Thread-safety
- **B.** A bounded work queue
- **C.** Cancellation with interruption
- **D.** **Completion callbacks, composition, and manual completion** — a non-blocking API

<details><summary>Answer</summary>

**D.** Three things: **callbacks** (`thenApply`, `thenAccept`) so you never block; **composition**
(`thenCompose`, `thenCombine`, `allOf`) so you can build a graph; and **manual completion**
(`complete`, `completeExceptionally`) so it doubles as a `Promise`.

**C is exactly backwards** and worth remembering: `CompletableFuture.cancel()` **does not interrupt** the
running thread. It just completes the future exceptionally with `CancellationException` — the underlying
work keeps burning CPU. `FutureTask.cancel(true)` *does* interrupt. This asymmetry surprises people who
switch from `ExecutorService` to `CompletableFuture`.

It implements both `Future` and `CompletionStage`. Scala's `Future` + `Promise` split is the same idea
with the read and write halves given separate types — which is arguably the better design.

→ `quiz/18` Q2; `quiz/09` Q6
</details>

---

### Q3. `supplyAsync` vs `runAsync`?

- **A.** `supplyAsync` is synchronous
- **B.** `runAsync` uses the caller thread
- **C.** `runAsync` takes a `Callable`
- **D.** `supplyAsync` takes a `Supplier` and yields `CompletableFuture<T>`; `runAsync` takes a `Runnable` and yields `CompletableFuture<Void>`

<details><summary>Answer</summary>

**D.** Value-producing vs side-effecting — the `Supplier`/`Runnable` distinction again.

The part that matters more than the naming: **both default to `ForkJoinPool.commonPool()`**, sized to
`availableProcessors() - 1` and shared with every `parallelStream()` in the JVM. So a blocking JDBC call
inside `supplyAsync` starves unrelated code.

**Always pass your own executor** for anything that blocks:
```java
CompletableFuture.supplyAsync(this::callDownstream, ioExecutor)
```
Note the edge case, **measured on JDK 21.0.11** with `-XX:ActiveProcessorCount=1`: the common pool stops
using workers entirely and gives each task a **brand-new `Thread-0`, `Thread-1`, …**. Be careful how you
state this — `ForkJoinPool.getCommonPoolParallelism()` still reports **1**, because the getter floors the
value, so the metric hides the behaviour. In a 1-CPU container your "pool" is an unbounded thread factory.

→ `quiz/18` Q3
</details>

---

### Q4. `future.thenApply(f)` — which thread runs `f`?

- **A.** Always the common pool
- **B.** Always the thread that called `thenApply`
- **C.** Always the thread that completed the future
- **D.** **Unspecified** — the completing thread if the future is still pending, or the calling thread if it's already done

<details><summary>Answer</summary>

**D.** This is the single most important `CompletableFuture` subtlety. Without the `Async` suffix, the
callback runs on **whichever thread happens to complete the stage** — or, if the stage is *already*
complete when you attach the callback, on the **calling** thread, synchronously.

The consequence: a long non-`Async` callback can hijack an event-loop or Netty I/O thread and stall
unrelated work. Non-determinism also makes it untestable — the same code takes different paths depending
on a race.

**Rule:** if the callback is non-trivial, use the `Async` variant *with an explicit executor*.

The trio: `thenApply(Function)` → transform, `thenAccept(Consumer)` → consume, `thenRun(Runnable)` →
ignore the value.

→ `quiz/18` Q4
</details>

---

### Q5. `thenApply` vs `thenCompose`?

- **A.** `thenCompose` runs on a different thread
- **B.** If your function itself returns a `CompletableFuture`, `thenApply` gives you a nested `CompletableFuture<CompletableFuture<T>>`; `thenCompose` **flattens** it
- **C.** `thenApply` is lazy, `thenCompose` is eager
- **D.** `thenCompose` cannot handle exceptions

<details><summary>Answer</summary>

**B.** `thenApply` is `map`; `thenCompose` is `flatMap`. When the function returns another async
operation, `thenApply` wraps it and you end up joining twice.

```java
// nested — wrong
CompletableFuture<CompletableFuture<Account>> bad = findUser(id).thenApply(this::loadAccount);
// flat — right
CompletableFuture<Account> good = findUser(id).thenCompose(this::loadAccount);
```

This is *the* monadic distinction, and it recurs everywhere: `Stream.map`/`flatMap`,
`Optional.map`/`flatMap`, Scala `Future.map`/`flatMap`. Recognising it as one idea rather than four API
quirks is what a functional-programming question is actually probing.

→ `quiz/18` Q5; `quiz/09` Q4
</details>

---

### Q6. Which of these does **not** consume the exception?

- **A.** `exceptionally`
- **B.** `handle`
- **C.** **`whenComplete`**
- **D.** All three consume it

<details><summary>Answer</summary>

**C.** `whenComplete(BiConsumer<T,Throwable>)` **observes** and returns a stage with the *same* outcome —
the exception propagates onward. It's the `finally` of the three.

- `exceptionally(Function<Throwable,T>)` — recovery path only; supplies a fallback value.
- `handle(BiFunction<T,Throwable,U>)` — always runs, sees both, and **can change the type**.
- `whenComplete` — always runs, sees both, changes nothing.

Two details that bite: the `Throwable` handed to you is usually wrapped in a **`CompletionException`**,
so unwrap with `getCause()` before matching on type. And nothing in this family is *reachable* if you
never attach any of them — an unobserved failed `CompletableFuture` is **silently swallowed**, with no
warning, unlike a raw thread's uncaught exception.

→ `quiz/18` Q6
</details>

---

### Q7. `CompletableFuture.allOf(a, b, c)` returns?

- **A.** `CompletableFuture<Void>` — you must re-read each input to get its value
- **B.** `CompletableFuture<List<T>>`
- **C.** `CompletableFuture<Object>` holding the first result
- **D.** `CompletableFuture<Stream<T>>`

<details><summary>Answer</summary>

**A.** A frequently-cursed signature. The idiom:
```java
CompletableFuture.allOf(futures)
    .thenApply(v -> Arrays.stream(futures).map(CompletableFuture::join).toList());
```
`join()` is safe there because `allOf` has already guaranteed completion.

Also: `allOf` completes exceptionally if **any** input fails, but it does **not** cancel the others.
`anyOf` returns `CompletableFuture<Object>` — untyped, because the inputs may differ — and completes with
the first outcome, **including the first failure**, which makes it a poor fit for hedged requests unless
you filter failures first.

For two futures specifically: `thenCombine(other, fn)` gives you both values typed, and
`thenAcceptBoth` the same without a return. `thenCombine` is the type-safe choice whenever the fan-out is
fixed and small.

→ `quiz/18` Q7
</details>

---

### Q8. `join()` vs `get()`?

- **A.** `join()` doesn't block
- **B.** `get()` returns `null` on failure
- **C.** **`join()` throws unchecked `CompletionException`; `get()` throws checked `ExecutionException` and `InterruptedException`**
- **D.** They're identical

<details><summary>Answer</summary>

**C.** Both block. The difference is purely the exception contract — which is why `join()` is usable
inside a lambda and `get()` is not.

The rest of the completion API: `complete(value)` completes it manually and returns whether *you* were
the one who did (that's the `Promise` half); `completeExceptionally(ex)` does the same with a failure;
`getNow(fallback)` never blocks; `isDone()` is true for success, failure **and** cancellation, so it is
not a success check.

And the one to say out loud: **`cancel()` does not interrupt the running task.**

→ `quiz/18` Q8
</details>

---

### Q9. Which synchroniser is **reusable** across rounds?

- **A.** `CountDownLatch`
- **B.** `Semaphore`
- **C.** Neither
- **D.** **`CyclicBarrier`** — it resets automatically after each trip

<details><summary>Answer</summary>

**D.** `CountDownLatch` is **one-shot**: once it hits zero it stays there forever, and there is no reset.
`CyclicBarrier` re-arms after every trip, which is what makes it right for iterative
simulations/round-based work.

The three, sharply:
- **`CountDownLatch(n)`** — "wait for n *events*". Asymmetric: waiters and counters are different
  threads.
- **`CyclicBarrier(n)`** — "wait for n *threads* to arrive". Symmetric, reusable, and takes an optional
  barrier action that runs on the last-arriving thread.
- **`Semaphore(n)`** — "at most n at once". Permit counting, not a rendezvous; `acquire`/`release` need
  not be the same thread, and it can be created **unfair** (barging, higher throughput) or fair.

`Semaphore` is the one used for real: bounding concurrent downstream calls, a crude bulkhead. Note
`release()` can be called without a matching `acquire()`, which *raises* the permit count — a real bug
source.

→ `quiz/18` Q9
</details>

---

### Q10. When one thread waiting at a `CyclicBarrier` is interrupted, what happens to the others?

- **A.** Nothing — they keep waiting
- **B.** They all receive **`BrokenBarrierException`**; the barrier is permanently broken until `reset()`
- **C.** They proceed as if the barrier tripped
- **D.** They receive `InterruptedException`

<details><summary>Answer</summary>

**B.** The barrier enters a **broken** state — by interruption, timeout, or a barrier action throwing —
and every current and subsequent waiter gets `BrokenBarrierException` until someone calls `reset()`
(which itself breaks the barrier for anyone currently waiting). This all-or-nothing failure mode is the
main reason `CyclicBarrier` is fragile in production code.

**`Phaser`** is the flexible alternative: a reusable barrier with a **dynamic** party count
(`register`/`arriveAndDeregister`), multiple **phases** with a monotonically increasing phase number,
hierarchical tiering to reduce contention, and `onAdvance` to control termination. It's strictly more
capable than both `CountDownLatch` and `CyclicBarrier` — and correspondingly harder to reason about, so
reach for it only when the party count genuinely changes at runtime.

→ `quiz/18` Q10
</details>

---

### Q11. Holding the read lock of a `ReentrantReadWriteLock`, you try to acquire the write lock. Result?

- **A.** **Deadlock** — upgrading is not supported
- **B.** It succeeds; the read lock is upgraded
- **C.** `IllegalMonitorStateException`
- **D.** It succeeds only if no other reader holds the lock

<details><summary>Answer</summary>

**A.** **Upgrade is not supported and will deadlock** — the thread waits for itself to release the read
lock. Downgrade (write → read) *is* supported: acquire read while holding write, then release write.
That's the documented idiom, and the asymmetry is a favourite question.

Other essentials:
- Readers share; a writer excludes everyone. So it only pays off when reads **massively** dominate and
  the critical section is non-trivial — otherwise the bookkeeping costs more than a plain
  `ReentrantLock`.
- Default is **unfair**, which permits **writer starvation** under continuous read load. Fair mode fixes
  that at a throughput cost.
- `StampedLock` (JDK 8) adds **optimistic reads** — `tryOptimisticRead` then `validate` — and is
  significantly faster for read-heavy work, but it is **not reentrant** and not directly usable with
  `Condition`.

Versus `synchronized`: `Lock` gives you `tryLock()` (with timeout), interruptible acquisition, non-block
-scoped locking, and multiple `Condition`s per lock. It costs you the **mandatory
`try`/`finally` unlock** — and on virtual threads it's the *preferred* choice, because `synchronized`
pins the carrier and `ReentrantLock` does not.

→ `quiz/18` Q11; `quiz/03` Q7
</details>

---

### Q12. `LockSupport.unpark(t)` called *before* `t` calls `park()` — what happens?

- **A.** The `unpark` is lost; `t` blocks
- **B.** `IllegalStateException`
- **C.** **The permit is remembered, so the subsequent `park()` returns immediately**
- **D.** `t` blocks until a second `unpark`

<details><summary>Answer</summary>

**C.** `LockSupport` holds a single **binary permit** per thread, and `unpark` is *not* lost if it
precedes `park`. That's precisely what makes it safe where `Object.wait`/`notify` is not — a `notify`
before a `wait` **is** lost, which is the classic missed-signal bug.

Other properties: `park()` may return **spuriously** at any time, so you must always re-check your
condition in a loop; it responds to interruption by returning (without throwing and **without clearing**
the flag).

It is the low-level primitive that **`AbstractQueuedSynchronizer`** is built on, and therefore what
underlies `ReentrantLock`, `Semaphore`, `CountDownLatch` and `FutureTask`. You'd rarely call it directly
— but knowing `AQS` sits on `park`/`unpark`, holds a `volatile int state` plus a CAS wait queue, is how
you answer "how does `ReentrantLock` actually work?"

→ `quiz/18` Q12
</details>

---

### Q13. `ConcurrentHashMap` vs `Collections.synchronizedMap`?

- **A.** They're equivalent in performance
- **B.** **`synchronizedMap` locks the whole map per operation; `ConcurrentHashMap` locks per bin and reads are lock-free**
- **C.** `synchronizedMap` allows concurrent reads
- **D.** `ConcurrentHashMap` throws `ConcurrentModificationException` and `synchronizedMap` doesn't

<details><summary>Answer</summary>

**B.** A single lock serialises *everything*, including reads. `ConcurrentHashMap` synchronises on the
individual bin for writes and reads without locking at all (via `volatile` node reads).

`ConcurrentHashMap` internals worth naming: **per-bin locking** on the head node (the pre-JDK-8 "segment"
answer is outdated), a CAS for the first insertion into an empty bin, `LongAdder`-style striped counting
for `size()`, and bins that convert from a list to a **red-black tree** past 8 entries.

Iterators are **weakly consistent**: they never throw CME, they reflect state at some point since
creation, and they tolerate concurrent modification. Hence **D** is backwards. Aggregate methods —
`size()`, `isEmpty()`, `containsValue()` — are **estimates** with no locking.

And the limit worth stating: atomic *per-entry*, not across entries. Two `put`s are not a transaction.
`ConcurrentHashMap` also **forbids null keys and values**, unlike `HashMap`, because `get() == null`
would be unresolvably ambiguous with no safe way to check-then-act.

→ `quiz/18` Q13; `quiz/16` Q19
</details>

---

### Q14. What is the hazard in `map.computeIfAbsent(k, key -> map.computeIfAbsent(other, …))`?

- **A.** Nothing — `ConcurrentHashMap` is reentrant
- **B.** It's slow but correct
- **C.** It throws `UnsupportedOperationException`
- **D.** **The mapping function runs while holding the bin lock, so a recursive update can deadlock or corrupt the map**

<details><summary>Answer</summary>

**D.** The remapping function is invoked **atomically, under the bin lock**. So three rules follow, all
documented and all violated regularly:

1. It must be **short** — every other writer to that bin is blocked meanwhile.
2. It must **not modify the same map** — recursion can deadlock or leave the table inconsistent. On
   modern JDKs `ConcurrentHashMap` throws `IllegalStateException: Recursive update` when it *detectably*
   would never complete — the Javadoc's own wording is "detectably", so it is **not** a reliable guard.
   (The check isn't specific to `computeIfAbsent` either: JDK 21u has nine identical throw sites, in
   `putVal`, `replaceNode`, `computeIfPresent`, `compute` and `merge` as well.)
3. It may be invoked **more than once** under contention on some `Map` implementations, so it must be
   side-effect-free.

Beyond the hazard, `computeIfAbsent` is the *right answer* to the check-then-act race: it replaces the
two-lookup, non-atomic `containsKey`/`get`/`put` sequence that `P04` measured — 8 threads, `expensive()`
invoked 8 times, 7 results discarded. `merge(k, v, fn)` is the equivalent for accumulation, and returning
`null` from any of these **removes** the entry.

→ `quiz/18` Q14; `quiz/04` Q9
</details>

---

### Q15. Why does `ConcurrentHashMap.newKeySet()` exist?

- **A.** **There is no `ConcurrentHashSet` class** — this is the concurrent `Set`
- **B.** It's faster than `HashSet`
- **C.** It provides an ordered set
- **D.** It's a read-only view

<details><summary>Answer</summary>

**A.** The JDK never shipped a `ConcurrentHashSet`. `ConcurrentHashMap.newKeySet()` is the intended
replacement, backed by a `ConcurrentHashMap` with `Boolean.TRUE` values — the same trick as
`Collections.newSetFromMap`, just built in.

`keySet(mappedValue)` gives you a view where `add` is permitted, inserting the supplied default value.
Plain `keySet()` on a map yields a view whose `add` throws `UnsupportedOperationException`.

The alternative for read-mostly sets is `CopyOnWriteArraySet`, but note it is **O(n) for `contains`**
because it's array-backed — fine for a handful of listeners, wrong for anything larger.

→ `quiz/18` Q15
</details>

---

### Q16. `SynchronousQueue` has what capacity?

- **A.** 1
- **B.** Unbounded
- **C.** **Zero** — every `put` must rendezvous with a `take`
- **D.** Configurable at construction

<details><summary>Answer</summary>

**C.** It holds nothing. A `put` blocks until a `take` arrives, and vice versa — a direct handoff. That's
exactly why **`newCachedThreadPool` uses it**: the queue always "rejects", so the pool creates a thread
for every task that has no idle thread waiting.

The family:

| Queue | Bound | Notes |
|---|---|---|
| `LinkedBlockingQueue` | optional — **unbounded by default** | two locks (head/tail), higher throughput |
| `ArrayBlockingQueue` | fixed, required | one lock, optional fairness, no allocation per element |
| `SynchronousQueue` | zero | handoff; `newCachedThreadPool` |
| `LinkedTransferQueue` | unbounded | adds `transfer()` — block until *consumed*, not just enqueued |

The **unbounded default** on `LinkedBlockingQueue` is the single most consequential fact here: it turns a
thread pool into an unbounded memory sink and makes backpressure impossible. `P05` measured it — the
queue grew to 100,000 entries while the pool stayed at its core size and never created a second thread.

`put`/`take` block, `offer`/`poll` return a boolean/null (optionally with timeout), `add`/`remove` throw.
Choose the family that matches your failure policy.

→ `quiz/18` Q16; `quiz/02` Q3
</details>

---

### Q17. `ConcurrentSkipListMap` vs `TreeMap`?

- **A.** Skip lists are O(1) lookup
- **B.** **Both are sorted and O(log n); the skip list is thread-safe and lock-free, the `TreeMap` is neither**
- **C.** `ConcurrentSkipListMap` is unsorted
- **D.** `TreeMap` is thread-safe if you only read

<details><summary>Answer</summary>

**B.** It's the concurrent `SortedMap`/`NavigableMap`. It uses a **probabilistic skip list** rather than a
red-black tree precisely because rebalancing a tree is very hard to do lock-free — a skip list needs only
localised CAS operations.

Practically: same `O(log n)` complexity class, but a higher constant factor and more memory (multiple
node levels), so single-threaded it loses to `TreeMap`. Choose it when you need
`headMap`/`tailMap`/`firstKey`/`ceilingKey` **and** concurrent access — e.g. a live order book keyed by
price, or a time-ordered event index.

`size()` is **O(n)** here, not O(1) — it walks the list. Don't call it in a loop.

**D** is a real misconception worth killing: a `TreeMap` is safe for concurrent *reads only if no thread
writes at all*, and "no writes" is a property of the whole program, not of one method.

→ `quiz/18` Q17
</details>

---

### Q18. When is `CopyOnWriteArrayList` the right choice?

- **A.** Write-heavy workloads
- **B.** Large collections needing fast random access
- **C.** Any time you'd use a `List` from multiple threads
- **D.** **Small, read-mostly collections** — listener/handler registries

<details><summary>Answer</summary>

**D.** Every mutation copies the entire backing array under a lock, so writes are **O(n)** and allocate.
Reads are lock-free and free of contention. That's a fantastic trade at 20 listeners registered at
startup and read constantly — and a disaster at 10,000 elements with regular writes.

The property that makes it pleasant: **iterators see an immutable snapshot** taken at creation. They
never throw `ConcurrentModificationException` (fail-safe rather than fail-fast), and correspondingly they
don't see later writes and `Iterator.remove()` is unsupported.

That snapshot semantics is also a subtle correctness trap: `for (Listener l : listeners) l.fire();` can
notify a listener that was removed mid-iteration.

`CopyOnWriteArraySet` is the same structure with `contains`-based dedup, hence **O(n) `contains`** —
`ConcurrentHashMap.newKeySet()` is usually the better set.

→ `quiz/18` Q18, Q19
</details>

---

### Q19. `Exchanger<V>` does what?

- **A.** **Lets exactly two threads swap objects at a rendezvous point**
- **B.** Transfers ownership of a lock
- **C.** Exchanges elements between two queues
- **D.** Swaps two threads' priorities

<details><summary>Answer</summary>

**A.** `exchange(myObject)` blocks until another thread calls `exchange` on the same `Exchanger`; each
receives the other's object. A `CyclicBarrier(2)` that also carries data.

The genuine use case is **buffer swapping** in a producer/consumer pair: the producer fills a buffer, the
consumer drains one, and they trade at the boundary — no allocation per batch, no queue.

Honest assessment: it's the rarest class in `java.util.concurrent`. Worth recognising, not worth
reaching for; a `BlockingQueue` of batches is clearer and nearly always fast enough.

→ `quiz/18` Q20
</details>

---

### Q20. `DelayQueue` — when can an element be taken?

- **A.** Immediately, in priority order
- **B.** After any other element has been taken
- **C.** **Only once its `getDelay()` returns ≤ 0**
- **D.** After the queue's global delay elapses

<details><summary>Answer</summary>

**C.** Elements must implement `Delayed`. `take()` blocks until the head's delay has expired, even if the
queue is non-empty — so `size()` counts elements you cannot yet get, and `poll()` returns `null` on a
non-empty queue. Both are routinely surprising.

It's the natural fit for **retry with backoff** and cache-expiry sweeping — much better than a
`ScheduledExecutorService` task per item when there are many items.

`PriorityBlockingQueue` is its unbounded, comparator-ordered sibling: **the iterator does not traverse in
priority order** (only `poll` does), and being unbounded it has the same memory-sink risk as
`LinkedBlockingQueue`.

→ `quiz/18` Q21
</details>

---

### Q21. Why avoid `Executors.newFixedThreadPool(n)`?

- **A.** It creates too many threads
- **B.** **Its queue is an unbounded `LinkedBlockingQueue`** — tasks accumulate without limit instead of applying backpressure
- **C.** It doesn't reuse threads
- **D.** It's deprecated

<details><summary>Answer</summary>

**B.** `newFixedThreadPool` and `newSingleThreadExecutor` both use an unbounded queue: under overload the
JVM OOMs rather than rejecting work. `newCachedThreadPool` has the opposite failure — unbounded
*threads*, `Integer.MAX_VALUE` of them, so it can OOM on thread stacks instead.

Two measured details on JDK 21.0.11 worth knowing: `newSingleThreadExecutor()` is returned **wrapped**, so
unlike `newFixedThreadPool(1)` it **cannot be cast to `ThreadPoolExecutor`** and reconfigured (verified:
`instanceof ThreadPoolExecutor` → `false` vs `true`). And `newWorkStealingPool()` isn't a
`ThreadPoolExecutor` at all — it's a `ForkJoinPool` at the **full** `availableProcessors()` with
`asyncMode` on, and it gives no ordering guarantee.

Construct `ThreadPoolExecutor` directly: explicit core/max sizes, a **bounded** queue, a named
`ThreadFactory` (so thread dumps are readable), and a deliberate `RejectedExecutionHandler` —
`CallerRunsPolicy` is the usual choice because it throttles the submitter naturally.

The admission algorithm is worth reciting, since it explains the counter-intuitive behaviour:
**(1)** below core size → new thread; **(2)** else try to **queue**; **(3)** only if the queue rejects →
new thread up to max; **(4)** else reject. So with an unbounded queue **step 3 never happens** and
`maximumPoolSize` is dead configuration. Measured in `P05`.

`newVirtualThreadPerTaskExecutor()` (JDK 21) is the modern answer for I/O-bound work — it isn't a pool at
all, and needs no sizing.

→ `quiz/18` Q22; `quiz/02` Q2, Q3
</details>

---

### Q22. `ForkJoinPool.commonPool().getParallelism()` typically returns?

- **A.** `availableProcessors()`
- **B.** `availableProcessors() * 2`
- **C.** 1
- **D.** **`availableProcessors() - 1`**

<details><summary>Answer</summary>

**D.** One fewer, because the *submitting* thread also helps out. Two measured caveats on JDK 21.0.11:

1. **The −1 is common-pool-only.** `new ForkJoinPool()` and `Executors.newWorkStealingPool()` both report
   the **full** `availableProcessors()` (measured: 14 cores → 14, 14, versus 13 for the common pool).
2. **On 1 CPU the getter lies.** With `-XX:ActiveProcessorCount=1`, `getCommonPoolParallelism()` returns
   **1**, but `supplyAsync` ran on `Thread-0`, `Thread-1`, `Thread-2` — a fresh thread per task, no
   workers. So don't say "parallelism is 0"; say **the pool falls back to a thread per task while the
   metric still reads 1**. That's the version of the claim that survives someone checking it.

`availableProcessors()` itself is worth a sentence — since JDK 10 it is **container-aware**, honouring
CPU limits rather than reporting host cores. Under a CFS quota that isn't a whole number of cores, you
get throttling that looks exactly like a GC pause in latency graphs.

**Work stealing** is the mechanism: each worker owns a **deque**, pushes and pops its own tasks LIFO (good
locality), and **steals from the tail** of a victim's deque when idle. That's why it excels at
divide-and-conquer with uneven subtasks.

`ManagedBlocker` is the escape hatch for blocking inside an FJP — it tells the pool to compensate by
temporarily adding a thread. It's exactly what Scala's `blocking { }` marker does, and the reason a
Scala `Future` doing JDBC must be wrapped in it.

Configure the common pool via `-Djava.util.concurrent.ForkJoinPool.common.parallelism=N`, ideally not at
all.

→ `quiz/18` Q23; `quiz/09` Q2
</details>

---

### Q23. Why must you call `ThreadLocal.remove()` in a servlet/thread-pool context?

- **A.** **Pooled threads are never discarded, so the entry survives the request and leaks — and can expose one user's data to the next**
- **B.** `ThreadLocal` isn't thread-safe
- **C.** `get()` fails on the second request
- **D.** The JVM caps `ThreadLocal` count

<details><summary>Answer</summary>

**A.** Two bugs for the price of one. The **leak**: `ThreadLocalMap` lives on the `Thread`, and a pooled
thread lives forever. The **correctness bug**, which is worse: request 2 on the same thread sees request
1's tenant/user/locale. Always `try { … } finally { tl.remove(); }`.

The reference structure is the detail interviewers probe: the map's **key is a weak reference** to the
`ThreadLocal`, but the **value is strong**. So even after the `ThreadLocal` object itself is collected,
the stale entry's value stays reachable from the live thread until something happens to clean it. Exactly
the `WeakHashMap` shape from `quiz/16` Q13. Measured in `P08`: 22 MB retained, 4 MB after `remove()`.

Second hazard: **`InheritableThreadLocal` copies to child threads at creation** — which does nothing
useful in a thread pool, where threads are created before your request and reused after it.

`ScopedValue` (JDK 21+, still a preview API) is the modern replacement: immutable, explicitly scoped via
`ScopedValue.where(V, val).run(...)`, automatically unbound at the end of the block, and cheap to inherit
across structured-concurrency forks. It exists largely because `ThreadLocal` scales badly to millions of
virtual threads.

→ `quiz/18` Q24; `quiz/04` Q21
</details>

---

### Q24. Why use `ThreadLocalRandom.current()` instead of a shared `Random`?

- **A.** It produces better-quality randomness
- **B.** `Random` isn't thread-safe
- **C.** **`Random`'s seed is a single `AtomicLong`, so concurrent `nextInt()` calls contend on CAS**
- **D.** `Random` can't be seeded

<details><summary>Answer</summary>

**C.** `Random` *is* thread-safe (so **B** is wrong) — and that's the problem. Every `next()` does a
`compareAndSet` on one shared `AtomicLong` seed, so under contention threads spin retrying and throughput
collapses. Classic false-sharing-adjacent contention on a single hot cache line.

`ThreadLocalRandom` gives each thread its own seed state: no sharing, no CAS, plus padding to avoid false
sharing. That makes `current()` effectively free — and note you must **never share the returned
instance** between threads, or you've reintroduced the problem without the safety.

Cost: you cannot seed it for reproducibility, which matters for tests. `SplittableRandom` is the choice
for parallel streams that need reproducible splitting; `SecureRandom` is the only correct choice for
anything security-relevant, and can block on entropy on Linux with `/dev/random`.

This is the same striping idea as **`LongAdder`**, which beats `AtomicLong` for contended counters for
exactly the same reason.

→ `quiz/18` Q25; `quiz/04` Q13
</details>

---

### Q25. A task submitted with `executor.submit(runnable)` throws. Is `Thread.UncaughtExceptionHandler` called?

- **A.** Yes, always
- **B.** **No** — `submit` wraps the task in a `FutureTask`, which captures the exception in the `Future`
- **C.** Only if the handler is set on the `ThreadFactory`
- **D.** Only for `Error`, not `Exception`

<details><summary>Answer</summary>

**B.** This is the standard "why did my exception vanish?" answer. `submit` catches everything and stores
it; you only see it when you call `get()`, which many callers never do. Nothing is logged.

`execute()` **does** reach the handler. So either use `execute` for fire-and-forget work, or override
`ThreadPoolExecutor.afterExecute(Runnable, Throwable)` — the standard fix, and note it must also unwrap a
`FutureTask` to see the failure.

Handler precedence: the thread's own handler → its `ThreadGroup`'s → the **default** set via
`Thread.setDefaultUncaughtExceptionHandler`. Set it in `main` for any production service; the default
behaviour is a stack trace to `System.err`, which in a container may go nowhere.

An uncaught exception kills only that thread, not the JVM. And the parallel gotcha:
`CompletableFuture` failures are also silently swallowed if nothing observes them.

→ `quiz/18` Q26; Q6
</details>

---

### Q26. `CompletionService` gives you what that a `List<Future>` doesn't?

- **A.** **Results in completion order, as they finish** — `take()` returns the next *completed* task
- **B.** Cancellation of all tasks at once
- **C.** A bounded queue
- **D.** Exception aggregation

<details><summary>Answer</summary>

**A.** Iterating `List<Future>` and calling `get()` blocks in **submission** order, so a slow first task
makes you wait even though later results are ready. `ExecutorCompletionService` wraps an executor and an
internal `BlockingQueue`, so `take()` hands you whatever finished first.

```java
var cs = new ExecutorCompletionService<Quote>(pool);
venues.forEach(v -> cs.submit(() -> fetch(v)));
for (int i = 0; i < venues.size(); i++) process(cs.take().get());   // completion order
```
Use it when you can act on partial results — first-response-wins quote fetching, or streaming results to
a client as they arrive. `poll(timeout)` gives you a deadline, so you can take what's ready and cancel
the rest.

`CompletableFuture.anyOf`/`allOf` covers much of this ground more expressively, so `CompletionService`
mostly shows up in older code. It remains the cleaner answer when you want *all* results but in
completion order.

Related timeouts: `TimeUnit` (`SECONDS.sleep(1)`, `toMillis`, `convert`) exists to make units explicit at
the call site. `TimeoutException` means "not done yet, still running" — it does **not** cancel anything —
whereas `InterruptedException` means someone asked you to stop and **clears the interrupt flag**, so
restore it.

→ `quiz/18` Q27, Q28
</details>

---

## Score yourself

- **21+** — the API-recall half is covered.
- **The six worth knowing cold:** Q4 (non-`Async` thread is unspecified), Q8 (`cancel` doesn't
  interrupt), Q11 (no lock upgrade), Q14 (`computeIfAbsent` runs under the bin lock), Q21 (unbounded
  queue), Q25 (`submit` swallows exceptions).
