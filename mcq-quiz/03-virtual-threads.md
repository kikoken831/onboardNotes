# MCQ 03 — Virtual Threads & Structured Concurrency (JDK 21)

10 questions. Source: [`quiz/03`](../quiz/03-virtual-threads-jdk21.md).

---

### Q1. What is a virtual thread?

- **A.** A thread pooled and reused by the JVM to avoid creation cost
- **B.** A lightweight OS thread created via a faster syscall
- **C.** A `Thread` scheduled by the JVM, whose stack lives on the heap as a continuation and unmounts from a carrier when it blocks
- **D.** A coroutine that requires `async`/`await` keywords to suspend

<details><summary>Answer</summary>

**C.** It's a real `java.lang.Thread`, but scheduled by the JVM onto a **carrier** (a platform
thread from a dedicated `ForkJoinPool`). When it blocks on an instrumented JDK call, its stack is
copied to the heap and the carrier is freed.

Measured: **~2.2 KB and ~0.7 µs each** — 50,000 live virtual threads created in 36 ms.

- **A** — pooling virtual threads is an *anti-pattern*; create one per task.
- **B** — no OS thread involved per virtual thread.
- **D** — no syntax change at all; that's the point.

→ `quiz/03` Q1, measured in `problems/java/P10VirtualThreads.java` Part 2
</details>

---

### Q2. On JDK 21, which of these pins a virtual thread's carrier?

- **A.** Blocking inside a `synchronized` block
- **B.** Blocking on a `ReentrantLock`
- **C.** `Thread.sleep()`
- **D.** Reading from a socket

<details><summary>Answer</summary>

**A.** On JDK 21 a monitor is tied to the carrier, so the VM can't unmount — the carrier is held
for the entire blocking operation. Native/JNI frames also pin.

**B**, **C** and **D** all unmount cleanly — `ReentrantLock` is virtual-thread-aware, and `sleep` and socket
I/O are instrumented.

Measured, and this is the striking part: 500 tasks each holding **their own** lock (zero
contention) and sleeping 100 ms took **3841 ms with `synchronized` vs 112 ms with
`ReentrantLock`** — a **34× difference from pinning alone**.

Diagnose with `-Djdk.tracePinnedThreads=short`. **JEP 491 in JDK 24 made monitors unpin;** native
frames still pin.

→ `quiz/03` Q2, measured in `P10` Part 3
</details>

---

### Q3. With virtual threads, how do you protect a database that supports 50 concurrent connections?

- **A.** A fixed pool of 50 virtual threads
- **B.** Nothing — virtual threads handle it automatically
- **C.** Set the carrier pool parallelism to 50
- **D.** A `Semaphore(50)` in front of it, with unbounded virtual threads

<details><summary>Answer</summary>

**D.** This is the central mental shift: **you stop sizing the thread pool and start bounding the
resource.** Threads parked on a semaphore cost essentially nothing.

Measured: 2000 virtual threads through a `Semaphore(50)` completed in 554 ms with max concurrency
exactly 50 — 1950 of them parked for free.

- **A** reintroduces pooling (and `ThreadLocal` bleed) for no benefit.
- **C** would throttle *all* work in the JVM, not just DB access.
- **B** is the dangerous assumption — removing the thread ceiling *removes your accidental
  admission control*.

→ `quiz/03` Q3, measured in `P10` Part 5
</details>

---

### Q4. Do virtual threads make reactive programming obsolete?

- **A.** Yes — they provide identical capabilities with simpler code
- **B.** No — they remove the scalability motivation, but reactive still owns backpressure as a protocol and streaming/time operators
- **C.** No — virtual threads can't handle more than a few thousand concurrent operations
- **D.** Yes for I/O, and reactive was never useful for anything else

<details><summary>Answer</summary>

**B.** They remove the *main* reason people adopted reactive (thread-per-request scalability), and
for ordinary request/response services virtual threads plus straight-line code win on readability,
debuggability and real stack traces at equivalent throughput.

What reactive still gives you: **backpressure as a first-class protocol** (`request(n)`) and
**streaming/time operators** (window, debounce, merge infinite streams). Virtual threads have no
equivalent — blocking is coarser implicit backpressure.

So: request/response → virtual threads. Streaming with flow control → reactive, fs2, or ZIO
Streams.

→ `quiz/03` Q4
</details>

---

### Q5. 10,000 tasks each blocking for 200 ms. Roughly how long on virtual threads vs a fixed pool of 200?

- **A.** ~250 ms vs ~10 s
- **B.** ~250 ms vs ~250 ms
- **C.** ~2 s vs ~10 s
- **D.** ~10 s vs ~10 s

<details><summary>Answer</summary>

**A.** Measured: **251 ms** vs **10,381 ms** — about 41×. The fixed pool does 10000/200 = 50
batches × 200 ms. Virtual threads run all 10,000 concurrently, so the total is roughly one sleep.

