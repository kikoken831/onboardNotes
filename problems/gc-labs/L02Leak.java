/*
 * L02 — A memory leak, and the method for finding one.
 *
 * !! COMPILE THIS ONE FIRST !!
 *   `java L02Leak.java` runs the in-memory compiler on the SAME 128 MB heap, so
 *   when the leak exhausts the heap the launcher itself dies and you never see
 *   the diagnosis. Precompile so only your program is competing for the heap:
 *
 *     mkdir -p /tmp/l02 && javac -d /tmp/l02 problems/gc-labs/L02Leak.java
 *
 *   (That is itself a small lesson: -Xmx bounds the whole JVM's Java heap, and
 *    everything sharing that JVM shares the budget.)
 *
 * RUN:
 *   1. Watch it die:
 *        java -Xmx128m -Xms128m -Xlog:gc -cp /tmp/l02 L02Leak
 *
 *   2. Watch it live (the fix):
 *        java -Xmx128m -Xms128m -Xlog:gc -cp /tmp/l02 L02Leak bounded
 *
 *   3. Get a heap dump on the OOM and analyse it:
 *        java -Xmx128m -Xms128m -XX:+HeapDumpOnOutOfMemoryError \
 *             -XX:HeapDumpPath=/tmp/leak.hprof -cp /tmp/l02 L02Leak
 *
 *   4. While it runs, in another terminal (find the pid with `jps`):
 *        jcmd <pid> GC.class_histogram | head -20
 *        jstat -gcutil <pid> 500ms
 *
 * TASK
 *   1. Before running: predict which of the four "caches" leaks. All four look
 *      reasonable. Exactly three are unbounded.
 *   2. Run mode 1. Read the printed live-set trend. What distinguishes a LEAK
 *      from ordinary allocation in that trend? (Quiz 04 Q22 step 1.)
 *   3. Run mode 3, then open /tmp/leak.hprof in Eclipse MAT. Find:
 *        - the dominator tree: what retains the most?
 *        - the path to GC roots for the biggest object
 *        - the retained (not shallow) size
 *   4. Explain why `WeakHashMap` did not save `weakButLeaky`.
 *   5. Name the four classic leak shapes from quiz 04 Q22 and match them to the
 *      caches here.
 */

import java.util.*;
import java.util.concurrent.*;

public class L02Leak {

    record SessionKey(long id) {}

    /** A chunky per-session object, so the leak is visible quickly. */
    static class SessionData {
        final byte[] payload = new byte[16 * 1024];      // 16 KB
        final String label;
        final SessionKey backReference;                   // note this!
        SessionData(SessionKey k) { this.label = "session-" + k.id(); this.backReference = k; }
    }

    // ---- LEAK 1: the classic. A static map used as a cache, with no eviction. ----
    static final Map<SessionKey, SessionData> STATIC_CACHE = new ConcurrentHashMap<>();

    // ---- LEAK 2: a listener registry nobody unregisters from. ----
    static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    // ---- LEAK 3: WeakHashMap, defeated by a back-reference from value to key. ----
    static final Map<SessionKey, SessionData> WEAK_BUT_LEAKY = new WeakHashMap<>();

