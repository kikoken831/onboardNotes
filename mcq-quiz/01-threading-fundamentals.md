# MCQ 01 — Threading Fundamentals & the JMM

20 questions. Source: [`quiz/01`](../quiz/01-threading-fundamentals.md).
Commit to an answer before expanding. Note *why* the others are wrong.

---

### Q1. What does `volatile` guarantee?

- **A.** Visibility, ordering, and atomicity of compound operations like `i++`
- **B.** Mutual exclusion, so only one thread can touch the field at a time
- **C.** Visibility and ordering, plus atomicity of the single read and single write — but not of compound operations
- **D.** That the field is kept in a CPU register for faster access

<details><summary>Answer</summary>

**C.** A volatile write is a release, a volatile read is an acquire, and each individual access
is atomic (including for `long`/`double`). But `i++` is read-modify-write — three operations —
so it still races.

- **A** — the single most common misconception. Compound atomicity needs `AtomicInteger` or a lock.
- **B** — `volatile` provides no mutual exclusion whatsoever.
- **D** — backwards. `volatile` *prevents* caching in a register; that's the point.

→ `quiz/01` Q1. Measured in `problems/java/P01Counter.java`: volatile lost 3,477 of 4,000,000
increments, and was *slower* than a plain `int` (86 ms vs 13 ms).
</details>

---

### Q2. Which of these is **not** a happens-before edge?

- **A.** Two threads writing to different `volatile` fields
- **B.** `t.start()` → everything in thread `t`
- **C.** Unlock of a monitor → subsequent lock of the *same* monitor
- **D.** Everything in `t` → `t.join()` returning

<details><summary>Answer</summary>

**A.** Volatile gives an edge only between a write and a subsequent read of **the same**
variable. Writes to two *different* volatiles establish nothing between those threads.

**C**, **B** and **D** are all standard edges, along with: program order within a thread, final-field freeze,
interrupt → detection, and anything through a `java.util.concurrent` structure.

→ `quiz/01` Q2. And happens-before is **transitive** — that's what you exploit in practice.
</details>

---

### Q3. Why is double-checked locking broken without `volatile`?

- **A.** The second null check is redundant, so the compiler removes it
- **B.** `synchronized` doesn't flush the field to main memory
- **C.** Two threads can both enter the synchronized block simultaneously
- **D.** `instance = new Heavy()` may publish the reference before the constructor's writes are visible, so another thread can see a half-built object

<details><summary>Answer</summary>

**D.** The allocation is allocate → run constructor → assign reference, and the JMM permits the
assignment to become visible before the constructor's writes. A thread passing the *unsynchronised*
first check can therefore return a partially-initialised object. `volatile` makes the write a
release, forbidding the reorder.

- **B** — `synchronized` does establish edges, but only for threads that *acquire the same monitor*.
  The bug lives on the unsynchronised fast path.
- **C** — impossible; that's what the monitor guarantees.

→ `quiz/01` Q4. Note the subtlety in `problems/java/P07LazyInit.java`: with **all-final fields**,
JSR-133 freeze semantics happen to protect you — which is not a reason to omit `volatile`.
</details>

---

### Q4. When should you prefer `ReentrantLock` over `synchronized`?

- **A.** Always — it's faster because it avoids monitor overhead
- **B.** When you need `tryLock`, a timeout, interruptible acquisition, fairness, or multiple condition variables
- **C.** When the critical section is very short
- **D.** When you want the lock released automatically on an exception

<details><summary>Answer</summary>

**B.** Those are the capabilities `synchronized` cannot express. Add one more for JDK 21:
`ReentrantLock` doesn't pin a virtual thread's carrier, and `synchronized` does.

- **A** — not generally faster; and `synchronized` is what the JIT optimises hardest.
- **C** — backwards; short sections are where `synchronized` is most attractive.
- **D** — exactly backwards. `synchronized` releases automatically; `ReentrantLock` needs
  `try { } finally { unlock(); }` every time or it leaks.

→ `quiz/01` Q5
</details>

---

### Q5. What is the ABA problem?

- **A.** A CAS succeeds because the value matches, even though it changed to something else and back in between
- **B.** Two threads deadlock by acquiring locks in opposite order
- **C.** A thread starves because higher-priority threads keep winning the lock
- **D.** An atomic operation is interrupted midway and leaves a partial write

<details><summary>Answer</summary>

