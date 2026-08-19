# MCQ — LeetCode set, Q1–Q35

Drill deck for [`../01-core-q1-q30.md`](../01-core-q1-q30.md) and the start of
[`../02-core-q31-q60.md`](../02-core-q31-q60.md). Numbers map to the source list.

---

### Q1. `Integer a = 128, b = 128; a == b` is?

- **A.** `true` — autoboxing caches all wrapper values
- **B.** A compile error
- **C.** `false` — 128 is outside the cache, so two distinct objects
- **D.** Platform-dependent

<details><summary>Answer</summary>

**C.** The cache is **−128..127** and it's a **JLS §5.1.7 requirement**, not an implementation detail —
OpenJDK asserts `IntegerCache.high >= 127`. At 127 the same expression is `true`, which is why the bug
survives testing.

Only the **upper** bound is tunable (`-XX:AutoBoxCacheMax`); the lower is `static final`. `Long`, `Short`,
`Byte` and `Character` (0–127) cache the same range; `Boolean` caches both values.

→ source Q1
</details>

---

### Q2. Which collector is **not** in the JDK 21 lineup?

- **A.** CMS — removed in JDK 14
- **B.** Epsilon
- **C.** Shenandoah
- **D.** ZGC

<details><summary>Answer</summary>

**A.** CMS was deprecated in JDK 9 and **removed in 14**. Naming it as a current option dates you, and
it's a common slip because most older material still discusses it.

JDK 21 ships **Serial**, **Parallel**, **G1** (default), **ZGC** (generational since 21),
**Shenandoah**, **Epsilon** (no-op).

Choose by requirement: batch throughput → Parallel; a latency SLA → G1 then ZGC; huge heap with a strict
tail → ZGC.

→ source Q2
</details>

---

### Q3. Which is a **checked** exception?

- **A.** `IllegalStateException`
- **B.** `InterruptedException`
- **C.** `NullPointerException`
- **D.** `OutOfMemoryError`

<details><summary>Answer</summary>

**B.** Everything under `Exception` **except** `RuntimeException` and its subclasses is checked.
`InterruptedException` extends `Exception` directly.

It's also the one you can't handle carelessly: **propagate it or restore the flag** with
`Thread.currentThread().interrupt()`. Catching it **clears** the interrupt status, destroying the only
cancellation signal the JVM has.

"Catch or specify" is a **compiler** rule, not a JVM one — which is why Scala and Kotlin ignore it.

→ source Q3
</details>

---

### Q4. On JDK 21 virtual threads, which pins the carrier thread?

- **A.** `ReentrantLock`
- **B.** Neither
- **C.** Both
- **D.** `synchronized`

<details><summary>Answer</summary>

**D.** A virtual thread blocking inside `synchronized` **pins** its carrier, so the platform thread can't
be reused — enough pinned carriers and you deadlock a system that would have been fine with locks.
`ReentrantLock` unmounts properly.

That's the most actionable JDK 21 migration fact: **audit `synchronized` blocks that block.** Diagnose
with `-Djdk.tracePinnedThreads=full`.

`synchronized` still gives mutual exclusion **plus** a happens-before edge, and uncontended it's cheap.

→ source Q4
</details>

---

### Q5. Inside a lambda, `this` refers to?

- **A.** The **enclosing** instance
- **B.** `null`
- **C.** The lambda object
- **D.** A synthetic wrapper

<details><summary>Answer</summary>

**A.** The sharpest difference from an anonymous inner class, where `this` is the anonymous instance.

The others: no separate `.class` file (an `invokedynamic` call site linked by `LambdaMetafactory`); a
non-capturing lambda can be cached as a singleton while `new Runnable(){}` allocates each time; a lambda
can hold no fields.

Both capture only effectively-final locals.

→ source Q5
</details>

---

### Q6. A `transient` field after deserialisation holds?

- **A.** The value from the constructor
- **B.** Its **type default** — `null`/`0`/`false`
- **C.** The value it had when serialised
- **D.** It throws `NotSerializableException`

<details><summary>Answer</summary>

