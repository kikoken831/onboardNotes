# MCQ — LeetCode set, Q71–Q109

Drill deck for the rest of [`../03-core-q61-q90.md`](../03-core-q61-q90.md) and all of
[`../04-core-q91-q109.md`](../04-core-q91-q109.md).

---

### Q71. Which is true of `Optional`?

- **A.** `get()` is deprecated
- **B.** **It is not `Serializable`, and `get()` is discouraged by `@apiNote`, not deprecated**
- **C.** It implements `Serializable`
- **D.** It is designed as a field type

<details><summary>Answer</summary>

**B.** Both halves are worth getting exactly right, because the common answers are wrong. `Optional.get()`
carries **no `@Deprecated` annotation** in any release through mainline — only an `@apiNote` recommending
`orElseThrow()` (no-arg form since **JDK 10**). Call it soft-deprecated, not deprecated.

And it declares **no `implements` clause at all**, which is one solid reason not to use it as a field.

The "never use it as a field or parameter" rule is **JDK-team commentary, not documentation** — attribute
it that way. An `Optional` parameter gives callers three states, which is worse than two.

→ source Q71
</details>

---

### Q72. `cf.complete(value)` returns?

- **A.** The future
- **B.** `void`
- **C.** **`true` if this call was the one that completed it**
- **D.** The previous value

<details><summary>Answer</summary>

**C.** Which doubles as a race-winner test — that's how you implement "first response wins" across several
sources without extra coordination.

Companions: `getNow(fallback)` reads without blocking; `completeAsync`/`completeOnTimeout` (JDK 9) cover
"default after N ms" without a manual timer; `obtrudeValue` forcibly overwrites a completed result — a
debugging tool, don't ship it.

→ source Q72
</details>

---

### Q73. `Collectors.toMap` with a duplicate key does what?

- **A.** **Throws `IllegalStateException`** unless you pass a merge function
- **B.** Keeps the first
- **C.** Overwrites silently
- **D.** Returns a multimap

<details><summary>Answer</summary>

**A.** And assert the **type**, never the message — `toMap`'s message text changed in JDK 9.

It also **NPEs on a null value**, from a `requireNonNull` in the accumulator. Be precise if pushed: that's
implementation behaviour and **not** in `toMap`'s documented contract; only `toUnmodifiableMap` documents
the prohibition. Null **keys** are accepted, because the backing map is a `HashMap`.

`collect` is a **mutable** reduction (supplier/accumulator/combiner), which is why `collect(joining())` is
O(n) while `reduce("", String::concat)` is O(n²). And `stream.toList()` (JDK 16) returns an
**unmodifiable** list, unlike `collect(toList())`.

→ source Q73
</details>

---

### Q74. What must you always attach to a fire-and-forget `runAsync`?

- **A.** An executor
- **B.** A `thenRun`
- **C.** A timeout
- **D.** **`exceptionally` or `whenComplete`** — otherwise a failure is silently swallowed

<details><summary>Answer</summary>

**D.** An unobserved failed `CompletableFuture` logs nothing, unlike a raw thread's uncaught exception. With
a `Void` result there's nothing to consume, so it's easy to never notice.

(An executor is also strongly advisable — the one-arg form uses the shared common pool — but the silent
failure is the bug that bites.)

`runAsync(Runnable)` → `CompletableFuture<Void>`; `supplyAsync(Supplier)` → `CompletableFuture<T>`.

→ source Q74
</details>

---

### Q75. Which does **not** exist in `java.util.function`?

- **A.** `BiPredicate`
- **B.** `BiConsumer`
- **C.** **`TriFunction`**
- **D.** `BiFunction`

<details><summary>Answer</summary>

**C.** And there's no `BiSupplier` either. Past two parameters the JDK expects you to pass a **record** —
which is usually the better design anyway.

The three that do exist: `BiPredicate<T,U>` → `(T,U) -> boolean`; `BiConsumer<T,U>` → `(T,U) -> void`
(what `Map.forEach` and `whenComplete` take); `BiFunction<T,U,R>` → `(T,U) -> R` (what `Map.merge` and
`thenCombine` take).

