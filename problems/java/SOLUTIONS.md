# Solutions — Java problems

Read these **after** attempting. For P09 especially: reading a solution to a concurrency
exercise teaches you almost nothing, because the hard part is knowing *why* the naive
version is wrong.

---

## P01 — Lost updates

**Verified output** (14 cores, JDK 21, 8 threads × 500k):

```
plain int            =  3,996,200  LOST 3,800 (0.10%)
volatile int         =  3,996,523  LOST 3,477 (0.09%)
AtomicInteger        =  4,000,000  OK
LongAdder            =  4,000,000  OK
synchronized method  =  4,000,000  OK

  plain (WRONG)         13 ms   result=1,202,231
  volatile (WRONG)      86 ms   result=  533,262
  AtomicInteger        179 ms   result=4,000,000
  LongAdder              5 ms   result=4,000,000
  synchronized         198 ms   result=4,000,000
```

**Why `volatile` doesn't fix `++`:** `i++` compiles to *read, add, write*. `volatile`
makes each read and each write individually atomic and visible, but the three steps
aren't one atomic operation — two threads can both read 5, both compute 6, both write 6.

Note `volatile` is *worse* than plain in the isolated timing (86 ms vs 13 ms) **and
loses more updates**: the plain version is so aggressively optimised that threads mostly
work on stale register values, whereas volatile forces real cache-line traffic on every
single increment while still not being atomic. **The worst of both worlds** — that's the
point of including it.

**Why `LongAdder` is 36× faster than `AtomicInteger`** (5 ms vs 179 ms): `AtomicInteger`
is one memory location, so 8 cores fight over one cache line and most CAS attempts fail
and retry. `LongAdder` keeps a padded, striped array of cells indexed by a per-thread
probe, so writes land on different cache lines and scale nearly linearly. The cost is
that `sum()` isn't an atomic snapshot and there's no CAS operation.

**Choosing:** write-heavy + read-rarely (metrics) → `LongAdder`. Need the exact current
value or a CAS ("increment only if under limit") → `AtomicInteger`.

---

## P02 — Visibility

**Reproduces reliably.** The worker never exits.

The JIT hoists the field read out of the loop, because nothing *in the loop* writes it:

```java
while (running) { n++; }      // becomes:   if (running) { while (true) n++; }
```

That's a legal transformation for a non-volatile field: with no happens-before edge
between the write in `main` and the read in the worker, the JMM never *requires* the
write to become visible.

- **Fix:** `static volatile boolean running`. Also correct: an `AtomicBoolean`, or
  guarding the field with a lock that both sides acquire.
- **Adding `println` inside the loop "fixes" it** because `println` is `synchronized`
  internally, which introduces memory barriers and prevents the hoist. **This is not a
  fix** — it works by accident, is a huge performance cost, and will break the moment
  someone removes the logging. Classic Heisenbug.
- **With `-Xint`** it usually terminates: the interpreter re-reads the field every
  iteration because it does no such optimisation. That's why "it works in dev" is not
  evidence — the bug appears only once C2 compiles the loop.

---

## P03 — Deadlock

**Verified.** Default mode deadlocks; `ThreadMXBean` reports it:

```
a->b is blocked on P03Deadlock$Account@58ea606c owned by b->a
b->a is blocked on P03Deadlock$Account@77f80c04 owned by a->b
```

**The wait-for graph** is a 2-cycle: thread `a->b` holds A and wants B; `b->a` holds B
and wants A. All four Coffman conditions hold.

**Fix 1 — lock ordering** (`transferOrdered`): acquire by ascending account id, so both
threads take A then B. Breaks the circular-wait condition. **This is the workhorse fix.**
Watch out for equal ids (self-transfer) — you'd deadlock on re-entering... actually no,
`synchronized` is reentrant, so a self-transfer is safe here, but with `ReentrantLock`
you must handle it. If there's no natural ordering key, `System.identityHashCode` works
(with a tie-breaker lock for the rare collision).

**Fix 2 — `tryLock` + backoff:** breaks hold-and-wait. Verified working, at a cost of
**203 retries** for 40 transfers — that's the price. The backoff must be **randomised**
(`1 + nextInt(10)` ms), otherwise two threads retry in lockstep, collide again, and you
have **livelock**: both threads busy, no progress, forever. Fixed backoff is the classic
mistake here.

**The best fix uses no locks at all.** Make the account immutable and put the pair of
balances in a single `AtomicReference` to an immutable snapshot:

```java
record Balances(long a, long b) {}
AtomicReference<Balances> state = new AtomicReference<>(new Balances(1000, 1000));

void transfer(long amount) {
    state.updateAndGet(s -> new Balances(s.a() - amount, s.b() + amount));
}
```
One CAS, no locks, no ordering to remember, deadlock impossible by construction. This
generalises: **the reason Scala's immutability-by-default is a concurrency win** is that
it pushes you here by default instead of toward lock ordering.

(For real money you'd use the database's transactions and let it handle isolation — also
worth saying.)

---

## P04 — `ConcurrentHashMap`

