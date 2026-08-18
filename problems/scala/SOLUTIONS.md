# Solutions — Scala problems

Verified on Scala 2.13.15 / JDK 21, 14 cores.

---

## S01 — Blocking the global `ExecutionContext`

**Verified output** (64 concurrent 200 ms "JDBC" calls; ideal = 200 ms):

```
PART 1  global EC, no blocking hint            1038 ms
PART 2  global EC + blocking { }                218 ms
PART 3  dedicated bounded EC (32 threads)       421 ms
PART 4  >>> DEADLOCK: TimeoutException
```

**Part 1 — why 1038 ms.** `ExecutionContext.global` is a `ForkJoinPool` with parallelism =
`availableProcessors()` = 14. 64 blocking tasks over 14 threads ≈ 5 batches × 200 ms. The
real damage isn't the latency, it's that **all 14 threads are asleep**, so *nothing else in
the application can make progress* — including the callbacks that would complete these very
futures.

**Part 2 — why `blocking { }` helps.** It wraps the body in a
`ForkJoinPool.ManagedBlocker`, which tells the pool "this thread is about to park", so the
pool **spawns compensation threads** to maintain its target parallelism. 218 ms ≈ full
concurrency.

But note what it did: **it grew the pool**. You converted thread starvation into
(nearly) unbounded thread growth — up to `scala.concurrent.context.maxThreads`, default
**256**. It's a mitigation, not a design, and it does nothing about bounding concurrent
access to the downstream.

**Why it does NOT help on a fixed pool:** a `ThreadPoolExecutor` has no compensation
mechanism — it can't create threads beyond its maximum. `ManagedBlocker` is a
`ForkJoinPool` feature. So `blocking { }` on a fixed-pool-backed EC is a **no-op**, and you
starve exactly as before. This is the single most misunderstood thing about `blocking`.

**Part 3 — the right answer.** A dedicated pool: bounded queue (`ArrayBlockingQueue`),
`AbortPolicy` so overload sheds loudly instead of accumulating, named daemon threads so
thread dumps are readable, and **sized to the downstream's capacity** (the JDBC pool), not
to your optimism. 421 ms = 2 batches over 32 threads — slower than Part 2, and *predictable*,
which is the point. Critically, **the compute EC was never touched**.

**Part 4 — the deadlock mechanism.** A 2-thread fixed pool; 2 outer tasks each occupy a
thread and then `Await` an inner `Future` that *also* needs a thread from that pool. No
thread can ever run an inner task. Permanent, and it presents as "the service hangs at 5%
CPU" with no error. `blocking { }` cannot save you here (fixed pool). This is why
**`Await` inside a pool thread is banned**, not merely discouraged.

**The rule:** two pools — compute (cores-sized, never blocks) and blocking (bounded, sized
to the resource). No `ExecutionContext.Implicits.global` in production code; enforce it with
a Scalafix/WartRemover rule rather than code review. No `Await` outside `main` and tests.

---

## S02 — `Future` composition traps

### Part 1 — accidental sequencing (verified)
```
for { a <- svcA(); b <- svcB(); c <- svcC() }    920 ms     <- SEQUENTIAL
val fa,fb,fc = ...; for { a <- fa; ... }        311 ms     <- parallel
Future.sequence(List(fa, fb, fc))               310 ms     <- parallel
```

The `for` desugars to nested `flatMap`s, so `svcB()` isn't **constructed** until `a`
arrives — and because `Future` is **eager**, construction *is* starting. Three 300 ms calls
become 900 ms.

**The only difference between 920 ms and 311 ms is where you put a `val`.** No type
signal, no compiler warning, visually near-identical. A code review will not catch it.

That's precisely what laziness fixes: in Cats Effect, `(ioA, ioB, ioC).parTupled` makes
parallelism **explicit in the API**, and `for { a <- ioA; b <- ioB }` is *always*
sequential and *always* obviously so.

### Part 2 — captured mutable state (verified)
```
expected        = 1000
var counter     =  987   LOST 13
ListBuffer.size =  995   LOST 5
AtomicLong      = 1000   (always correct)
```
`var` in Scala is a plain mutable JVM field; `+=` is a non-atomic read-modify-write. The
JMM is unchanged by prettier syntax. **`val builder` does not help** — the reference is
immutable, the `ListBuffer` it points at is not.

