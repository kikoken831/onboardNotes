/*
 * S01 — The signature Scala production incident: blocking the global ExecutionContext.
 *
 * RUN:  scala problems/scala/S01BlockingGlobalEC.scala
 *       (first run compiles, so it takes ~20s)
 *
 * TASK
 *   1. Predict the timings for PART 1, 2 and 3 before running.
 *   2. PART 4 attempts a real deadlock. Explain the mechanism precisely.
 *   3. Why does `blocking { }` help on the global EC but NOT on a fixed pool?
 *   4. Write down the production rule this file exists to teach.
 */

import scala.concurrent.{Await, ExecutionContext, Future, blocking}
import scala.concurrent.duration._
import java.util.concurrent.{Executors, ThreadPoolExecutor, ArrayBlockingQueue, TimeUnit, ThreadFactory}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

object S01BlockingGlobalEC {

  val cores: Int = Runtime.getRuntime.availableProcessors()

  /** Pretends to be a JDBC call: blocks a thread for `ms`. */
  def blockingDbCall(ms: Long): String = {
    Thread.sleep(ms)
    "row"
  }

  def time[T](label: String)(body: => T): T = {
    val t0 = System.nanoTime()
    val r = body
    val ms = (System.nanoTime() - t0) / 1000000
    println(f"  $label%-46s $ms%6d ms")
    r
  }

  def main(args: Array[String]): Unit = {
    println(s"cores = $cores")
    println(s"global EC parallelism = $cores (it is a ForkJoinPool sized to your CPUs)")
    val n = 64
    val blockMs = 200L
    println(s"\nWorkload: $n concurrent 'JDBC' calls, each blocking for ${blockMs}ms")
    println(s"Ideal with true concurrency: ${blockMs}ms\n")

    part1_globalEcNaive(n, blockMs)
    part2_globalEcWithBlockingHint(n, blockMs)
    part3_dedicatedBoundedEc(n, blockMs)
    part4_deadlock()
    part5_theRule()
  }

  // ==================================================================
  def part1_globalEcNaive(n: Int, blockMs: Long): Unit = {
    println("=== PART 1: Future { blockingDbCall } on the global EC (WRONG) ===")
    import ExecutionContext.Implicits.global

    time("global EC, no blocking hint") {
      val futures = (1 to n).map(_ => Future { blockingDbCall(blockMs) })
      Await.result(Future.sequence(futures), 5.minutes)
    }
    println(s"  --> ~$n/$cores batches. The pool has only $cores threads, and every one")
    println("      of them is asleep. Nothing else in the application can make progress.")
  }

  // ==================================================================
  def part2_globalEcWithBlockingHint(n: Int, blockMs: Long): Unit = {
    println("\n=== PART 2: same, but wrapped in scala.concurrent.blocking { } ===")
    import ExecutionContext.Implicits.global

    time("global EC + blocking { }") {
      val futures = (1 to n).map(_ => Future { blocking { blockingDbCall(blockMs) } })
      Await.result(Future.sequence(futures), 5.minutes)
    }
    println("  --> Much faster. `blocking` wraps the body in a ForkJoinPool.ManagedBlocker,")
    println("      so the pool SPAWNS COMPENSATION THREADS to keep its target parallelism.")
    println("      But note: it grew the pool to do it. That is a mitigation, not a design --")
    println("      you have converted thread starvation into unbounded thread growth")
    println("      (up to scala.concurrent.context.maxThreads, default 256).")
  }