**B.** Because **deserialisation does not run the constructor**, so constructor-assigned defaults never
happen. Restore it in a custom `readObject`, or compute it lazily.

The three modifiers that get asked together: `transient` = **serialisation**, `volatile` =
**concurrency**, `static` = **scope**. Their only overlap is that neither `transient` nor `static` fields
are serialised — so `static transient` is redundant.

→ source Q6, Q15
</details>

---

### Q7. When is `finalize()` guaranteed to run?

- **A.** At the next full GC
- **B.** Just before memory is reclaimed
- **C.** On JVM shutdown
- **D.** **Never** — there is no guarantee it runs at all

<details><summary>Answer</summary>

**D.** The JVM may exit first. It also needs two GC cycles, one slow finalizer stalls the shared queue and
can OOM you with reclaimable garbage, it can **resurrect** the object, and exceptions in it are swallowed.

Status precisely: deprecated in **JDK 9**, deprecated **for removal** in **18** (JEP 421, which added
`--finalization=disabled`). Still present and enabled by default — not removed.

Use `try`-with-resources / `AutoCloseable`, or `Cleaner` as a native-memory safety net. **GC manages
memory, not resources.**

→ source Q7
</details>

---

### Q8. Which is **not** polymorphic?

- **A.** An overridden instance method
- **B.** A method reached through an interface reference
- **C.** A `static` method
- **D.** An abstract method implemented by a subclass

<details><summary>Answer</summary>

**C.** Static methods are **hidden**, not overridden, and dispatch by **compile-time** type — so one
called through a superclass-typed reference runs the superclass version regardless of the runtime object.

Also not polymorphic: **fields** (hidden, resolved by declared type) and `private`/`final` methods
(statically bound). And **overloading** is compile-time resolution — not polymorphism at all.

→ source Q8
</details>

---

### Q9. Which is **not** true of `static`?

- **A.** `static` fields are not serialised
- **B.** A `static` nested class needs no enclosing instance
- **C.** A `static` method can be overridden
- **D.** `static` initialiser blocks run once, at class initialisation

<details><summary>Answer</summary>

**C.** They're **hidden**, not overridden — see Q8.

The rest are true and worth knowing. **B** in particular: default to a `static` nested class, because a
non-static inner class holds an implicit `Outer.this` and can pin the enclosing object in memory — a
classic leak.

The cost of `static` to name: static state is **global mutable state** — hard to test, and a shared
mutable static field is a concurrency bug waiting to happen.

→ source Q9
</details>

---

### Q10. Since JDK 9, an interface may contain?

- **A.** `default`, `static` and **`private`** methods
- **B.** `default` and `static` methods only
- **C.** Instance fields
- **D.** Constructors

<details><summary>Answer</summary>

**A.** `default` and `static` arrived in 8; **`private`** (including `private static`) in **9**, so
`default` methods can share helpers without exposing them.

Still impossible: instance fields (interface fields are implicitly `public static final`) and
constructors — the real difference from an abstract class being that **an interface cannot hold state.**

Default to an interface; abstract class when subtypes must share fields or constructor logic. Since 17,
often better still: a **`sealed` interface** with `record` implementations.

→ source Q10
</details>

---

### Q11. `protected` grants access to?

- **A.** Subclasses only
- **B.** Exactly the same set as default
- **C.** The declaring class only
- **D.** **Package classes and subclasses**

<details><summary>Answer</summary>

**D.** The detail most people get wrong: `protected` is strictly **wider** than default — a same-package
class needs no inheritance relationship at all. (Confirmed by compiling it.)

Ordering, narrowest to widest: `private` → default → `protected` → `public`.

Subtlety (JLS §6.6.2.1): from a *different* package, a subclass may access a protected instance member
only **through a reference of its own type**, not through an arbitrary superclass reference.

→ source Q11
</details>

---

### Q12. `abstract final class X {}` — what happens?

- **A.** It compiles; the class becomes a static-only utility
- **B.** **Compile error** — the modifiers contradict
- **C.** It compiles only for nested classes
- **D.** It compiles with a warning