**A.** Thread 1 reads `A`; thread 2 changes it `A → B → A`; thread 1's CAS succeeds even though
the world moved. Harmless for counters, dangerous when the value is a reference and history
matters (a recycled node in a lock-free stack). Fix with a version stamp —
`AtomicStampedReference`.

- **B** is deadlock, **C** is starvation, **D** can't happen — CAS is atomic.

→ `quiz/01` Q6
</details>

---

### Q6. When is `LongAdder` the better choice over `AtomicLong`?

- **A.** When you need `compareAndSet` on the counter
- **B.** When reads are frequent and must be an exact atomic snapshot
- **C.** When writes are frequent and contended, and you read the total rarely
- **D.** When the counter must be visible across JVMs

<details><summary>Answer</summary>

**C.** `LongAdder` stripes across padded cells so contending threads write to different cache
lines. Measured **36× faster** than a contended `AtomicLong` (5 ms vs 179 ms, 8 threads).

- **A** — `LongAdder` has no CAS operation, so "increment only if under limit" is impossible.
- **B** — `sum()` is explicitly **not** an atomic snapshot.
- **D** — neither works across JVMs.

→ `quiz/01` Q7, measured in `problems/java/P01Counter.java`
</details>

---

### Q7. Which is the most reliable way to prevent deadlock?

- **A.** Give one thread a higher priority so it always wins
- **B.** Acquire locks in a consistent global order everywhere
- **C.** Use `synchronized` rather than `ReentrantLock`
- **D.** Make all the locked objects `volatile`

<details><summary>Answer</summary>

**B.** A global lock ordering breaks the circular-wait condition and is the workhorse fix.
`tryLock` with **randomised** backoff is the other main option (breaking hold-and-wait) — but
fixed backoff there gives you livelock instead.