    // ---- NOT A LEAK: bounded LRU with real eviction. ----
    static final int MAX = 500;
    static final Map<SessionKey, SessionData> BOUNDED = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<SessionKey, SessionData> e) {
                    return size() > MAX;
                }
            });

    public static void main(String[] args) {
        boolean bounded = args.length > 0 && args[0].equals("bounded");
        System.out.println("=== L02: leak hunt ===");
        System.out.println("max heap = " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.println("mode     = " + (bounded ? "BOUNDED (the fix)" : "LEAKING"));
        System.out.println();
        System.out.printf("%8s  %12s  %12s  %12s%n", "requests", "postGC live", "cache size", "verdict");

        long request = 0;
        // In bounded mode we stop after a fixed number of requests, because the whole
        // point is that it NEVER dies -- an infinite loop would just run forever.
        final long limit = bounded ? 20_000 : Long.MAX_VALUE;
        try {
            while (request < limit) {
                request++;
                var key = new SessionKey(request);
                var data = new SessionData(key);

                if (bounded) {
                    BOUNDED.put(key, data);                     // evicts past MAX
                } else {
                    STATIC_CACHE.put(key, data);                // LEAK 1
                    LISTENERS.add(() -> data.payload[0] = 1);   // LEAK 2: closure captures `data`
                    WEAK_BUT_LEAKY.put(key, data);              // LEAK 3
                }

                // Every 200 "requests", report the post-GC live set. This is exactly
                // the measurement that distinguishes a leak from churn.
                if (request % 200 == 0) {
                    System.gc();                                 // demo only! See quiz 04 Q9.
                    sleep(30);
                    long liveMb = usedHeapMb();
                    long maxMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                    int cacheSize = bounded ? BOUNDED.size() : STATIC_CACHE.size();
                    String verdict = liveMb > maxMb * 0.8 ? "CRITICAL"
                            : liveMb > maxMb * 0.5 ? "climbing" : "ok";
                    // Print sparsely in bounded mode -- it runs a long time on purpose.
                    if (!bounded || request % 2000 == 0) {
                        System.out.printf("%8d  %10d MB  %12d  %12s%n", request, liveMb, cacheSize, verdict);
                    }
                }
            }

            // Only reachable in bounded mode.
            System.out.println("""

                    >>> Completed 20,000 requests with NO OutOfMemoryError.
                    >>> The post-GC live set stayed FLAT and the cache size stayed at its
                    >>> bound. That flat floor is what a healthy service looks like:
                    >>> total heap usage sawtooths, but the level after each collection
                    >>> returns to the same place.
                    >>>
                    >>> The fix was not a GC flag. It was an eviction policy.
                    >>> In production use Caffeine (size and/or time bounds, async loading,
                    >>> and metrics) rather than a hand-rolled LinkedHashMap.""");
        } catch (OutOfMemoryError e) {
            // FIRST, free the leaked memory. An OOM handler that allocates will itself
            // throw OOM -- which is a real lesson: after OutOfMemoryError the JVM is in
            // a degraded state and even error handling may fail. This is why
            // -XX:+ExitOnOutOfMemoryError is the right production setting (quiz 04 Q8).
            long leaked = STATIC_CACHE.size();
            STATIC_CACHE.clear();
            LISTENERS.clear();
            WEAK_BUT_LEAKY.clear();

            System.out.println("\n>>> OutOfMemoryError: " + e.getMessage());
            System.out.println(">>> after " + request + " requests, holding " + leaked + " sessions");
            System.out.println("""

                    --- DIAGNOSIS ---
                      The post-GC live set rose MONOTONICALLY. That is the signature of a
                      leak: a healthy service's post-GC live set is FLAT (sawtooth in
                      total usage, but the floor after each collection stays put).

                      The three leaks here, matched to quiz 04 Q22's list:
                        1. STATIC_CACHE     -- unbounded static collection. #1 cause in
                                               the wild. A "cache" without eviction is a
                                               memory leak with good intentions.
                        2. LISTENERS        -- registration without deregistration. The
                                               lambda CAPTURES `data`, so every session is
                                               retained by the closure even though the
                                               list only holds Runnables. Easy to miss in
                                               review because the list looks harmless.
                        3. WEAK_BUT_LEAKY   -- WeakHashMap holds the KEY weakly, but the
                                               VALUE strongly. Here SessionData holds a
                                               back-reference to its own SessionKey, so
                                               the key is still strongly reachable through
                                               the value, and the entry never clears.
                                               WeakHashMap does not save you from a
                                               value->key reference.

                      Compare with ThreadLocal (P08): weak KEY, strong VALUE -- the same
                      trap in a different structure.

                    --- WHAT TO DO NEXT ---
                      java -Xmx128m -XX:+HeapDumpOnOutOfMemoryError \\
                           -XX:HeapDumpPath=/tmp/leak.hprof -cp /tmp/l02 L02Leak
                      then open /tmp/leak.hprof in Eclipse MAT:
                        - Leak Suspects report
                        - Dominator tree, sorted by RETAINED size
                        - "Path to GC roots -> exclude weak/soft references"
                      Cheaper first step in production, no dump needed:
                        jcmd <pid> GC.class_histogram | head -20""");
        }
    }

    static long usedHeapMb() {
        var rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