<details><summary>Answer</summary>

**B.** `abstract` = must be extended; `final` = cannot be. Also illegal: `abstract final` methods, and
`abstract` + `private`/`static`.

The genuine middle ground since JDK 17 is **`sealed`** — extension permitted, but only by classes you
name. That's what enables exhaustive `switch` pattern matching, and it's the right tool for an ADT such as
a `TradeEvent` hierarchy.

→ source Q12
</details>

---

### Q13. When is `LinkedList` genuinely the better choice over `ArrayList`?

- **A.** Frequent random access
- **B.** **Almost never** — realistically only when you need `List` *and* `Deque` on one object
- **C.** Frequent removal from the middle
- **D.** Large collections

<details><summary>Answer</summary>

**B.** And saying so is a better answer than reciting the complexity table.

**C** is the textbook answer and it's misleading: `LinkedList` removal is O(1) only **given the node**, and
you must walk O(n) to find it. Pointer-chasing also destroys cache locality, so `ArrayList` usually wins
even where the big-O says otherwise. For queue-like use, `ArrayDeque` beats both.

→ source Q13
</details>

---

### Q14. What breaks if you mutate a field used in `hashCode()` while the object is in a `HashMap`?

- **A.** Nothing — `HashMap` rehashes automatically
- **B.** `ConcurrentModificationException`
- **C.** The entry becomes **unreachable** — still stored, but not findable
- **D.** The map throws on the next `get`

<details><summary>Answer</summary>

**C.** The entry sits in the bucket chosen by the **old** hash, while lookups compute the **new** one. It's
in the map, `size()` counts it, and you cannot get it out — the classic mutable-key bug.

Hence the contract: equal objects **must** have equal hash codes, and the value must not change while the
object is in a hash collection. Always override `hashCode` and `equals` together on the same fields —
`Objects.hash(...)`, or use a `record`.

Implementation detail worth knowing: `HashMap` spreads the hash (`h ^ (h >>> 16)`) because buckets are a
power of two, and a bucket becomes a **red-black tree** past 8 entries, degrading collisions to O(log n).

→ source Q14
</details>

---

### Q15. Which fields does default Java serialisation write?

- **A.** All fields
- **B.** All except `transient`
- **C.** All except `static`
- **D.** All except `transient` **and** `static`

<details><summary>Answer</summary>

**D.** Default serialisation covers only **non-static, non-transient** fields.

A `static` field appears to survive only because it's whatever the receiving JVM already has loaded —
nothing was written. A `transient` field comes back as its type default.

Related rule: **always declare `serialVersionUID` explicitly** (`private static final long`). Without it
the compiler computes one from the class structure, so adding a field changes it and deserialising old
data throws **`InvalidClassException`**.

→ source Q15
</details>

---

### Q16. `Callable` differs from `Runnable` how?

- **A.** It **returns a value and may throw a checked exception**
- **B.** It runs on a pooled thread
- **C.** It isn't a functional interface
- **D.** It cannot be submitted to an executor

<details><summary>Answer</summary>

**A.** `call()` returns `V` and `throws Exception`; `run()` returns `void` and throws nothing checked.
Both are functional interfaces.

That dictates error handling. A `Callable`'s exception is **captured in the `Future`** and surfaces only on
`get()`, wrapped in `ExecutionException`.

The trap: `submit(Runnable)` also wraps in a `FutureTask`, so **an exception from a submitted `Runnable`
is silently swallowed too.** Use `execute()` for fire-and-forget, or override `afterExecute`.

→ source Q16
</details>

---

### Q17. Why must you call `ThreadLocal.remove()` in a thread-pool context?

- **A.** `ThreadLocal` isn't thread-safe
- **B.** `get()` fails on the second use
- **C.** The JVM caps `ThreadLocal` count
- **D.** **The pooled thread outlives the request** — so the value leaks *and* the next request sees it

<details><summary>Answer</summary>