**Part A, verified:** 8 threads, `containsKey` + `put` → **`expensive()` ran 8 times**,
map size 1. Seven results computed and silently discarded. If the value were a
connection or a file handle, you'd have leaked seven.

Each call is atomic; **the combination isn't**. Fixes:
```java
map.computeIfAbsent(k, this::expensive);   // atomic, runs at most once per key
map.putIfAbsent(k, expensive(k));          // atomic, but computes eagerly & may discard
```
Verified: `computeIfAbsent` → **1 call**.

**Part B, verified:** the recursive `fib` memo throws
`IllegalStateException: Recursive update`.

Note the *first* recursive example in Part B **did not throw** — `"outer"` and `"inner"`
hashed to different bins, so it "worked". **That's the dangerous case**: code that is
incorrect but passes because of accidental hash layout. On JDK 8 the same shape could
**deadlock** or corrupt the table instead of throwing; JDK 9+ added the detection.

Fix: compute the value fully *before* touching the map, or use a plain `HashMap` if the
recursion is single-threaded.

**Part C:** the mapping function runs **while holding the bin lock**. Verified: 4 threads
× 4 distinct keys × 300 ms ran in **310 ms** (parallel) because the keys hashed to
different bins. Keys colliding in one bin would serialise to ~1200 ms — and *nothing in
your source tells you which case you're in*. So: keep the function short, pure, and
non-blocking; never do I/O in it. For async loading with per-key coalescing, use
Caffeine's `AsyncLoadingCache`.

**`size()` is weakly consistent** because CHM maintains a striped `LongAdder`-style
counter rather than one contended field. `size()` sums the stripes without locking, so
concurrent writes may not be reflected consistently. Fine for metrics and heuristics;
never use it for a correctness decision (e.g. `if (map.size() < limit) map.put(...)` is
a race).

---

## P05 — `CompletableFuture`

**Verified output:**
```
PART 1: thenApply on main, thenAccept on main       <- already-complete stage
PART 2: supplyAsync / thenApply / thenApplyAsync / thenAccept  all on
        ForkJoinPool.commonPool-worker-1
PART 3: all stages on my-io-pool
PART 4: fire-and-forget exception -> NO output at all
        join() -> CompletionException;  get() -> ExecutionException
PART 5: thenApply -> nested CompletableFuture;  thenCompose -> 42
PART 6: 300ms timeout returned fallback, but the task finished 1.5s later
PART 7: 17 tasks x 400ms on a 13-parallelism pool -> 818 ms
```

**The rule for non-`Async` stages:** they run on **whichever thread completes the
previous stage**, or on the **calling** thread if the stage is already complete when the
callback is attached (Part 1 proves this — `main`). You don't control it. So a
`thenApply` can execute on your HTTP request thread, a Netty event loop, or a common-pool
worker depending on a race. **Never block or do long work in a non-`Async` stage**, and
always pass an explicit `Executor` to the `*Async` variants in production.

**Part 4 — the swallowed exception** is the nastiest one. A `runAsync` that throws
produces *no output anywhere*: the throwable is stored in the (unobserved) future.
Contrast a plain `Thread`, whose uncaught exception reaches the default
`UncaughtExceptionHandler` and prints. Minimum fix: terminate every chain with
`whenComplete((v, t) -> { if (t != null) log.error("...", t); })`.

**Part 6 — `orTimeout` does not cancel.** Verified: we gave up at 300 ms, the task
completed at ~1.5 s. `CompletableFuture.cancel` doesn't interrupt the underlying work
either — it only completes *your* future exceptionally. So every timeout **leaks the
capacity** of the in-flight work (and its DB connection). This is a genuine, structural
limitation and the sharpest argument for fiber-based cancellation (Cats Effect/ZIO),
where `timeout` really does interrupt the work and run its finalizers.

**Fixing Part 3 to avoid the common pool** — that's what Part 3 already shows: pass a
named, bounded executor to every stage.

---

## P06 — Producer/consumer

### The four bugs in `BuggyQueue`

**A. `if` instead of `while`.** `wait()` may return spuriously, and even after a real
`notify` another thread may have changed the condition before you reacquired the monitor.
The condition **must** be rechecked in a loop.

**B. `notify()` instead of `notifyAll()`.** One monitor serves both "not full" and "not
empty" waiters. `notify()` wakes **one arbitrary** waiter, which may be the wrong kind —
a producer waking another producer — and the signal is lost forever.

**C. Unguarded `removeFirst()`.** Follows from A: if the queue is empty on wakeup, this
throws `NoSuchElementException`.

**D. Interruption/cancellation.** `put`/`take` declare `throws InterruptedException`, but
any caller that swallows it makes the queue uncancellable, and a thread that dies mid-
operation (as in C) can strand producers forever.

### The deterministic demonstration (verified)

The stress test **often passes** with the buggy queue — which is the real lesson. Forcing
the interleaving reproduces both bugs every time:

```
Scenario 1: two consumers parked in wait(), ONE item produced
  >>> java.util.NoSuchElementException
      C1 woke, took the item, called notify() again -> woke C2, which did not
      re-check emptiness and called removeFirst() on an empty list.

Scenario 2: two producers parked on a FULL queue of capacity 1
  final size = 2 (capacity is 1)
  >>> the bound was silently violated
```

Scenario 2 is the more insidious failure: **no exception, just a "bounded" queue that
isn't bounded** — which converts your backpressure mechanism into an unbounded queue
(quiz 02 Q2).

**A passing concurrency test proves almost nothing.** The buggy queue passed the 600-item
stress test on a fast idle machine. This is why you reason about the code, and why
jcstress exists.

### Exercise 1 — `ReentrantLock` + two `Condition`s

```java
public void put(T item) throws InterruptedException {
    lock.lock();
    try {
        while (items.size() == capacity) notFull.await();
        items.addLast(item);
        notEmpty.signal();              // only wakes a CONSUMER
    } finally { lock.unlock(); }
}

public T take() throws InterruptedException {
    lock.lock();
    try {
        while (items.isEmpty()) notEmpty.await();
        T item = items.removeFirst();
        notFull.signal();               // only wakes a PRODUCER
        return item;
    } finally { lock.unlock(); }
}
```

**Why two conditions:** `signal()` on a *specific* condition wakes only a waiter of that
kind, so there's no thundering herd and no wrong-waiter problem. That's exactly why bug B
can't happen here — and it's what `ArrayBlockingQueue` actually does internally. Note the
`while` loop is *still* required: `Condition.await()` also permits spurious wakeups, and
`signal()` doesn't transfer the lock atomically with the state change.

Two details that matter: `lock()` goes **outside** the `try`, `unlock()` **must** be in
`finally`, and `await()`/`signal()` require the lock to be held.

---

## P07 — Lazy initialisation

| approach | correct? | lazy? | hot read path |
|---|---|---|---|
| Eager `static final` | yes | **no** | plain static read (free) |
| `synchronized` accessor | yes | yes | monitor acquire on **every** read |
| **Broken DCL** | **no** | yes | plain read (but unsafe) |
| DCL + `volatile` | yes | yes | one volatile read (cheap) |
| **Holder idiom** | yes | yes | plain static read (free) ← **best for statics** |
| Memoizing supplier | yes | yes | one volatile read ← best for instance fields |

**Why broken DCL is broken.** `instance = new Heavy()` is three steps: allocate, run the
constructor, publish the reference. The JMM permits the *publish* to become visible before
the *constructor writes*. Thread B passes the unsynchronised first check, gets a non-null
reference, and reads default-valued fields from a half-built object. The `volatile` write
is a **release**, which forbids the constructor's writes from floating past it.

**The `volatile` is load-bearing on the read too:** without it, the first check may read a
stale cached value forever.

**Why we can't test our way to confidence.** Verified: **200,000 racing initialisations
produced 0 torn objects.** That's expected and proves nothing:
- x86 is strongly ordered (TSO), so only store→load reorders; this particular reordering
  is rare in practice. **ARM/Apple Silicon is weakly ordered**, so bug likelihood differs
  by *architecture* — your laptop and the server disagree.
- The window is nanoseconds wide, and each class initialises once per classloader.

It's **legal per the spec** and has been observed on weakly-ordered hardware. "I couldn't
reproduce it" is not an argument. Use **jcstress** if you want evidence about the JMM.

**An important subtlety worth knowing:** the demo class deliberately uses **non-final**
fields. If every field were `final`, JSR-133 **freeze semantics** would guarantee that any
thread seeing a reference to a correctly-constructed object also sees correct final-field
values — *even through a data race*. So broken DCL over an all-final-fields object happens
to be safe for those fields. That's not a reason to omit `volatile`: it breaks the moment
anyone adds a non-final field, or a final field pointing at something mutable.

**In a Spring singleton:** you rarely need any of this — let the container construct the
bean eagerly with constructor injection and `final` fields. **In Scala:** `lazy val`, which
compiles to double-checked initialisation guarded by the instance (a bitmap + `synchronized`
for multiple lazy vals). Its failure mode: **deadlock** when two threads initialise
interdependent `lazy val`s in opposite orders, or when a `lazy val` initialiser acquires a
lock someone else holds while waiting on the initialisation. Also relevant to virtual
threads on JDK 21, since that `synchronized` pins.

---

## P08 — `ThreadLocal`

**Part A, verified.** On a 1-thread pool, request 2 (unauthenticated, sets nothing) sees:
```
request 2 [pool-1-thread-1] principal=Principal[tenant=acme-corp, user=alice]
                            mdc={traceId=trace-111}
```
**This is cross-tenant data exposure**, not a bug report — request 2 reads request 1's
principal because the pool thread was reused and nothing cleared the `ThreadLocal`. The
trace id is wrong too, so your logs actively lie about which request did what.

**Part B, verified:** 22 MB retained with 2 pool threads × 8 MB buffers; **4 MB after
`remove()`**.

