/*
 * S03 — Scala allocation pressure: where the garbage comes from, and what it costs.
 *
 * RUN:  scala problems/scala/S03AllocationPressure.scala
 *       scala -J-Xlog:gc problems/scala/S03AllocationPressure.scala   # watch the GCs
 *
 * TASK
 *   1. Rank the six PART 1 variants by allocation before running. Check yourself.
 *   2. PART 2: explain the boxing. Compute the bytes by hand for 1M Ints in a
 *      List[Int] vs an Array[Int]. (Header 16, ref 4, Integer 16, cons cell 32.)
 *   3. PART 3: why does `.view` help, and when does it NOT help?
 *   4. PART 4: string interpolation in a log statement. Why is this the single
 *      most common real-world find, and what is the fix?
 *   5. Which of these would you actually change in production code, and why
 *      is the answer "almost none of them, until a profiler says so"?
 */

import scala.collection.mutable

object S03AllocationPressure {

  val N = 2_000_000

  /** Approximate allocated bytes for the calling thread. Uses the JMX bean. */
  private val threadMx = java.lang.management.ManagementFactory.getThreadMXBean
    .asInstanceOf[com.sun.management.ThreadMXBean]

  def allocated[T](label: String)(body: => T): T = {
    val tid = Thread.currentThread().getId
    System.gc()
    Thread.sleep(50)
    val before = threadMx.getThreadAllocatedBytes(tid)
    val t0 = System.nanoTime()
    val r = body
    val ms = (System.nanoTime() - t0) / 1000000
    val mb = (threadMx.getThreadAllocatedBytes(tid) - before) / (1024.0 * 1024.0)
    println(f"  $label%-40s $mb%8.1f MB  $ms%5d ms")
    r
  }

  def main(args: Array[String]): Unit = {
    println(s"N = $N elements. Allocation measured per-thread via ThreadMXBean.\n")
    part1_pipelineStyles()
    part2_boxing()
    part3_viewsAndIterators()
    part4_stringInterpolation()
    part5_tuplesAndOptions()
    part6_theLesson()
  }

  // ==================================================================
  def part1_pipelineStyles(): Unit = {
    println("=== PART 1: six ways to sum 2M doubled-then-filtered Ints ===")
    val data = Array.range(0, N)

    allocated("List: map.filter.sum") {
      data.toList.map(_ * 2).filter(_ % 3 == 0).sum
    }
    allocated("Vector: map.filter.sum") {
      data.toVector.map(_ * 2).filter(_ % 3 == 0).sum
    }
    allocated("Array: map.filter.sum") {
      data.map(_ * 2).filter(_ % 3 == 0).sum
    }
    allocated("Array.view: map.filter.sum (fused)") {
      data.view.map(_ * 2).filter(_ % 3 == 0).sum
    }
    allocated("iterator: map.filter.sum") {
      data.iterator.map(_ * 2).filter(_ % 3 == 0).sum
    }
    allocated("while loop over Array") {
      var i = 0; var acc = 0L
      while (i < data.length) {
        val v = data(i) * 2
        if (v % 3 == 0) acc += v
        i += 1
      }
      acc
    }
    println("""
  `List` allocates a 32-byte cons cell AND a boxed Integer per element, for
  every intermediate collection. `.view` and `.iterator` FUSE the stages into
  a single pass with no intermediate collection at all. The while loop over a
  primitive Array allocates essentially nothing.""")
  }

  // ==================================================================
  def part2_boxing(): Unit = {
    println("\n=== PART 2: boxing ===")
    val n = 1_000_000

    allocated(s"Array[Int]($n)") { val a = new Array[Int](n); a(0) = 1; a.length }
    allocated(s"Array[Integer]($n) boxed") {
      val a = new Array[java.lang.Integer](n)
      var i = 0; while (i < n) { a(i) = i; i += 1 }
      a.length
    }
    allocated(s"List[Int]($n)") { List.range(0, n).length }
    allocated(s"Vector[Int]($n)") { Vector.range(0, n).length }
    allocated(s"Set[Int]($n)") { (0 until n).toSet.size }
    allocated(s"Map[Int,Int]($n)") { (0 until n).map(i => i -> i).toMap.size }

    println(f"""
  Hand computation for $n%,d Ints (compressed oops):
    Array[Int]  = 16 header + 4*$n%,d                       = ~${(16 + 4L * n) / 1048576}%,d MB
    List[Int]   = $n%,d * (24 cons cell + 16 Integer)       = ~${(40L * n) / 1048576}%,d MB
  Compare those to the measured numbers above -- they should agree closely.
  A cons cell is 16 bytes of header + 2 references (4 bytes each) = 24 bytes;
  the boxed Integer is another 16. So ~10x the footprint of a primitive array,
  and the cells are scattered across the heap, making traversal pointer-chasing
  with poor cache locality -- often a bigger cost than the allocation itself.

  Note `Integer.valueOf` caches only -128..127, so boxing is NOT amortised for
  real data. Use ArraySeq.ofInt for an immutable, unboxed, contiguous sequence.""")
  }

