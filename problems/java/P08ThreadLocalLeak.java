/*
 * P08 — ThreadLocal on a pooled thread: context bleed and memory leak.
 *
 * RUN:  java problems/java/P08ThreadLocalLeak.java
 *
 * TASK
 *   1. PART A: predict what request #2 sees. Run it. This is a SECURITY BUG in a
 *      multi-tenant service -- explain why.
 *   2. PART B: what is retained, and why doesn't nulling the ThreadLocal help?
 *      (Read the comment about weak KEYS and strong VALUES.)
 *   3. PART C: implement the TaskDecorator-style fix (propagate + clear).
 *   4. Explain how virtual threads change this picture -- and what NEW problem
 *      they introduce that makes ScopedValue desirable.
 *   5. Name every ThreadLocal-backed piece of ambient state in Spring you can.
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class P08ThreadLocalLeak {

    // ---- The kind of thing frameworks put in a ThreadLocal ----
    record Principal(String tenant, String user) {}

    static final ThreadLocal<Principal> CURRENT_USER = new ThreadLocal<>();

    /** Simulates SLF4J's MDC. */
    static final ThreadLocal<Map<String, String>> MDC = ThreadLocal.withInitial(HashMap::new);

    public static void main(String[] args) throws Exception {
        partA_contextBleed();
        partB_memoryRetention();
        partC_theFix();
        partD_virtualThreads();
    }

    // =====================================================================
    static void partA_contextBleed() throws Exception {
        System.out.println("=== PART A: context bleeding between requests on a pooled thread ===");

        // A pool with exactly ONE thread guarantees reuse, as a pool under load does.
        var pool = Executors.newFixedThreadPool(1);

        // Request 1: a filter sets the principal, the handler reads it. Nobody clears it.
        pool.submit(() -> {
            CURRENT_USER.set(new Principal("acme-corp", "alice"));
            MDC.get().put("traceId", "trace-111");
            System.out.println("  request 1 [" + Thread.currentThread().getName() + "]"
                    + " principal=" + CURRENT_USER.get() + " mdc=" + MDC.get());
            // BUG: no CURRENT_USER.remove() / MDC.clear() in a finally block.
        }).get();

        // Request 2: an UNAUTHENTICATED request. The filter sets nothing.
        pool.submit(() -> {
            Principal leaked = CURRENT_USER.get();
            System.out.println("  request 2 [" + Thread.currentThread().getName() + "]"
                    + " principal=" + leaked + " mdc=" + MDC.get());
            if (leaked != null) {
                System.out.println("""
                          >>> BLEED: request 2 is unauthenticated but sees request 1's
                          >>> principal. In a multi-tenant service this is cross-tenant
                          >>> data exposure -- a security incident, not a bug report.
                          >>> The traceId is wrong too, so your logs lie about it.""");
            }
        }).get();

        pool.shutdown();
    }

    // =====================================================================
    static void partB_memoryRetention() throws Exception {
        System.out.println("\n=== PART B: retention through the thread's ThreadLocalMap ===");

        var pool = Executors.newFixedThreadPool(2);
        var counter = new AtomicLong();

        // Each "request" stashes 8MB in a ThreadLocal and never removes it.
        ThreadLocal<byte[]> bigBuffer = new ThreadLocal<>();

        for (int i = 0; i < 4; i++) {
            pool.submit(() -> {
                bigBuffer.set(new byte[8 * 1024 * 1024]);   // 8 MB per pool thread
                counter.incrementAndGet();
            }).get();
        }

        System.gc();      // only for demonstration; see quiz 04 Q9
        Thread.sleep(200);
        long used = usedHeapMb();
        System.out.println("  " + counter.get() + " tasks ran on 2 pool threads");
        System.out.println("  heap used after System.gc() = " + used + " MB");
        System.out.println("""
                  The last byte[] per pool thread is STILL REACHABLE from that
                  thread's ThreadLocalMap. The threads are alive for the pool's
                  lifetime, so the buffers are too.

                  Why nulling out the ThreadLocal variable does not help:
                    ThreadLocalMap.Entry extends WeakReference<ThreadLocal<?>>
                    -> the KEY (the ThreadLocal object) is weakly held
                    -> the VALUE is held STRONGLY
                  So dropping your reference to the ThreadLocal lets the key be
                  collected, leaving a stale entry whose value is still strongly
                  reachable until that slot is reused or the thread dies.
                  This is exactly the classic webapp classloader leak.""");

        // Prove remove() works.
        var futures = new ArrayList<Future<?>>();
        for (int i = 0; i < 2; i++) {
            futures.add(pool.submit(() -> bigBuffer.remove()));
        }
        for (var f : futures) f.get();
        System.gc();
        Thread.sleep(200);
        System.out.println("\n  after remove() on both pool threads: heap used = " + usedHeapMb() + " MB");
        pool.shutdown();
    }

    // =====================================================================
    static void partC_theFix() throws Exception {
        System.out.println("\n=== PART C: propagate deliberately, clear always ===");

        var pool = Executors.newFixedThreadPool(1);

        // Fix 1: always clear in a finally. This is the minimum bar.
        pool.submit(() -> {
            try {
                CURRENT_USER.set(new Principal("acme-corp", "alice"));
                System.out.println("  request A: " + CURRENT_USER.get());
            } finally {
                CURRENT_USER.remove();        // <-- the non-negotiable part
                MDC.remove();
            }
        }).get();

        pool.submit(() -> System.out.println("  request B: " + CURRENT_USER.get() + "   (correctly null)")).get();

        // Fix 2: an ExecutionContext/Executor wrapper that propagates explicitly.
        // This is what Spring's TaskDecorator does, and what you need for @Async.
        Executor decorated = contextPropagating(pool);
        CURRENT_USER.set(new Principal("beta-inc", "bob"));
        var latch = new CountDownLatch(1);
        decorated.execute(() -> {
            System.out.println("  inside decorated executor: " + CURRENT_USER.get()
                    + "   (propagated from the submitting thread)");
            latch.countDown();
        });
        latch.await();
        CURRENT_USER.remove();

        pool.submit(() -> System.out.println("  pool thread after decorated task: "
                + CURRENT_USER.get() + "   (cleared, no bleed)")).get();
        pool.shutdown();
    }

    /** The TaskDecorator pattern: capture at submit, install for the task, clear after. */
    static Executor contextPropagating(Executor delegate) {
        return task -> {
            Principal captured = CURRENT_USER.get();
            Map<String, String> capturedMdc = new HashMap<>(MDC.get());
            delegate.execute(() -> {
                try {
                    if (captured != null) CURRENT_USER.set(captured);
                    MDC.set(capturedMdc);
                    task.run();
                } finally {
                    CURRENT_USER.remove();      // ALWAYS clear -- this is the bug-preventer
                    MDC.remove();
                }
            });
        };
    }

    // =====================================================================
    static void partD_virtualThreads() throws Exception {
        System.out.println("\n=== PART D: virtual threads ===");

        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 3; i++) {
                final int id = i;
                ex.submit(() -> {
                    Principal before = CURRENT_USER.get();
                    CURRENT_USER.set(new Principal("tenant-" + id, "user-" + id));
                    System.out.printf("  vthread task %d: saw %s before setting %s%n",
                            id, before, CURRENT_USER.get());
                    // Deliberately NOT removing -- and it does not matter.
                }).get();
            }
        }

        System.out.println("""
                  Every task got a FRESH virtual thread, so `before` is always null:
                  no bleed, even with no remove(). Virtual threads make ThreadLocal
                  SAFER, because there is no reuse.

                  But two new problems appear:
                    1. FOOTPRINT -- a million virtual threads each with a
                       ThreadLocalMap is real memory. Inheritable thread-locals are
                       COPIED per thread, which is worse.
                    2. ThreadLocal-as-a-cache stops working. A per-thread
                       SimpleDateFormat or reusable buffer was an optimisation that
                       relied on thread reuse; now it allocates per request.

                  Hence ScopedValue (JEP 446, preview in 21): immutable, lexically
                  scoped, inherited by structure rather than by copying, and unbound
                  automatically at the end of the block.

                      ScopedValue.where(USER, principal).run(() -> handle(req));""");
    }

    static long usedHeapMb() {
        var rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }
}