The idiomatic fix isn't a lock, it's not sharing: collect the results
(`Future.sequence(futures)`) and fold them. Or `Ref[IO, Int]` / ZIO `Ref` if the state is
genuinely shared.

### Part 3 — unbounded fan-out (verified)
```
Future.traverse (unbounded)     33 ms   peak concurrent = 500 of 500
throttled, 20 at a time        689 ms   peak concurrent =  20 of 500
```
Note the calls here are **genuinely async** (a `Promise` completed by a scheduler). That
matters: with `Thread.sleep` the EC's thread count caps concurrency *by accident* and you'd
wrongly conclude `traverse` is safe. A real non-blocking HTTP client behaves like the
async version — so `traverse` over 5000 items really does launch 5000 concurrent requests.

The throttled version is **20× slower in wall-clock**. That's the trade you're making on
purpose, and the downstream survives.

```scala
def traverseN[A, B](items: List[A], n: Int)(f: A => Future[B])(
    implicit ec: ExecutionContext): Future[List[B]] =
  items.grouped(n).foldLeft(Future.successful(List.empty[B])) { (acc, batch) =>
    acc.flatMap(done => Future.traverse(batch)(f).map(done ++ _))
  }
```
The throttle *is* the `acc.flatMap` — the next batch isn't constructed (= started) until
the previous completes. Same mechanism as the Part 1 bug, used deliberately.

There is **no built-in throttled traverse** for `scala.concurrent.Future`. Compare
`parTraverseN(20)`, `ZIO.foreachPar(...).withParallelism(20)`, `mapAsync(20)`.

### Part 4 — you cannot retry a `Future` (verified)
```
retryValue(future, 5)       = Failure(attempt 1 failed)   attempts made = 1
retryThunk(() => future, 5) = succeeded on attempt 3      attempts made = 3
```
A `Future` is a **running (then completed) computation**, so `recoverWith` on the *same*
value just re-observes the same failure. Retry requires a `() => Future[A]` **thunk** that
can produce a *new* computation.

Same root cause as the referential-transparency break:
```scala
val f = Future(sideEffect); (f, f)          // ONE effect
(Future(sideEffect), Future(sideEffect))    // TWO effects
```
You cannot substitute a value for its definition, so you cannot refactor by equational
reasoning. `IO`/`ZIO` are *descriptions*, so `io.retry(policy)` is well-defined and built in.

### Part 5 — fail-fast without cancellation (verified)
`Future.sequence` returned at ~50 ms with the failure; the three 1-second tasks **all ran
to completion** afterwards. You gave up; they kept holding connections and burning capacity
for a result nobody will read.

`scala.concurrent.Future` has **no cancellation API at all** — not fixable by a wrapper.
To collect all outcomes instead of failing fast:
```scala
Future.sequence(fs.map(_.transform(Success(_))))   // Future[List[Try[T]]]
```

**All four traps are consequences of one design decision: `Future` is eager.** That's the
strongest technical argument for an effect type, and the reason to ask which the team chose.

---

## S03 — Allocation pressure

### Part 1 — six ways to do the same work (verified, 2M elements)
```
List: map.filter.sum                178.0 MB   280 ms
Vector: map.filter.sum               91.9 MB   178 ms
Array: map.filter.sum               130.1 MB    94 ms
Array.view: map.filter.sum (fused)   71.2 MB    43 ms
iterator: map.filter.sum             71.2 MB    56 ms
while loop over Array                 0.0 MB     3 ms
```
`List` is worst on both axes: a cons cell **and** a boxed `Integer` per element, for
*every* intermediate collection. `.view`/`.iterator` fuse the stages into one pass, so
there are no intermediates. The `while` loop over a primitive array allocates **nothing**
and is ~90× faster than the `List` version.

### Part 2 — boxing (verified, 1M elements)
```
Array[Int]         3.8 MB      <- primitive, contiguous
Array[Integer]    19.1 MB      <- boxed: 5x
List[Int]         38.1 MB      <- 10x
Vector[Int]       19.7 MB
Set[Int]         131.4 MB
Map[Int,Int]     245.7 MB      <- 65x the primitive array
```
Hand computation, which matches: a cons cell is 16 bytes of header + 2 references (4 bytes
each, compressed oops) = **24 bytes**, plus a **16-byte** boxed `Integer` = 40 bytes/element
≈ 38 MB. `Integer.valueOf` caches only −128..127, so boxing is **not** amortised for real
data.