- **A** — priorities don't prevent cycles, and can make it worse (priority inversion).
- **C**/**D** — irrelevant to deadlock.

→ `quiz/01` Q8. Reproduced with `jstack` output and both fixes verified in
`problems/java/P03Deadlock.java`.
</details>

---

### Q8. `x = 1; a = y;` in thread 1 and `y = 1; b = x;` in thread 2, all non-volatile. Can you observe `a == 0 && b == 0`?

- **A.** No — one thread must run second, so at least one of them sees the other's write
- **B.** Only if the JIT hasn't compiled the code yet
- **C.** No — but only on x86; it's possible on ARM
- **D.** Yes — both compiler and CPU may reorder each thread's independent store and load

<details><summary>Answer</summary>

**D.** This is Dekker's example. There's no happens-before between the threads, so the store and
the load in each may reorder. On x86 the store buffer alone produces it.

**A** is the intuitive-but-wrong reasoning: it assumes sequential consistency, and the JMM only
guarantees SC for **data-race-free** programs. This program has a race, so all bets are off.
Making `x` and `y` volatile makes the outcome impossible.

→ `quiz/01` Q9
</details>

---

### Q9. `synchronized (this) { if (queue.isEmpty()) wait(); return queue.remove(); }` — what's the primary bug?

- **A.** `wait()` should be `sleep()`
- **B.** `wait()` doesn't release the monitor, so nothing can add to the queue
- **C.** `if` should be `while` — `wait()` can return spuriously, and another consumer may have taken the item
- **D.** `synchronized (this)` should lock the queue instead

<details><summary>Answer</summary>

**C.** Spurious wakeups are permitted by the spec, and even after a real `notify` another thread
may consume the item before you reacquire the monitor. The condition **must** be rechecked in a
loop.

- **B** is exactly backwards — `wait()` *does* release the monitor (that's the difference from
  `sleep()`).

Also wrong here: no `InterruptedException` handling, and whoever signals should use `notifyAll()`.
The meta-answer: don't hand-roll monitors — use `ArrayBlockingQueue`.

→ `quiz/01` Q10. `problems/java/P06ProducerConsumer.java` reproduces both the
`NoSuchElementException` **and** a silent capacity violation, deterministically.
</details>

---

### Q10. Is `if (!map.containsKey(k)) map.put(k, expensive(k));` safe on a `ConcurrentHashMap`?

- **A.** No — check-then-act isn't atomic, so `expensive()` may run several times and writes can be lost
- **B.** Yes — every `ConcurrentHashMap` method is atomic
- **C.** Yes, provided the map was safely published
- **D.** No — `containsKey` isn't thread-safe on a `ConcurrentHashMap`

<details><summary>Answer</summary>

**A.** Each call is atomic; the **combination is not**. Measured: 8 threads → `expensive()` ran
**8 times**, 7 results silently discarded. Fix with `computeIfAbsent` (verified: exactly 1 call).

- **B** is the trap — per-operation atomicity does not compose.
- **D** — `containsKey` is thread-safe; that's not the issue.

→ `quiz/01` Q11, measured in `problems/java/P04ComputeIfAbsentDeadlock.java`
</details>

---

### Q11. Which does **not** safely publish an object to another thread?

- **A.** Storing it in a `volatile` field
- **B.** Storing it in a `final` field of a properly-constructed object
- **C.** Putting it into a `BlockingQueue` the other thread takes from
- **D.** Assigning it to a plain (non-final, non-volatile) field after the constructor returns

<details><summary>Answer</summary>

**D.** No happens-before edge, so the reader may see `null` or a partially-constructed object.
This is *the* canonical Spring bug — a singleton bean with a mutable field populated after
construction.

**A**, **B** and **C** are all valid, along with: static initialisers, lock-guarded fields, `Thread.start()`,
`Future.get()`, and latches.

→ `quiz/01` Q3 and Q12
</details>

---

### Q12. What is false sharing?

- **A.** Two threads reading the same variable without synchronisation
- **B.** Two threads writing to different variables that happen to occupy the same cache line
- **C.** A thread seeing a stale cached value of a shared field
- **D.** Sharing an object without safe publication

<details><summary>Answer</summary>

**B.** No logical contention at all, but the cache-coherency protocol invalidates the whole line
on every write, so it ping-pongs between cores. Throughput can drop an order of magnitude with no
lock in sight. Fix by padding (`@Contended`) or by not sharing — which is what `LongAdder`'s
padded cells do.

→ `quiz/01` Q13
</details>

---

### Q13. Why is `ThreadLocal` dangerous on a pooled thread?

- **A.** Pool threads are reused, so a value set by one task is visible to unrelated later tasks — and the value is strongly retained for the pool's lifetime
- **B.** `ThreadLocal` isn't thread-safe
- **C.** Every `ThreadLocal` access requires a lock
- **D.** `ThreadLocal` values are copied to every thread in the pool

<details><summary>Answer</summary>

**A.** Two consequences: **context bleed** (task A reads task B's principal — a security incident
in a multi-tenant service) and a **memory leak**, because `ThreadLocalMap.Entry` holds the *key*
weakly but the *value* strongly.

Measured: an unauthenticated request read the previous request's principal; and 22 MB retained,
dropping to 4 MB after `remove()`.

→ `quiz/01` Q14, measured in `problems/java/P08ThreadLocalLeak.java`
</details>

---

### Q14. You catch `InterruptedException` in a `Runnable` and can't propagate it. What should you do?

- **A.** Log it and continue the loop
- **B.** Swallow it — the interrupt has been handled by catching it
- **C.** Call `Thread.currentThread().interrupt()` to restore the flag, then stop doing work
- **D.** Rethrow it as a `RuntimeException`

<details><summary>Answer</summary>

**C.** Catching the exception **clears** the interrupt status, so code further up the stack —
including the pool — can never learn the thread was interrupted. Restore the bit and return.

- **A**/**B** destroy the only cancellation signal the JVM has. Interruption is *cooperative*;
  a task that ignores interrupts is uncancellable.
- **D** obscures the meaning and is likely to be caught by generic handling.

→ `quiz/01` Q15
</details>

---

### Q15. `Thread.sleep()` vs `Object.wait()` — which releases the lock?

- **A.** Both release all held locks
- **B.** `wait()` releases the monitor it was called on; `sleep()` releases nothing
- **C.** `sleep()` releases the monitor; `wait()` holds it
- **D.** Neither releases anything

<details><summary>Answer</summary>

**B.** `wait()` atomically releases *that* monitor and must reacquire it before returning — but
any *other* locks the thread holds are still held, which is a deadlock source. `sleep()` holds
everything, which is why sleeping inside a `synchronized` block is a good way to build a
bottleneck.

→ `quiz/01` Q16
</details>

---

### Q16. You need "at most 10 concurrent calls to this downstream service." Which primitive?

