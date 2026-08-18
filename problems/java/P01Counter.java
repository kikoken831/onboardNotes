/*
 * P01 — Lost updates.
 *
 * RUN:  java problems/java/P01Counter.java
 *
 * TASK
 *   1. Predict each printed value BEFORE running.
 *   2. Run it several times. Which counters are wrong? Is `volatile` enough?
 *   3. Run with `-XX:-TieredCompilation` and with 2 vs 8 threads. Does the loss change?
 *   4. Explain WHY volatile does not fix `++`, in one sentence.
 *   5. Time the four correct variants (there is a rough timing at the end).
 *      Explain the ordering you see.
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class P01Counter {

    static final int THREADS = 8;
    static final int PER_THREAD = 500_000;

    // --- the contenders ---
    static int plain = 0;                                    // no protection at all
    static volatile int volatileCounter = 0;                 // visibility only
    static final AtomicInteger atomic = new AtomicInteger();  // CAS
    static final LongAdder adder = new LongAdder();           // striped cells
    static int synced = 0;
    static synchronized void incSynced() { synced++; }        // mutual exclusion
    static final Object lock = new Object();
    static int lockedByObject = 0;

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        var pool = Executors.newFixedThreadPool(THREADS);
        var startGate = new CountDownLatch(1);
        var doneGate = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    startGate.await();              // maximise real contention
                    for (int i = 0; i < PER_THREAD; i++) {
                        plain++;
                        volatileCounter++;
                        atomic.incrementAndGet();
                        adder.increment();
                        incSynced();
                        synchronized (lock) { lockedByObject++; }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        doneGate.await();
        pool.shutdown();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        int expected = THREADS * PER_THREAD;
        System.out.println("threads=" + THREADS + "  increments/thread=" + PER_THREAD);
        System.out.println("expected           = " + expected);
        report("plain int", plain, expected);
        report("volatile int", volatileCounter, expected);
        report("AtomicInteger", atomic.get(), expected);
        report("LongAdder", (int) adder.sum(), expected);
        report("synchronized method", synced, expected);
        report("synchronized block", lockedByObject, expected);
        System.out.println("\ntotal wall time    = " + elapsedMs + " ms (all six counters, contended)");

        System.out.println("\n--- isolated timings (single counter per run) ---");
        timeOne("plain (WRONG)",    new PlainBox());
        timeOne("volatile (WRONG)", new VolatileBox());
        timeOne("AtomicInteger",    new AtomicBox());
        timeOne("LongAdder",        new AdderBox());
        timeOne("synchronized",     new SyncBox());
        System.out.println("\n(These are rough numbers, not JMH. Relative order is the point,");
        System.out.println(" and it depends heavily on thread count vs core count.)");
    }

    static void report(String name, int actual, int expected) {
        int lost = expected - actual;
        System.out.printf("%-20s = %,10d  %s%n", name, actual,
                lost == 0 ? "OK" : String.format("LOST %,d (%.2f%%)", lost, 100.0 * lost / expected));
    }

    // --- micro-timing scaffolding ---
    interface Box { void inc(); long get(); }
    static class PlainBox    implements Box { int v;                  public void inc(){v++;}                     public long get(){return v;} }
    static class VolatileBox implements Box { volatile int v;         public void inc(){v++;}                     public long get(){return v;} }
    static class AtomicBox   implements Box { final AtomicInteger v = new AtomicInteger(); public void inc(){v.incrementAndGet();} public long get(){return v.get();} }
    static class AdderBox    implements Box { final LongAdder v = new LongAdder();          public void inc(){v.increment();}      public long get(){return v.sum();} }
    static class SyncBox     implements Box { int v; public synchronized void inc(){v++;}    public synchronized long get(){return v;} }

    static void timeOne(String name, Box box) throws Exception {
        var pool = Executors.newFixedThreadPool(THREADS);
        var gate = new CountDownLatch(1);
        var done = new CountDownLatch(THREADS);
        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try { gate.await(); for (int i = 0; i < PER_THREAD; i++) box.inc(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        long t0 = System.nanoTime();
        gate.countDown();
        done.await();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        pool.shutdown();
        long expected = (long) THREADS * PER_THREAD;
        System.out.printf("  %-18s %5d ms   result=%,d%s%n", name, ms, box.get(),
                box.get() == expected ? "" : "  <-- WRONG");
    }
}
