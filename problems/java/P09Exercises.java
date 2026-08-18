/*
 * P09 — Implementation exercises. Write real concurrent code.
 *
 * RUN:  java problems/java/P09Exercises.java
 *
 * Four exercises with a test harness. Implement each; the harness verifies
 * correctness under contention and reports PASS/FAIL. Solutions in SOLUTIONS.md,
 * but write your own first -- reading a solution teaches you almost nothing.
 *
 *   EX1  BoundedSemaphoreGate  -- limit concurrency to N, correctly, with timeout
 *   EX2  TokenBucketLimiter    -- rate limit to N/sec, lock-free
 *   EX3  StripedCounter        -- scalable counter without LongAdder
 *   EX4  ExpiringCache         -- concurrent cache with TTL and single-flight loading
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.Function;

public class P09Exercises {

    // =====================================================================
    // EX1: Allow at most `permits` threads inside run() at once.
    //      - must not exceed the limit, ever
    //      - must not leak permits when the body throws
    //      - tryRun must respect the timeout and report failure
    // =====================================================================
    static class BoundedSemaphoreGate {
        private final Semaphore semaphore;

        BoundedSemaphoreGate(int permits) {
            this.semaphore = new Semaphore(permits);
        }

        <T> T run(Callable<T> body) throws Exception {
            // TODO: acquire, run body, release in finally
            throw new UnsupportedOperationException("EX1: implement run()");
        }

        <T> Optional<T> tryRun(Callable<T> body, long timeout, TimeUnit unit) throws Exception {
            // TODO: tryAcquire with timeout; Optional.empty() if we could not get in
            throw new UnsupportedOperationException("EX1: implement tryRun()");
        }

        int available() { return semaphore.availablePermits(); }
    }

    // =====================================================================
    // EX2: Token bucket. `permitsPerSecond` sustained rate, `burst` capacity.
    //      Must be lock-free (a single CAS loop) and must not over-admit.
    //      HINT: pack tokens + lastRefillNanos into one AtomicLong, or use
    //            AtomicReference to a small immutable record.
    // =====================================================================
    static class TokenBucketLimiter {
        private record State(double tokens, long nanos) {}

        private final double permitsPerSecond;
        private final double burst;
        private final AtomicReference<State> state;

        TokenBucketLimiter(double permitsPerSecond, double burst) {
            this.permitsPerSecond = permitsPerSecond;
            this.burst = burst;
            this.state = new AtomicReference<>(new State(burst, System.nanoTime()));
        }

        boolean tryAcquire() {
            // TODO: CAS loop -- refill based on elapsed time, capped at burst,
            //       then take one token if available.
            throw new UnsupportedOperationException("EX2: implement tryAcquire()");
        }
    }

    // =====================================================================
    // EX3: A scalable counter. Do NOT use LongAdder -- build the idea yourself.
    //      Stripe across cells to avoid a single contended cache line.
    //      HINT: index by Thread.currentThread().threadId(), pad to 64 bytes.
    // =====================================================================
    static class StripedCounter {
        private static final int STRIPES = 64;
        // Padding: AtomicLong is 16 bytes; an array of them holds references,
        // so the referenced objects may share lines. Simplest fix for the
        // exercise: an AtomicLongArray with a stride so we skip cache lines.
        private final AtomicLongArray cells = new AtomicLongArray(STRIPES * 8);

        void increment() {
            // TODO: pick a stripe from the current thread, incrementAndGet it
            throw new UnsupportedOperationException("EX3: implement increment()");
        }

        long sum() {
            // TODO: sum every stripe
            throw new UnsupportedOperationException("EX3: implement sum()");
        }
    }

    // =====================================================================
    // EX4: Concurrent cache with TTL and SINGLE-FLIGHT loading.
    //      - concurrent get() for the same missing key must call the loader ONCE
    //      - expired entries must be reloaded
    //      - must NOT hold a lock while the loader runs (see P04 part C)
    //      HINT: store CompletableFuture<V> as the value, so racing callers all
    //            join the same in-flight load. This is the standard trick.
    // =====================================================================
    static class ExpiringCache<K, V> {
        private record Entry<V>(CompletableFuture<V> future, long expiresAtNanos) {}

        private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
        private final long ttlNanos;
        private final Function<K, V> loader;
        final AtomicInteger loaderCalls = new AtomicInteger();

        ExpiringCache(long ttl, TimeUnit unit, Function<K, V> loader) {
            this.ttlNanos = unit.toNanos(ttl);
            this.loader = k -> { loaderCalls.incrementAndGet(); return loader.apply(k); };
        }

        V get(K key) {
            // TODO: single-flight load with TTL. Do not run the loader inside
            //       computeIfAbsent's mapping function for longer than necessary --
            //       create the CompletableFuture there, complete it outside.
            throw new UnsupportedOperationException("EX4: implement get()");
        }
    }

    // #####################################################################
    // TEST HARNESS
    // #####################################################################
    static int passed = 0, failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("cores = " + Runtime.getRuntime().availableProcessors() + "\n");
        testEx1();
        testEx2();
        testEx3();
        testEx4();
        System.out.printf("%n===== %d passed, %d failed =====%n", passed, failed);
        if (failed > 0) System.out.println("Implement the TODOs above. Solutions in SOLUTIONS.md.");
    }

    static void check(String name, boolean ok, String detail) {
        System.out.printf("  [%s] %-34s %s%n", ok ? "PASS" : "FAIL", name, detail);
        if (ok) passed++; else failed++;
    }

    static void skip(String name, Throwable t) {
        System.out.printf("  [SKIP] %-34s not implemented (%s)%n", name, t.getClass().getSimpleName());
        failed++;
    }

    /**
     * Returns true if the exercise still throws UnsupportedOperationException.
     * Probing FIRST matters: the harness itself blocks on latches that an
     * unimplemented method never counts down.
     */
    static boolean notImplemented(String name, ThrowingRunnable probe) {
        try {
            probe.run();
            return false;
        } catch (UnsupportedOperationException e) {
            skip(name, e);
            return true;
        } catch (Throwable t) {
            return false;   // some other exception: it is implemented, let the tests run
        }
    }

    interface ThrowingRunnable { void run() throws Exception; }

    // ---------------------------------------------------------------
    static void testEx1() throws Exception {
        System.out.println("=== EX1: BoundedSemaphoreGate ===");
        if (notImplemented("BoundedSemaphoreGate", () -> new BoundedSemaphoreGate(1).run(() -> null))) return;

        final int limit = 5;
        var gate = new BoundedSemaphoreGate(limit);
        var concurrent = new AtomicInteger();
        var maxSeen = new AtomicInteger();

        try {
            var pool = Executors.newFixedThreadPool(32);
            var done = new CountDownLatch(200);
            for (int i = 0; i < 200; i++) {
                pool.submit(() -> {
                    try {
                        gate.run(() -> {
                            int now = concurrent.incrementAndGet();
                            maxSeen.accumulateAndGet(now, Math::max);
                            Thread.sleep(5);
                            concurrent.decrementAndGet();
                            return null;
                        });
                    } catch (Exception e) { /* ignore */ }
                    finally { done.countDown(); }
                });
            }
            done.await(20, TimeUnit.SECONDS);
            pool.shutdown();

            check("never exceeds the limit", maxSeen.get() <= limit,
                    "max concurrent observed = " + maxSeen.get() + " (limit " + limit + ")");
            check("no permits leaked", gate.available() == limit,
                    "available = " + gate.available() + " (expected " + limit + ")");

            // Permits must be released even when the body throws.
            var g2 = new BoundedSemaphoreGate(1);
            try { g2.run(() -> { throw new RuntimeException("boom"); }); } catch (Exception expected) { }
            check("releases on exception", g2.available() == 1, "available = " + g2.available());

            // tryRun must time out rather than block forever.
            var g3 = new BoundedSemaphoreGate(1);
            var holding = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var t = new Thread(() -> {
                try { g3.run(() -> { holding.countDown(); release.await(); return null; }); }
                catch (Exception e) { }
            });
            t.setDaemon(true);
            t.start();
            holding.await();
            long t0 = System.nanoTime();
            var result = g3.tryRun(() -> "should not run", 200, TimeUnit.MILLISECONDS);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            release.countDown();
            check("tryRun times out", result.isEmpty() && ms >= 150 && ms < 1000,
                    "empty=" + result.isEmpty() + " after " + ms + "ms");
        } catch (UnsupportedOperationException e) {
            skip("BoundedSemaphoreGate", e);
        }
    }

    // ---------------------------------------------------------------
    static void testEx2() throws Exception {
        System.out.println("\n=== EX2: TokenBucketLimiter ===");
        if (notImplemented("TokenBucketLimiter", () -> new TokenBucketLimiter(1, 1).tryAcquire())) return;
        try {
            // 100/sec, burst 10. Hammer it for 1 second from many threads.
            var limiter = new TokenBucketLimiter(100, 10);
            var admitted = new AtomicInteger();
            var pool = Executors.newFixedThreadPool(8);
            var stop = new AtomicBoolean();
            for (int i = 0; i < 8; i++) {
                pool.submit(() -> {
                    while (!stop.get()) {
                        if (limiter.tryAcquire()) admitted.incrementAndGet();
                    }
                });
            }
            Thread.sleep(1000);
            stop.set(true);
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);

            int n = admitted.get();
            // ~100 refilled + up to 10 initial burst; allow generous slack for scheduling.
            boolean ok = n >= 80 && n <= 160;
            check("admits ~100 in 1s (burst 10)", ok, "admitted = " + n + " (expected 80..160)");

            // A fresh bucket must allow exactly `burst` immediately, then stop.
            var b2 = new TokenBucketLimiter(1, 5);   // 1/sec so refill is negligible
            int immediate = 0;
            for (int i = 0; i < 100; i++) if (b2.tryAcquire()) immediate++;
            check("initial burst is bounded", immediate >= 5 && immediate <= 6,
                    "immediate admits = " + immediate + " (expected 5)");
        } catch (UnsupportedOperationException e) {
            skip("TokenBucketLimiter", e);
        }
    }

    // ---------------------------------------------------------------
    static void testEx3() throws Exception {
        System.out.println("\n=== EX3: StripedCounter ===");
        if (notImplemented("StripedCounter", () -> new StripedCounter().increment())) return;
        try {
            var counter = new StripedCounter();
            int threads = 8, per = 200_000;
            var pool = Executors.newFixedThreadPool(threads);
            var gate = new CountDownLatch(1);
            var done = new CountDownLatch(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try { gate.await(); for (int i = 0; i < per; i++) counter.increment(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    finally { done.countDown(); }
                });
            }
            long t0 = System.nanoTime();
            gate.countDown();
            done.await();
            long ms = (System.nanoTime() - t0) / 1_000_000;
            pool.shutdown();

            long expected = (long) threads * per;
            check("counts exactly", counter.sum() == expected,
                    "sum = " + counter.sum() + " expected " + expected + " (" + ms + "ms)");

            // Compare against a single contended AtomicLong.
            var atomic = new AtomicLong();
            var pool2 = Executors.newFixedThreadPool(threads);
            var gate2 = new CountDownLatch(1);
            var done2 = new CountDownLatch(threads);
            for (int t = 0; t < threads; t++) {
                pool2.submit(() -> {
                    try { gate2.await(); for (int i = 0; i < per; i++) atomic.incrementAndGet(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    finally { done2.countDown(); }
                });
            }
            long t1 = System.nanoTime();
            gate2.countDown();
            done2.await();
            long ms2 = (System.nanoTime() - t1) / 1_000_000;
            pool2.shutdown();
            System.out.println("         striped=" + ms + "ms  single AtomicLong=" + ms2
                    + "ms  -> speedup " + String.format("%.1fx", (double) ms2 / Math.max(ms, 1)));
        } catch (UnsupportedOperationException e) {
            skip("StripedCounter", e);
        }
    }

    // ---------------------------------------------------------------
    static void testEx4() throws Exception {
        System.out.println("\n=== EX4: ExpiringCache ===");
        if (notImplemented("ExpiringCache",
                () -> new ExpiringCache<String, String>(1, TimeUnit.SECONDS, k -> k).get("probe"))) return;
        try {
            var cache = new ExpiringCache<String, String>(300, TimeUnit.MILLISECONDS, k -> {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "v:" + k;
            });

            // 16 threads, same key, simultaneously -> loader must run ONCE.
            var pool = Executors.newFixedThreadPool(16);
            var gate = new CountDownLatch(1);
            var results = new CopyOnWriteArrayList<String>();
            var done = new CountDownLatch(16);
            for (int i = 0; i < 16; i++) {
                pool.submit(() -> {
                    try { gate.await(); results.add(cache.get("hot")); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    finally { done.countDown(); }
                });
            }
            gate.countDown();
            done.await(10, TimeUnit.SECONDS);

            check("single-flight loading", cache.loaderCalls.get() == 1,
                    "loader called " + cache.loaderCalls.get() + " time(s) for 16 concurrent gets");
            check("all callers get the value",
                    results.size() == 16 && results.stream().allMatch("v:hot"::equals),
                    results.size() + " results, all correct: "
                            + results.stream().allMatch("v:hot"::equals));

            // Cached read must not call the loader again.
            int before = cache.loaderCalls.get();
            cache.get("hot");
            check("serves from cache", cache.loaderCalls.get() == before,
                    "loader calls unchanged at " + cache.loaderCalls.get());

            // After the TTL, it must reload.
            Thread.sleep(400);
            cache.get("hot");
            check("reloads after TTL", cache.loaderCalls.get() == before + 1,
                    "loader calls = " + cache.loaderCalls.get() + " (expected " + (before + 1) + ")");
            pool.shutdown();
        } catch (UnsupportedOperationException e) {
            skip("ExpiringCache", e);
        }
    }
}