**Why nulling the `ThreadLocal` variable doesn't help:**
`ThreadLocalMap.Entry extends WeakReference<ThreadLocal<?>>` — the **key** is weak, the
**value** is strong. Dropping your reference to the `ThreadLocal` lets the *key* be
collected, leaving a stale entry whose **value is still strongly reachable** until that
slot is reused or the thread dies. Since pool threads never die, that's a leak. It's the
mechanism behind the classic webapp classloader leak: a `ThreadLocal` value whose class
came from the webapp classloader pins that entire classloader after redeploy.

**Part C — the fix.** Always `remove()` in a `finally`. To propagate deliberately, wrap
the executor (this is what Spring's `TaskDecorator` does):
```java
Executor decorated = task -> {
    var captured = CURRENT_USER.get();
    var capturedMdc = new HashMap<>(MDC.get());
    delegate.execute(() -> {
        try { CURRENT_USER.set(captured); MDC.set(capturedMdc); task.run(); }
        finally { CURRENT_USER.remove(); MDC.remove(); }   // the bug-preventer
    });
};
```

**Part D — virtual threads, verified:** every task got a fresh virtual thread, so `before`
was always `null` **even with no `remove()`**. Virtual threads make `ThreadLocal` *safer*
because there's no reuse. But two new problems: **footprint** (a million
`ThreadLocalMap`s; inheritable thread-locals are *copied* per thread) and
**`ThreadLocal`-as-a-cache stops working** (a per-thread `SimpleDateFormat` or reusable
buffer now allocates per request — look for this when migrating). Hence `ScopedValue`.

**Spring's `ThreadLocal`-backed ambient state**, the list to have ready:
`RequestContextHolder`, `SecurityContextHolder`, `TransactionSynchronizationManager` (the
JDBC connection for `@Transactional`), SLF4J `MDC`, `LocaleContextHolder`,
`RequestAttributes`/`@RequestScope`. **Every one of them breaks across an async boundary.**

---

## P09 — Implementation exercises

All four solutions below were run against the harness: **11 passed, 0 failed.**

### EX1 — `BoundedSemaphoreGate`
```java
<T> T run(Callable<T> body) throws Exception {
    semaphore.acquire();
    try { return body.call(); } finally { semaphore.release(); }
}

<T> Optional<T> tryRun(Callable<T> body, long timeout, TimeUnit unit) throws Exception {
    if (!semaphore.tryAcquire(timeout, unit)) return Optional.empty();
    try { return Optional.ofNullable(body.call()); } finally { semaphore.release(); }
}
```
The whole exercise is **`release()` in `finally`**. Miss it and one thrown exception
permanently burns a permit; after N exceptions the gate is closed forever — a slow-motion
outage that looks like a hang. The harness checks this explicitly ("releases on exception").

Note `acquire()` is interruptible (throws `InterruptedException`), which is what you want
— `acquireUninterruptibly()` makes the gate uncancellable.

### EX2 — `TokenBucketLimiter`
```java
boolean tryAcquire() {
    while (true) {
        State cur = state.get();
        long now = System.nanoTime();
        double refilled = Math.min(burst,
                cur.tokens() + (now - cur.nanos()) / 1e9 * permitsPerSecond);
        if (refilled < 1.0) return false;
        if (state.compareAndSet(cur, new State(refilled - 1.0, now))) return true;
    }
}
```
Verified: **111 admitted in 1 s** at a configured 100/s with burst 10; a fresh bucket
admits exactly its burst of **5** and then stops.

Points: refill is computed **lazily from elapsed time** (no timer thread); the CAS loop
makes it lock-free; `System.nanoTime()` (monotonic) not `currentTimeMillis()` (can jump).
If that one `AtomicReference` becomes the bottleneck, **shard** it — N buckets at rate/N,
picked by thread probe — trading exactness for scalability, same idea as `LongAdder`.

And say the systems part: with M instances a local limiter enforces M × rate. Distributed
limiting needs shared state (Redis `INCR` + TTL, or leased tokens), which adds a network
hop and a "fail open or closed?" decision.

### EX3 — `StripedCounter`
```java
void increment() {
    int stripe = (int) (Thread.currentThread().threadId() % STRIPES);
    cells.incrementAndGet(stripe * 8);     // *8 skips a cache line
}
long sum() {
    long total = 0;
    for (int i = 0; i < STRIPES; i++) total += cells.get(i * 8);
    return total;
}
```
Verified: **1.7× faster** than a single contended `AtomicLong` (38 ms vs 64 ms) — modest
here because 14 cores on a fresh `AtomicLongArray` isn't extreme contention; `LongAdder`
in P01 showed 36× under harder conditions.

The `* 8` stride is the point: an `AtomicLongArray` packs 8-byte longs contiguously, so 8
counters share one 64-byte cache line. Without the stride you get **false sharing** and
the striping buys you nothing. `threadId()` is a stable per-thread value; the real
`LongAdder` uses a randomised, rehashable probe so unlucky collisions can be fixed at
runtime.

