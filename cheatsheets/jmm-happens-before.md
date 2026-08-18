# Cheatsheet — Java Memory Model & happens-before

One page. Skim the morning of.

---

## Happens-before edges

If **A happens-before B**, then A's memory effects are visible to B. Everything in the JMM
reduces to finding a chain of these. **It's transitive** — that's what you exploit.

| Edge | Detail |
|---|---|
| **Program order** | within a single thread |
| **Monitor** | unlock of M → every subsequent lock of **the same** M |
| **Volatile** | write to V → every subsequent read of **the same** V |
| **Thread start** | `t.start()` → everything in `t` |
| **Thread join** | everything in `t` → `t.join()` returning |
| **Final field** | freeze at end of constructor → any read of the correctly-constructed object |
| **Interrupt** | `t.interrupt()` → `t` detecting it |
| **j.u.c.** | `BlockingQueue.put` → the `take` that gets it; `countDown()` → `await()` returns; `AtomicX` set → get; `Future.get()` sees everything the task did |
| **Class init** | static initialiser → any thread's first use of the class |

**The rule the JMM actually gives you:** *SC-DRF* — if your program is **data-race-free**,
it behaves as if sequentially consistent. If it has a data race, **all bets are off** and
reasoning like "one thread must run second" is invalid (quiz 01 Q9).

---

## `volatile` in one line

> **Visibility + ordering + single-access atomicity. NOT compound atomicity.**

`volatile int i; i++` is still a race. Use `AtomicInteger`/`LongAdder`.

- **volatile read** = *acquire*: nothing after it can float above it.
- **volatile write** = *release*: nothing before it can sink below it. Also needs a
  `StoreLoad` fence, which is why **volatile writes cost more than reads**.
- On **x86 (TSO)** volatile reads are nearly free. On **ARM/Apple Silicon** (weakly
  ordered) real barriers are emitted both ways — so **the same bug reproduces differently
  on your laptop and the server.** Good thing to say out loud.

---

## Safe publication — the six ways

To hand an object to another thread correctly, store it in:

1. a **static initialiser**
2. a **`volatile` field** or `AtomicReference`
3. a **`final` field** of a properly-constructed object
4. a field guarded by a **lock** that readers also acquire
5. a **thread-safe collection** (`ConcurrentMap`, `BlockingQueue`, …)
6. handed via `Thread.start()`, `Future.get()`, a latch, an `Exchanger`

**Immutable** objects (all fields final, nothing mutable reachable) can be shared freely.
**Effectively immutable** objects need safe publication once.

"Properly constructed" = **`this` did not escape the constructor** (no registering
listeners, starting threads, calling overridable methods, or passing `this` out).

---

## Choosing a primitive

| Need | Use |
|---|---|
| mutual exclusion, simple | `synchronized` |
| timeout / interruptible / fair / multiple conditions / non-block-scoped | `ReentrantLock` |
| **blocking on JDK 21 virtual threads** | `ReentrantLock` (`synchronized` **pins**) |
| read-mostly, long critical sections | `ReentrantReadWriteLock` |
| read-mostly, tiny hot critical sections | `StampedLock` (optimistic; not reentrant) |
| counter, read rarely | `LongAdder` |
| counter, need exact value or CAS | `AtomicInteger`/`AtomicLong` |
| single value swap / lock-free update | `AtomicReference` + CAS loop |
| "wait for N things" (one-shot) | `CountDownLatch` |
| "N threads rendezvous repeatedly" | `CyclicBarrier` |
| dynamic party count | `Phaser` |
| **limit concurrency** | `Semaphore` |
| limit **rate** | token bucket / Guava `RateLimiter` / Bucket4j |
| producer-consumer | `ArrayBlockingQueue` (bounded!) |
| many readers, rare writes, small list | `CopyOnWriteArrayList` |
| map | `ConcurrentHashMap` |
| lazily-init static | **holder idiom** |
| lazily-init instance field | `volatile` + double-checked, or a memoizing supplier |

---