→ source Q75
</details>

---

### Q76. Which gives you `finally` semantics on a `CompletableFuture`?

- **A.** `thenRun`
- **B.** **`whenComplete`**
- **C.** `exceptionally`
- **D.** `handle`

<details><summary>Answer</summary>

**B.** `whenComplete(BiConsumer<T,Throwable>)` always runs, sees both outcomes, and **leaves the result
unchanged** — the exception propagates onward.

The other three: `thenRun` **doesn't run on failure** (so `f.thenRun(sem::release)` leaks a permit);
`exceptionally` is `catch` — failure only, supplies a fallback; `handle` always runs, sees both, and **can
change the type**.

`handle` is a reasonable answer too — but it *consumes* the exception, so it's `catch`+transform, not
`finally`.

→ source Q76, Q77
</details>

---

### Q77. The throwable passed to `exceptionally` is usually?

- **A.** The original exception
- **B.** Wrapped in `ExecutionException`
- **C.** **Wrapped in `CompletionException`** — unwrap with `getCause()`
- **D.** An `Error`

<details><summary>Answer</summary>

**C.** Forget this and your `instanceof TimeoutException` check silently never matches — a genuinely common
bug.

Same for `orTimeout`, which completes the stage with a `TimeoutException` **wrapped** in
`CompletionException`.

(`ExecutionException` is the `Future.get()` wrapper — different API, same idea. Knowing which wrapper
belongs to which entry point is the actual distinction being tested.)

`exceptionallyCompose` (JDK 12) is the version for when your fallback is itself async.

→ source Q77
</details>

---

### Q78. `Map.forEach` takes which interface?

- **A.** **`BiConsumer<K,V>`**
- **B.** `Consumer<Map.Entry<K,V>>`
- **C.** `BiFunction<K,V,Void>`
- **D.** `Consumer<K>`

<details><summary>Answer</summary>

**A.** `positions.forEach((book, qty) -> log.info("{} = {}", book, qty))` — no `Entry` unwrapping needed.

`CompletableFuture.whenComplete(BiConsumer<T,Throwable>)` is the same interface, and a nice illustration of
the pattern "value **and** error, observe both, change neither".

Its only default method is `andThen`. Primitive variants are the odd-looking `ObjIntConsumer<T>`,
`ObjLongConsumer<T>`, `ObjDoubleConsumer<T>`.

On a `ConcurrentHashMap`, `forEach` is safe to read but gives you **no atomicity across entries**.

→ source Q78
</details>

---

### Q79. For a fixed fan-out of exactly two futures, prefer?

- **A.** `allOf`
- **B.** **`thenCombine`** — both values, typed
- **C.** `anyOf`
- **D.** Two sequential `thenCompose`s

<details><summary>Answer</summary>

**B.** `a.thenCombine(b, (x, y) -> new Settlement(x, y))` — no `Void`, no re-reading, no casting.

**A** works but returns `CompletableFuture<Void>`; **C** is untyped and completes on the first *failure*
too; **D** is the actual bug — chaining `thenCompose` **serialises** the two calls, so you lose the
concurrency entirely.

Note `thenCombine` composes, it doesn't start anything: build both futures eagerly (`supplyAsync`) so they
overlap.

`thenAcceptBoth` is the same shape with a `BiConsumer`; `runAfterBoth` ignores both values.

→ source Q79, Q80
</details>

---

### Q80. Which is the **typed** alternative to `anyOf` for two futures?

- **A.** `thenAcceptBoth`
- **B.** `runAfterBoth`
- **C.** `allOf`
- **D.** **`applyToEither` / `acceptEither`**

<details><summary>Answer</summary>

**D.** The "either" family mirrors the "both" family and keeps the type — which is generally what you
actually wanted from `anyOf`: `a.applyToEither(b, quote -> price(quote))`.

The full set: **both** → `thenCombine` (returns a value), `thenAcceptBoth` (consumes), `runAfterBoth`
(ignores). **Either** → `applyToEither`, `acceptEither`, `runAfterEither`.

Same failure semantics throughout: if the relevant input fails, the combined stage fails and the action
doesn't run — so cleanup belongs in `whenComplete`.

