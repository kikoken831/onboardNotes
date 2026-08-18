/*
 * L03 — The OutOfMemoryError taxonomy. Reproduce each one on demand.
 *
 * COMPILE FIRST (so the launcher's compiler does not share your tiny heap):
 *   mkdir -p /tmp/l03 && javac -d /tmp/l03 problems/gc-labs/L03OomVariants.java
 *
 * RUN each variant and read the message carefully -- the message IS the diagnosis:
 *
 *   heap:      java -Xmx64m                        -cp /tmp/l03 L03OomVariants heap
 *   overhead:  java -Xmx64m -XX:+UseParallelGC     -cp /tmp/l03 L03OomVariants overhead
 *                (EXPECT "Java heap space", not "GC overhead limit exceeded" --
 *                 see the honest caveat on gcOverhead() below. That is the lesson.)
 *   direct:    java -Xmx64m -XX:MaxDirectMemorySize=32m -cp /tmp/l03 L03OomVariants direct
 *   threads:   java -Xmx64m                        -cp /tmp/l03 L03OomVariants threads
 *   array:     java -Xmx64m                        -cp /tmp/l03 L03OomVariants array
 *   metaspace: java -Xmx64m -XX:MaxMetaspaceSize=16m -cp /tmp/l03 L03OomVariants metaspace
 *   stack:     java -Xss256k                       -cp /tmp/l03 L03OomVariants stack
 *
 * TASK
 *   1. For each, predict the exact message before running.
 *   2. Which of these are NOT fixed by increasing -Xmx? (At least four.)
 *   3. Which produce a heap dump with -XX:+HeapDumpOnOutOfMemoryError, and which
 *      leave you with nothing useful?
 *   4. `threads` and `direct` are OFF-HEAP. Explain how you would diagnose each
 *      in production. (Hint: -XX:NativeMemoryTracking=summary.)
 *   5. Which of these looks like exit code 137 / SIGKILL instead of a Java error,
 *      and why is that worse?
 */

import java.nio.ByteBuffer;
import java.util.*;