  // ==================================================================
  def part3_dedicatedBoundedEc(n: Int, blockMs: Long): Unit = {
    println("\n=== PART 3: a dedicated, BOUNDED blocking EC (RIGHT) ===")

    val threadCount = 32
    val namedFactory = new ThreadFactory {
      private val counter = new AtomicInteger(0)
      override def newThread(r: Runnable): Thread = {
        val t = new Thread(r, s"jdbc-${counter.incrementAndGet()}")
        t.setDaemon(true)
        t
      }
    }
    val pool = new ThreadPoolExecutor(
      threadCount, threadCount,
      60L, TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](1000),        // BOUNDED: real backpressure
      namedFactory,
      new ThreadPoolExecutor.AbortPolicy            // shed load loudly, do not accumulate
    )
    // Implicit in this scope only -- note we do NOT import the global EC here.
    implicit val jdbcEc: ExecutionContext = ExecutionContext.fromExecutorService(pool)

    try {
      time(s"dedicated bounded EC ($threadCount threads)") {
        val futures = (1 to n).map(_ => Future { blockingDbCall(blockMs) })
        Await.result(Future.sequence(futures), 5.minutes)
      }
      println(s"  --> $n tasks over $threadCount threads = 2 batches. Predictable.")
      println("      Crucially, the COMPUTE EC was never touched: the rest of the app")
      println("      kept running at full speed the whole time.")
      println("      Size threadCount to the DOWNSTREAM's capacity (e.g. the JDBC pool),")
      println("      not to your own optimism.")
    } finally {
      pool.shutdown()
    }
  }

  // ==================================================================
  def part4_deadlock(): Unit = {
    println("\n=== PART 4: an actual deadlock on a bounded EC ===")

    // A pool with exactly 2 threads makes the deadlock deterministic.
    val tiny = Executors.newFixedThreadPool(2, (r: Runnable) => {
      val t = new Thread(r, "tiny-pool"); t.setDaemon(true); t
    })
    implicit val tinyEc: ExecutionContext = ExecutionContext.fromExecutorService(tiny)

    // Each outer task blocks waiting for an inner task that needs a thread
    // from the SAME pool. Both threads are consumed by outer tasks, so no
    // inner task can ever be scheduled.
    val outer = (1 to 2).map { i =>
      Future {
        val inner = Future { "inner result" }        // needs a thread from `tiny`
        Await.result(inner, 3.seconds)               // holds a thread while waiting
      }
    }

    val result = scala.util.Try(Await.result(Future.sequence(outer), 6.seconds))
    result match {
      case scala.util.Success(v) =>
        println(s"  completed: $v  (did not deadlock -- did you resize the pool?)")
      case scala.util.Failure(e) =>
        println(s"  >>> DEADLOCK: ${e.getClass.getSimpleName}")
        println("      2 threads, 2 outer tasks, each holding a thread while Awaiting an")
        println("      inner task that requires a thread. Classic thread-pool starvation")
        println("      deadlock. Note that `blocking { }` would NOT save you here: a")
        println("      FixedThreadPool cannot create compensation threads. The hint only")
        println("      works on a ForkJoinPool-backed EC.")
    }
    tiny.shutdownNow()
  }

  // ==================================================================
  def part5_theRule(): Unit = {
    println("""
=== THE RULE ===

  TWO POOLS. ALWAYS.

    1. COMPUTE  -- sized to cores. CPU work, Future combinators, pure logic.
                   NEVER blocks. This is what the global EC is for.
    2. BLOCKING -- bounded, sized to the downstream resource. JDBC, file I/O,
                   legacy Java libraries, anything that parks a thread.

  Corollaries:
    - `import scala.concurrent.ExecutionContext.Implicits.global` belongs in
      tests and the REPL. In production code it is a latent incident.
    - Pass the EC explicitly at the call site, or take it as a constructor
      parameter. Do not let an implicit be resolved by accident.
    - No `Await` anywhere except `main` and tests.
    - On Cats Effect / ZIO this is built in: IO.blocking / ZIO.blocking shift
      to the runtime's blocking pool for you, and the type tells the reader
      which kind of effect it is. That is a genuine advantage over bare Future.
    - Ban the wildcard import with a Scalafix/WartRemover rule so the compiler
      enforces this rather than the code reviewer.""")
  }
}
