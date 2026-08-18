/*
 * P04 — ConcurrentHashMap: check-then-act, and the recursive-update trap.
 *
 * RUN:  java problems/java/P04ComputeIfAbsentDeadlock.java
 *
 * TASK
 *   1. PART A: predict how many times `expensive()` is called. Run it. Explain.
 *   2. PART B: predict what happens. Run it. What exception, and why?
 *   3. PART C: explain why holding the bin lock during the mapping function matters.
 *      What would happen if `expensive()` made an HTTP call taking 2 seconds?
 *   4. Rewrite PART A correctly in two different ways.
 *   5. BONUS: why is `map.size()` described as "weakly consistent"? When does that
 *      matter, and when is it fine?
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class P04ComputeIfAbsentDeadlock {

    static final AtomicInteger expensiveCalls = new AtomicInteger();

    static String expensive(String key) {
        expensiveCalls.incrementAndGet();
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return "value-for-" + key;
    }

    public static void main(String[] args) throws Exception {
        partA_checkThenAct();
        partA_fixed();
        partB_recursiveUpdate();
        partC_lockScope();
    }

    // ================= PART A: check-then-act is not atomic =================
    static void partA_checkThenAct() throws Exception {
        System.out.println("=== PART A: containsKey + put (BUGGY) ===");
        var map = new ConcurrentHashMap<String, String>();
        expensiveCalls.set(0);

        int threads = 8;
        var pool = Executors.newFixedThreadPool(threads);
        var gate = new CountDownLatch(1);
        var done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    gate.await();
                    // BUG: two separate atomic operations are not one atomic operation.
                    if (!map.containsKey("k")) {
                        map.put("k", expensive("k"));
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        gate.countDown();
        done.await();
        pool.shutdown();

        System.out.println("  threads          = " + threads);
        System.out.println("  expensive() runs = " + expensiveCalls.get() + "   (should be 1!)");
        System.out.println("  map size         = " + map.size());
        System.out.println("  --> " + (expensiveCalls.get() > 1
                ? "RACE REPRODUCED: duplicated work, and " + (expensiveCalls.get() - 1) + " results silently discarded."
                : "did not reproduce this run -- try again, it is timing dependent."));
    }

    static void partA_fixed() throws Exception {
        System.out.println("\n=== PART A (fixed): computeIfAbsent ===");
        var map = new ConcurrentHashMap<String, String>();
        expensiveCalls.set(0);

        int threads = 8;
        var pool = Executors.newFixedThreadPool(threads);
        var gate = new CountDownLatch(1);
        var done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try { gate.await(); map.computeIfAbsent("k", P04ComputeIfAbsentDeadlock::expensive); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        gate.countDown();
        done.await();
        pool.shutdown();

        System.out.println("  expensive() runs = " + expensiveCalls.get() + "   (atomic: exactly once)");
    }

    // ================= PART B: recursive update =================
    static void partB_recursiveUpdate() {
        System.out.println("\n=== PART B: recursive computeIfAbsent on the same map ===");
        var map = new ConcurrentHashMap<String, Integer>();

        try {
            map.computeIfAbsent("outer", k -> {
                // BUG: mutating the same map from inside the mapping function.
                // We are holding the bin lock for "outer" right now.
                map.computeIfAbsent("inner", k2 -> 1);
                return 2;
            });
            System.out.println("  no exception -- keys hashed to different bins, so we got away with it.");
            System.out.println("  map = " + map + "   <-- this is the DANGEROUS case: it works by luck.");
        } catch (IllegalStateException e) {
            System.out.println("  IllegalStateException: " + e.getMessage());
            System.out.println("  --> JDK 9+ detects this. On JDK 8 it could DEADLOCK or corrupt the table.");
        }

        // A memoized-recursion shape that reliably breaks. Classic "memoized fibonacci".
        System.out.println("\n  Memoized recursion (the realistic version of this bug):");
        var memo = new ConcurrentHashMap<Integer, Long>();
        try {
            System.out.println("  fib(20) = " + fib(20, memo));
            System.out.println("  --> got away with it this time; correctness depends on bin layout.");
        } catch (IllegalStateException e) {
            System.out.println("  IllegalStateException: " + e.getMessage());
            System.out.println("  --> Fix: use a plain HashMap (single-threaded recursion), or");
            System.out.println("      compute the value fully BEFORE calling putIfAbsent.");
        }
    }

    static long fib(int n, ConcurrentHashMap<Integer, Long> memo) {
        if (n <= 1) return n;
        // BUG: recursive computeIfAbsent on the same map.
        return memo.computeIfAbsent(n, k -> fib(k - 1, memo) + fib(k - 2, memo));
    }

    // ================= PART C: lock scope =================
    static void partC_lockScope() throws Exception {
        System.out.println("\n=== PART C: the mapping function holds a bin lock ===");
        var map = new ConcurrentHashMap<Integer, String>();

        var pool = Executors.newFixedThreadPool(4);
        long t0 = System.nanoTime();
        var futures = new ArrayList<Future<?>>();
        // 4 threads, 4 DIFFERENT keys. Each mapping function sleeps 300ms.
        for (int i = 0; i < 4; i++) {
            final int key = i;
            futures.add(pool.submit(() -> map.computeIfAbsent(key, k -> {
                try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "v" + k;
            })));
        }
        for (var f : futures) f.get();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        pool.shutdown();

        System.out.println("  4 threads x 4 distinct keys x 300ms each took " + ms + " ms");
        System.out.println("  Different keys usually hash to different bins -> parallel (~300ms).");
        System.out.println("  But keys colliding in one bin would SERIALISE (~1200ms), and there is");
        System.out.println("  nothing in your code to tell you which case you are in.");
        System.out.println("\n  RULE: keep the mapping function short, pure, and non-blocking.");
        System.out.println("        Never do I/O inside computeIfAbsent. Use a real cache");
        System.out.println("        (Caffeine) if you need async loading with per-key coalescing.");
    }
}