`sum()` is **not** an atomic snapshot — deliberately. That's the trade.

### EX4 — `ExpiringCache` (single-flight)
```java
V get(K key) {
    long now = System.nanoTime();
    var mine = new CompletableFuture<V>();
    var entry = map.compute(key, (k, old) ->
            (old != null && old.expiresAtNanos() > now) ? old : new Entry<>(mine, now + ttlNanos));
    if (entry.future() == mine) {                       // I won the race: I load
        try { mine.complete(loader.apply(key)); }
        catch (Throwable t) { map.remove(key, entry); mine.completeExceptionally(t); }
    }
    return entry.future().join();                        // losers join the same future
}
```
Verified: **loader called exactly 1×** for 16 concurrent gets, all 16 got the value,
cached reads don't reload, and it reloads after the TTL.

**The trick:** store a `CompletableFuture<V>` as the value, not the value. Racing callers
all get the *same* future and `join()` it, so the loader runs once — but the mapping
function inside `compute` only **creates** the future (fast), it doesn't **run the loader**.
The load happens *outside* the bin lock. That's the fix for P04 Part C's problem, and it's
exactly how Caffeine's `AsyncLoadingCache` works.

Two details: on failure you must **remove the entry** or you cache the exception forever;
and identity comparison (`entry.future() == mine`) is how you know whether you won.

In production: **use Caffeine.** It handles size and time bounds, refresh-ahead, weak
keys, stats, and eviction far better than this. The exercise is for understanding, not
for shipping.

---

## P10 — Virtual threads

**Verified on JDK 21.0.11, 14 cores:**

```
PART 1: 10,000 tasks each blocking 200ms
  virtual thread per task      251 ms      <- ~one sleep, all concurrent
  fixed pool of 200         10,381 ms      <- 50 batches x 200ms
  fixed pool of 14 (x1000)  14,847 ms      -> ~148,000 ms for 10,000
  => virtual is 41x faster than the 200-thread pool

PART 2: 50,000 LIVE virtual threads created in 36 ms, 105 MB heap
        = ~0.7 microseconds and ~2.2 KB each
        2000 virtual threads ran on ~14 carriers

PART 3: PINNING -- 500 tasks, each with its OWN lock, 100ms sleep
  synchronized (pins)  : 3,841 ms   <-- limited by carrier count
  ReentrantLock (free) :   112 ms   <-- limited only by the sleep
  => 34x difference with ZERO lock contention

PART 4: 200 CPU-bound tasks
  virtual            68 ms
  fixed pool(14)     48 ms          <- virtual threads do not help
PART 5: 2000 virtual threads through Semaphore(50): 554 ms, max concurrent = 50
```

**Part 3 is the most valuable measurement in the file.** Each task uses its **own** lock,
so there is *no contention at all* — yet `synchronized` is **34× slower**. That's pure
pinning: on JDK 21 a virtual thread blocking inside `synchronized` cannot be unmounted, so
it holds its carrier, and with ~14 carriers you get ~14-way concurrency instead of 500.
`ReentrantLock` is virtual-thread-aware and unmounts cleanly.

Diagnose with `-Djdk.tracePinnedThreads=short`. **JDK 24 (JEP 491) made monitors unpin**;
native frames still pin on every version.

**Part 4:** virtual threads solve **waiting**, not **computing**. The bottleneck is cores.
A fixed platform pool sized to cores is still correct for CPU-bound work, and is slightly
*faster* (less scheduling overhead).

**Part 5 — the mental shift.** 1950 of the 2000 virtual threads were parked on a semaphore
costing nothing. A 50-thread pool expresses the same limit but **conflates** "the
downstream supports 50 concurrent calls" with "we have 50 units of execution capacity".
Separating them is the point:

> **old:** size the thread pool to protect the downstream
> **new:** unbounded virtual threads + a `Semaphore` per dependency

**Spring Boot adoption checklist** (quiz 03 Q9): `spring.threads.virtual.enabled=true`;
hunt `synchronized` on blocking paths (including in dependencies) with
`-Djdk.tracePinnedThreads=full`; **the connection pool becomes the real limit**, so bound
and tune it; you've removed your accidental admission control, so add explicit rate limits;
thread-count metrics and `jstack` stop being useful (use
`jcmd <pid> Thread.dump_to_file -format=json`).

---

## P11 — Collections

**Verified on 14 cores, JDK 21.**

### Part 1 — map performance (200 000 entries, shuffled keys)

```
                     put (ms)     get (ms) iterate (ms)
HashMap                     8            2            7
LinkedHashMap              11            5            9
TreeMap                    64           51           10
```

`HashMap`/`LinkedHashMap` are O(1); `TreeMap` is O(log n), which at n = 200 000 is ~17
comparisons per operation, each a virtual `compareTo` call. **`TreeMap` is ~8× slower on
`put` and ~25× on `get`.**

**This disproves the crib note's "`TreeMap` is good for frequent add/remove" as a speed
claim.** The correct version: if you need data **kept in sorted order** while it's frequently
modified, `TreeMap` maintains that order incrementally at O(log n), which beats re-sorting a
list on every change. **The advantage is the ordering, not the mutation speed.**

