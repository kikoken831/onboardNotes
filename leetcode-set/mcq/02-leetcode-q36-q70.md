# MCQ — LeetCode set, Q36–Q70

Drill deck for the rest of [`../02-core-q31-q60.md`](../02-core-q31-q60.md) and the start of
[`../03-core-q61-q90.md`](../03-core-q61-q90.md).

---

### Q36. `supplyAsync(supplier)` with no executor runs where?

- **A.** **`ForkJoinPool.commonPool()`** — shared with every `parallelStream()` in the JVM
- **B.** A new thread per call
- **C.** The calling thread
- **D.** A dedicated `CompletableFuture` pool

<details><summary>Answer</summary>

**A.** Sized to `cores − 1`, so one blocking JDBC call there starves unrelated code. **Always pass your own
executor for blocking work.**

Measured edge case on JDK 21.0.11 with `-XX:ActiveProcessorCount=1`: the common pool stops using workers
and hands each task a **brand-new thread** (`Thread-0`, `Thread-1`, …) — while
`getCommonPoolParallelism()` still reports 1, so metrics hide it.

An exception inside the supplier completes the future exceptionally rather than propagating.

→ source Q36
</details>

---

### Q37. `filter` vs `map`?

- **A.** **`filter` keeps the type and may reduce the count; `map` keeps the count and may change the type**
- **B.** Both may change count and type
- **C.** `filter` changes the type
- **D.** `map` is eager

<details><summary>Answer</summary>

**A.** `filter(Predicate<T>) → Stream<T>`; `map(Function<T,R>) → Stream<R>`, exactly n out of n.

`flatMap` is the third and the one to be able to explain: it maps each element to a **stream** and
concatenates, so it's the only one that changes **both** type and count.

Both are **lazy**: nothing runs until a terminal operation, then each element flows through the whole chain
once (fusion), with short-circuiting where possible.

→ source Q37
</details>

---

### Q38. `f.thenApply(fn)` without `Async` — which thread runs `fn`?

- **A.** Always the common pool
- **B.** Always the caller
- **C.** Always the thread that created `f`
- **D.** **Unspecified** — the completing thread, or the caller if the stage is already done

<details><summary>Answer</summary>

**D.** The most important `CompletableFuture` subtlety. A long non-`Async` callback can hijack a Netty I/O
thread, and the non-determinism makes it hard to test — the same code takes different paths depending on a
race.

Rule: if the callback is non-trivial, use `thenApplyAsync(fn, myExecutor)`.

The trio: `thenApply(Function)` transforms, `thenAccept(Consumer)` consumes, `thenRun(Runnable)` ignores
the value.

→ source Q38, Q53, Q76
</details>

---

### Q39. `join()` vs `get()`?

- **A.** `join()` doesn't block
- **B.** **`join()` throws unchecked `CompletionException`; `get()` throws checked `ExecutionException`/`InterruptedException`**
- **C.** They're identical
- **D.** `get()` returns `null` on failure

<details><summary>Answer</summary>

**B.** Both block. The exception contract is the difference — which is why `join()` works inside a lambda
and `get()` doesn't:

```java
allOf(futures).thenApply(v -> Arrays.stream(futures).map(CompletableFuture::join).toList());
```

Safe there because `allOf` has already guaranteed completion. Calling `join()` on a pending future from a
common-pool thread can deadlock the pool.

Unwrap `getCause()` before matching on exception type.

→ source Q39, Q92
</details>

---

### Q40. `CountDownLatch` — which is true?

- **A.** It resets automatically when it reaches zero
- **B.** Waiters and counters must be the same threads
- **C.** **It is one-shot: once at zero it stays there, with no reset**
- **D.** `getCount()` blocks

<details><summary>Answer</summary>

**C.** That's the defining difference from `CyclicBarrier`, which re-arms after every trip. Need another
round? Construct a new latch.

It's also **asymmetric** — counters and waiters are different threads — which is what makes the two
standard idioms work: a count-1 **starting gun** to release all workers simultaneously (how every test in
`problems/java/` maximises race visibility), and a count-n **finish line**.

Put `countDown()` in a `finally` and use `await(timeout, unit)`, or one missed count hangs every waiter
forever.