## Traps, ranked by how often they're real

1. **Mutable state on a Spring singleton** — every `@Service` field is shared by all
   request threads.
2. **Unbounded queue in a thread pool** — `newFixedThreadPool` is unbounded; overload
   becomes memory growth + unbounded latency instead of backpressure.
3. **`ThreadLocal` not `remove()`d on a pooled thread** — context bleed (a security bug in
   multi-tenant) + memory leak. Weak **key**, strong **value**.
4. **check-then-act on a concurrent collection** — `containsKey` + `put` is a race; use
   `computeIfAbsent`.
5. **Blocking in a compute pool** — the FJP common pool, or Scala's global EC. Starvation,
   sometimes deadlock.
6. **`if` instead of `while` around `wait()`** — spurious wakeups + lost signals.
7. **Swallowing `InterruptedException`** — destroys the only cancellation signal.
8. **`@Async`/`@Transactional` self-invocation** — the proxy is bypassed; the annotation
   silently does nothing.
9. **Non-`Async` `CompletableFuture` stages** — run on an unpredictable thread, possibly
   the caller's.
10. **`synchronized` around blocking on JDK 21 virtual threads** — pinning (34× measured in
    P10, with zero contention).

---

## Diagnosing, by symptom

| Symptom | First look |
|---|---|
| Hang, threads `BLOCKED` | `jstack` — it reports Java-level deadlocks automatically |
| Hang, threads `WAITING`, low CPU | pool starvation: is something blocking in a compute pool? `Await`/`join` on a pool thread? |
| High p99, low CPU | queueing — connection pool, executor queue depth, downstream p99 |
| High CPU, low throughput | lock contention (async-profiler `-e lock`), false sharing, or GC |
| Wrong results, intermittent | data race — missing `volatile`/lock, or check-then-act |
| "Works in dev, fails in prod" | JIT-dependent (P02), or x86-vs-ARM ordering, or higher concurrency |
| Memory grows | `jcmd GC.class_histogram`, then post-GC live-set trend |
| Thread count grows | leaked executor, or `newCachedThreadPool` |

Take **three `jstack`s ~5 s apart** and diff them: a thread in the same frame in all three
is your problem.

```bash
jps                                   # find the pid
jcmd <pid> Thread.print               # thread dump (jstack)
jcmd <pid> Thread.dump_to_file -format=json /tmp/t.json   # includes virtual threads
jstat -gcutil <pid> 1s                # live GC watch
jcmd <pid> GC.class_histogram | head -20
jcmd <pid> VM.native_memory summary   # needs -XX:NativeMemoryTracking=summary
```

---

## Scala mapping

| Java | Scala |
|---|---|
| `synchronized` | `x.synchronized { }` — but ask whether the state should be immutable instead |
| `volatile` field | `@volatile var` |
| `AtomicInteger` | `AtomicInteger`, or `Ref[IO, Int]` / ZIO `Ref` |
| `ExecutorService` | `ExecutionContext` |
| `CompletableFuture` | `Future` (eager) / `IO`, `ZIO` (lazy) |
| `thenApply` / `thenCompose` | `map` / `flatMap` |
| `allOf` | `Future.sequence` / `parTupled` / `collectAllPar` |
| double-checked locking | `lazy val` (can deadlock on interdependent lazy vals) |
| `try`-with-resources | `Resource[IO, A]` / ZIO `Scope` — composable and cancellation-safe |
| `CountDownLatch(1)` | `Deferred[IO, A]` / ZIO `Promise` |
| `BlockingQueue` | `Queue[IO, A]` (semantically blocking) |
| `ThreadLocal` | pass it as a value; or `IOLocal` / `FiberRef` |
| `Semaphore` | `Semaphore[IO]`, or `parTraverseN(n)` |
| thread-per-request + `ThreadLocal` context | **nothing** — pass context explicitly |

**The one-sentence version:** Java makes thread-safety opt-in on shared mutable defaults;
Scala makes immutability the default and sharing something you reach for. Everything else
follows from that.