Note `LinkedHashMap` costs almost nothing over `HashMap` — so if you want deterministic
iteration order, it's nearly free, and it's the better default for anything whose output you
compare in tests.

### Part 2 — what `TreeMap` actually buys

```
HashMap iteration order: [50, 20, 40, 10, 30]     <- unspecified, changes on resize
TreeMap iteration order: [10, 20, 30, 40, 50]     <- always sorted

floorKey(35)   = 30      ceilingKey(35) = 40      higherKey(30) = 40
headMap(30)    = {10=v10, 20=v20}
subMap(20, 45) = {20=v20, 30=v30, 40=v40}         <- RANGE QUERY
```
**`NavigableMap` is the reason to choose it**, not performance. `HashMap` cannot do range
queries or nearest-match at any price.

**Nulls, verified — the crib note is half right:**
```
HashMap null key  : OK        (one null key allowed)
TreeMap null VALUE: OK
TreeMap null KEY  : NullPointerException
```
So "`TreeMap` doesn't support nulls" is true for **keys only**. Being precise here is a cheap
credibility win.

### Part 3 — breaking the `hashCode`/`equals` contract

**Case A — `equals` without `hashCode`:**
```
new BadKey("k1").equals(existing) = true
map.get(equal key)               = null    <-- different buckets
set.size() after adding 2 equals = 2       <-- a Set containing duplicates
```

**Case C — mutating a key after insertion. The expensive one:**
```
before mutation: get(key) = value
after  mutation: get(key) = null      <-- using the SAME object reference!
containsKey(key)          = false
map.size()                = 1         <-- still there
remove(key) returns       = null
size after remove         = 1         <-- UNREACHABLE AND UNREMOVABLE
iteration shows:            key=mutated value=value
```
The entry sits in the bucket computed from its *old* hash. You can't get it, you can't
remove it, and iteration proves it's still there. **This is why map keys must be immutable**,
and it's the single most persuasive demonstration in the file.

**Case D — a legal but constant `hashCode`:**
```
good hashCode:        10 ms
constant hashCode:  4734 ms    (473x slower)
```
Contract-compliant and catastrophic. Every entry lands in one bucket; Java 8+ treeifies it so
it's O(log n) rather than O(n) — still 473× worse than O(1). Pre-Java-8 this was a
denial-of-service vector via crafted collisions.

### Part 4 — `ArrayList` vs `LinkedList`

```
get(i) x 100,000   ArrayList :   3 ms
get(i) x  20,000   LinkedList: 534 ms     <- 5x fewer operations, 178x slower

insert at MIDDLE x 20,000
  ArrayList :   8 ms      (O(n) arraycopy -- but vectorised)
  LinkedList: 223 ms      (O(1) unlink, but O(n) to WALK there)
```

**The textbook claim that `LinkedList` wins on middle-insertion is wrong here by 28×**, for
the reasons in quiz 09 Q6: you must traverse to the position first, `System.arraycopy` is a
single vectorised move, and cache misses dominate at realistic sizes. Constant factors beat
asymptotics.

**Use `ArrayList` for lists and `ArrayDeque` for queues/stacks. `LinkedList` is effectively
never the right answer.**

### Part 5 — `List.contains` in a loop

```
20,000 lookups against a List:  98.7 ms
20,000 lookups against a Set :   0.9 ms
--> 114x difference, and the gap grows LINEARLY with n
```

**The most common real performance bug in ordinary business code**, and it hides beautifully:
fast with 100 elements in a unit test, quadratic with 50 000 in production. Whenever you see
`.contains()` inside a loop, or a nested loop over two collections, reach for a `Set` or a
`Map`.

Same shape as the **N+1 query problem** (quiz 11 Q9) — correct, and accidentally quadratic.
Being able to connect those two is a good sign you're pattern-matching on the *cause* rather
than memorising two separate gotchas.

---

## P12 — DSA practice

**All 36 assertions verified passing** against the reference solutions below.

For each one: the code, then **the sentence to say out loud**. On a recorded round the
narration is half the mark, so practise the sentence, not just the code.

### 1. `reverseString` — two pointers
```java
char[] c = s.toCharArray();
for (int i = 0, j = c.length - 1; i < j; i++, j--) { char t = c[i]; c[i] = c[j]; c[j] = t; }
return new String(c);
```
> "O(n) time, one pass swapping from both ends. **O(n) space, because Java strings are
> immutable** so I have to allocate a char array — there's no true in-place version. If it were
> a `char[]` parameter it would be O(1) extra space."

That immutability point is a cheap way to show precision. `new StringBuilder(s).reverse()` is
the one-liner — say you know it, then show the manual version.

### 2. `reverseWords` — split, reverse, join
```java
String[] w = s.trim().split("\\s+");
Collections.reverse(Arrays.asList(w));
return String.join(" ", w);
```
> "O(n) time and O(n) space. The edge cases are leading/trailing whitespace and multiple
> spaces between words — `trim()` plus splitting on `\\s+` handles both, so
> `\"  hello   world  \"` gives `\"world hello\"`."

