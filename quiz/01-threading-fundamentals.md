# Quiz 01 — Threading Fundamentals & the Java Memory Model

25 questions. Answer out loud before expanding. Target: 20 min.

---

### 1. What exactly does `volatile` guarantee, and what does it not?

<details><summary>Answer</summary>

Guarantees:
- **Visibility** — a write is immediately visible to any thread that subsequently reads it. No caching in registers, no eliding the read in a loop.
- **Ordering** — a volatile write is a release, a volatile read is an acquire. Everything written before the volatile write is visible to a thread that reads that volatile and sees the new value. This is what makes safe publication work.
- **Atomicity of the single read and the single write**, including for `long`/`double` (non-volatile 64-bit access may tear on a 32-bit JVM).

Does **not** guarantee:
- Atomicity of compound actions. `i++` is read-modify-write — three operations, still a race.
- Mutual exclusion of any kind.

One-liner: *volatile fixes "when do you see it", not "who wins".*
</details>

---

### 2. Name five happens-before edges from memory.

<details><summary>Answer</summary>

1. **Program order** within a single thread.
2. **Monitor lock** — unlock of a monitor happens-before every subsequent lock of the *same* monitor.
3. **Volatile** — write to a volatile happens-before every subsequent read of that same volatile.
4. **Thread start** — `t.start()` happens-before everything in `t`.
5. **Thread join** — everything in `t` happens-before `t.join()` returning.

Bonuses worth mentioning: `final` field freeze at end of constructor; anything through a `java.util.concurrent` structure (a `put` into a `BlockingQueue` happens-before the `take` that retrieves it); `CountDownLatch.countDown()` → `await()` return; `AtomicX` writes have volatile semantics; interrupting a thread happens-before it detects the interrupt.

Happens-before is **transitive** — that's what you exploit in practice.
</details>

---

### 3. Is this class thread-safe? Why or why not?

```java
class Config {
    private Map<String, String> settings;   // written once
    void init(Map<String, String> m) { this.settings = m; }
    String get(String k) { return settings.get(k); }
}
```

<details><summary>Answer</summary>

No — two separate problems:

1. **Unsafe publication.** A reader thread may see `settings` as `null`, or see a non-null reference to a partially-constructed map. There's no happens-before edge between `init` and `get`. The reference write and the map's internal writes can be observed out of order.
2. **The map itself** may be mutated by whoever still holds `m`.

Fixes, best to worst:
- `private final Map<String,String> settings;` set in the constructor from `Map.copyOf(m)` — immutable, safely published by final-field semantics, no `init` method at all.
- `private volatile Map<String,String> settings` + store an immutable copy — supports atomic hot-reload of the whole map.
- `synchronized` on both methods — works, needlessly serialises reads.

This is *the* canonical Spring bug: a singleton bean with a mutable field populated after construction.
</details>

---

### 4. Why is double-checked locking broken without `volatile`, and what's the better idiom?

<details><summary>Answer</summary>

```java
if (instance == null) {                 // 1st check, unsynchronized
    synchronized (this) {
        if (instance == null) instance = new Heavy();
    }
}
return instance;
```

`instance = new Heavy()` is allocate → run constructor → assign reference. The JIT (or the hardware) may make the reference visible before the constructor's writes are visible. Thread B passes the first check, returns a reference to a **half-initialised object**, and reads default-valued fields. Declaring `instance` `volatile` closes the hole: the volatile write is a release, so the constructor's writes can't float past it.

Better in most cases — the **initialization-on-demand holder**:

```java
static class Holder { static final Heavy INSTANCE = new Heavy(); }
static Heavy get() { return Holder.INSTANCE; }
```