→ source Q40, Q100
</details>

---

### Q41. A thread waiting at a `CyclicBarrier` is interrupted. The others get?

- **A.** **`BrokenBarrierException`** — the barrier is broken until `reset()`
- **B.** `InterruptedException`
- **C.** Nothing — they keep waiting
- **D.** They proceed as if it tripped

<details><summary>Answer</summary>

**A.** A barrier breaks on interruption, timeout, or a throwing barrier action — and then **every** current
and subsequent waiter fails until `reset()` (which itself breaks it for anyone waiting).

So one flaky worker takes down the whole round. Treat `BrokenBarrierException` as "the round is dead", not
as retryable — looping back into `await()` just spins.

That all-or-nothing failure model is the honest argument for a `Phaser` or a per-round latch in production.

→ source Q41, Q99, Q101
</details>

---

### Q42. `ClassLoader.defineClass()` is?

- **A.** `public static`
- **B.** `private`
- **C.** **`protected final`** — callable only from inside a `ClassLoader` subclass
- **D.** Deprecated for removal

<details><summary>Answer</summary>

**C.** All four overloads. Which is why the modern, public alternatives exist:

- **`MethodHandles.Lookup.defineClass(byte[])`** (`@since 9`) — requires **PACKAGE** lookup mode, defines
  the class with the **same loader, package and protection domain** as the lookup class, and links it
  **without running the static initialiser**.
- **`Lookup.defineHiddenClass(bytes, initialize, options)`** (`@since 15`) — needs full privilege access
  and yields a class with **no binary name**, undiscoverable by `Class.forName`/`loadClass`/`findClass`.

Related pair: `ClassNotFoundException` (checked, reflective load failed) vs `NoClassDefFoundError` (present
at compile time, missing or failed initialisation at runtime).

→ source Q42
</details>

---

### Q43. Holding the read lock of a `ReentrantReadWriteLock`, you take the write lock. Result?

- **A.** It succeeds if no other reader holds it
- **B.** It upgrades
- **C.** `IllegalMonitorStateException`
- **D.** **Deadlock** — upgrade is not supported

<details><summary>Answer</summary>

**D.** The thread waits for itself to release the read lock. **Downgrade** (write → read, then release
write) *is* supported and is the documented idiom — that asymmetry is a favourite question.

Two more: the default is **unfair**, permitting **writer starvation** under continuous read load; and it
only pays off when reads massively dominate and the critical section is non-trivial — otherwise a plain
`ReentrantLock` is faster, and `StampedLock`'s optimistic read faster still.

→ source Q43, Q57
</details>

---

### Q44. `ForkJoinPool`'s work stealing means each worker?

- **A.** Shares one global queue
- **B.** **Owns a deque — LIFO on its own end, stealing from the tail of others**
- **C.** Steals from the head of others
- **D.** Round-robins tasks

<details><summary>Answer</summary>

**B.** Own-end LIFO gives cache locality (the most recently forked subtask is hottest); stealing from the
**tail** takes the oldest, largest chunk from a victim, which balances well with uneven subtasks.

`ManagedBlocker` is the escape hatch for blocking inside an FJP — it tells the pool to compensate with an
extra thread. That's exactly what Scala's `blocking { }` marker does, which is the bridge to the Scala side
of this role.

Backs `parallelStream()`, `CompletableFuture`'s default executor, and `Arrays.parallelSort`.

→ source Q44
</details>

---

### Q45. What's the classic `Semaphore` bug?

- **A.** `acquire` and `release` must be on the same thread
- **B.** It can't be fair
- **C.** **A `release()` with no matching `acquire()` raises the permit count**, silently widening the limit
- **D.** `tryAcquire` always fails

<details><summary>Answer</summary>

**C.** A `Semaphore` isn't ownership-based, so nothing checks the pairing. One stray `release()` in a
retry path and your "max 10 concurrent" quietly becomes 11 forever. Always `release()` in a `finally`.

**A** is wrong and it's the *feature*: `acquire` and `release` may be different threads, which is what makes
it usable as a bulkhead:
```java
if (!sem.tryAcquire(50, MILLISECONDS)) throw new OverloadedException();
```
It can be fair (FIFO) or unfair (barging, the default). `Semaphore(1)` is a non-reentrant mutex.