`Arrays.asList` is a *view*, so `Collections.reverse` mutates the original array — a nice
detail to mention.

### 3. `wordFrequency` / `topKWords` — `merge`
```java
Map<String, Integer> m = new HashMap<>();
for (String w : text.toLowerCase().trim().split("\\s+")) m.merge(w, 1, Integer::sum);

// top k, count descending then alphabetical:
return wordFrequency(text).entrySet().stream()
    .sorted(Map.Entry.<String,Integer>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
    .limit(k).map(Map.Entry::getKey).toList();
```
> "Counting is O(n) time and O(d) space for d distinct words. `map.merge(w, 1, Integer::sum)`
> is the idiomatic form — cleaner than get-or-default and it's a single map operation.
> For top-k, sorting is O(d log d); if k is much smaller than d I'd use a **min-heap of size
> k** for O(d log k) instead."

Offering the heap alternative unprompted is the differentiator here. **And note the
tie-break** — an unspecified tie-break makes the test non-deterministic, which is worth
raising as a clarifying assumption.

### 4. `firstNonRepeating` — `LinkedHashMap`
```java
Map<Character, Integer> counts = new LinkedHashMap<>();
for (char c : s.toCharArray()) counts.merge(c, 1, Integer::sum);
for (var e : counts.entrySet()) if (e.getValue() == 1) return e.getKey();
return '\0';
```
> "Two passes, but O(n) overall. The trick is **`LinkedHashMap`, which preserves insertion
> order**, so the second pass finds the *first* such character — with a plain `HashMap` the
> iteration order is unspecified and the answer would be wrong. For ASCII-only input I could
> use an `int[128]` counter array plus a second pass over the string, which is O(1) space."

The `LinkedHashMap`-vs-`HashMap` point is exactly the sort of thing these questions test, and
it links to quiz 09 Q2.

### 5–6. `hasDuplicates` / `findDuplicates` — `Set.add` returns a boolean
```java
Set<Integer> seen = new HashSet<>();
for (int x : a) if (!seen.add(x)) return true;      // add returns false if already present
return false;

// all duplicates, ascending -- TreeSet gives sorted + deduped:
Set<Integer> seen = new HashSet<>(), dup = new TreeSet<>();
for (int x : a) if (!seen.add(x)) dup.add(x);
return new ArrayList<>(dup);
```
> "O(n) time, O(n) space, and it **exits early** on the first duplicate. Using `add`'s return
> value avoids a separate `contains` call — one hash lookup instead of two."

Follow-up to be ready for: *"do it in O(1) space."* Answer: sort first, then scan adjacent
pairs — O(n log n) time, O(1) extra space **if you may mutate the input**. That "if" is the
real answer, and asking about it is the right move.

### 7. `longestUniqueSubstring` — the sliding window
```java
Map<Character, Integer> last = new HashMap<>();
int best = 0, left = 0;
for (int right = 0; right < s.length(); right++) {
    char c = s.charAt(right);
    Integer prev = last.get(c);
    if (prev != null && prev >= left) left = prev + 1;   // >= left: only if INSIDE the window
    last.put(c, right);
    best = Math.max(best, right - left + 1);
}
return best;
```
> "Sliding window, **O(n) time** — `right` advances once per character and `left` only ever
> moves forward, so each index is visited at most twice. **O(min(n, k)) space** where k is the
> alphabet size, because the map holds at most one entry per distinct character.
> The brute force is O(n²) or O(n³) checking every substring.
> Edge cases: empty string is 0, all-identical is 1, all-distinct is n — all covered."

**The bug to avoid, and worth narrating:** `prev >= left` is essential. Without it, a repeat
that's *before* the current window drags `left` backwards and you get a wrong answer.
`"abba"` is the input that catches it — a good one to walk through by hand on camera.

### 8. `isBalanced` — a stack
```java
Deque<Character> st = new ArrayDeque<>();
Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
for (char c : s.toCharArray()) {
    if (pairs.containsValue(c)) st.push(c);
    else if (pairs.containsKey(c)) { if (st.isEmpty() || st.pop() != pairs.get(c)) return false; }
}
return st.isEmpty();
```
> "O(n) time, O(n) space worst case — all opening brackets. **The two failure modes are a
> mismatch and leftover opens**, so I check the stack isn't empty before popping, and that
> it's empty at the end. `\"(\"` and `\")\"` are the two edge cases that catch a wrong answer."

Use **`ArrayDeque`, not `java.util.Stack`** — `Stack` is synchronised and extends `Vector`
(quiz 09 Q3). Saying that is a free credibility point.

### 9. `twoSum` — the canonical space-for-time trade
```java
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    Integer j = seen.get(target - nums[i]);
    if (j != null) return new int[]{j, i};
    seen.put(nums[i], i);
}
return null;
```
> "Brute force is O(n²) with O(1) space. This is **O(n) time and O(n) space** — I trade memory
> for time by remembering what I've seen and looking for the complement. One pass: I check
> before inserting, which also correctly handles the case where the same value appears twice."