  // ==================================================================
  def part3_viewsAndIterators(): Unit = {
    println("\n=== PART 3: where .view helps and where it does not ===")
    val data = Array.range(0, N)

    allocated("3 chained stages, strict") {
      data.map(_ + 1).map(_ * 2).filter(_ % 5 == 0).length
    }
    allocated("3 chained stages, view") {
      data.view.map(_ + 1).map(_ * 2).filter(_ % 5 == 0).size
    }
    allocated("view but forced to a List at the end") {
      data.view.map(_ + 1).filter(_ % 5 == 0).toList.length
    }
    allocated("single stage, strict (view adds nothing)") {
      data.map(_ + 1).length
    }
    allocated("sorted (view cannot fuse this)") {
      data.view.map(_ + 1).toArray.sorted.length
    }

    println("""
  `.view` fuses ELEMENTWISE stages (map/filter/take/flatMap) into one pass.
  It does NOT help when:
    - there is only one stage (nothing to fuse)
    - you force the result into a strict collection anyway (.toList allocates it)
    - the operation is inherently whole-collection: sorted, groupBy, reverse
    - the collection is tiny (view machinery has its own per-stage cost)
  And beware: views over a MUTABLE source are lazy, so they see later mutations.
  That is a genuine correctness trap; `.iterator` has the same caveat.""")
  }

  // ==================================================================
  def part4_stringInterpolation(): Unit = {
    println("\n=== PART 4: string interpolation in logging ===")
    val n = 200_000
    val user = "alice"
    val orderId = 12345L
    var debugEnabled = false   // pretend the log level is INFO

    // The single most common real-world allocation find.
    allocated("s\"...\" evaluated eagerly (log OFF)") {
      var i = 0; var sink = 0
      while (i < n) {
        val msg = s"processing order $orderId for user $user attempt $i"
        if (debugEnabled) sink += msg.length     // never true, but msg was BUILT anyway
        i += 1
      }
      sink
    }

    allocated("guarded by the level check") {
      var i = 0; var sink = 0
      while (i < n) {
        if (debugEnabled) {
          val msg = s"processing order $orderId for user $user attempt $i"
          sink += msg.length
        }
        i += 1
      }
      sink
    }

    println("""
  Scala's s"..." is EAGER: the StringBuilder runs, the ints box for the
  interpolator, and the String is built -- then thrown away because the level
  is off. In Java, SLF4J's `log.debug("order {} user {}", id, user)` defers
  formatting, so the idiomatic Java code did not have this problem and the
  direct Scala port does.

  Fixes:
    - a logging library with by-name arguments: log4s, scala-logging
      (scala-logging uses macros so `logger.debug(s"...")` compiles to
       `if (logger.isDebugEnabled) ...` -- zero cost when off)
    - or guard it yourself
  This is usually the top hit in a Scala service's allocation profile, and it
  is a one-line fix. Look for it first.""")
  }

  // ==================================================================
  def part5_tuplesAndOptions(): Unit = {
    println("\n=== PART 5: tuples, Options, and case class copies ===")
    val n = 1_000_000

    allocated("zipWithIndex (Tuple2 per element)") {
      Array.range(0, n).zipWithIndex.length
    }
    allocated("indices only, no tuples") {
      val a = Array.range(0, n)
      var i = 0; var acc = 0L
      while (i < a.length) { acc += a(i) + i; i += 1 }
      acc
    }
    allocated("Option[Int] per element (Some allocates)") {
      var i = 0; var acc = 0L
      while (i < n) { val o: Option[Int] = if (i % 2 == 0) Some(i) else None; acc += o.getOrElse(0); i += 1 }
      acc
    }
    allocated("groupBy (tuples + boxed keys + maps)") {
      Array.range(0, n).groupBy(_ % 100).size
    }

    case class Order(id: Long, qty: Int, price: Double, note: String)
    allocated("case class .copy in a loop") {
      var o = Order(1, 1, 1.0, "x")
      var i = 0
      while (i < n) { o = o.copy(qty = i); i += 1 }
      o.qty
    }

    println("""
  A Tuple2 of two Ints costs: Tuple2 object (~24 B) + two boxed Integers
  (~32 B) = ~56 bytes to carry 8 bytes of data. `Some(x)` is 16 bytes, plus
  boxing for a primitive. `None` is a singleton and free.

  Every one of these is fine in ordinary code and only matters in a hot loop.
  Note also that escape analysis SOMETIMES eliminates these -- but only in
  C2-compiled code, only when inlining succeeds, and not reliably. Do not
  assume it; measure.""")
  }

  // ==================================================================
  def part6_theLesson(): Unit = {
    println("""
=== THE LESSON ===

  This file makes idiomatic Scala look expensive. Resist over-correcting.

  1. Nearly all of this garbage dies in EDEN and is collected by a copying
     young collector that never touches it. Allocation is ~10ns; the cost is
     GC FREQUENCY, not per-object overhead. Quiz 05 Q10 is the framing.
  2. The right process is: profile (async-profiler -e alloc), find the top
     3-5 allocation sites, fix THOSE, re-measure. Do not de-idiomatise a
     codebase on principle -- you will make it worse and slower to change.
  3. In practice the wins are boringly repetitive:
       - eager string interpolation in log statements  <- check this first
       - a map/filter chain over a large collection in a request path
       - boxing in a numeric fold
       - List where Vector or Array belonged
       - `xs :+ x` in a loop (accidentally quadratic)
  4. The 90% of code that runs once per request does not matter. The 1% in a
     loop over 10^6 elements does.

  And the counterpoint to have ready: Scala's immutability-by-default removes
  a whole class of concurrency bug. Trading a few percent of CPU for that is
  usually a good deal -- but you should know the number before you defend it.""")
  }
}
