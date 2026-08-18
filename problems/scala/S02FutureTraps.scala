/*
 * S02 — Future composition traps: accidental sequencing, unbounded fan-out,
 *       captured mutable state, and the retry that cannot exist.
 *
 * RUN:  scala problems/scala/S02FutureTraps.scala
 *
 * TASK
 *   1. PART 1: predict the two timings. Which is 3x slower and WHY?
 *      This is the single most common Scala Future bug.
 *   2. PART 2: predict `counter` and `builder.size`. Run it 3 times.
 *   3. PART 3: what breaks when you traverse 5000 items against a service
 *      that tolerates 20 concurrent calls? Implement the throttled version.
 *   4. PART 4: explain why `retry(future)` is impossible and `retry(() => future)`
 *      is necessary. Connect that to eager vs lazy evaluation.
 *   5. PART 5: why does the failure of one future in `Future.sequence` not stop
 *      the others? What does that cost you?
 */

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

object S02FutureTraps {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def time[T](label: String)(body: => T): T = {
    val t0 = System.nanoTime()
    val r = body
    println(f"  $label%-44s ${(System.nanoTime() - t0) / 1000000}%6d ms")
    r
  }

  def service(name: String, ms: Long): Future[Int] = Future {
    Thread.sleep(ms)
    name.length
  }

  def main(args: Array[String]): Unit = {
    part1_accidentalSequencing()
    part2_capturedMutableState()
    part3_unboundedFanOut()
    part4_retryNeedsLaziness()
    part5_failFastButNotCancel()
    part6_summary()
  }

  // ==================================================================
  def part1_accidentalSequencing(): Unit = {
    println("=== PART 1: for-comprehension over futures ===")

    // SEQUENTIAL: each service call is CONSTRUCTED inside the previous flatMap,
    // and Future is EAGER, so construction == starting.
    time("for { a <- svcA(); b <- svcB(); c <- svcC() }") {
      val f = for {
        a <- service("alpha", 300)
        b <- service("beta", 300)
        c <- service("gamma", 300)
      } yield a + b + c
      Await.result(f, 30.seconds)
    }

    // PARALLEL: construct all three FIRST, then compose.
    time("val fa,fb,fc = ...; for { a <- fa; ... }") {
      val fa = service("alpha", 300)
      val fb = service("beta", 300)
      val fc = service("gamma", 300)
      val f = for { a <- fa; b <- fb; c <- fc } yield a + b + c
      Await.result(f, 30.seconds)
    }

    // Same thing, more idiomatically.
    time("Future.sequence(List(fa, fb, fc))") {
      val fs = List(service("alpha", 300), service("beta", 300), service("gamma", 300))
      Await.result(Future.sequence(fs).map(_.sum), 30.seconds)
    }

    println("""
  The ONLY difference is where you put the `val`. There is no type-level
  signal, no compiler warning, and the code looks identical at a glance.
  A code review will not catch it. This is what laziness buys you:

    Cats Effect:  (ioA, ioB, ioC).parTupled     <- parallelism is EXPLICIT
                  for { a <- ioA; b <- ioB }    <- ALWAYS sequential, always obvious
    ZIO:          ZIO.collectAllPar(...)  vs  ZIO.collectAll(...)""")
  }

  // ==================================================================
  def part2_capturedMutableState(): Unit = {
    println("\n=== PART 2: mutable state captured by Future bodies ===")

    var counter = 0                                  // plain var: a data race
    val builder = mutable.ListBuffer[Int]()          // mutable collection: also a race
    val safeCounter = new AtomicLong()

    val n = 1000
    val futures = (1 to n).map { i =>
      Future {
        counter += 1                                 // BUG: non-atomic read-modify-write
        builder += i                                 // BUG: ListBuffer is not thread-safe
        safeCounter.incrementAndGet()                // correct
      }
    }
    Await.result(Future.sequence(futures), 30.seconds)

    println(s"  expected            = $n")
    println(s"  var counter         = $counter" + (if (counter != n) s"   LOST ${n - counter}" else "   (got lucky)"))
    println(s"  ListBuffer.size     = ${builder.size}" + (if (builder.size != n) s"   LOST ${n - builder.size}" else "   (got lucky)"))
    println(s"  AtomicLong          = ${safeCounter.get()}   (always correct)")
    println("""
  `var` in Scala is a plain mutable JVM field. Scala's syntax is prettier;
  the Java Memory Model is unchanged. Note that `val builder` does NOT help --
  the reference is immutable, the ListBuffer it points at is not.

  The idiomatic fix is not a lock. It is to not share mutable state:
      val results = Await.result(Future.sequence(futures), timeout)   // fold the results
  Or Ref[IO, Int] / ZIO Ref if you genuinely need shared state.""")
  }