Lazy (the holder class isn't initialised until first touch), thread-safe by JVM class-initialisation locking, and zero synchronisation on the read path.
</details>

---

### 5. `synchronized` vs `ReentrantLock` — when do you reach for the lock?

<details><summary>Answer</summary>

`ReentrantLock` when you need something `synchronized` can't do:
- **`tryLock()`**, with or without timeout — deadlock avoidance, backpressure.
- **Interruptible acquisition** — `lockInterruptibly()`.
- **Fairness** — `new ReentrantLock(true)`. Costs throughput; buys bounded wait.
- **Multiple condition variables** — `newCondition()`, e.g. separate `notFull`/`notEmpty` so you wake only the right waiters.
- **Non-block-scoped locking** — hand-over-hand traversal, lock acquired in one method and released in another.
- On **JDK 21 specifically**: `ReentrantLock` doesn't pin a virtual thread's carrier; `synchronized` does.

Otherwise prefer `synchronized`: it can't leak (no missing `finally`), it's what the JIT optimises hardest (biased locking is gone as of JDK 15, but lock coarsening and elision remain), and it reads better. `ReentrantLock` requires the `try { } finally { lock.unlock(); }` discipline every single time.
</details>

---

### 6. What is a CAS, and what's the ABA problem?

<details><summary>Answer</summary>

**CAS** = compare-and-swap: atomically, "if this location still holds `expected`, store `newValue` and report success; else report failure". One hardware instruction (`lock cmpxchg` on x86, `casal`/LL-SC on ARM). It's the basis of every `Atomic*` class and most of `java.util.concurrent`. The usage pattern is a retry loop:

```java
int cur, next;
do { cur = ref.get(); next = f(cur); } while (!ref.compareAndSet(cur, next));
```

Lock-free, not wait-free: a thread can in principle retry indefinitely under heavy contention, but the *system* always progresses.

**ABA**: thread 1 reads `A`. Thread 2 changes it `A → B → A`. Thread 1's CAS succeeds because the value matches, even though the world moved underneath it. For plain counters this is harmless. It bites when the value is a reference and identity/history matters — a popped-then-recycled node in a lock-free stack. Fix with a version stamp: `AtomicStampedReference` (value + int stamp) or `AtomicMarkableReference`.
</details>

---

### 7. `AtomicLong` vs `LongAdder` — which and why?

<details><summary>Answer</summary>

`AtomicLong` is a single memory location. Under high contention every core fights over that one cache line, CAS failures spike, and throughput collapses — the line ping-pongs between caches.

`LongAdder` keeps a **striped array of cells**, one per contending thread (approximately, via a thread-local probe hash), padded to avoid false sharing. Writes hit different cache lines and scale nearly linearly. `sum()` walks the cells and is **not** an atomic snapshot — it may not reflect a consistent instant if writes are concurrent.

Rule: **write-heavy, read-rarely → `LongAdder`** (metrics counters, request tallies). **Need the exact current value to make a decision, or need CAS on it → `AtomicLong`.** `LongAdder` has no `compareAndSet`, so you can't build a "increment only if under limit" check with it.
</details>

---

### 8. Four preconditions for deadlock, and three ways to prevent it.

<details><summary>Answer</summary>

Coffman conditions — all four must hold:
1. **Mutual exclusion** — resources aren't shareable.
2. **Hold and wait** — a thread holding one resource waits for another.
3. **No preemption** — resources can't be forcibly taken back.
4. **Circular wait** — a cycle in the wait-for graph.

Prevention, in order of practical usefulness:
1. **Global lock ordering** — every code path acquires locks in the same total order (e.g. by `System.identityHashCode`, or by account id in a transfer). Breaks circular wait. This is the workhorse fix.
2. **`tryLock` with timeout + backoff** — breaks hold-and-wait; caller retries or fails fast. Watch for livelock; add randomised backoff.
3. **Don't hold a lock across a call you don't control** — no I/O, no callbacks, no alien methods inside a critical section. Also: shrink the critical section, or eliminate the second lock (single coarse lock, or a lock-free/immutable design).

Detection: `jstack` prints `Found one Java-level deadlock` with the cycle. `ThreadMXBean.findDeadlockedThreads()` does it programmatically.

Related but distinct: **livelock** (threads keep acting, no progress — two people stepping aside in a corridor), **starvation** (a thread is perpetually denied a resource it needs), **priority inversion**.
</details>

---

### 9. Predict the output.

```java
class Puzzle {
    static int x = 0, y = 0;
    static int a = 0, b = 0;
    // Thread 1:  x = 1;  a = y;
    // Thread 2:  y = 1;  b = x;
}
```
Can we observe `a == 0 && b == 0`?

<details><summary>Answer</summary>

**Yes.** This is Dekker's example / "store buffer" reordering. Neither thread has any happens-before relationship with the other, so both the compiler and the CPU may reorder each thread's independent store and load. On x86 the store buffer alone produces it: both stores sit in buffers while both loads read stale zeros.

Intuition that says "at least one thread must run second, so at least one of a/b must be 1" assumes sequential consistency. The JMM only gives you SC for **data-race-free** programs — that's the "SC-DRF" guarantee. This program has a data race, so all bets are off.

Fix: make `x` and `y` `volatile`. The volatile store→load pair forbids the reorder (inserts an `mfence`/`StoreLoad` barrier), and `a == 0 && b == 0` becomes impossible.

If asked to demonstrate: jcstress is the tool built exactly for this.
</details>

---

### 10. What's wrong here?

```java
synchronized (this) {
    if (queue.isEmpty()) wait();
    return queue.remove();
}
```

<details><summary>Answer</summary>

Three bugs:

1. **`if` instead of `while`.** `wait()` can return without a corresponding `notify` — a **spurious wakeup** is permitted by the spec. Worse, even with a real `notify`, another thread may have consumed the item between your wakeup and your reacquiring the monitor. The condition must be **re-tested in a loop**:
   ```java
   while (queue.isEmpty()) wait();
   ```
2. **`InterruptedException` handling** isn't shown — either propagate it or restore the flag with `Thread.currentThread().interrupt()`. Never swallow it.
3. Whoever signals must use **`notifyAll()`**, not `notify()`, if more than one condition or more than one waiter type shares the monitor — a single `notify` can wake the wrong waiter and the signal is lost forever.

And the meta-answer, which is what they actually want to hear: **don't write `wait`/`notify` in 2026.** Use `ArrayBlockingQueue`/`LinkedBlockingQueue`, or `ReentrantLock` with named `Condition`s. Hand-rolled monitors are where lost-wakeup bugs live.
</details>

---

### 11. Is `ConcurrentHashMap` thread-safe for this?

```java
if (!map.containsKey(k)) map.put(k, expensive(k));
```

<details><summary>Answer</summary>

Each individual call is atomic. **The combination is not** — classic check-then-act race. Two threads both see the key absent, both call `expensive`, one write is lost. If the value is a shared resource (a connection, a cache entry other threads hold), the lost one may leak.

Fixes:
- `map.computeIfAbsent(k, this::expensive)` — atomic, and `expensive` is called at most once per key. This is what you want.
- `map.putIfAbsent(k, v)` — atomic, but you must compute `v` eagerly, so you may compute and discard.

Caveats on `computeIfAbsent` that make it a good follow-up question:
- The mapping function runs **while holding the bin lock**. Keep it short. Doing I/O there blocks every other writer hashing to that bin.
- **Recursively updating the same map inside the function throws `IllegalStateException("Recursive update")`** (JDK 9+) or, historically, deadlocked. See `problems/java/P04ComputeIfAbsentDeadlock.java`.
- Aggregate/bulk views (`size()`, `isEmpty()`, iteration) are **weakly consistent** — they reflect some state at some point, not a snapshot.
</details>

---

### 12. Safe publication — list the ways to hand an object to another thread correctly.

<details><summary>Answer</summary>

An object is safely published if it's:
1. Initialised from a **static initializer** (class-init locking does it for you).
2. Stored into a **`volatile` field** or an `AtomicReference`.
3. Stored into a **`final` field** of a properly-constructed object (nothing escapes `this` during construction).
4. Stored into a field guarded by a **lock** that readers also acquire.
5. Placed into a **thread-safe collection** — `BlockingQueue`, `ConcurrentMap`, etc. — the collection's internals supply the edge.
6. Passed via `Thread.start()` / retrieved via `Future.get()` / handed through a `CountDownLatch` or `Exchanger`.

**Effectively immutable** objects only need safe publication once. **Immutable** objects (all fields final, no mutable state reachable) can be shared freely, published however you like. This is the argument for Scala's `case class` + `val` default, and it's the single biggest concurrency win of the migration.
</details>

---

### 13. What is false sharing, and how do you fix it?

<details><summary>Answer</summary>

Two threads write to two *different* variables that happen to live on the **same 64-byte cache line**. There's no logical contention, but the cache coherency protocol (MESI) invalidates the whole line on every write, so the line ping-pongs between cores. Throughput can drop by an order of magnitude, with no lock in sight.

Diagnose: `perf c2c` on Linux, or the smell of "adding a thread made it slower" on an array-of-counters.

Fix: **pad** so hot fields land on separate lines. `@sun.misc.Contended` (needs `-XX:-RestrictContended` outside the JDK) does it declaratively; manual padding fields do it crudely. Or stop sharing — that's what `LongAdder`'s `Cell` (which *is* `@Contended`) and per-thread accumulation do.
</details>

---

### 14. `ThreadLocal` in a thread pool — what's the trap?

<details><summary>Answer</summary>

Pool threads are **reused and never die**, so a `ThreadLocal` set during task A is still set when unrelated task B runs on that thread. Two consequences:

1. **Data leak / correctness bug.** Task B reads task A's tenant id, user principal, or `SecurityContext`. In a multi-tenant service this is a security incident, not a bug. Spring's `RequestContextHolder`, `SecurityContextHolder`, and SLF4J's `MDC` are all `ThreadLocal`-backed and all bite here.
2. **Memory leak.** The value is strongly reachable from the thread's `ThreadLocalMap` for the pool's lifetime. `ThreadLocal.Entry` holds a *weak* reference to the **key** but a *strong* reference to the **value**, so clearing your reference to the `ThreadLocal` object doesn't free the value. Classic web-app classloader leak: a `ThreadLocal` value whose class was loaded by the webapp classloader pins that classloader after redeploy.

Discipline: **always `remove()` in a `finally`**, and wrap task submission so context is explicitly propagated and cleared (Spring's `TaskDecorator`, or `ScopedValue`/structured concurrency on newer JDKs). See `problems/java/P08ThreadLocalLeak.java`.

Follow-up they may ask: with **virtual threads** this pressure mostly disappears — each task gets its own short-lived thread — but `ThreadLocal` per-virtual-thread at millions of threads is a footprint concern, which is exactly why `ScopedValue` exists.
</details>

---

### 15. How should you handle `InterruptedException`?

<details><summary>Answer</summary>

Two legitimate choices, and one crime.

1. **Propagate it.** Add `throws InterruptedException` and let the caller decide. Best default for library code.
2. **Restore the flag and return.** If you can't propagate (e.g. you're in `Runnable.run()`):
   ```java
   catch (InterruptedException e) {
       Thread.currentThread().interrupt();   // restore the bit
       return;                                // stop doing work
   }
   ```
   Catching the exception **clears** the interrupt status. If you don't restore it, code further up the stack (including the pool itself) can never learn the thread was interrupted.
3. **The crime:** `catch (InterruptedException e) { }` or `log.error(...)` and carry on. You've destroyed the only cancellation signal the JVM has.

Framing for the interview: interruption is **cooperative**. There is no safe way to stop a thread from outside — `Thread.stop()` is removed for exactly that reason. `Future.cancel(true)` interrupts; a task that ignores interrupts is uncancellable. Blocking on a socket read doesn't respond to interrupts at all (you must close the socket) — which is one of the real motivations for `InterruptibleChannel` and for virtual threads.
</details>

---

### 16. `Thread.sleep()` vs `Object.wait()` — does either release the lock?

<details><summary>Answer</summary>

`sleep()` **holds every lock it owns** while sleeping. Sleeping inside a `synchronized` block is a good way to build a bottleneck.

`wait()` **atomically releases the monitor it was called on** and parks; on wakeup it must **reacquire** it before returning. It releases only *that* monitor — any other locks the thread holds are still held, which is a deadlock source.

Also: `wait()` must be called while holding that monitor (else `IllegalMonitorStateException`); `sleep()` is a static method with no monitor requirement. Both are interruptible.
</details>

---

### 17. `CountDownLatch` vs `CyclicBarrier` vs `Semaphore` vs `Phaser`.

<details><summary>Answer</summary>

- **`CountDownLatch`** — one-shot gate. Counts down to zero and stays there. "Wait until N things have happened." Not reusable. Waiters and counters are different roles.
- **`CyclicBarrier`** — reusable rendezvous. N threads all wait for each other, then all proceed; the barrier resets. Optional barrier action runs once per generation. If one party dies, everyone else gets `BrokenBarrierException`. "Iterative simulation, all workers finish step k before step k+1."
- **`Semaphore`** — counting permits. Bounds concurrency rather than sequencing it. "At most 10 in-flight calls to this downstream." A permit-holder needn't be the acquirer, and permits can be released more than acquired (it's not a lock).
- **`Phaser`** — a `CyclicBarrier` with **dynamic registration** (parties join/leave), tiered scaling, and `arriveAndDeregister`. Reach for it when the number of participants isn't known up front.
</details>

