/*
 * L01 — Allocation rate, GC frequency, and the effect of heap size.
 *
 * This is the lab where you learn to READ A GC LOG. Run all five commands and
 * compare. The program does the same work every time; only the JVM flags change.
 *
 * ---------------------------------------------------------------------------
 * RUN THESE IN ORDER, and keep the output:
 *
 * 1. Baseline, see the GC lines:
 *      java -Xmx256m -Xms256m -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *
 * 2. Small heap -- many more collections:
 *      java -Xmx64m -Xms64m -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *
 * 3. Big heap -- far fewer collections, better throughput:
 *      java -Xmx1g -Xms1g -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *
 * 4. Retention ON -- objects survive, so promotion and old-gen work appear:
 *      java -Xmx256m -Xms256m -Xlog:gc problems/gc-labs/L01AllocationRate.java retain
 *
 * 5. Collector comparison (same heap, same work):
 *      java -Xmx512m -Xms512m -XX:+UseSerialGC   -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *      java -Xmx512m -Xms512m -XX:+UseParallelGC -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *      java -Xmx512m -Xms512m -XX:+UseG1GC       -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *      java -Xmx512m -Xms512m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc problems/gc-labs/L01AllocationRate.java
 *
 * 6. Deeper detail once you can read the basics:
 *      java -Xmx256m -Xms256m -Xlog:gc,gc+heap=info,gc+age=trace problems/gc-labs/L01AllocationRate.java retain
 *
 * ---------------------------------------------------------------------------
 * QUESTIONS TO ANSWER
 *   1. In run 1, pick one GC line. Identify: cause, before->after, total heap,
 *      pause. How many MB did it reclaim? What fraction of `before` survived?
 *   2. Compare runs 2 and 3. How does GC COUNT scale with heap? How does MEAN
 *      PAUSE scale? Why do they move in opposite directions, and why does total
 *      overhead still fall?
 *   3. Run 2 and 3 BOTH with and without `retain`. You should find that more heap
 *      barely helps throughput in churn mode but helps a lot in retain mode.
 *      Explain why. (This is the most important result in the lab.)
 *   4. In run 4 (retain), what appears in the log that was absent in run 1?
 *      Find the first "Pause Young (Concurrent Start)" and explain what triggered it.
 *   5. In run 5, which collector has the shortest pauses? The best throughput?
 *      Does the ranking match what quiz 05 Q1 predicts?
 *   6. Compute GC overhead as a percentage for each run (the program prints it).
 *      Which runs are above 5%?
 *   7. Why does the reported allocation rate barely change between runs, while
 *      GC count changes enormously?
 */

import java.util.*;

public class L01AllocationRate {

    /** A typical short-lived request-scoped object graph. */
    record Order(long id, String customer, List<String> items, byte[] payload) {}

    static Order makeOrder(long i) {
        return new Order(
                i,
                "customer-" + (i % 1000),
                List.of("item-a", "item-b", "item-c"),
                new byte[512]);
    }

    public static void main(String[] args) throws Exception {
        boolean retain = args.length > 0 && args[0].equals("retain");

        long iterations = 30_000_000L;
        System.out.println("=== L01: allocation rate ===");
        System.out.println("max heap        = " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.println("collector       = " + collectorName());
        System.out.println("mode            = " + (retain ? "RETAIN (10% of objects survive)" : "churn (all garbage)"));
        System.out.println("iterations      = " + String.format("%,d", iterations));
        System.out.println();

        // Only used in retain mode: a bounded "cache" that forces promotion.
        List<Order> retained = new ArrayList<>();
        long retainEvery = 10;                       // keep 10%
        int retainCap = 20_000;

        long gcCountBefore = totalGcCount();
        long gcTimeBefore = totalGcTimeMs();
        long t0 = System.nanoTime();
        long checksum = 0;

        for (long i = 0; i < iterations; i++) {
            Order o = makeOrder(i);
            checksum += o.payload().length + o.customer().length();

            if (retain && i % retainEvery == 0) {
                retained.add(o);
                // Bound it -- a real cache with eviction. Remove from the FRONT so the
                // survivors are long-lived enough to be promoted to old gen.
                if (retained.size() > retainCap) retained.subList(0, retainCap / 2).clear();
            }
        }

        long elapsedNs = System.nanoTime() - t0;
        long gcCount = totalGcCount() - gcCountBefore;
        long gcTime = totalGcTimeMs() - gcTimeBefore;
        double elapsedS = elapsedNs / 1e9;

        // Each Order allocates: record (~32B) + customer String (~48B) + byte[512] (~528B)
        // + the List.of (~40B). Roughly 650 bytes; measured below for real.
        long approxBytesPerIteration = 650;
        double allocatedGb = iterations * approxBytesPerIteration / 1e9;

        System.out.println("--- results ---");
        System.out.printf("wall time            = %.2f s%n", elapsedS);
        System.out.printf("approx allocated     = %.2f GB%n", allocatedGb);
        System.out.printf("approx alloc rate    = %.2f GB/s   <-- the number that drives GC frequency%n",
                allocatedGb / elapsedS);
        System.out.printf("GC count             = %d%n", gcCount);
        System.out.printf("total GC pause time  = %d ms%n", gcTime);
        System.out.printf("GC overhead          = %.2f %%   <-- healthy is < 5%%%n",
                100.0 * gcTime / (elapsedS * 1000));
        System.out.printf("throughput           = %.2f M iterations/s%n", iterations / elapsedS / 1e6);
        if (gcCount > 0) {
            System.out.printf("mean pause           = %.2f ms%n", (double) gcTime / gcCount);
            System.out.printf("time between GCs     = %.1f ms%n", elapsedS * 1000 / gcCount);
        }
        if (retain) System.out.printf("retained objects     = %d%n", retained.size());
        System.out.println("checksum             = " + checksum + " (prevents dead-code elimination)");

        System.out.println("""

                --- WHAT TO NOTICE ---
                  * Allocation RATE is roughly constant across heap sizes: the program
                    allocates the same bytes either way.
                  * GC COUNT is inversely proportional to heap size. 4x the heap gives
                    you ~1/4 the collections, because frequency = alloc_rate/eden_size.
                  * MEAN PAUSE goes UP with heap size (more survivors to copy per
                    collection) while COUNT goes down faster -- so total GC overhead
                    still falls. Averages hide this; look at both.
                  * Whether a bigger heap improves THROUGHPUT depends on whether GC
                    was actually costing you anything:
                      - churn mode: almost nothing survives, so GC is already nearly
                        free. More heap does NOT speed things up, and may slightly slow
                        them (a larger eden has worse cache locality).
                      - retain mode: objects survive and get promoted, so GC overhead
                        is large at a small heap. Here more heap is a BIG throughput win.
                    Run both and compare -- this is the single most useful thing in
                    this lab, because it shows why "just add heap" is sometimes the
                    right fix and sometimes does nothing.
                  * Cost tracks the LIVE SET, not the garbage. A young GC over 4 GB of
                    dead objects is nearly free. Quiz 04 Q1.""");
    }

    static String collectorName() {
        var names = new ArrayList<String>();
        for (var bean : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            names.add(bean.getName());
        }
        return String.join(" + ", names);
    }

    static long totalGcCount() {
        long n = 0;
        for (var bean : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            long c = bean.getCollectionCount();
            if (c > 0) n += c;
        }
        return n;
    }

    static long totalGcTimeMs() {
        long t = 0;
        for (var bean : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            long ms = bean.getCollectionTime();
            if (ms > 0) t += ms;
        }
        return t;
    }
}