  // ==================================================================
  def part3_unboundedFanOut(): Unit = {
    println("\n=== PART 3: unbounded fan-out vs throttled ===")

    val inFlight = new AtomicInteger()
    val maxInFlight = new AtomicInteger()

    // NOTE: this call is genuinely ASYNC -- it does not block an EC thread.
    // That matters: if we used Thread.sleep, the EC's thread count would cap
    // concurrency for us by accident, and we would not see the real fan-out.
    // A real non-blocking HTTP client behaves like this.
    val scheduler = java.util.concurrent.Executors.newScheduledThreadPool(2, (r: Runnable) => {
      val t = new Thread(r, "scheduler"); t.setDaemon(true); t
    })

    def trackedCall(i: Int): Future[Int] = {
      val now = inFlight.incrementAndGet()
      maxInFlight.accumulateAndGet(now, math.max)
      val p = Promise[Int]()
      scheduler.schedule(new Runnable {
        def run(): Unit = { inFlight.decrementAndGet(); p.success(i) }
      }, 20, java.util.concurrent.TimeUnit.MILLISECONDS)
      p.future
    }

    val items = (1 to 500).toList

    maxInFlight.set(0)
    time("Future.traverse (unbounded)") {
      Await.result(Future.traverse(items)(trackedCall), 60.seconds)
    }
    println(s"  peak concurrent calls = ${maxInFlight.get()} of 500  <-- ALL of them at once")

    maxInFlight.set(0)
    time("throttled, 20 at a time") {
      Await.result(traverseN(items, 20)(trackedCall), 60.seconds)
    }
    println(s"  peak concurrent calls = ${maxInFlight.get()} of 500  <-- bounded on purpose")

    scheduler.shutdown()

    println("""
  With 5000 items against a service that tolerates 20 concurrent calls,
  `Future.traverse` is a self-inflicted denial of service. Note that the
  throttled version is SLOWER in wall-clock time -- that is the trade you
  are making on purpose, and the downstream stays alive.

  There is no built-in throttled traverse for scala.concurrent.Future.
  That is a real gap in the API:

    Cats Effect:   items.parTraverseN(20)(call)
    ZIO:           ZIO.foreachPar(items)(call).withParallelism(20)
    Pekko Streams: Source(items).mapAsync(20)(call).runWith(Sink.seq)""")
  }

  /** Bounded-concurrency traverse: process `n` items at a time. EXERCISE: understand this. */
  def traverseN[A, B](items: List[A], n: Int)(f: A => Future[B])(
      implicit ec: ExecutionContext): Future[List[B]] =
    items.grouped(n).foldLeft(Future.successful(List.empty[B])) { (acc, batch) =>
      // acc.flatMap means the NEXT batch is not constructed (= not started)
      // until the previous batch completes. That is the throttle.
      acc.flatMap(done => Future.traverse(batch)(f).map(done ++ _))
    }