→ source Q45
</details>

---

### Q46. `computeIfAbsent`'s mapping function runs?

- **A.** Outside any lock, possibly twice
- **B.** Lazily, on first read
- **C.** On a pool thread
- **D.** **Atomically, while holding the bin lock — at most once**

<details><summary>Answer</summary>

**D.** So three documented rules follow: keep it **short** (other writers to that bin are blocked), **don't
modify the same map** inside it, and don't put I/O in it.

The `IllegalStateException: Recursive update` guard only fires when the recursion is **detectably**
non-terminating — the Javadoc's own word — so it's not a reliable safety net. It isn't specific to
`computeIfAbsent` either; JDK 21u has nine identical throw sites.

Its value: it replaces the check-then-act race that `P04` measured (8 threads, loader ran 8 times, 7
results discarded). Returning `null` removes/leaves-absent the entry.

→ source Q46, Q97
</details>

---

### Q47. `ConcurrentLinkedDeque` is?

- **A.** Blocking and bounded
- **B.** **Lock-free and unbounded** — never blocks, no capacity
- **C.** Blocking and unbounded
- **D.** Lock-free and bounded

<details><summary>Answer</summary>

**B.** CAS-based, both ends, no blocking and no capacity limit.

The four-way distinction to have ready: `ConcurrentLinkedQueue`/`Deque` are lock-free and unbounded;
`LinkedBlockingQueue`/`Deque` block and can be bounded.