public class L03OomVariants {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "help";
        System.out.println("=== L03: OOM variant '" + mode + "' ===");
        System.out.println("max heap = " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB\n");

        try {
            switch (mode) {
                case "heap"      -> heapSpace();
                case "overhead"  -> gcOverhead();
                case "direct"    -> directBuffer();
                case "threads"   -> nativeThreads();
                case "array"     -> arraySize();
                case "metaspace" -> metaspace();
                case "stack"     -> stackOverflow(0);
                default          -> help();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\n>>> " + e.getClass().getName());
            System.out.println(">>> message: " + e.getMessage());
            explain(mode);
        } catch (StackOverflowError e) {
            System.out.println("\n>>> java.lang.StackOverflowError  (NOT an OutOfMemoryError)");
            explain(mode);
        }
    }

    // ------------------------------------------------------------------
    /** "Java heap space" -- the classic. Live set exceeds -Xmx. */
    static void heapSpace() {
        var list = new ArrayList<byte[]>();
        long mb = 0;
        while (true) {
            list.add(new byte[1024 * 1024]);
            mb++;
            if (mb % 16 == 0) System.out.println("  allocated and retained " + mb + " MB");
        }
    }

    // ------------------------------------------------------------------
    /**
     * "GC overhead limit exceeded" -- >98% of time in GC recovering <2% of heap
     * over 5 consecutive collections.
     *
     * HONEST CAVEAT: this one is genuinely hard to reproduce on demand, and modern
     * collectors often report "Java heap space" instead. The condition needs the
     * heap to be almost-but-not-quite full for a sustained period. ParallelGC
     * reports it most reliably, hence -XX:+UseParallelGC in the suggested command.
     * If you get "Java heap space", that is a legitimate outcome -- the two failures
     * have the same root cause and the same fix.
     */
    static void gcOverhead() {
        // Phase 1: fill the heap to ~97% with LIVE data. Small blocks, so we can get
        // very close to the ceiling without overshooting into "Java heap space".
        var retained = new ArrayList<byte[]>();
        long max = Runtime.getRuntime().maxMemory();
        System.out.println("  phase 1: filling the heap with live data...");
        try {
            // Measure the TRUE live set (post-GC), otherwise totalMemory-freeMemory
            // counts garbage and we overshoot straight into "Java heap space".
            while (true) {
                for (int i = 0; i < 500; i++) retained.add(new byte[4 * 1024]);
                System.gc();
                if (usedHeap() >= max * 96 / 100) break;
            }
        } catch (OutOfMemoryError e) {
            System.out.println("  (filled to the wall during phase 1)");
            throw e;
        }
        System.out.printf("  phase 1 done: %,d live blocks, heap %d%% used (post-GC)%n",
                retained.size(), 100 * usedHeap() / max);

        // Phase 2: churn TINY objects. They are collectible, so the heap never
        // overflows -- but each GC can only reclaim the sliver of free space left,
        // which is under the 2% threshold. That is the GC-overhead condition.
        System.out.println("  phase 2: churning in the remaining sliver...");
        long spins = 0;
        long deadline = System.nanoTime() + 20_000_000_000L;   // 20s cap so it cannot hang
        while (System.nanoTime() < deadline) {
            for (int i = 0; i < 1000; i++) {
                var tmp = new byte[64];
                tmp[0] = 1;
            }
            spins += 1000;
            if (spins % 5_000_000 == 0) {
                System.out.printf("  churn=%,d  heap used=%d%%%n", spins, 100 * usedHeap() / max);
            }
        }
        System.out.println("""

                  >>> Ran to the time limit without an OOM.
                  >>> That is a legitimate result: "GC overhead limit exceeded" is a
                  >>> heuristic (>98% of time in GC, <2% of heap reclaimed, over 5
                  >>> consecutive collections) and it is genuinely hard to hit on
                  >>> purpose -- the collector here is keeping up.
                  >>> In production you will see "Java heap space" far more often, and
                  >>> the root cause and the fix are identical. That IS the lesson:
                  >>> do not treat these two messages as different problems.""");
    }

    // ------------------------------------------------------------------
    /** "Direct buffer memory" -- OFF-HEAP. The Java heap stays nearly empty. */
    static void directBuffer() {
        var buffers = new ArrayList<ByteBuffer>();
        long mb = 0;
        while (true) {
            buffers.add(ByteBuffer.allocateDirect(1024 * 1024));
            mb++;
            if (mb % 8 == 0) {
                System.out.printf("  direct buffers: %d MB   (Java heap used: %d MB -- barely moving!)%n",
                        mb, usedHeap() / (1024 * 1024));
            }
        }
    }

    // ------------------------------------------------------------------
    /** "unable to create native thread" -- the OS refused. Stacks are OFF-HEAP. */
    static void nativeThreads() {
        var threads = new ArrayList<Thread>();
        try {
            while (true) {
                var t = new Thread(() -> {
                    try { Thread.sleep(Long.MAX_VALUE); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
                t.setDaemon(true);
                t.start();
                threads.add(t);
                if (threads.size() % 500 == 0) System.out.println("  platform threads: " + threads.size());
            }
        } finally {
            System.out.println("  reached " + threads.size() + " platform threads");
            System.out.println("""
                      Compare: virtual threads. P10 creates 50,000 of them in ~36ms
                      using a few MB. Each PLATFORM thread here reserves ~1 MB of
                      stack (-Xss) outside the Java heap, so a BIGGER -Xmx makes this
                      failure MORE likely on a fixed-memory box, not less.""");
            threads.forEach(Thread::interrupt);
        }
    }

    // ------------------------------------------------------------------
    /** "Requested array size exceeds VM limit" -- distinct from running out of heap. */
    static void arraySize() {
        System.out.println("  requesting an array of Integer.MAX_VALUE elements...");
        var huge = new int[Integer.MAX_VALUE];       // ~8 GB, and over the VM's array limit
        System.out.println("  never reached: " + huge.length);
    }

    // ------------------------------------------------------------------
    /**
     * "Metaspace" -- too many CLASSES, not too many objects. We generate classes
     * at runtime with a fresh classloader each time, as CGLIB/proxies/scripting do.
     */
    static void metaspace() {
        var loaders = new ArrayList<ClassLoader>();
        int n = 0;
        while (true) {
            var loader = new JunkLoader();
            loaders.add(loader);                       // retain, so metadata cannot be freed
            loader.define("Junk" + n, minimalClass("Junk" + n));
            if (++n % 200 == 0) {
                System.out.println("  generated classes: " + n
                        + "   (Java heap used: " + usedHeap() / (1024 * 1024) + " MB -- barely moving!)");
            }
        }
    }

    /** A ClassLoader that exposes defineClass, so we need no reflection or --add-opens. */
    static class JunkLoader extends ClassLoader {
        JunkLoader() { super(L03OomVariants.class.getClassLoader()); }
        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    /** Hand-assembled minimal class file: `public class <name> {}` targeting Java 8. */
    static byte[] minimalClass(String name) {
        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] superName = "java/lang/Object".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var out = new java.io.ByteArrayOutputStream();
        var d = new java.io.DataOutputStream(out);
        try {
            d.writeInt(0xCAFEBABE);
            d.writeShort(0);            // minor
            d.writeShort(52);           // major = Java 8
            d.writeShort(5);            // constant_pool_count = 5 (entries 1..4)
            d.writeByte(1); d.writeShort(nameBytes.length); d.write(nameBytes);   // #1 Utf8 name
            d.writeByte(7); d.writeShort(1);                                       // #2 Class -> #1
            d.writeByte(1); d.writeShort(superName.length); d.write(superName);   // #3 Utf8 Object
            d.writeByte(7); d.writeShort(3);                                       // #4 Class -> #3
            d.writeShort(0x0021);       // ACC_PUBLIC | ACC_SUPER
            d.writeShort(2);            // this_class = #2
            d.writeShort(4);            // super_class = #4
            d.writeShort(0);            // interfaces
            d.writeShort(0);            // fields
            d.writeShort(0);            // methods
            d.writeShort(0);            // attributes
            d.flush();
        } catch (java.io.IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    static class UncheckedIOException extends RuntimeException {
        UncheckedIOException(Throwable c) { super(c); }
    }

    // ------------------------------------------------------------------
    /** StackOverflowError -- per-thread stack, NOT the heap, NOT an OutOfMemoryError. */
    static int stackOverflow(int depth) {
        return stackOverflow(depth + 1) + 1;
    }

    // ------------------------------------------------------------------
    static void explain(String mode) {
        String text = switch (mode) {
            case "heap" -> """
                  "Java heap space": the live set exceeds -Xmx.
                    Fix: find the leak (L02), bound the caches, or raise -Xmx.
                    Diagnose: heap dump + MAT, or jcmd GC.class_histogram.
                    Heap dump on OOM: YES, very useful.""";
            case "overhead" -> """
                  "GC overhead limit exceeded": >98% of time in GC, <2% reclaimed,
                  over 5 consecutive collections. A pre-emptive failure so you do not
                  thrash forever -- you would have hit "Java heap space" eventually.
                    Same root causes and same fixes as "Java heap space".
                    -XX:-UseGCOverheadLimit disables it. Rarely a good idea.""";
            case "direct" -> """
                  "Direct buffer memory": OFF-HEAP, bounded by -XX:MaxDirectMemorySize
                  (which DEFAULTS to roughly -Xmx). Note the Java heap barely moved.
                    Sources: Netty, NIO, off-heap caches, compression codecs.
                    Direct buffers are freed via Cleaner, i.e. only after a GC notices
                    them -- so you can OOM off-heap with a nearly EMPTY heap, because
                    nothing is creating heap pressure to trigger that GC.
                    Diagnose: BufferPoolMXBean metrics, -XX:NativeMemoryTracking=summary.
                    Heap dump on OOM: useless -- the memory is not in the heap.""";
            case "threads" -> """
                  "unable to create native thread": the OS refused a thread.
                    Either ulimit -u / threads-max, or address space exhausted by
                    thread stacks (~1 MB each, -Xss).
                    COUNTERINTUITIVE: a BIGGER -Xmx makes this MORE likely, because the
                    heap and the stacks compete for the same address space / RAM.
                    Diagnose: jcmd Thread.print | grep -c tid, NMT's "Thread" category.
                    Fix: bound your pools (quiz 02 Q2), or use virtual threads.""";
            case "array" -> """
                  "Requested array size exceeds VM limit": the array length exceeds
                  what the VM can represent (~Integer.MAX_VALUE - 2), independent of
                  how much heap you have. Raising -Xmx does NOT help.
                    Fix: chunk the data. This often shows up reading a huge file or
                    response into one byte[].""";
            case "metaspace" -> """
                  "Metaspace": too many CLASSES, not too many objects.
                    Sources: classloader leaks -- repeated hot redeploys, CGLIB/dynamic
                    proxies, Groovy/JS scripting, per-tenant classloaders, some mocking
                    frameworks in long test runs.
                    Metaspace is UNBOUNDED by default: it will consume the host until
                    the kernel OOM-kills you. Always set -XX:MaxMetaspaceSize.
                    Diagnose: jcmd VM.classloader_stats, nonheap metaspace metrics.
                    Raising -Xmx does NOT help -- metaspace is not the heap.""";
            case "stack" -> """
                  StackOverflowError is NOT an OutOfMemoryError. It is the per-thread
                  stack (-Xss, default ~512KB-1MB), which lives OUTSIDE the heap.
                    Causes: unbounded recursion, or very deep call chains.
                    In Scala: a non-tail-recursive function on a large input. Use
                    @tailrec (which makes it a compile error if elimination fails), or
                    trampoline it. IO/ZIO flatMap chains are heap-interpreted, so they
                    are immune -- see quiz 06 D4.
                    Raising -Xmx does NOT help. -Xss does.""";
            default -> "  (no explanation for mode: " + mode + ")";
        };
        System.out.println("\n--- DIAGNOSIS ---");
        System.out.println(text);
    }

    static void help() {
        System.out.println("""
                Pick a mode:
                  heap       Java heap space
                  overhead   GC overhead limit exceeded
                  direct     Direct buffer memory        (OFF-HEAP)
                  threads    unable to create native thread (OFF-HEAP)
                  array      Requested array size exceeds VM limit
                  metaspace  Metaspace                   (OFF-HEAP)
                  stack      StackOverflowError          (not an OOM at all)

                See the header comment for the exact command line each one needs.

                THE POINT: only TWO of these are fixed by raising -Xmx. Reading the
                message is the whole first step of diagnosis, and "we got an OOM so
                let us add memory" is wrong more often than it is right.""");
    }

    static long usedHeap() {
        var rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}