And the cells are **scattered across the heap**, so traversal is pointer-chasing with poor
cache locality — often a bigger cost than the allocation itself. Use `ArraySeq.ofInt` for
an immutable, unboxed, contiguous sequence.

### Part 3 — where `.view` helps, and where it doesn't (verified)
```
3 chained stages, strict                   173.4 MB
3 chained stages, view                      91.6 MB    <- fused, one pass
view but forced to a List at the end        70.2 MB
single stage, strict (view adds nothing)     68.7 MB
sorted (view cannot fuse this)              76.3 MB
```
`.view` fuses **elementwise** stages (map/filter/take/flatMap). It does **not** help with a
single stage, when you force to a strict collection anyway, for whole-collection operations
(`sorted`, `groupBy`, `reverse`), or on tiny collections where the view machinery costs more
than it saves.

**Correctness caveat:** a view over a *mutable* source is lazy, so it observes later
mutations. `.iterator` has the same trap. That's a genuine bug source, not just a perf note.

### Part 4 — eager string interpolation
`s"..."` is **eager**: the `StringBuilder` runs, values box for the interpolator, and the
`String` is built — *then discarded* because the log level is off. Java's SLF4J
`log.debug("order {} user {}", id, user)` defers formatting, so idiomatic Java **didn't**
have this problem and the direct Scala port does.

**This is usually the top hit in a real Scala service's allocation profile, and it's a
one-line fix.** Look for it first. Use `scala-logging`, whose macros rewrite
`logger.debug(s"...")` into `if (logger.isDebugEnabled) ...`, so it's zero-cost when off.

### Part 5 — tuples, `Option`s, `copy`
A `Tuple2` of two `Int`s ≈ 24 bytes + two 16-byte `Integer`s = **~56 bytes to carry 8 bytes
of data**. `Some(x)` is 16 bytes (+ boxing); `None` is a singleton and free. `groupBy`
allocates tuples, boxed keys, *and* maps. `copy` allocates a whole new object.

Don't assume escape analysis rescues you: it only applies in **C2-compiled** code, needs
inlining to succeed, is all-or-nothing per allocation site, and won't scalar-replace arrays
above `EliminateAllocationArraySizeLimit` (64). Measure.

### The lesson
This file makes idiomatic Scala look expensive. **Resist over-correcting.**

1. Nearly all of this garbage **dies in eden** and a copying young collector never touches
   it. Allocation is ~10 ns. The cost is **GC frequency**, not per-object overhead.
2. **Profile, then fix the top 3–5 sites, then re-measure.** async-profiler `-e alloc`.
   Don't de-idiomatise a codebase on principle — you'll make it slower to change and
   probably not faster to run.
3. The wins are boringly repetitive: eager interpolation in log statements; a
   `map`/`filter` chain over a large collection in a request path; boxing in a numeric fold;
   `List` where `Vector`/`Array` belonged; `xs :+ x` in a loop (accidentally quadratic).
4. Code that runs **once per request** doesn't matter. Code in a loop over 10⁶ elements does.

**The counterpoint to have ready:** Scala's immutability-by-default removes an entire class
of concurrency bug. Trading a few percent of CPU for that is usually a good deal — but you
should know the number before defending it.

---

## Exercises left for you

These aren't in the runnable files; work them on paper.

1. **Rewrite S01 Part 3 using Cats Effect**, assuming `IO`:
   ```scala
   def findUser(id: Long): IO[User] = IO.blocking(repo.findById(id))
   // then bound it:
   sem.permit.use(_ => findUser(id))          // Semaphore[IO]
   ids.parTraverseN(20)(findUser)             // bounded fan-out, one argument
   ```
   Note what disappeared: no `ExecutionContext` to pass, no `blocking { }` to remember
   (it's in the type), and bounded concurrency is a parameter.

2. **Fix the Akka actor bug** from quiz 06 B2 — capture `sender()` into a `val`, and use
   `pipeTo self` so state mutation happens only in `receive`.

3. **Write the `traverseN` above using a `Semaphore`** instead of batching. Which is
   better, and why? (Hint: batching makes every item in a batch wait for the slowest;
   a semaphore keeps the pipeline full. The semaphore version is strictly better and is
   what `parTraverseN` does.)

4. **Take one Java problem and port it.** `P09`'s `ExpiringCache` in Scala with
   `Ref[IO, Map[K, Deferred[IO, V]]]` is an excellent exercise — and shows why
   `Deferred` exists.