→ source Q80
</details>

---

### Q81. `MethodHandle`'s documented advantage over core reflection?

- **A.** It can access private members without `setAccessible`
- **B.** **The access check happens once, at `Lookup` time — not on every invocation**
- **C.** It works without a `SecurityManager`
- **D.** It's the only way to invoke a constructor reflectively

<details><summary>Answer</summary>

**B.** `Method.invoke` re-checks against the caller on **every** call; a `Lookup` checks once when it mints
the handle. That's the spec-level claim — treat "it's faster" as a consequence rather than a documented
guarantee, though a `static final` handle is designed to be JIT-inlinable.

**A** is wrong in an interesting way: the `Lookup` **carries** the rights of the class that created it, so
it can't grant access the creating context doesn't already have.

`invokeExact` requires the type descriptor to match **exactly**; `invoke` permits `asType` adaptations.
Why it exists: to support **`invokedynamic`**, which underpins lambdas, string concat (JDK 9+) and records.
Sibling: **`VarHandle`** (JDK 9), the supported replacement for `sun.misc.Unsafe`.

→ source Q81
</details>

---

### Q82. Which operation is unique to `ConcurrentSkipListMap` versus a `PriorityBlockingQueue`?

- **A.** **Range views** — `headMap`, `subMap`, `ceilingKey`, `descendingMap`
- **B.** Thread safety
- **C.** Ordered insertion
- **D.** O(log n) insertion

<details><summary>Answer</summary>

**A.** It implements **`ConcurrentNavigableMap`**, so you get live range views alongside
`pollFirstEntry`/`pollLastEntry` — atomically remove the extreme *and* query neighbours. That's what makes
it usable as an order book keyed by price.

A `PriorityBlockingQueue` orders only for `poll`/`take`: **its iterator does not traverse in priority
order**, and it has no range access.

Remember `size()` is **O(n)** here.

→ source Q82
</details>

---

### Q83. `PriorityBlockingQueue` — which is true?

- **A.** It's bounded by its initial capacity
- **B.** Its iterator traverses in priority order
- **C.** `put` blocks when full
- **D.** **It's unbounded, so it cannot apply backpressure**

<details><summary>Answer</summary>

**D.** `put` never blocks; under overload it grows until you OOM — the same memory-sink risk as
`LinkedBlockingQueue`'s unbounded default.

**B** is the other surprise: only `poll`/`take` respect ordering. The iterator, `toString` and `stream()`
do not — which trips people writing tests.

`take()` blocks only when the queue is **empty**, unlike `DelayQueue` where the head may exist but not yet
be available. Elements must be mutually comparable, and mutating a queued element's ordering field corrupts
the heap.

→ source Q83
</details>

---

### Q84. `TransferQueue.transfer(e)` differs from `put(e)` how?

- **A.** It's non-blocking
- **B.** It bypasses the queue order
- **C.** **It blocks until a consumer *receives* the element**, not merely until it's enqueued
- **D.** It's bounded

<details><summary>Answer</summary>

**C.** `put` returns as soon as the element is queued, so the producer learns nothing about progress.
`transfer` gives genuine **handoff** semantics — what you want for request/response over a queue, or to
stop a work item sitting unclaimed.

Also `tryTransfer(e)` (hand off only if a consumer is already waiting), `tryTransfer(e, timeout, unit)`,
`hasWaitingConsumer()`, `getWaitingConsumerCount()`.

Think of it as spanning `LinkedBlockingQueue` (always buffer) and `SynchronousQueue` (never buffer) — it
can do either, per operation. The JDK's only implementation is `LinkedTransferQueue`.

→ source Q84, Q86
</details>

---

### Q85. `SynchronousQueue`'s capacity is?

- **A.** 1
- **B.** Unbounded
- **C.** **Zero** — every `put` must rendezvous with a `take`
- **D.** Set at construction

<details><summary>Answer</summary>

**C.** It holds nothing: `isEmpty()` is always `true`, `size()` always `0`, `peek()` always `null`.