Choose lock-free when a consumer must never block (you'd poll and do something else if empty); choose
blocking when you want producer/consumer handoff with **backpressure**. `size()` on the lock-free ones is
**O(n)** and only an estimate — never use it for control flow.

→ source Q47, Q106
</details>

---

### Q48. `StringBuffer` vs `StringBuilder`?

- **A.** **Identical APIs; `StringBuffer` synchronises every method and is legacy**
- **B.** `StringBuilder` is immutable
- **C.** `StringBuffer` is faster
- **D.** Different APIs

<details><summary>Answer</summary>

**A.** Use `StringBuilder`. A builder is nearly always a thread-confined local, so the synchronisation is
pure overhead — and a shared `StringBuffer` is usually a design mistake anyway, since per-method atomicity
doesn't make a *sequence* of appends atomic.

The related question they're really testing: **`String` `+` in a loop is O(n²)**, because each `+=` copies
everything so far. The compiler optimises `+` **within a single expression** (`StringConcatFactory`,
`invokedynamic`, JDK 9+) but **cannot** across loop iterations.

→ source Q48
</details>

---

### Q49. A task submitted with `executor.submit(task)` throws. Does `UncaughtExceptionHandler` fire?

- **A.** **No** — `submit` wraps it in a `FutureTask` that captures the exception in the `Future`
- **B.** Yes, always
- **C.** Only for `Error`
- **D.** Only if set on the `ThreadFactory`

<details><summary>Answer</summary>

**A.** The standard "why did my exception vanish?" answer. Nothing is logged; you only see it if someone
calls `get()`.

`execute()` **does** reach the handler. Otherwise override
`ThreadPoolExecutor.afterExecute(Runnable, Throwable)` — and note it must unwrap the `FutureTask` to see
the failure.

Precedence: thread's own handler → its `ThreadGroup`'s → the **default** via
`setDefaultUncaughtExceptionHandler`. Set the default in `main`; the fallback prints to `System.err`, which
in a container may go nowhere. An uncaught exception kills only that thread.

→ source Q49
</details>

---

### Q50. What must you never do with `ThreadLocalRandom.current()`?

- **A.** Call it in a loop
- **B.** **Store or share the returned instance across threads**
- **C.** Use it for bounded ranges
- **D.** Call it from a virtual thread

<details><summary>Answer</summary>

**B.** It returns the **calling thread's own** instance. Caching it in a field and using it from other
threads reintroduces the contention you were avoiding — and the races, without the safety.

Call `current()` at each use site; it resolves to a field read on the `Thread`, so it's effectively free.

Also: `setSeed` throws `UnsupportedOperationException` (not reproducible), and it adds API `Random` lacks —
bounded `nextLong(origin, bound)`, `nextDouble(origin, bound)`, and the `ints()`/`longs()` stream
factories.

→ source Q50
</details>

---

### Q51. `Arrays.parallelSort(byte[] a)` on a 1,000,000-element array?

- **A.** Parallelises above 8192 elements
- **B.** Parallelises above 4096 elements
- **C.** Throws for primitive arrays
- **D.** **Never parallelises** — that path has no parallelism parameter at all

<details><summary>Answer</summary>

**D.** Read out of JDK 21.0.11's `src.zip`: `parallelSort(byte[])` calls
`DualPivotQuicksort.sort(a, low, high)` — no parallelism argument exists. Same for `char[]` and `short[]`.

The thresholds, precisely:

| Overload | Threshold |
|---|---|
| `T[]` / `T[]` + `Comparator` | **8192** (`Arrays.MIN_ARRAY_SORT_GRAN`) — object paths only |
| `int[]`, `long[]`, `float[]`, `double[]` | **4096** (`DualPivotQuicksort.MIN_PARALLEL_SORT_SIZE`), plus `parallelism > 1` |
| `byte[]`, `char[]`, `short[]` | never |

So the widely-quoted "8192" is object-arrays-only. It uses the **common pool** and needs O(n) extra space.

→ source Q51
</details>

---

### Q52. Your function returns a `CompletableFuture`. Use which?

- **A.** `thenApply`
- **B.** `thenAccept`
- **C.** **`thenCompose`** — it flattens
- **D.** `thenCombine`

<details><summary>Answer</summary>

**C.** `thenApply` would give you `CompletableFuture<CompletableFuture<T>>`.

```java
findUser(id).thenCompose(this::loadAccount);   // CompletableFuture<Account>
```

This is the `map` vs `flatMap` distinction, and it's the same idea as `Stream.map`/`flatMap`,
`Optional.map`/`flatMap` and Scala `Future.map`/`flatMap`. Recognising it as **one** pattern rather than
four API quirks is what an FP question is actually probing.

→ source Q52
</details>

---

### Q53. `thenRun(action)` runs when the upstream stage **fails**?

- **A.** Yes, always
- **B.** **No** — the chain short-circuits, so "cleanup" never happens
- **C.** Only for unchecked exceptions
- **D.** Only with an executor

<details><summary>Answer</summary>

**B.** A real bug: `f.thenRun(sem::release)` leaks a permit on failure.

For `finally` semantics use **`whenComplete`**, which always runs, sees both outcomes, and leaves the
result unchanged:
```java
f.whenComplete((v, ex) -> sem.release());
```

The family: `exceptionally` = `catch` (failure only, supplies a fallback); `handle` = both, and can change
the type; `whenComplete` = `finally`, changes nothing and **propagates** the exception.

→ source Q53, Q76, Q77
</details>

---

### Q54. `anyOf(a, b, c)` completes with?

- **A.** The first **successful** result
- **B.** A list of all results
- **C.** **The first outcome — including the first failure**
- **D.** The last to finish

<details><summary>Answer</summary>

**C.** Which makes it a poor fit for hedged requests unless each input swallows its own failure first
(`.exceptionally(ex -> null)` then filter).

It's also **untyped** — `CompletableFuture<Object>`, because inputs may differ — and it does **not cancel**
the losers, which keep running.

For two typed futures prefer `applyToEither`/`acceptEither`; for real hedging on JDK 21, structured
concurrency's "first successful" joiner expresses it properly.

→ source Q54
</details>

---

### Q55. `allOf(a, b, c)` returns?

- **A.** **`CompletableFuture<Void>`** — you must re-read each input
- **B.** `CompletableFuture<Object>`
- **C.** `CompletableFuture<List<T>>`
- **D.** `CompletableFuture<Stream<T>>`

<details><summary>Answer</summary>

**A.** Hence the idiom:
```java
allOf(futures).thenApply(v -> Arrays.stream(futures).map(CompletableFuture::join).toList());
```

It completes **exceptionally if any input fails**, but does **not cancel** the others and still waits for
all of them. For "collect the successes, ignore failures", attach `.exceptionally(...)` to each input
*before* passing it in.

For a fixed, small fan-out prefer **`thenCombine`** — both values typed, no re-reading.

→ source Q55
</details>

---

### Q56. Why does `ConcurrentHashMap.newKeySet()` exist?

- **A.** It's faster than `HashSet`
- **B.** It's read-only
- **C.** It's an ordered set
- **D.** **There is no `ConcurrentHashSet` class** — this is the concurrent `Set`

<details><summary>Answer</summary>

**D.** Backed by a `ConcurrentHashMap` with `Boolean.TRUE` values — the same trick as
`Collections.newSetFromMap`, built in.

The `mappedValue` form matters for `add`: `keySet(V mappedValue)` returns a view whose `add` inserts that
value, usable only when one value serves every addition, and it **throws NPE if `mappedValue` is null**.
The no-arg `keySet()` view has a null mapped value, so its `add`/`addAll` throw
`UnsupportedOperationException`.

It's a **view**: removals write through. The alternative, `CopyOnWriteArraySet`, is **O(n) `contains`**.

→ source Q56
</details>

---

### Q57. The JDK ships how many `ReadWriteLock` implementations?

- **A.** Three
- **B.** Two, including `StampedLock`
- **C.** **One** — `ReentrantReadWriteLock`
- **D.** None; it's an SPI

<details><summary>Answer</summary>

**C.** `StampedLock` deliberately does **not** implement the interface — it isn't reentrant and its modes
are stamp-based — though it offers `asReadLock()`/`asWriteLock()` adapter views.

The interface itself is just two methods returning `Lock`s. All the interesting behaviour — fairness,
reentrancy, starvation, the illegal upgrade — belongs to the implementation, which is why coding to
`ReadWriteLock` documents intent but protects you from nothing.

→ source Q57
</details>

---

### Q58. What does `Lock` give you that `synchronized` doesn't?

- **A.** Mutual exclusion
- **B.** A happens-before edge
- **C.** Reentrancy
- **D.** **`tryLock` with timeout, interruptible acquisition, non-block-scoped locking, multiple `Condition`s**

<details><summary>Answer</summary>

**D.** `synchronized` already gives you **A**, **B** and **C**.

What it costs: the `try`/`finally` is on you — forget `unlock()` and the lock is held forever, whereas
`synchronized` releases even on exception.

Two reasons it matters here: `tryLock` + randomised backoff is the standard alternative to global lock
ordering for deadlock avoidance (measured in `problems/java/P03`), and on **JDK 21 virtual threads**
`ReentrantLock` is preferred because `synchronized` **pins** the carrier.

→ source Q58
</details>

---

### Q59. `CopyOnWriteArrayList` iterators?

- **A.** Throw `ConcurrentModificationException`
- **B.** **Iterate an immutable snapshot** — never throw, don't see later writes, `remove()` unsupported
- **C.** Are weakly consistent
- **D.** Require external synchronisation

<details><summary>Answer</summary>

**B.** That's fail-**safe** rather than fail-fast, and it has a subtle consequence:
`for (var l : listeners) l.fire();` can notify a listener that was removed mid-iteration.

Cost profile: every mutation copies the whole backing array under a lock, so writes are **O(n)** and
allocate, while reads are lock-free. Right for **small, read-mostly** collections — listener registries
populated at startup. Wrong for 10,000 elements with regular writes.

→ source Q59
</details>

---

### Q60. `CopyOnWriteArraySet.contains()` is?

- **A.** **O(n)** — it's array-backed with no hashing
- **B.** O(log n)
- **C.** O(1) — it hashes
- **D.** Unsupported

<details><summary>Answer</summary>

**A.** It's backed by a `CopyOnWriteArrayList`, so uniqueness is enforced by **scanning**: `add` costs O(n)
for the scan plus O(n) for the copy.

Use it only for tiny, read-mostly sets where snapshot iteration is the point. For anything else
**`ConcurrentHashMap.newKeySet()`** is the better concurrent `Set` — O(1) `contains`, per-bin write
locking.

→ source Q60
</details>

---

### Q61. Which composes a `Predicate` correctly?

- **A.** `p.andThen(q)`
- **B.** `p.plus(q)`
- **C.** `p.compose(q)`
- **D.** `p.and(q)`, `p.or(q)`, `p.negate()`, and static `Predicate.not(p)`

<details><summary>Answer</summary>

**D.** `Predicate.not(p)` is JDK 11 and is the idiomatic way to avoid a clumsy lambda:
`filter(Predicate.not(String::isBlank))`.

**C** and **A** belong to `Function` — and their order is the classic mix-up: `f.andThen(g)` is `g(f(x))`,
`f.compose(g)` is `f(g(x))`. `Consumer` has only `andThen`, because there's nothing to compose out of.

Primitive specialisations exist to dodge boxing: `IntPredicate`, `LongPredicate`, `DoublePredicate`.

→ source Q61, Q62, Q64
</details>

---

### Q62. `stream().forEach(list::add)` on a **parallel** stream is?

- **A.** Fine — `forEach` is synchronised
- **B.** **A data race** unless the list is thread-safe
- **C.** A compile error
- **D.** Fine if the list is pre-sized

<details><summary>Answer</summary>

**B.** `Consumer` exists to mutate something, which is exactly where stream code goes wrong. Use
`collect(toList())` instead — it handles the parallel merge for you via the combiner.

Two related facts: on a parallel stream `forEach` does **not** preserve encounter order (use
`forEachOrdered`), and `collect(toList())` **does** preserve it.

`BiConsumer<T,U>` is the two-argument version — what `Map.forEach` and
`CompletableFuture.whenComplete` take.

→ source Q62, Q78
</details>

---

### Q63. Why does `orElseGet(this::load)` beat `orElse(load())`?

- **A.** It's null-safe
- **B.** It can throw
- **C.** **`orElse`'s argument is evaluated eagerly, even when the `Optional` is present**
- **D.** `orElse` is deprecated

<details><summary>Answer</summary>

**C.** `orElse` takes a *value*, so `load()` runs on every call — including the common case where the
result is discarded. With a DB call as the default that's a real, easily-missed cost.

That's the point of `Supplier`: it is **deferred computation**. Same reason
`CompletableFuture.supplyAsync` and `ThreadLocal.withInitial` take one, and why
`log.debug(() -> expensiveDump())` is the right shape.

There is deliberately **no `BiSupplier`** — zero inputs can't be doubled.

→ source Q63
</details>

---

### Q64. `f.andThen(g)` means?

- **A.** **`g(f(x))`**
- **B.** `f(g(x))`
- **C.** `f(x)` then discard
- **D.** Both applied to `x` independently

<details><summary>Answer</summary>

**A.** And `f.compose(g)` is `f(g(x))` — the reverse. Getting these backwards is the standard slip.

Family relations worth naming: `UnaryOperator<T>` **is** `Function<T,T>`; `BiFunction<T,U,R>` takes two
(what `Map.merge` and `thenCombine` take); `BinaryOperator<T>` is `BiFunction<T,T,T>` (what `reduce`
takes). Primitive variants �� `ToIntFunction`, `IntFunction` — exist purely to avoid boxing.

`Function.identity()` is the one you'll reach for with `Collectors.toMap`.

→ source Q64
</details>

---

### Q65. Wiring a callback API to a `CompletableFuture` — what's the bug to avoid?

- **A.** **Wiring only the success path**, so a failure leaves it pending forever
- **B.** Using `complete` instead of `completeExceptionally`
- **C.** Calling `complete` twice
- **D.** Returning the future before completing it

<details><summary>Answer</summary>

**A.** The caller hangs with no error anywhere. Wire both, and add a timeout as a safety net:

```java
var cf = new CompletableFuture<Ack>();
amps.subscribe(id, msg -> cf.complete(parse(msg)),
                   err -> cf.completeExceptionally(err));
return cf.orTimeout(2, SECONDS);
```

`complete`/`completeExceptionally` are the **`Promise`** half of the class; both return `true` if *this*
call was the transition, which doubles as a race-winner test. Scala splits these into `Future` (read) and
`Promise` (write), which is arguably the better design.

→ source Q65, Q72
</details>

---

### Q66. `CompletableFuture.cancel(true)` interrupts the worker?

- **A.** Yes
- **B.** **No** — the flag is ignored and the work continues
- **C.** Only on JDK 21+
- **D.** Only for `runAsync`

<details><summary>Answer</summary>

**B.** It just completes the future with `CancellationException`. `FutureTask.cancel(true)` *does*
interrupt — that asymmetry surprises people migrating from `ExecutorService`.

For real cancellation: keep the `Future` from `submit(...)`, check `Thread.interrupted()` inside long work,
or use structured concurrency on JDK 21, where scope exit interrupts unfinished subtasks.

Note interruption only helps if the task **checks** it or blocks in an interruptible call — a tight compute
loop or most socket reads run to completion regardless.

→ source Q66
</details>

---

### Q67. `list.stream().peek(print).count()` on JDK 21 prints?

- **A.** Every element
- **B.** Every element, unordered
- **C.** The count
- **D.** **Possibly nothing** — `count()` may skip traversing the source

<details><summary>Answer</summary>

**D.** `count()`'s `@apiNote` permits computing the size directly from the source, in which case no
elements are traversed and no intermediate operations run. That's a **JDK 9+** behaviour; 8u documented
`count()` as strictly equivalent to `mapToLong(e -> 1L).sum()`.

`peek` is also skipped for elements a short-circuiting op never produces. So **`peek` is for debugging
only**. To observe *and* keep the stream, use `map(x -> { log(x); return x; })`, which cannot be elided.

And with **no terminal operation at all**, nothing runs — the other half of the trap.

→ source Q67
</details>

---

### Q68. `Exchanger` is used by?

- **A.** Any number of threads
- **B.** One thread and a queue
- **C.** **Exactly two threads, swapping objects at a rendezvous**
- **D.** A pool and its workers

<details><summary>Answer</summary>

**C.** Each calls `exchange(myObject)` and receives the other's — effectively a `CyclicBarrier(2)` that
carries data.

The genuine use is **buffer swapping**: the producer fills one buffer while the consumer drains the other,
and they trade at the boundary — no per-batch allocation, no queue.

Honest assessment: the rarest class in `java.util.concurrent`. Worth recognising, not worth reaching for —
a `BlockingQueue` of batches is clearer and nearly always fast enough.

→ source Q68
</details>

---

### Q69. On a 1-CPU container, `ForkJoinPool.getCommonPoolParallelism()` returns?

- **A.** **1 — but the pool actually gives each task a brand-new thread**
- **B.** 0
- **C.** The host's core count
- **D.** −1

<details><summary>Answer</summary>

**A.** Measured on JDK 21.0.11 with `-XX:ActiveProcessorCount=1`: the getter returns **1** because it
floors the value, while `supplyAsync` ran on `Thread-0`, `Thread-1`, `Thread-2` — no workers at all.

So describe the **behaviour**, not the number: *"it degrades to a thread per task while the metric still
reads 1."* Saying "parallelism is 0" is checkable and wrong.

Two more measured facts: the default is `availableProcessors() − 1` (14 cores → **13**) because the
submitting thread helps; and **the −1 is common-pool-only** — `new ForkJoinPool()` and
`newWorkStealingPool()` both report the full **14**. `availableProcessors()` itself is container-aware
since JDK 10, derived from the cgroup **quota**.

→ source Q69
</details>

---

### Q70. `Cloneable` is?

- **A.** An interface declaring `clone()`
- **B.** An annotation
- **C.** An abstract class
- **D.** **An empty marker interface** — it declares nothing

<details><summary>Answer</summary>

**D.** `public interface Cloneable { }`. It declares no `clone()` at all — it merely signals to
`Object.clone()` that a field-for-field copy is legal. Without it, `clone()` throws
`CloneNotSupportedException`.

The rest of the critique: the copy is **shallow**, so nested mutable state is shared; it **bypasses
constructors**, so invariants and `final` assignment are skipped; and the identity invariants
(`x.clone() != x`, same class) are documented as "typically true", **not absolute requirements**.

Use a **copy constructor** or **static factory** instead — or design the type immutable and never need a
copy. Josh Bloch, who wrote `Cloneable`, recommends against it.

→ source Q70
</details>