  // ==================================================================
  def part4_retryNeedsLaziness(): Unit = {
    println("\n=== PART 4: you cannot retry a Future ===")

    val attempts = new AtomicInteger()
    def flakyCall(): Future[String] = Future {
      val n = attempts.incrementAndGet()
      if (n < 3) throw new RuntimeException(s"attempt $n failed")
      s"succeeded on attempt $n"
    }

    // WRONG: retrying a VALUE. The Future is already completed (and failed).
    attempts.set(0)
    val alreadyRunning = flakyCall()
    val wrong = retryValue(alreadyRunning, 5)
    println(s"  retryValue(future, 5)  = ${Try(Await.result(wrong, 5.seconds))}")
    println(s"    attempts made        = ${attempts.get()}  <-- only 1. Retrying a completed")
    println("      Future just re-observes the same failed result, forever.")

    // RIGHT: retrying a THUNK that can produce a new Future each time.
    attempts.set(0)
    val right = retryThunk(() => flakyCall(), 5)
    println(s"\n  retryThunk(() => future, 5) = ${Await.result(right, 5.seconds)}")
    println(s"    attempts made             = ${attempts.get()}  <-- it actually retried")

    println("""
  This is the eager/lazy distinction with teeth. `Future` is a RUNNING
  computation, so it cannot be re-run. `IO`/`ZIO` are DESCRIPTIONS of a
  computation, so `io.retry(policy)` is well-defined and built in.

  It is also why Future breaks referential transparency:
      val f = Future(sideEffect); (f, f)      // one effect
      (Future(sideEffect), Future(sideEffect)) // two effects
  You cannot substitute a value for its definition, which means you cannot
  refactor by equational reasoning.""")
  }

  def retryValue[T](f: Future[T], n: Int)(implicit ec: ExecutionContext): Future[T] =
    if (n <= 0) f else f.recoverWith { case _ => retryValue(f, n - 1) }   // futile

  def retryThunk[T](mk: () => Future[T], n: Int)(implicit ec: ExecutionContext): Future[T] =
    if (n <= 0) mk() else mk().recoverWith { case _ => retryThunk(mk, n - 1) }

  // ==================================================================
  def part5_failFastButNotCancel(): Unit = {
    println("\n=== PART 5: sequence fails fast but does not cancel ===")

    val completed = new AtomicInteger()

    def slowOk(ms: Long): Future[String] = Future {
      Thread.sleep(ms)
      completed.incrementAndGet()
      "ok"
    }
    def fastFail(): Future[String] = Future {
      Thread.sleep(50)
      throw new RuntimeException("I fail early")
    }

    val fs = List(slowOk(1000), fastFail(), slowOk(1000), slowOk(1000))
    val t0 = System.nanoTime()
    val outcome = Try(Await.result(Future.sequence(fs), 30.seconds))
    val ms = (System.nanoTime() - t0) / 1000000

    println(s"  Future.sequence returned after ${ms}ms: ${outcome.failed.get.getMessage}")
    println(s"  slow tasks completed so far: ${completed.get()} of 3")
    Thread.sleep(1200)
    println(s"  after waiting 1.2s more:     ${completed.get()} of 3  <-- they ALL ran to completion")

    println("""
  You gave up after 50ms; the three 1-second tasks kept running, holding
  connections and burning capacity for a result nobody will read.
  scala.concurrent.Future has NO cancellation API at all. This is its
  single biggest limitation, and it is not fixable by a wrapper.

  If you need "collect all results including failures":
      Future.sequence(fs.map(_.transform(Success(_))))   // Future[List[Try[T]]]

  If you need real cancellation: Cats Effect / ZIO fibers, where
  `race`, `timeout`, and `cancel` actually stop the work and run finalizers.""")
  }

  // ==================================================================
  def part6_summary(): Unit = {
    println("""
=== SUMMARY: the four Future traps ===

  1. ACCIDENTAL SEQUENCING -- construct futures before composing them,
     or use Future.sequence/traverse. `for` over futures constructed
     inline is sequential.
  2. SHARED MUTABLE STATE -- `var` and mutable collections captured by
     Future bodies are races. Fold results instead.
  3. UNBOUNDED FAN-OUT -- traverse/sequence start everything at once.
     Batch, or use parTraverseN / mapAsync(n).
  4. NO RETRY, NO CANCELLATION -- because Future is eager. Pass thunks
     for retry; accept that timeouts leak work.

  All four are consequences of ONE design decision: Future is eager.
  Every one of them is either impossible or explicit in Cats Effect / ZIO.
  That is the strongest technical argument for adopting an effect type --
  and the reason to ask your team which way they went.""")
  }
}