**D.** Two bugs for one mistake. The **leak**: `ThreadLocalMap` lives on the `Thread`, which lives forever.
The **correctness bug**, which is worse: request 2 on the same thread sees request 1's user or tenant.

Always `try { … } finally { tl.remove(); }`. Measured in `problems/java/P08`: 22 MB retained, 4 MB after
`remove()`.

The structure interviewers probe: the map's **key is a weak reference**, the **value is strong** — the same
shape as `WeakHashMap`, which is why stale entries persist. And `InheritableThreadLocal` copies at thread
*creation*, so it's useless in a pool.

→ source Q17, Q88
</details>

---

### Q18. `comparing(Trade::getBook).thenComparing(Trade::getId).reversed()` sorts how?

- **A.** **Both keys descending** — `reversed()` reverses the whole comparator
- **B.** Book ascending, id descending
- **C.** Book descending, id ascending
- **D.** Compile error

<details><summary>Answer</summary>

**A.** `reversed()` applies to **everything built so far**, not just the last key. That's the classic bug.

To reverse one key only, pass a comparator to that `thenComparing`:
```java
comparing(Trade::getBook).thenComparing(Trade::getId, reverseOrder())
```

Also: `comparingInt`/`comparingLong` avoid boxing; `nullsFirst`/`nullsLast` tolerate nulls; and never
`(a,b) -> a.x - b.x` on ints — it overflows, and an inconsistent comparator makes TimSort throw
`IllegalArgumentException: Comparison method violates its general contract!`.

→ source Q18, Q32
</details>

---

### Q19. `Class.forName("X")` vs `loader.loadClass("X")`?

- **A.** Identical
- **B.** **`forName` initialises** (runs static blocks); `loadClass` does not
- **C.** `loadClass` initialises; `forName` only links
- **D.** `forName` works only on the bootstrap classpath

<details><summary>Answer</summary>

**B.** `forName(String)` is documented as exactly `forName(name, true, currentLoader)`, where
`currentLoader` is the **defining loader of the calling class** — *not* the thread context loader. Use
`forName(name, false, loader)` for load-without-initialise.

That's why the old JDBC idiom `Class.forName("com.mysql.jdbc.Driver")` worked — it ran the registering
static block. Unnecessary since JDBC 4.0 (`ServiceLoader`).

Documented nuance almost nobody has: `forName("[Lcom.Foo;")` for an **array** type loads `Foo` but
**never initialises** it, whatever the flag says.

→ source Q19
</details>

---

### Q20. `System.gc()` is best described as?

- **A.** A command that forces a full GC
- **B.** A way to collect only the young generation
- **C.** A **hint** the JVM may ignore
- **D.** Removed in JDK 11

<details><summary>Answer</summary>

**C.** The Javadoc calls it a suggestion. In practice HotSpot usually *does* respond — with a **full
stop-the-world GC**, which is why it destroys tail latency and why `-XX:+DisableExplicitGC` exists.

Legitimate uses are narrow: establishing a clean baseline before a benchmark, which is exactly how
`problems/gc-labs/` uses it.

If you feel you need it in production the real problem is elsewhere — a leak, an undersized heap, or the
wrong collector. Note what it *does* reliably do is clear `SoftReference`s and enqueue references, which
is why it can appear to "fix" a leak while only deferring it.

→ source Q20
</details>

---

### Q21. `java.lang.reflect.Proxy` can proxy?

- **A.** Any class
- **B.** **Interfaces only**
- **C.** Any non-final class
- **D.** Only annotated types

<details><summary>Answer</summary>

**B.** And the *structural* reason is the good answer: the generated class is `final` and already
**extends `java.lang.reflect.Proxy`**, so there's no superclass slot left. CGLIB/ByteBuddy exist to work
around that, and then the target class and methods must be non-`final`.

Details worth having: primitive args are **boxed**; `args` is **`null`**, not empty, for a no-arg method;
an undeclared checked exception surfaces as `UndeclaredThrowableException`; and
**`equals`/`hashCode`/`toString` are dispatched to your handler** while `getClass`/`wait`/`notify` are not.

