/*
 * L04 — G1 humongous allocations.
 *
 * COMPILE FIRST:
 *   mkdir -p /tmp/l04 && javac -d /tmp/l04 problems/gc-labs/L04Humongous.java
 *
 * RUN AND COMPARE:
 *
 *   1. Default region size for a 512m heap (G1 picks 1m regions):
 *        java -Xmx512m -Xms512m -XX:+UseG1GC -Xlog:gc,gc+heap=info \
 *             -cp /tmp/l04 L04Humongous
 *
 *   2. See the humongous allocations explicitly:
 *        java -Xmx512m -Xms512m -XX:+UseG1GC -Xlog:gc,gc+humongous=debug \
 *             -cp /tmp/l04 L04Humongous
 *
 *   3. THE FIX -- bigger regions, so the same objects are no longer humongous:
 *        java -Xmx512m -Xms512m -XX:+UseG1GC -XX:G1HeapRegionSize=8m \
 *             -Xlog:gc,gc+humongous=debug -cp /tmp/l04 L04Humongous
 *
 *   4. THE BETTER FIX -- chunk the allocations (mode `chunked`):
 *        java -Xmx512m -Xms512m -XX:+UseG1GC -Xlog:gc \
 *             -cp /tmp/l04 L04Humongous chunked
 *
 * TASK
 *   1. What makes an object "humongous"? (Larger than HALF a region.)
 *   2. Find the region size G1 chose in run 1. Verify with:
 *        java -Xmx512m -XX:+UseG1GC -XX:+PrintFlagsFinal -version | grep G1HeapRegionSize
 *   3. Count the "(G1 Humongous Allocation)" GC causes in runs 1 and 3. Explain
 *      the difference.
 *   4. Why does a humongous object skip eden entirely, and why does that matter
 *      for the generational hypothesis?
 *   5. Compute the WASTE: a 3.5 MB object in 1 MB regions occupies how many
 *      regions? How many bytes are unusable? Check the program's arithmetic.
 *   6. In run 4, why is chunking better than raising the region size?
 */

import java.util.*;

public class L04Humongous {