This is the whole value proposition in one measurement, and it's exactly the shape of "10,000
concurrent requests each waiting on a downstream."

→ `quiz/03` Q8, measured in `P10` Part 1
</details>

---

### Q6. Same 10,000 tasks, but each burns 200 ms of CPU instead of sleeping. Now what?

- **A.** Virtual threads are still ~40× faster
- **B.** Virtual threads are slower by an order of magnitude
- **C.** Roughly the same — both are bounded by core count
- **D.** Virtual threads throw an exception for CPU-bound work

<details><summary>Answer</summary>

**C.** Measured: 68 ms (virtual) vs 48 ms (fixed pool sized to cores) — the platform pool actually
slightly *ahead*, from lower scheduling overhead.

**Virtual threads solve waiting, not computing.** The bottleneck is cores, and they add zero
parallelism. For CPU-bound work a fixed platform pool sized to cores is still correct.

→ `quiz/03` Q4/Q8, measured in `P10` Part 4
</details>

---

### Q7. What problem does structured concurrency solve?

- **A.** It makes virtual threads faster to create
- **B.** It guarantees subtasks complete before the enclosing block exits, giving cancellation propagation and no leaked tasks
- **C.** It provides a lock-free alternative to `synchronized`
- **D.** It automatically parallelises sequential code

<details><summary>Answer</summary>

**B.** Concurrency follows the lexical block the way `try`/`finally` does for resources. That fixes
leaked tasks that outlive a request, missing cancellation propagation (one leg fails, four keep
burning capacity for a discarded response), and unattributable stack traces.

`ShutdownOnFailure` = all must succeed, interrupting siblings on first error.
`ShutdownOnSuccess` = first wins (hedged requests).

Say it's **preview in JDK 21** (`--enable-preview`, JEP 453) and the API was reshaped later —
don't quote 21's signatures as final.

→ `quiz/03` Q6
</details>

---

### Q8. What replaces `ThreadLocal` for virtual threads, and why?

- **A.** `AtomicReference`, because it's thread-safe
- **B.** Nothing — `ThreadLocal` works identically
- **C.** `InheritableThreadLocal`, which was designed for this
- **D.** `ScopedValue` — immutable, lexically scoped, inherited by structure rather than copying

<details><summary>Answer</summary>

**D.** `ThreadLocal` is mutable (untraceable provenance), unbounded in lifetime (you must remember
`remove()`), and expensive at scale — a million `ThreadLocalMap`s is real footprint. `ScopedValue`
(JEP 446, **preview in 21**) binds for a block and unbinds automatically.

- **C** is worse: inheritable thread-locals are **copied** per child thread.
- **B** is half-right and half-dangerous. Virtual threads make `ThreadLocal` *safer* (no reuse, so
  no bleed — verified: `before` was always `null`), but they break `ThreadLocal`-**as-a-cache**,
  because a per-thread `SimpleDateFormat` or buffer now allocates per request.

→ `quiz/03` Q7, measured in `problems/java/P08ThreadLocalLeak.java` Part D
</details>

---

### Q9. You enable `spring.threads.virtual.enabled=true`. What becomes the real concurrency limit?

- **A.** The carrier pool parallelism
- **B.** Nothing; the service is now unbounded and faster
- **C.** The connection pool — requests now queue at HikariCP instead of at the thread pool
- **D.** The JVM's maximum thread count

<details><summary>Answer</summary>

**C.** Removing the 200-thread ceiling doesn't make anything faster by itself — it moves the
queue. 10,000 requests now contend for your 20 connections, so you must bound explicitly and tune
`connectionTimeout`, or you've converted thread starvation into connection-timeout failures.

And the subtler point: **you've removed your accidental admission control.** The old pool was
implicitly shedding load. Now you accept everything and can overload downstreams — so add explicit
rate limits, semaphores and bulkheads.

→ `quiz/03` Q9
</details>

---

### Q10. Which tool shows virtual threads in a dump?

- **A.** `jcmd <pid> Thread.dump_to_file -format=json <file>`
- **B.** `jcmd <pid> Thread.print`
- **C.** `jstack <pid>`
- **D.** `jmap -histo <pid>`

<details><summary>Answer</summary>

**A.** Only the JSON thread dump includes virtual threads and their structured-concurrency nesting.
`jstack` and `Thread.print` show platform threads only.

This is part of a broader operational shift worth stating: **thread-count metrics stop being a
useful health signal.** In the old model "threads climbing" was a reliable alarm; with virtual
threads the count stays flat and small no matter how bad things get. The equivalent signals become
queue depth, in-flight request count, and pinned-thread events.

→ `quiz/03` Q5, Q9; `quiz/07` Q10
</details>

---

## Score yourself

- **8+** — good.
- Q2 (pinning, and the 34× number), Q3 (semaphore not pool) and Q4 (the reactive question) are the
  three that separate "read a blog post" from "thought about it."

Being able to give the **JDK 21 vs 24 pinning history** is a cheap credibility signal — use it.
