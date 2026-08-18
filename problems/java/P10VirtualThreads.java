/*
 * P10 — Virtual threads: the win, the pinning trap, and what does NOT get faster.
 *
 * RUN:  java problems/java/P10VirtualThreads.java
 *       java -Djdk.tracePinnedThreads=short problems/java/P10VirtualThreads.java
 *
 * TASK
 *   1. Predict all four timings before running.
 *   2. PART 3: explain why `synchronized` collapses the virtual-thread advantage
 *      on JDK 21. Which JDK fixed it, and how?
 *   3. PART 4: explain why virtual threads give you NOTHING here.
 *   4. Re-run with -Djdk.tracePinnedThreads=short and read the output.
 *   5. Change PART 3 to use ReentrantLock instead of synchronized. Re-measure.
 *      (The code is there, commented out.)
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.stream.*;

public class P10VirtualThreads {

    static final int TASKS = 10_000;
    static final int BLOCK_MS = 200;

    public static void main(String[] args) throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("JDK " + Runtime.version() + ", cores = " + cores);
        System.out.println("tasks = " + TASKS + ", each blocking for " + BLOCK_MS + "ms");
        System.out.println("ideal (perfect concurrency) = " + BLOCK_MS + "ms\n");

        part1_blockingIo(cores);
        part2_threadCost();
        part3_pinning();
        part4_cpuBound(cores);
        part5_semaphoreNotPool();
    }

    // ==================================================================
    static void part1_blockingIo(int cores) throws Exception {
        System.out.println("=== PART 1: 10,000 blocking tasks ===");

        long virtual = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < TASKS; i++) ex.submit(() -> { sleep(BLOCK_MS); return null; });
            }   // close() waits for all
        });
        System.out.printf("  virtual thread per task     %6d ms%n", virtual);

        long fixed200 = time(() -> {
            var ex = Executors.newFixedThreadPool(200);
            try {
                var futures = new ArrayList<Future<?>>();
                for (int i = 0; i < TASKS; i++) futures.add(ex.submit(() -> { sleep(BLOCK_MS); return null; }));
                for (var f : futures) f.get();
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { ex.shutdown(); }
        });
        System.out.printf("  fixed pool of 200           %6d ms%n", fixed200);

        // A cores-sized pool would take 10000/14 * 200ms = ~2.5 MINUTES for the full
        // set, so run a tenth of the work and extrapolate rather than make you wait.
        final int smallN = TASKS / 10;
        long fixedCoresSmall = time(() -> {
            var ex = Executors.newFixedThreadPool(cores);
            try {
                var futures = new ArrayList<Future<?>>();
                for (int i = 0; i < smallN; i++) futures.add(ex.submit(() -> { sleep(BLOCK_MS); return null; }));
                for (var f : futures) f.get();
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { ex.shutdown(); }
        });
        System.out.printf("  fixed pool of %-3d           %6d ms  (only %d tasks -> ~%d ms for %d)%n",
                cores, fixedCoresSmall, smallN, fixedCoresSmall * 10, TASKS);

        System.out.printf("%n  virtual is %.0fx faster than the 200-thread pool.%n", (double) fixed200 / virtual);
        System.out.println("  The pool sizes ARE the bottleneck: 10000/200 = 50 batches x 200ms = 10s.");
        System.out.println("  Virtual threads run all 10000 concurrently, so the total is ~one sleep.");
    }

    // ==================================================================
    static void part2_threadCost() throws Exception {
        System.out.println("\n=== PART 2: what does a thread cost? ===");

        int n = 50_000;
        long t0 = System.nanoTime();
        var vthreads = new ArrayList<Thread>(n);
        var latch = new CountDownLatch(1);
        for (int i = 0; i < n; i++) {
            var t = Thread.ofVirtual().unstarted(() -> {
                try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            t.start();
            vthreads.add(t);
        }
        long createMs = (System.nanoTime() - t0) / 1_000_000;
        long heapMb = usedHeapMb();
        System.out.printf("  created %,d LIVE virtual threads in %d ms, heap used %d MB%n", n, createMs, heapMb);
        System.out.printf("  ~%.1f microseconds and ~%.0f bytes each%n",
                createMs * 1000.0 / n, heapMb * 1024.0 * 1024.0 / n);
        latch.countDown();
        for (var t : vthreads) t.join();

        System.out.println("\n  Try the same with Thread.ofPlatform(): 50,000 platform threads means");
        System.out.println("  ~50 GB of reserved stack. You will get OutOfMemoryError: unable to");
        System.out.println("  create native thread long before that. (Not run here on purpose.)");

        // Show how few carriers there are.
        var carriers = ConcurrentHashMap.<String>newKeySet();
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 2000; i++) {
                ex.submit(() -> {
                    // The carrier name appears in the virtual thread's toString().
                    carriers.add(Thread.currentThread().toString().replaceAll(".*@", ""));
                    sleep(5);
                    return null;
                });
            }
        }
        System.out.println("\n  2000 virtual threads ran on ~" + carriers.size()
                + " distinct carrier(s) (cores = " + Runtime.getRuntime().availableProcessors() + ")");
    }

    // ==================================================================
    static final Object MONITOR = new Object();
    static final ReentrantLock LOCK = new ReentrantLock();

    static void part3_pinning() throws Exception {
        System.out.println("\n=== PART 3: PINNING -- synchronized + blocking on JDK 21 ===");
        int n = 500;

        long pinned = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < n; i++) ex.submit(() -> {
                    // On JDK 21 this PINS the carrier: the monitor cannot be
                    // unmounted, so the carrier thread is held for the full sleep.
                    synchronized (MONITOR) { sleep(20); }
                    return null;
                });
            }
        });
        System.out.printf("  %d tasks, synchronized + 20ms sleep : %6d ms%n", n, pinned);

        long notPinned = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < n; i++) ex.submit(() -> {
                    // ReentrantLock is virtual-thread-aware: the fiber unmounts cleanly.
                    LOCK.lock();
                    try { sleep(20); } finally { LOCK.unlock(); }
                    return null;
                });
            }
        });
        System.out.printf("  %d tasks, ReentrantLock + 20ms sleep: %6d ms%n", n, notPinned);

        System.out.println("""

                  Both serialise here (they share one lock), so the totals are similar --
                  that is the point: a lock is a lock. The DIFFERENCE is what happens to
                  the CARRIER. With `synchronized` the carrier is pinned and unavailable
                  to any other virtual thread; with ReentrantLock it is released.

                  To see it, run:
                    java -Djdk.tracePinnedThreads=short problems/java/P10VirtualThreads.java

                  JDK 21: `synchronized` and native frames pin.
                  JDK 24 (JEP 491): monitors no longer pin. Native frames still do.""");

        // Now show the throughput difference when the lock is NOT the bottleneck:
        // many threads blocking while holding DIFFERENT monitors.
        long manyMonitorsPinned = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < 500; i++) {
                    final Object ownMonitor = new Object();   // no contention at all
                    ex.submit(() -> { synchronized (ownMonitor) { sleep(100); } return null; });
                }
            }
        });
        long manyLocksFree = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < 500; i++) {
                    final ReentrantLock ownLock = new ReentrantLock();
                    ex.submit(() -> { ownLock.lock(); try { sleep(100); } finally { ownLock.unlock(); } return null; });
                }
            }
        });
        System.out.println("\n  500 tasks, each with its OWN lock (zero contention), 100ms sleep:");
        System.out.printf("    synchronized (pins)  : %6d ms  <-- limited by carrier count!%n", manyMonitorsPinned);
        System.out.printf("    ReentrantLock (free) : %6d ms  <-- limited only by the sleep%n", manyLocksFree);
        System.out.println("    THIS is the pinning cost: uncontended locks, and still serialised.");
    }

    // ==================================================================
    static void part4_cpuBound(int cores) throws Exception {
        System.out.println("\n=== PART 4: CPU-bound work -- virtual threads do NOT help ===");
        int n = 200;

        long virtual = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < n; i++) ex.submit(P10VirtualThreads::burnCpu);
            }
        });
        long platform = time(() -> {
            var ex = Executors.newFixedThreadPool(cores);
            try {
                var futures = new ArrayList<Future<?>>();
                for (int i = 0; i < n; i++) futures.add(ex.submit(P10VirtualThreads::burnCpu));
                for (var f : futures) f.get();
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { ex.shutdown(); }
        });

        System.out.printf("  %d CPU-bound tasks, virtual        : %6d ms%n", n, virtual);
        System.out.printf("  %d CPU-bound tasks, fixed pool(%d) : %6d ms%n", n, cores, platform);
        System.out.println("""
                  Roughly the same, because the bottleneck is CORES, not threads.
                  Virtual threads solve WAITING, not COMPUTING. For CPU-bound work,
                  a fixed platform pool sized to cores is still correct -- and it has
                  less scheduling overhead.""");
    }

    // ==================================================================
    static void part5_semaphoreNotPool() throws Exception {
        System.out.println("\n=== PART 5: bound the RESOURCE, not the threads ===");

        // 10,000 virtual threads, but a downstream that tolerates only 50 at once.
        var limiter = new Semaphore(50);
        var concurrent = new java.util.concurrent.atomic.AtomicInteger();
        var maxSeen = new java.util.concurrent.atomic.AtomicInteger();

        long ms = time(() -> {
            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < 2000; i++) {
                    ex.submit(() -> {
                        limiter.acquire();               // parks the FIBER, costs no thread
                        try {
                            int now = concurrent.incrementAndGet();
                            maxSeen.accumulateAndGet(now, Math::max);
                            sleep(10);
                            concurrent.decrementAndGet();
                        } finally { limiter.release(); }
                        return null;
                    });
                }
            }
        });

        System.out.println("  2000 virtual threads through a Semaphore(50): " + ms + " ms");
        System.out.println("  max concurrent inside the gate = " + maxSeen.get() + " (limit 50)");
        System.out.println("""
                  1950 virtual threads were parked on the semaphore, costing nothing.
                  A 50-thread pool would have expressed the same limit -- but it would
                  ALSO have conflated 'the downstream supports 50' with 'we have 50
                  units of execution'. Separating them is the real mental shift:

                      old: size the thread pool to protect the downstream
                      new: unbounded virtual threads + a Semaphore per dependency""");
    }

    // ==================================================================
    static long burnCpu() {
        long sum = 0;
        for (int i = 0; i < 8_000_000; i++) sum += i * 31L;
        return sum;
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static long time(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return (System.nanoTime() - t0) / 1_000_000;
    }

    static long usedHeapMb() {
        var rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }
}