    public static void main(String[] args) {
        boolean chunked = args.length > 0 && args[0].equals("chunked");

        long regionSize = regionSizeBytes();
        System.out.println("=== L04: humongous allocations ===");
        System.out.println("max heap           = " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.printf("G1HeapRegionSize   = %s%n", human(regionSize));
        System.out.printf("humongous threshold= larger than %s (half a region)%n", human(regionSize / 2));
        System.out.println("mode               = " + (chunked ? "CHUNKED (the fix)" : "ONE BIG ARRAY per request"));
        System.out.println();

        final int requests = 3000;
        // 3.5 MB: deliberately NOT a whole multiple of a 1 MB region, so the
        // wasted region tail is visible. With -XX:G1HeapRegionSize=8m it also
        // drops below the 4 MB humongous threshold entirely.
        final int payloadBytes = 7 * 512 * 1024;       // 3.5 MB "file upload"
        final int chunkBytes = 64 * 1024;              // 64 KB chunks

        System.out.printf("simulating %d requests, each handling a %s payload%n",
                requests, human(payloadBytes));
        boolean isHumongous = payloadBytes > regionSize / 2;
        if (!chunked && !isHumongous) {
            System.out.printf("  -> %s is NOT humongous with %s regions (threshold %s),%n",
                    human(payloadBytes), human(regionSize), human(regionSize / 2));
            System.out.println("     so these are ordinary allocations. Compare against the");
            System.out.println("     default region size to see the difference.");
        } else if (!chunked) {
            System.out.printf("  -> each %s array is HUMONGOUS (> %s), so it is allocated%n",
                    human(payloadBytes), human(regionSize / 2));
            System.out.printf("     directly into OLD GEN across %d contiguous regions%n",
                    (payloadBytes + regionSize - 1) / regionSize);
            long waste = ((payloadBytes + regionSize - 1) / regionSize) * regionSize - payloadBytes;
            System.out.printf("     wasting %d KB in the tail of the last region, per allocation%n",
                    waste / 1024);
        } else {
            System.out.printf("  -> split into %d x %d KB chunks, all ordinary eden allocations%n",
                    payloadBytes / chunkBytes, chunkBytes / 1024);
        }
        System.out.println();

        long gcBefore = totalGcCount();
        long gcTimeBefore = totalGcTimeMs();
        long t0 = System.nanoTime();
        long checksum = 0;

        for (int i = 0; i < requests; i++) {
            if (chunked) {
                // Process in chunks. Every chunk is small, short-lived, and dies in eden.
                int chunks = payloadBytes / chunkBytes;
                for (int c = 0; c < chunks; c++) {
                    byte[] chunk = new byte[chunkBytes];
                    chunk[0] = (byte) c;
                    checksum += chunk[0];
                }
            } else {
                // One giant array: a humongous allocation.
                byte[] payload = new byte[payloadBytes];
                payload[0] = (byte) i;
                checksum += payload[0];
            }

            if ((i + 1) % 500 == 0) {
                System.out.printf("  %d requests, GCs so far: %d%n", i + 1, totalGcCount() - gcBefore);
            }
        }

        double elapsedS = (System.nanoTime() - t0) / 1e9;
        long gcCount = totalGcCount() - gcBefore;
        long gcTime = totalGcTimeMs() - gcTimeBefore;

        System.out.println("\n--- results ---");
        System.out.printf("wall time     = %.2f s%n", elapsedS);
        System.out.printf("GC count      = %d%n", gcCount);
        System.out.printf("GC pause time = %d ms%n", gcTime);
        System.out.printf("GC overhead   = %.2f %%%n", 100.0 * gcTime / (elapsedS * 1000));
        System.out.println("checksum      = " + checksum);

        System.out.println("""

                --- WHAT TO NOTICE ---
                  Run this in both modes and compare GC count and overhead.

                  A HUMONGOUS object (larger than half a G1 region):
                    * is allocated DIRECTLY INTO OLD GEN, skipping eden -- so it never
                      gets the benefit of the generational hypothesis. A short-lived
                      3 MB buffer is treated like long-lived data.
                    * needs a run of CONTIGUOUS free regions. A fragmented heap can
                      fail to find them even with plenty of total free space, which
                      triggers a full GC and can end in OutOfMemoryError.
                    * WASTES the tail of its last region.
                    * shows up in the log as the GC cause "(G1 Humongous Allocation)".

                  TWO FIXES, and the order matters:
                    1. -XX:G1HeapRegionSize=8m -- the objects stop being humongous.
                       One flag, works immediately. But it is a workaround: bigger
                       regions coarsen G1's collection-set granularity, which can
                       lengthen pauses, and it does nothing about the underlying
                       "we allocate 3 MB per request" problem.
                    2. CHUNK the data -- stream it, use a pooled or direct buffer, or
                       process incrementally. Now the allocations are ordinary
                       short-lived eden objects that the copying collector reclaims for
                       almost nothing. This is the real fix.

                  Real-world sources: file upload/download buffers, reading a whole
                  response body into one byte[], large JSON/protobuf payloads, an
                  oversized ArrayList backing array after growth, on-heap ByteBuffers.""");
    }

    /** Formats bytes as KB or MB, keeping a decimal place when it is not a whole number. */
    static String human(long bytes) {
        long mb = 1024 * 1024;
        if (bytes >= mb) {
            double v = (double) bytes / mb;
            return v == Math.floor(v) ? String.format("%.0f MB", v) : String.format("%.1f MB", v);
        }
        double v = (double) bytes / 1024;
        return v == Math.floor(v) ? String.format("%.0f KB", v) : String.format("%.1f KB", v);
    }

    /** Reads the actual region size G1 chose, via the diagnostic MXBean. */
    static long regionSizeBytes() {
        try {
            var server = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            var mbean = new javax.management.ObjectName("com.sun.management:type=HotSpotDiagnostic");
            var vmOption = (javax.management.openmbean.CompositeData)
                    server.invoke(mbean, "getVMOption", new Object[]{"G1HeapRegionSize"},
                            new String[]{"java.lang.String"});
            return Long.parseLong((String) vmOption.get("value"));
        } catch (Exception e) {
            // Fall back to G1's heuristic: heap/2048, clamped to a power of 2 in [1m, 32m].
            long heuristic = Runtime.getRuntime().maxMemory() / 2048;
            long size = 1024 * 1024;
            while (size < heuristic && size < 32L * 1024 * 1024) size *= 2;
            return size;
        }
    }

    static long totalGcCount() {
        long n = 0;
        for (var b : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            if (b.getCollectionCount() > 0) n += b.getCollectionCount();
        }
        return n;
    }

    static long totalGcTimeMs() {
        long t = 0;
        for (var b : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            if (b.getCollectionTime() > 0) t += b.getCollectionTime();
        }
        return t;
    }
}
