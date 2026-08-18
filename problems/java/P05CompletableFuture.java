/*
 * P05 — CompletableFuture: which thread runs what, and where exceptions go.
 *
 * RUN:  java problems/java/P05CompletableFuture.java
 *
 * TASK
 *   1. For EACH of the six parts, write down your prediction before running.
 *   2. Part 2 is the important one: explain the rule for non-Async stages.
 *   3. Part 4: why does the exception vanish? What is the minimum change that
 *      makes it observable?
 *   4. Part 6: `orTimeout` completes your future -- but what happened to the
 *      thread doing the slow work? Why is that a problem in a real service?
 *   5. Fix Part 3 so it does not use the common pool.
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class P05CompletableFuture {

    static void p(String label) {
        System.out.printf("    %-14s on %s%n", label, Thread.currentThread().getName());
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("main thread = " + Thread.currentThread().getName());
        System.out.println("cores = " + Runtime.getRuntime().availableProcessors()
                + ", commonPool parallelism = " + ForkJoinPool.commonPool().getParallelism());

        part1_alreadyComplete();
        part2_whichThread();
        part3_explicitExecutor();
        part4_swallowedException();
        part5_applyVsCompose();
        part6_timeoutDoesNotCancel();
        part7_blockingTheCommonPool();
    }

    // ---------------------------------------------------------------
    static void part1_alreadyComplete() {
        System.out.println("\n=== PART 1: attaching to an ALREADY-completed future ===");
        var done = CompletableFuture.completedFuture(1);
        done.thenApply(x -> { p("thenApply"); return x; })
            .thenAccept(x -> p("thenAccept"))
            .join();
        System.out.println("    --> ran on the CALLING thread (main), because the stage was");
        System.out.println("        already complete when the callback was attached.");
    }

    // ---------------------------------------------------------------
    static void part2_whichThread() {
        System.out.println("\n=== PART 2: non-Async vs Async stages ===");
        CompletableFuture
                .supplyAsync(() -> { p("supplyAsync"); sleep(50); return 1; })
                .thenApply(x -> { p("thenApply"); return x + 1; })
                .thenApplyAsync(x -> { p("thenApplyAsync"); return x + 1; })
                .thenAccept(x -> p("thenAccept"))
                .join();
        System.out.println("    RULE: a non-Async stage runs on whichever thread COMPLETES the");
        System.out.println("          previous stage -- or on the caller's thread if the previous");
        System.out.println("          stage was already done. You do not control it. Never block there.");
    }

    // ---------------------------------------------------------------
    static void part3_explicitExecutor() {
        System.out.println("\n=== PART 3: explicit executor (what you should actually write) ===");
        var ex = Executors.newFixedThreadPool(2, r -> {
            var t = new Thread(r, "my-io-pool");
            t.setDaemon(true);
            return t;
        });
        try {
            CompletableFuture
                    .supplyAsync(() -> { p("supplyAsync"); return 1; }, ex)
                    .thenApplyAsync(x -> { p("thenApplyAsync"); return x + 1; }, ex)
                    .thenAcceptAsync(x -> p("thenAcceptAsync"), ex)
                    .join();
            System.out.println("    --> named threads, bounded pool, no common-pool contention.");
        } finally {
            ex.shutdown();
        }
    }

    // ---------------------------------------------------------------
    static void part4_swallowedException() {
        System.out.println("\n=== PART 4: the silently swallowed exception ===");
        // Fire and forget. Nobody ever observes this future.
        CompletableFuture.runAsync(() -> {
            throw new IllegalStateException("nobody will ever see me");
        });
        sleep(200);
        System.out.println("    --> No stack trace printed. No log line. The failure is GONE.");
        System.out.println("        (Contrast: an uncaught exception in a plain Thread reaches");
        System.out.println("         the default UncaughtExceptionHandler and IS printed.)");

        System.out.println("\n    Fixed by terminating the chain with an observer:");
        CompletableFuture.runAsync(() -> { throw new IllegalStateException("now I am logged"); })
                .whenComplete((v, t) -> {
                    if (t != null) System.out.println("      caught: " + t.getCause());
                });
        sleep(200);

        System.out.println("\n    join() vs get() wrapping:");
        var failed = CompletableFuture.failedFuture(new IllegalArgumentException("boom"));
        try { failed.join(); }
        catch (CompletionException e) { System.out.println("      join()  threw " + e.getClass().getSimpleName() + ", cause=" + e.getCause()); }
        try { failed.get(); }
        catch (ExecutionException e) { System.out.println("      get()   threw " + e.getClass().getSimpleName() + ", cause=" + e.getCause()); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("      --> different wrapper types for the same failure. Always unwrap getCause().");
    }

    // ---------------------------------------------------------------
    static void part5_applyVsCompose() {
        System.out.println("\n=== PART 5: thenApply vs thenCompose ===");
        CompletableFuture<Integer> base = CompletableFuture.completedFuture(21);

        // WRONG: nested future
        CompletableFuture<CompletableFuture<Integer>> nested =
                base.thenApply(x -> CompletableFuture.supplyAsync(() -> x * 2));
        System.out.println("    thenApply   -> " + nested.join().getClass().getSimpleName()
                + " (a nested future -- you must join twice)");
        System.out.println("                   value = " + nested.join().join());

        // RIGHT: flattened
        CompletableFuture<Integer> flat =
                base.thenCompose(x -> CompletableFuture.supplyAsync(() -> x * 2));
        System.out.println("    thenCompose -> " + flat.join() + " (flattened -- this is flatMap)");
    }

    // ---------------------------------------------------------------
    static void part6_timeoutDoesNotCancel() throws Exception {
        System.out.println("\n=== PART 6: orTimeout does NOT stop the underlying work ===");
        var stillRunning = new AtomicBoolean(false);

        var slow = CompletableFuture.supplyAsync(() -> {
            sleep(1500);
            stillRunning.set(true);          // proves the work continued
            return "slow result";
        });

        var withTimeout = slow.orTimeout(300, TimeUnit.MILLISECONDS)
                              .exceptionally(t -> "fallback (" + t.getClass().getSimpleName() + ")");

        System.out.println("    result after 300ms timeout = " + withTimeout.join());
        System.out.println("    work finished yet? " + stillRunning.get());
        sleep(1500);
        System.out.println("    ...after waiting 1.5s more, work finished? " + stillRunning.get());
        System.out.println("    --> The task RAN TO COMPLETION on a pool thread, holding whatever");
        System.out.println("        resources it held (a DB connection!), long after you gave up.");
        System.out.println("        This is why Future/CompletableFuture 'timeouts' leak capacity,");
        System.out.println("        and why real cancellation (ZIO/Cats Effect fibers) matters.");
    }

    // ---------------------------------------------------------------
    static void part7_blockingTheCommonPool() {
        System.out.println("\n=== PART 7: blocking the common pool ===");
        int parallelism = ForkJoinPool.commonPool().getParallelism();
        System.out.println("    common pool parallelism = " + parallelism);
        System.out.println("    submitting " + (parallelism + 4) + " tasks that each sleep 400ms...");

        long t0 = System.nanoTime();
        var all = new CompletableFuture<?>[parallelism + 4];
        for (int i = 0; i < all.length; i++) {
            all[i] = CompletableFuture.runAsync(() -> sleep(400));   // no executor -> common pool
        }
        CompletableFuture.allOf(all).join();
        long ms = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("    took " + ms + " ms for " + all.length + " x 400ms tasks");
        System.out.println("    --> If this is ~800ms+ rather than ~400ms, the pool was saturated.");
        System.out.println("        Note the pool may have grown via ForkJoinPool compensation.");
        System.out.println("        Meanwhile every parallelStream() in the JVM was queued behind you.");
    }
}