It's how Spring implements `@Transactional`/`@Async`/`@Cacheable` — hence **self-invocation bypasses the
proxy**.

→ source Q21
</details>

---

### Q22. `map.get(k)` returns `null` on a `HashMap`. What do you know?

- **A.** The key is absent
- **B.** The map is empty
- **C.** An NPE was suppressed
- **D.** **Either absent or mapped to `null`** — ambiguous

<details><summary>Answer</summary>

**D.** `HashMap` permits one null key and many null values, so it's genuinely ambiguous; `containsKey`
distinguishes them.

`ConcurrentHashMap` and `TreeMap` **forbid null values**, so there `null` does mean absent — and CHM
forbids them precisely *because* the ambiguity would be unresolvable in a map where you can't safely
check-then-act.

**The important part:** `containsKey` then `get` is **two lookups** and, concurrently, **a race** —
measured in `P04`: 8 threads, the loader ran 8 times, 7 results discarded. Prefer `getOrDefault`,
`computeIfAbsent`, or `Optional.ofNullable`.

→ source Q22
</details>

---

### Q23. `ConcurrentHashMap`'s `size()` is?

- **A.** Exact, and O(1)
- **B.** Exact, and O(n)
- **C.** An **estimate** — no locking, so it may be stale under concurrent writes
- **D.** Unsupported

<details><summary>Answer</summary>

**C.** `size()`, `isEmpty()` and `containsValue()` are all documented as estimates: they don't lock, and
use `LongAdder`-style striped counters.

The rest of the internals worth naming: **per-bin locking** for writes (the pre-JDK-8 "segments" answer is
outdated), a CAS for the first insert into an empty bin, bins that convert to a **red-black tree** past 8
entries, and **weakly consistent iterators** that never throw `ConcurrentModificationException`.

And the limit: atomic **per entry**, not across entries. Two `put`s are not a transaction.

→ source Q23
</details>

---

### Q24. With an **unbounded** queue, `maximumPoolSize` on a `ThreadPoolExecutor` is?

- **A.** **Dead configuration** — it's never reached
- **B.** The pool's hard limit, as usual
- **C.** Used only for scheduled tasks
- **D.** Interpreted as core size

<details><summary>Answer</summary>

**A.** Recite the admission algorithm and it's obvious: (1) below core → new thread; (2) else **queue**;
(3) only if the queue **rejects** → new thread up to max; (4) else reject. An unbounded queue never
rejects, so **step 3 never happens.**

Measured in `problems/java/P05`: the queue reached 100,000 entries while the pool stayed at core size and
never created a second thread.

Hence: **bound the queue**, name the threads, and choose the rejection policy deliberately —
`CallerRunsPolicy` throttles the submitter naturally.

→ source Q24, Q107
</details>

---

### Q25. Why prefer `ThreadLocalRandom` to a shared `Random`?

- **A.** Better randomness quality
- **B.** `Random` isn't thread-safe
- **C.** `Random` can't be bounded
- **D.** **`Random`'s seed is one `AtomicLong`**, so concurrent calls contend on CAS

<details><summary>Answer</summary>

**D.** `Random` **is** thread-safe (so **B** is wrong) — and that's the problem: every `next()` does a
`compareAndSet` on one shared seed, so threads spin retrying on a single hot cache line.

`ThreadLocalRandom` gives each thread its own state, padded against false sharing, making `current()`
effectively free. **Never share the returned instance.**

Cost: not seedable, so not reproducible — use `SplittableRandom` for reproducible parallel work, and
`SecureRandom` for anything security-relevant. Same striping idea as `LongAdder` vs `AtomicLong`.

→ source Q25, Q50
</details>

---

### Q26. `CompletableFuture.cancel(true)` does what to the running task?

- **A.** Interrupts it
- **B.** **Nothing** — it completes the future with `CancellationException` and the work continues
- **C.** Throws `UnsupportedOperationException`
- **D.** Blocks until the task notices

<details><summary>Answer</summary>

**B.** The `mayInterruptIfRunning` flag is **ignored**. `CompletableFuture` has no handle on the thread
doing the work.