Where you meet it: **`Executors.newCachedThreadPool()` uses it.** Because an offer with no waiting consumer
always fails, the admission algorithm falls straight through to creating a thread — which is how a cached
pool reaches one thread per concurrent task, and why it can OOM on thread stacks with
`maximumPoolSize = Integer.MAX_VALUE`.

Use it directly when the producer must not run ahead of the consumer at all. Optionally fair (FIFO).

→ source Q85
</details>

---

### Q86. `LinkedTransferQueue` is the wrong choice when you need?

- **A.** Handoff semantics
- **B.** Lock-free throughput
- **C.** Both-ends access
- **D.** **Backpressure** — it is strictly unbounded

<details><summary>Answer</summary>

**D.** It has no capacity constructor at all. `LinkedBlockingQueue` at least *offers* a bound, which is what
you need at a system boundary.

Otherwise it's the better queue: a "dual queue" holding either waiting elements or waiting consumers,
matched by CAS with no locks on the fast path, and it can buffer (`put`) or rendezvous (`transfer`) per
operation. `take()` spins briefly before parking, so it does well when producers and consumers are roughly
balanced.

(**C** is `LinkedBlockingDeque`'s job.)

→ source Q86
</details>

---

### Q87. When is `LinkedBlockingDeque` the right choice?

- **A.** Maximum throughput under symmetric load
- **B.** **Work stealing, or urgent-first insertion without a comparator**
- **C.** Priority ordering
- **D.** Unbounded fan-out

<details><summary>Answer</summary>

**B.** Both ends, blocking: own end LIFO for locality and the other end for thieves; or `putFirst(urgent)`
versus `putLast(normal)` with no `Comparator` needed. It's also the only bounded **LIFO** blocking
structure — `ArrayBlockingQueue` can't be one.

**A** is wrong and is the trade-off: it uses **one lock for both ends**, so under heavy symmetric load
`LinkedBlockingQueue`'s **two-lock** design (separate head and tail) scales better.

Every operation comes in a First/Last pair across the three failure styles: `putX`/`takeX` block,
`offerX`/`pollX` return boolean/null, `addX`/`removeX` throw.

→ source Q87, Q106
</details>

---

### Q88. `ScopedValue` improves on `ThreadLocal` how?

- **A.** **Immutable, lexically scoped, unbound automatically at block exit — so it can't leak**
- **B.** It's mutable and thread-safe
- **C.** It's inherited by all threads
- **D.** It's faster to write

<details><summary>Answer</summary>

**A.** `ScopedValue.where(CTX, ctx).run(() -> handle(req))` — no `finally`, no `remove()`, no possibility of
one request seeing another's context.

It exists largely because `ThreadLocal` scales badly to millions of virtual threads (a per-thread map per
virtual thread is a lot of maps), and because it's cheap to inherit across structured-concurrency forks.

State it accurately: **still a preview API as of August 2026** (JDK 21+), so it needs `--enable-preview`.

→ source Q88
</details>

---

### Q89. `newCachedThreadPool()`'s failure mode?

- **A.** Unbounded queue → heap OOM
- **B.** **Unbounded threads** → `OutOfMemoryError: unable to create native thread`
- **C.** Deadlock
- **D.** Tasks silently dropped

<details><summary>Answer</summary>

**B.** `ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, SECONDS, new SynchronousQueue<>())` — the
zero-capacity queue forces a new thread for every task with no idle worker.

It's the mirror image of `newFixedThreadPool`'s unbounded **queue** (heap OOM). Neither applies
backpressure; they just fail differently.

Suits short-lived, bursty, **non-blocking** work where the burst is naturally bounded. On JDK 21,
`newVirtualThreadPerTaskExecutor()` is the honest version of what people wanted here.

→ source Q89
</details>

---

### Q90. Which can be cast to `ThreadPoolExecutor` and reconfigured?

- **A.** **`newFixedThreadPool(1)`**
- **B.** `newSingleThreadExecutor()`
- **C.** Both
- **D.** Neither

<details><summary>Answer</summary>

**A.** Measured on JDK 21.0.11: `newFixedThreadPool(1) instanceof ThreadPoolExecutor` → **`true`**;
`newSingleThreadExecutor() instanceof ThreadPoolExecutor` → **`false`**.

`newSingleThreadExecutor` is returned **wrapped** in a delegating class precisely so nobody can widen it to
more threads and break the sequencing guarantee.

Both share the unbounded-`LinkedBlockingQueue` memory risk. And if the single thread dies from an uncaught
exception, a **new one replaces it** — quietly, which is why you want an `afterExecute` hook that logs.

→ source Q90, Q91
</details>

---

### Q91. What does `newSingleThreadExecutor()` guarantee that a 1-thread pool built by hand might not?

- **A.** Lower latency
- **B.** Exception logging
- **C.** A bounded queue
- **D.** **Sequential execution, protected by the wrapper that prevents reconfiguration**

<details><summary>Answer</summary>

**D.** Its value is exactly that guarantee: **serial execution without locks**, so state touched only by
tasks on this executor needs no synchronisation — a single file writer, an ordered event applier.

A hand-built `ThreadPoolExecutor(1, 1, …)` gives the same behaviour but can be widened later by anyone
holding the reference, silently breaking every assumption that depended on serialisation.

**C** is wrong — its queue is unbounded, same risk as `newFixedThreadPool`.

→ source Q91
</details>

---

### Q92. `Future.get()` throws `InterruptedException`. What must you do?

- **A.** Log and continue
- **B.** Wrap it in a `RuntimeException`
- **C.** **Propagate it, or restore the flag with `Thread.currentThread().interrupt()`**
- **D.** Retry the `get()`

<details><summary>Answer</summary>

**C.** Catching it **clears** the interrupt status, so swallowing it makes the thread unstoppable — the
classic un-shutdownable worker.

The three exceptions from `get()`: `ExecutionException` (the task threw — real cause in `getCause()`),
`InterruptedException` (**you** were interrupted), `CancellationException` (it was cancelled).

**Always prefer `get(timeout, unit)`.** An unbounded `get()` is how a pool deadlocks — a task that `get()`s
on another task submitted to the same single-threaded pool waits forever.

→ source Q92, Q108
</details>

---

### Q93. `future.isDone()` returns `true` when?

- **A.** Only on success
- **B.** On success or failure
- **C.** **On success, failure, or cancellation**
- **D.** Once the task has started

<details><summary>Answer</summary>

**C.** Which is exactly why **`isDone()` is not a success check** — you still have to call `get()` and
handle the exceptions, or check `isCancelled()` first.

It's also useless as a spin condition: `while (!f.isDone()) {}` burns a core. Use `get(timeout, unit)`, or
`CompletableFuture` callbacks so you never poll.

Legitimate uses: a non-blocking status check for an endpoint, and filtering a batch for the futures you can
read without blocking.

→ source Q93
</details>

---

### Q94. `f.cancel(true)` on a **running** `FutureTask` stops it when?

- **A.** **Only if the task checks `Thread.interrupted()` or blocks in an interruptible call**
- **B.** Immediately
- **C.** Never
- **D.** At the next safepoint

<details><summary>Answer</summary>

**A.** Interruption is **cooperative**. A tight compute loop or most socket reads run to completion
regardless — `Thread.stop()` was deprecated for good reasons and is now removed.

If the task hasn't started, `cancel` prevents it from ever running. Afterwards `isCancelled()` and
`isDone()` are both `true` and `get()` throws `CancellationException`.

Use `isCancelled()` to distinguish "cancelled" from "failed" when draining a batch, so you don't log a
cancellation as an error. And remember `CompletableFuture.cancel()` never interrupts at all.

→ source Q94
</details>

---

### Q95. `CompletionService` gives you?

- **A.** Cancellation of all tasks at once
- **B.** **Results in completion order — `take()` returns the next *finished* task**
- **C.** Exception aggregation
- **D.** A bounded queue

<details><summary>Answer</summary>

**B.** Iterating `List<Future>` and calling `get()` blocks in **submission** order, so one slow first task
makes you wait even though later results are ready.

```java
var cs = new ExecutorCompletionService<Quote>(pool);
venues.forEach(v -> cs.submit(() -> fetch(v)));
for (int i = 0; i < venues.size(); i++) process(cs.take().get());
```

Use it when you can act on partial results — first-response-wins fetching, streaming to a client as results
arrive. `poll(timeout)` gives you a deadline so you can take what's ready and cancel the rest.

`CompletableFuture` covers much of this more expressively, but this remains the cleanest way to get **all**
results **in completion order**.

→ source Q95
</details>

---

### Q96. `new LinkedBlockingQueue<>()` has what capacity?

- **A.** 16
- **B.** It's a required constructor argument
- **C.** 0
- **D.** **`Integer.MAX_VALUE`** — effectively unbounded

<details><summary>Answer</summary>

**D.** The single most consequential default in `java.util.concurrent`: it's what makes
`newFixedThreadPool` and `newSingleThreadExecutor` memory sinks, and what makes `maximumPoolSize` dead
configuration because the queue never rejects.

Pass a capacity to get backpressure: `new LinkedBlockingQueue<>(1000)` — now `put()` blocks, which is the
point.

Its **two-lock** design (separate head and tail) is why it scales better than `ArrayBlockingQueue`'s single
lock under symmetric load. Method families: `put`/`take` block, `offer`/`poll` return boolean/null,
`add`/`remove` throw. At a boundary prefer `offer(e, timeout, unit)` so you can shed load deliberately.

→ source Q96
</details>

---

### Q97. Returning `null` from `computeIfPresent`'s function does what?

- **A.** Leaves the value unchanged
- **B.** **Removes the entry**
- **C.** Stores `null`
- **D.** Throws NPE

<details><summary>Answer</summary>

**B.** Which makes it the idiomatic **atomic conditional remove**. (Storing `null` is impossible — CHM
forbids null values.)

The four-method family:

| Method | Runs when | `null` return |
|---|---|---|
| `computeIfAbsent` | key **absent** | leave absent |
| `computeIfPresent` | key **present** | **remove** |
| `compute` | always | remove / stay absent |
| `merge` | always; fn only if present | remove |

Same contract throughout: the function runs **atomically under the bin lock**, so keep it short, don't
modify the same map, and no I/O.

→ source Q97
</details>

---

### Q98. `Phaser.arriveAndDeregister()` does what?

- **A.** Waits for the phase to advance
- **B.** Resets the phase number
- **C.** Terminates the phaser
- **D.** **Arrives and reduces the party count**, without waiting

<details><summary>Answer</summary>

**D.** That's what makes a dynamic party count work: a task that's finished for good stops being counted,
so the remaining parties aren't waiting for it.

Contrast `arrive()` (arrive, don't wait, stay registered) and `arriveAndAwaitAdvance()` (arrive and block
until everyone has). `forceTermination()` is the abort; `onAdvance` returning `true` is the graceful stop.

Escalate from a latch to a `Phaser` when the party count genuinely changes at runtime, you need multiple
rounds **plus** termination, or you have enough parties to want **tiering**.

→ source Q98
</details>

---

### Q99. A `CyclicBarrier`'s barrier action runs on which thread?

- **A.** A pool thread
- **B.** The first to arrive
- **C.** **The last to arrive**, before any party is released
- **D.** The thread that constructed the barrier

<details><summary>Answer</summary>

**C.** Which makes it a safe place to merge per-round results or advance shared state without extra locking
— nothing else in the barrier is running at that moment.

```java
var barrier = new CyclicBarrier(workers, () -> grid.commitRound());
```

If the action **throws**, the barrier breaks and everyone gets `BrokenBarrierException`. `getParties()` is
fixed for life; `getNumberWaiting()` and `isBroken()` are for inspection.

→ source Q99
</details>

---

### Q100. Which is the standard way to maximise the chance a race shows up in a test?

- **A.** **A count-1 `CountDownLatch` as a starting gun**, so all threads begin simultaneously
- **B.** Run the test 1000 times
- **C.** `Thread.sleep` between starts
- **D.** Raise thread priorities

<details><summary>Answer</summary>

**A.** Every worker `await()`s the same latch; one `countDown()` releases them at once. That's what every
concurrency test in `problems/java/` uses, paired with a count-n latch as the finish line.

**B** helps but is weaker on its own — and the deeper lesson from `P06` is that **a passing concurrency
test proves almost nothing**: the buggy queue there passed a stress test, and only a deliberately forced
interleaving reproduced the bug every run.

Put `countDown()` in a `finally` and use `await(timeout, unit)`.

→ source Q100
</details>

---

### Q101. You catch `BrokenBarrierException`. The right response?

- **A.** Retry `await()`
- **B.** **Treat the round as dead** — clean up and abandon or `reset()` from a coordinator
- **C.** Ignore it
- **D.** Call `countDown()`

<details><summary>Answer</summary>

**B.** Looping back into `await()` is a spin: the barrier is still broken until a successful `reset()`, and
`reset()` itself breaks it for anyone currently waiting.

Causes: a waiting thread interrupted, a waiting thread timing out, the barrier action throwing, or someone
calling `reset()` mid-wait.

Which is the honest argument for `Phaser` or a per-round latch in production — one flaky worker shouldn't
poison a shared synchroniser for everyone.

→ source Q101
</details>

---

### Q102. `TimeoutException` from `f.get(300, MILLISECONDS)` means?

- **A.** The task was cancelled
- **B.** The task failed
- **C.** **The task is still running** — nothing was cancelled
- **D.** The executor is shut down

<details><summary>Answer</summary>

**C.** So the correct handler is almost always two lines:

```java
try { return f.get(300, MILLISECONDS); }
catch (TimeoutException e) { f.cancel(true); return fallback(); }
```

Distinguish the neighbours: **`TimeoutException`** = "not done yet, still running";
**`InterruptedException`** = "someone asked *you* to stop", and it **clears the interrupt flag** so restore
it; **`CompletableFuture.orTimeout`** completes the stage with a `TimeoutException` **wrapped in
`CompletionException`**.

Every remote call in a trading system needs one of these paths wired.

→ source Q102
</details>

---

### Q103. Why does every timed method in `java.util.concurrent` take `(long, TimeUnit)`?

- **A.** **To make the unit explicit at the call site** rather than hidden in a `long`
- **B.** Backwards compatibility
- **C.** Because `Duration` didn't exist until JDK 11
- **D.** For precision above milliseconds

<details><summary>Answer</summary>

**A.** It removed a whole class of bug: a bare `await(30)` where the reader can't tell seconds from
milliseconds.

Useful members beyond the constants: `SECONDS.sleep(1)`, `timedWait`, `timedJoin`, `convert`, and the
`toXxx` converters. Conversions **truncate** rather than round, and saturate rather than wrapping on
overflow.

`java.time.Duration` (JDK 8, so **C** is wrong) is the modern choice for new APIs. And for elapsed time
always use **`System.nanoTime()`** — monotonic — never `currentTimeMillis()`, which can jump backwards on
NTP correction.

→ source Q103
</details>

---

### Q104. Under heavy contention, prefer which counter?

- **A.** `AtomicLong`
- **B.** `volatile long`
- **C.** `synchronized` getter/setter
- **D.** **`LongAdder`**

<details><summary>Answer</summary>

**D.** Measured in `problems/java/P02`: `LongAdder` beat `AtomicLong` by roughly **4×** at high contention —
it stripes across padded cells and only sums on `sum()`. Costs: slower uncontended, and `sum()` is
approximate during concurrent updates.

`AtomicLong` uses **CAS**: read, compute, swap only if unchanged, retry otherwise — so under contention
threads spin on one hot cache line.

**B** is the classic wrong answer: `volatile` gives visibility but `count++` is read-modify-write, so
increments are still lost.

Also know **ABA**: CAS sees "unchanged" if a value goes A → B → A. `AtomicStampedReference` adds a version
to fix it.

→ source Q104
</details>

---

### Q105. `DelayQueue.poll()` on a **non-empty** queue can return `null`. Why?

- **A.** A bug
- **B.** **The head hasn't matured** — its `getDelay()` is still positive
- **C.** Only if another thread raced you
- **D.** Only when the queue is being drained

<details><summary>Answer</summary>

**B.** And `take()` blocks for the same reason, even though `size()` is non-zero. Elements you cannot yet
get still count.

Elements implement `Delayed`: `getDelay(unit)` plus a `compareTo` ordering by due time.

Natural fits: **retry with backoff**, cache-expiry sweeping, scheduled settlement instructions — one thread
and one queue instead of a `ScheduledExecutorService` task per item. Being **unbounded**, it can't apply
backpressure.

→ source Q105
</details>

---

### Q106. `BlockingDeque`'s inherited `put`/`take` map to?

- **A.** `putFirst`/`takeLast`
- **B.** They throw; you must use the explicit forms
- **C.** `putFirst`/`takeFirst` (LIFO)
- **D.** **`putLast`/`takeFirst`** — so it behaves as FIFO by default

<details><summary>Answer</summary>

**D.** So a `BlockingDeque` used through the `BlockingQueue` API is an ordinary FIFO queue — you opt into
LIFO or double-ended behaviour explicitly.

Every operation exists as a First/Last pair in three failure styles: `addX` throws, `offerX` returns a
boolean, `putX` blocks (plus timed `offerX`). Same for removal.

The JDK's only implementation is **`LinkedBlockingDeque`**. If you don't need both ends,
`LinkedBlockingQueue`'s two-lock design scales better.

→ source Q106
</details>

---

### Q107. Which `RejectedExecutionHandler` throttles the submitter?

- **A.** **`CallerRunsPolicy`**
- **B.** `DiscardPolicy`
- **C.** `DiscardOldestPolicy`
- **D.** `AbortPolicy`

<details><summary>Answer</summary>

**A.** It runs the task **on the submitting thread**, so the submitter can't submit again while it's busy —
natural backpressure, and usually the right choice.

`AbortPolicy` (the default) throws `RejectedExecutionException`; the two `Discard` policies silently drop
work, which is almost never what you want in a settlement pipeline.

The rest of what to configure deliberately: a **bounded** queue, a **named** `ThreadFactory` (so thread
dumps are readable), `beforeExecute`/`afterExecute` hooks (the standard place to log swallowed exceptions
and clean `ThreadLocal`s), `allowCoreThreadTimeOut(true)`, and `shutdown()` (drain) versus `shutdownNow()`
(interrupt and return the queue).

→ source Q107
</details>

---

### Q108. In a `take()` loop, catching `InterruptedException` and continuing does what?

- **A.** Nothing harmful
- **B.** Causes a busy-wait
- **C.** **Clears the interrupt flag, making the thread unstoppable**
- **D.** Loses one queue element

<details><summary>Answer</summary>

**C.** The classic un-shutdownable worker. Either restore the flag and exit, or propagate:

```java
try { t = queue.take(); }
catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
```

`take()` blocks indefinitely while empty. Siblings: `poll()` returns `null` at once,
`poll(timeout, unit)` waits then returns `null`, `remove()` throws `NoSuchElementException`. Use
`poll(timeout, unit)` when the worker also needs periodic housekeeping.

→ source Q108
</details>

---

### Q109. For a task whose duration varies, prefer?

- **A.** `scheduleAtFixedRate`
- **B.** A `Timer`
- **C.** `schedule` in a loop
- **D.** **`scheduleWithFixedDelay`**

<details><summary>Answer</summary>

**D.** Fixed **rate** schedules by start time, so an overrun makes subsequent runs fire back-to-back trying
to catch up. Fixed **delay** measures from the end of the previous run, so it can never pile up.

Two hazards specific to a scheduled executor: **an uncaught exception silently cancels the repeating
task** — the schedule stops, nothing is logged, your heartbeat is just gone, so wrap the body in
try/catch; and one slow task delays everything else on a single-threaded scheduler.

It also isn't a cron: no calendar semantics, no persistence, no cluster coordination, and it drifts across
DST. Use Quartz or `@Scheduled(cron=…)` for business-time scheduling — and on a distributed system, an
external scheduler, since every instance would otherwise fire its own copy.

→ source Q109
</details>