---

### 18. `StampedLock` vs `ReentrantReadWriteLock` — why does `StampedLock` exist?

<details><summary>Answer</summary>

`ReentrantReadWriteLock` lets many readers or one writer proceed, but **every reader still writes** to the lock's shared state to register itself. On read-mostly workloads across many cores, that shared counter is itself the bottleneck (and writers can starve without the fair mode).

`StampedLock` adds **optimistic reading**:
```java
long stamp = lock.tryOptimisticRead();     // no write to shared state at all
int localX = x, localY = y;                // read the fields
if (!lock.validate(stamp)) {               // did a writer intervene?
    stamp = lock.readLock();               // fall back to a real read lock
    try { localX = x; localY = y; } finally { lock.unlockRead(stamp); }
}
```
The optimistic path is essentially free. The cost: `StampedLock` is **not reentrant** (re-acquiring deadlocks you), doesn't support `Condition`, and the optimistic block must read into locals and tolerate seeing a torn/inconsistent snapshot before `validate` rejects it. Also more error-prone — stamps are values you must not lose. Use it for small, hot, read-mostly structures, not as a general RWLock replacement.

Also worth knowing: **read-write locks only pay off when reads are long**. If the critical section is a few nanoseconds, a plain `synchronized` beats any RW lock on bookkeeping alone.
</details>