If the array were **sorted**, mention the **two-pointer** alternative: O(n) time, O(1) space.
Knowing which precondition unlocks which algorithm is the point.

### 10. `isAnagram` — sort or count
```java
char[] x = a.toLowerCase().replaceAll("\\s+", "").toCharArray();
char[] y = b.toLowerCase().replaceAll("\\s+", "").toCharArray();
Arrays.sort(x); Arrays.sort(y);
return Arrays.equals(x, y);
```
> "Sorting is O(n log n). **Counting is O(n)** — an `int[26]` or a map, incremented for one
> string and decremented for the other, then check all zeros. I'd mention that Unicode makes
> this harder: normalisation, and case-folding isn't 1:1 in every locale."

Raising Unicode without being asked is a strong signal, especially at a global bank.

### 11. `fizzBuzz` — check 15 first
```java
for (int i = 1; i <= n; i++)
    out.add(i % 15 == 0 ? "FizzBuzz" : i % 3 == 0 ? "Fizz" : i % 5 == 0 ? "Buzz" : String.valueOf(i));
```
> "O(n). The only trap is checking divisibility by 15 **first** — or equivalently checking
> both conditions before either individually."

Trivial, and people do fumble it under camera pressure. Don't be one of them.

### 12. `mergeIntervals` — sort then sweep
```java
Arrays.sort(a, Comparator.comparingInt(x -> x[0]));      // by START
List<int[]> out = new ArrayList<>();
int[] cur = a[0].clone();
for (int i = 1; i < a.length; i++) {
    if (a[i][0] <= cur[1]) cur[1] = Math.max(cur[1], a[i][1]);   // overlap -> extend
    else { out.add(cur); cur = a[i].clone(); }
}
out.add(cur);
```
> "**O(n log n)**, dominated by the sort; the sweep is O(n). O(n) space for the output.
> Sorting by start is what makes the single sweep correct — then any interval either overlaps
> the current one or starts a new one.
> Two details: `Math.max` on the end, because an interval can be **fully contained** in the
> previous one; and `<=` rather than `<` if touching intervals like `[1,4],[4,5]` should
> merge — which is a clarifying question worth asking."

`comparingInt` rather than `comparing` avoids boxing (quiz 09 Q7), and **never subtract for
comparison** (`x[0] - y[0]` overflows).

---

### The pattern-recognition table

Most bank-screen problems are one of these. Recognising the pattern in the first 20 seconds is
the actual skill:

| Cue in the question | Reach for |
|---|---|
| "longest/shortest substring or subarray with property X" | **sliding window** |
| "does a pair/triple sum to X" | **hash map**, or two pointers if sorted |
| "count occurrences / frequency / most common" | **HashMap + `merge`** |
| "any duplicates / first unique" | **Set / LinkedHashMap** |
| "matched / nested / valid sequence" | **stack (`ArrayDeque`)** |
| "top K" | **heap of size K** (`PriorityQueue`) |
| "intervals, overlaps, meetings" | **sort by start, then sweep** |
| "in a sorted array…" | **binary search or two pointers** |
| "all permutations / combinations / subsets" | **backtracking** |
| "shortest path, levels, nearest" | **BFS** |
| "tree/graph traversal, connected components" | **DFS** |
| "can I reach / how many ways / optimal over subproblems" | **DP** — memoise first, then tabulate |
| "kth smallest/largest" | heap, or quickselect for O(n) average |

### The SQL ones

The reported bank set includes second-highest-salary and join types (quiz 11 has the depth).

```sql
-- Second-highest salary. Window function -- the modern answer:
SELECT DISTINCT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employees
) t WHERE rnk = 2;

-- Or, simply:
SELECT MAX(salary) FROM employees WHERE salary < (SELECT MAX(salary) FROM employees);
```
> "I'd use `DENSE_RANK` rather than `LIMIT 1 OFFSET 1`, because **`DENSE_RANK` handles ties
> the way the business usually means it** — if two people share the top salary, the
> second-highest *distinct* salary is what's wanted. And it returns `NULL` rather than erroring
> if there's no second value, which is worth stating as an assumption."

`RANK` skips numbers after a tie, `DENSE_RANK` doesn't, `ROW_NUMBER` breaks ties arbitrarily.
Knowing the difference between those three is the actual question being asked.

**Join types**, in one line each: `INNER` (matches only) · `LEFT` (all left + matches) ·
`RIGHT` · `FULL OUTER` (all of both) · `CROSS` (cartesian) · `SELF` (a table to itself) ·
plus the **anti-join** (`NOT EXISTS` / `LEFT JOIN ... IS NULL`) and **semi-join**
(`EXISTS` / `IN`). The trap worth naming: **a `LEFT JOIN` with a `WHERE` on the right table's
column silently becomes an inner join**, because `NULL` fails the predicate — put the
condition in the `ON` clause instead (quiz 11 Q5).