- **A.** `CountDownLatch(10)`
- **B.** `CyclicBarrier(10)`
- **C.** `ReentrantLock` with fairness enabled
- **D.** `Semaphore(10)`

<details><summary>Answer</summary>

**D.** A semaphore bounds **concurrency** — counting permits, acquire/release.

- **A** is a one-shot gate ("wait until 10 things have happened") and can't be reused.
- **B** is a rendezvous — 10 threads wait for *each other*, then all proceed.
- **C** bounds concurrency to **one**.

Also worth knowing: a `Semaphore` limits concurrency, **not rate**. Rate limiting needs a token
bucket — people conflate these constantly.

→ `quiz/01` Q17; implemented and verified in `problems/java/P09Exercises.java` EX1 and EX2.
</details>

---

### Q17. Why does `StampedLock` exist, given `ReentrantReadWriteLock` already allows concurrent reads?

- **A.** It's reentrant, unlike `ReentrantReadWriteLock`
- **B.** It supports `Condition` objects, which RWLock doesn't
- **C.** It offers optimistic reading that writes nothing to shared lock state on the happy path
- **D.** It guarantees writer priority, preventing writer starvation

<details><summary>Answer</summary>

**C.** In a `ReentrantReadWriteLock`, *every reader writes* to the lock's shared state to register
itself — and on read-mostly workloads across many cores that counter is the bottleneck.
`tryOptimisticRead` + `validate` avoids touching shared state at all.

- **A** is exactly backwards — `StampedLock` is **not** reentrant, and re-acquiring deadlocks you.
- **B** is backwards too — it does **not** support `Condition`.

→ `quiz/01` Q18
</details>

---

### Q18. What does `CopyOnWriteArrayList` give you that `Collections.synchronizedList` doesn't?

- **A.** Lock-free reads and iteration that can't throw `ConcurrentModificationException`
- **B.** Faster writes
- **C.** Lower memory use
- **D.** Atomic compound operations

<details><summary>Answer</summary>

**A.** Every mutation copies the whole backing array, so readers work on an immutable snapshot —
no lock, no CME, and the iterator doesn't see later writes.

- **B** is backwards: writes are **O(n)** with a garbage spike per write.
- **C** is backwards for the same reason.
- **D** — neither provides that.

Right for listener registries. Never for a list you append to in a loop — that's accidentally
quadratic.

→ `quiz/01` Q19
</details>

---

### Q19. Which reference type is cleared as soon as the referent is no longer strongly reachable?

- **A.** Soft
- **B.** Strong
- **C.** Phantom
- **D.** Weak

<details><summary>Answer</summary>

**D.** Weak references are cleared at the next GC once nothing strongly reachable points at the
object — correct for canonicalising maps and metadata keyed by an object you don't own.

- **A** — soft references are cleared **at the GC's discretion when memory is tight**, which
  makes them a poor cache (they tend to hold garbage until they cause a long pause).
- **C** — phantom refs are enqueued *after* finalisation and `get()` always returns `null`.

→ `quiz/04` Q21
</details>

---

### Q20. A `@Service` bean has `private List<Order> pending = new ArrayList<>()` and adds to it in a request-handling method. What's the problem?

- **A.** Nothing, provided the method is short
- **B.** Spring beans are singletons, so all request threads share that list and `ArrayList.add` corrupts it
- **C.** `ArrayList` should be `LinkedList` for concurrent appends
- **D.** The field should be `volatile`

<details><summary>Answer</summary>

**B.** Default bean scope is singleton — one instance shared by every request thread. Concurrent
`add` corrupts the backing array: lost elements, or `ArrayIndexOutOfBoundsException` from a torn
resize.

- **C** — `LinkedList` is equally unsafe.
- **D** — `volatile` on the *reference* does nothing for the mutable list it points at. The
  classic "`val`/`final` isn't immutability" error.

**The rule: Spring beans should be stateless.** This is the highest-frequency real Java
concurrency bug.

→ `quiz/01` Q24, `quiz/10` Q3
</details>

---

## Score yourself

- **17+** — solid. Move on.
- **12–16** — reread `quiz/01` Q3, Q9, Q12, Q24; those four are the ones that ship as real bugs.
- **<12** — run `problems/java/P01`–`P03` before rereading. Empirics stick better than prose.

**Whatever you scored:** Q1, Q11 and Q20 must be automatic. Say each answer out loud in a full
sentence before you consider this file done.