That's the asymmetry to remember when moving from `ExecutorService`: `FutureTask.cancel(true)` *does*
interrupt. For real cancellation keep the `Future` from `submit(...)`, check `Thread.interrupted()` inside
long work, or use structured concurrency on JDK 21.

Also: `isDone()` is `true` for success, failure **and** cancellation — so it is not a success check.

→ source Q26, Q66, Q94
</details>

---

### Q27. Which is true of `throw` and `throws`?

- **A.** **A `return` in `finally` discards an in-flight exception**
- **B.** An override may declare **broader** checked exceptions
- **C.** `throws` raises the exception
- **D.** `throw null` is a compile error

<details><summary>Answer</summary>

**A.** And it's a real bug source — the exception vanishes silently. Never `return` or `throw` from
`finally`; try-with-resources handles this properly via **suppressed** exceptions.

Why the others are wrong: `throw null` **compiles** and throws NPE at runtime; an override may declare
**fewer or narrower** checked exceptions, never broader (Liskov, enforced by the compiler); and `throw` is
the statement that raises while `throws` is the signature clause that declares.

Bonus: **more precise rethrow** (JDK 7+) lets `catch (Exception e) { throw e; }` compile in a method
declaring only the specific checked types.

→ source Q27
</details>

---

### Q28. Which `Collector` characteristic lets `collect` skip the finisher?

- **A.** `CONCURRENT`
- **B.** `UNORDERED`
- **C.** **`IDENTITY_FINISH`**
- **D.** `PARALLEL_SAFE`

<details><summary>Answer</summary>

**C.** It declares that the accumulation container *is* the result, so the framework casts instead of
calling `finisher()`. That's why `toList` is cheap.