---

### 19. What does `Collections.synchronizedList` give you that `CopyOnWriteArrayList` doesn't, and vice versa?

<details><summary>Answer</summary>

`Collections.synchronizedList` — one lock around every method. Writes are cheap-ish; reads are serialised. **Iteration is not safe** unless you synchronise on the list yourself for the whole loop (otherwise `ConcurrentModificationException`). Compound operations still need external synchronisation.

`CopyOnWriteArrayList` — every mutation **copies the whole backing array**. Reads are lock-free and iteration is safe on an immutable snapshot (no CME, and the iterator doesn't see later writes; `remove` on the iterator is unsupported). O(n) writes and a garbage spike per write.

Choose CoW when: many readers, very few writers, small list. Listener registries are the canonical fit. Never for a list you append to in a loop — that's accidentally quadratic and a young-gen firehose.
</details>

---

### 20. Why was `finalize()` deprecated, and what replaced it?

<details><summary>Answer</summary>

`finalize()` is unfixable:
- **No guarantee it ever runs**, or when. The JVM may exit first.
- Objects with finalizers need **at least two GC cycles** and go through a queue serviced by a single finalizer thread — a slow finalizer stalls the queue and you OOM with reclaimable garbage on the heap.
- A finalizer can **resurrect** the object (store `this` somewhere), so the GC must handle un-dying objects.
- Exceptions thrown in a finalizer are swallowed.
- Finalizer attacks: a subclass's finalizer can get at a partially-constructed object even if the constructor threw.

Replacements:
- **`try`-with-resources / `AutoCloseable`** — deterministic, lexically obvious. This is the answer 95% of the time.
- **`java.lang.ref.Cleaner`** — a safety net for native memory, registered with a state object that must **not** capture the referent (or it's never collectable). Still non-deterministic; it's a backstop, not a strategy.
- `PhantomReference` + `ReferenceQueue` if you want to run the loop yourself.

Scala equivalent framing: Cats Effect `Resource` / ZIO `Scope` give you deterministic, composable, cancellation-safe acquire-release — strictly better than either.
</details>

---

### 21. Strong, soft, weak, phantom — when would you use each?

<details><summary>Answer</summary>

- **Strong** — normal reference. Never collected while reachable.
- **Soft** — cleared **at the GC's discretion when memory is tight**, before OOM. In theory a memory-sensitive cache; in practice a bad one, because the JVM's policy (`-XX:SoftRefLRUPolicyMSPerMB`) is crude, clearing happens in a full GC, and it tends to hold garbage right up until it causes a long pause. Prefer a real cache with an explicit bound (Caffeine).
- **Weak** — cleared **as soon as the referent is not strongly reachable**, at the next GC. Correct tool for canonicalising maps and metadata keyed by an object you don't own: `WeakHashMap`, `ThreadLocal`'s keys, listener registries that shouldn't leak.
- **Phantom** — `get()` always returns `null`; enqueued **after** the object is finalizable. Used purely to learn "this is definitely gone, run my cleanup now" — the mechanism behind `Cleaner`.

Reachability ladder: strongly → softly → weakly → phantom → unreachable. A key follow-up: **`WeakHashMap` values can still leak** if a value strongly references its own key — the entry never becomes collectable.
</details>

---

### 22. What is a memory barrier / fence, and which ones does the JMM imply?

<details><summary>Answer</summary>

A fence constrains reordering of memory operations around it (in the compiler and in hardware). The four conceptual kinds:

- **LoadLoad**, **StoreStore**, **LoadStore**, **StoreLoad** — the last is the expensive one (drains the store buffer; `mfence`/`lock addl` on x86).

Where the JMM puts them:
- **Volatile read** → acquire: `LoadLoad` + `LoadStore` after it. Nothing after the read can float above it.
- **Volatile write** → release: `StoreStore` + `LoadStore` before it, and a `StoreLoad` after it (that's why volatile *writes* cost more than reads).
- **Monitor enter/exit** → same acquire/release shape.
- **Final field freeze** at constructor end → `StoreStore`.

x86 is strongly ordered (TSO): only store→load can be reordered, so volatile reads are nearly free and only volatile writes need a real instruction. **ARM/Apple Silicon is weakly ordered**, so barriers are genuinely emitted in both directions. Worth saying out loud, since your laptop is ARM and the server is probably x86 — concurrency bugs reproduce differently on each. `VarHandle.fullFence()` and friends expose these directly.
</details>

---

### 23. What's the difference between `Future`, `CompletableFuture`, and a reactive `Mono`?

<details><summary>Answer</summary>

- **`Future`** (JDK 5) — a handle you can `get()` (blocking) or `cancel()`. No composition, no callbacks. Every use ends in blocking a thread.
- **`CompletableFuture`** (JDK 8) — a `Future` you can compose: `thenApply`, `thenCompose` (flatMap), `thenCombine` (zip), `allOf`, `exceptionally`, `handle`. **Eager** — the work starts when the future is created. Completed exactly once. Callback-driven, so no thread need block.
- **`Mono`/`Flux`** (Reactor) or `IO`/`ZIO` — **lazy** descriptions of work, not running computations. Nothing happens until you subscribe/run. Consequences: they're retryable and cancellable by construction, composition is total (no "already started" problem), and `Flux` adds **backpressure** for streams of many elements.

The distinction that matters in the migration: `Future`/`CompletableFuture` and Scala's `Future` are **eager**; `IO`/`ZIO`/`Mono` are **lazy**. Eager futures can't be retried (`f.retry` would just re-observe the same completed value) and start doing work before you've decided to run them. If the Scala side picks Cats Effect or ZIO, "why is laziness better?" is a near-certain question.
</details>

---

### 24. Spot the bugs.

```java
@Service
public class OrderService {
    private final List<Order> pending = new ArrayList<>();
    private int processed;

    public void submit(Order o) { pending.add(o); }

    @Async
    public void process() {
        for (Order o : pending) { doWork(o); processed++; }
        pending.clear();
    }
}
```

<details><summary>Answer</summary>

Five distinct problems:

1. **Spring `@Service` beans are singletons** and every HTTP request thread calls `submit` concurrently. `ArrayList.add` from multiple threads corrupts the backing array — lost elements, or `ArrayIndexOutOfBoundsException` from a torn resize.
2. **`processed++`** is a non-atomic read-modify-write with no visibility guarantee.
3. **`ConcurrentModificationException`** — iterating `pending` while another thread adds to it. (Or silently processing a torn view.)
4. **Lost work** — `pending.clear()` discards anything added during the loop.
5. **`@Async` on a method called from within the same bean does nothing** — Spring's proxy is bypassed on self-invocation, so `this.process()` runs synchronously. (Also: the default `@Async` executor was historically `SimpleAsyncTaskExecutor`, which creates an unbounded number of threads. Always define your own `TaskExecutor` bean.)

A reasonable rewrite: make `pending` a `BlockingQueue<Order>` (or an unbounded-avoided `ArrayBlockingQueue`), `drainTo` a local list before processing, and use a `LongAdder` for `processed`. Better still: keep the bean **stateless** and put the queue in a real broker.

This is the highest-value question in the file — it's what the migration will actually hand you.
</details>

---

### 25. What does the `final` keyword have to do with thread safety?

<details><summary>Answer</summary>

`final` fields get **freeze semantics**: the JMM guarantees that any thread which sees a reference to a correctly-constructed object sees the fully-initialised values of its `final` fields — *without* any synchronisation. That's what makes immutable objects safe to share freely and why `String`'s fields are final.

"Correctly constructed" is the load-bearing caveat: if `this` **escapes during the constructor** (registering a listener, starting a thread, passing `this` to an alien method, calling an overridable method) another thread can observe the object before the freeze, and the guarantee is void.

Two more notes:
- `final` on a **reference** freezes the reference, not the referent. `private final List<X> l = new ArrayList<>()` is still mutable and still unsafe to share.
- `final` on a local/parameter ("effectively final") is a compile-time capture rule for lambdas and anonymous classes, unrelated to the memory model — though it does prevent one class of accidental sharing.
</details>

---

## Score yourself

- **20+** — solid. Move to `quiz/02`.
- **14–19** — reread Q3, Q9, Q12, Q24. Those four are the ones that come up as real bugs.
- **<14** — do `problems/java/P01`–`P03` hands-on first; empirics stick better than prose.