The other two real ones: **`UNORDERED`** says the result doesn't depend on encounter order (`toSet`);
**`CONCURRENT`** allows a single shared container, honoured only when unordered too — the subtlety behind
`groupingByConcurrent`. (**D** doesn't exist.)

The five methods: `supplier`, `accumulator`, `combiner`, `finisher`, `characteristics`. Prefer composing
existing collectors to writing one.

→ source Q28
</details>

---

### Q29. `WeakHashMap` holds what weakly?

- **A.** Keys and values
- **B.** The value only
- **C.** The **key** only
- **D.** Neither

<details><summary>Answer</summary>

**C.** Values are strong — which produces a real leak: if a **value references its own key**, the key stays
strongly reachable through the value and the entry never clears. Planted deliberately in
`problems/gc-labs/L02Leak.java`, which OOMs a 128 MB heap in about 2 seconds.

`ThreadLocal` has the identical shape (weak key, strong value), which is why an unremoved `ThreadLocal`
leaks on a pooled thread.

The ladder: **strong** → **soft** (cleared under memory pressure) → **weak** (next GC) → **phantom**
(post-finalisation cleanup). What weak references are **not** is a bounded cache — use Caffeine.

→ source Q29
</details>

---

### Q30. `ConcurrentSkipListMap.size()` is?

- **A.** **O(n)** — it walks the list
- **B.** O(1)
- **C.** O(log n)
- **D.** Unsupported

<details><summary>Answer</summary>

**A.** Don't call it in a loop. `TreeMap.size()` is O(1) by contrast.

The rest of the comparison: both are sorted `NavigableMap`s at O(log n), but `ConcurrentSkipListMap` is
thread-safe and **lock-free**, using a probabilistic **skip list** rather than a red-black tree precisely
because rebalancing a tree is very hard to do lock-free.

Cost: higher constant factor and more memory (multiple node levels), so single-threaded it loses to
`TreeMap`. Choose it when you need range queries (`headMap`, `ceilingKey`, `pollFirstEntry`) **and**
concurrency — a live order book keyed by price.

→ source Q30, Q82
</details>

---

### Q31. `Object.wait()` vs `Thread.sleep()` — the key difference?

- **A.** `sleep` is more accurate
- **B.** `wait` can't be interrupted
- **C.** `sleep` must hold a lock
- **D.** **`wait` releases the monitor; `sleep` releases nothing**

<details><summary>Answer</summary>

**D.** Holding a lock and calling `sleep` is how you write a deadlock. `wait()` must be called **holding**
the monitor, releases it, parks, and re-acquires before returning.

Consequences: `wait()` without the monitor throws `IllegalMonitorStateException`; it can wake
**spuriously**, so it must always sit in a `while` loop re-checking the condition, never an `if`; and
prefer `notifyAll` to `notify`.

Also: a `notify` with no one waiting is **lost** — unlike `LockSupport.unpark`, whose permit is
remembered.

→ source Q31
</details>

---

### Q32. `Comparable` vs `Comparator` — which is right?

- **A.** `Comparator` must be implemented by the element type
- **B.** **`Comparable` is the type's natural order, implemented on the class; `Comparator` is external**
- **C.** `Comparable` allows many orderings
- **D.** They're interchangeable

<details><summary>Answer</summary>

**B.** `Comparable<T>.compareTo(T)` gives one ordering, and it's what `TreeMap`/`TreeSet`/`Arrays.sort`
use by default. `Comparator<T>.compare(a, b)` gives as many as you like, including for types you don't
own.

The consistency rule that bites: `TreeSet` dedupes by **`compareTo == 0`**, not `equals` — so an
inconsistent pair silently drops elements.

→ source Q32
</details>

---

### Q33. What can a `Phaser` do that a `CyclicBarrier` cannot?

- **A.** Run a barrier action
- **B.** Time out
- **C.** Support more than two threads
- **D.** **Change the number of parties at runtime**

<details><summary>Answer</summary>

**D.** `register()`/`bulkRegister()`/`arriveAndDeregister()` let parties join and leave; a `CyclicBarrier`
fixes its count at construction.

It also adds numbered **phases** (`getPhase()`), a termination hook (`onAdvance` returning `true`),
**tiering** for lower contention across many parties, and `arrive()` to arrive **without** waiting.

And it degrades better: one interrupted party doesn't poison it for everyone the way
`BrokenBarrierException` does. The cost is conceptual load — default to a latch, escalate when the party
count genuinely changes.

→ source Q33, Q98
</details>

---

### Q34. `StampedLock`'s main advantage, and its main catch?

- **A.** Fairness; but it's slower
- **B.** Reentrancy; but no `Condition`
- **C.** **Optimistic reads; but it is not reentrant**
- **D.** Lock upgrade; but writers starve

<details><summary>Answer</summary>

**C.** `tryOptimisticRead()` takes **no lock at all** — you read into locals, then `validate(stamp)` to
check no writer intervened. On read-heavy state that's much faster than `ReentrantReadWriteLock`.

The catches: **not reentrant** (re-acquiring deadlocks), you must carry the **stamp** to unlock, and
there's no `Condition` support. It does offer explicit **conversion** (`tryConvertToWriteLock`) rather
than the illegal upgrade.

And you must copy into locals **before** validating, or you may act on a torn read.

→ source Q34
</details>

---

### Q35. `LockSupport.unpark(t)` called **before** `t` parks?

- **A.** The signal is lost; `t` blocks
- **B.** **The permit is remembered** — the later `park()` returns immediately
- **C.** `t` blocks until a second `unpark`
- **D.** `IllegalStateException`

<details><summary>Answer</summary>

**B.** A single binary permit per thread, and `unpark` is **not** lost if it precedes `park`. That's
exactly what makes it safe where `Object.wait`/`notify` isn't — a `notify` before a `wait` **is** lost,
the classic missed-signal bug.

Also: `park()` may return **spuriously**, so re-check your condition in a loop; and it returns on
interruption **without throwing and without clearing** the flag.

It matters because **`AbstractQueuedSynchronizer`** is built on it — `volatile int state` + a CAS wait
queue + `park`/`unpark` — and AQS is what `ReentrantLock`, `Semaphore`, `CountDownLatch` and `FutureTask`
are made of.

→ source Q35
</details>
