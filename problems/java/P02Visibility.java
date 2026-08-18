/*
 * P02 — Visibility. The stop-flag that never stops.
 *
 * RUN:  java problems/java/P02Visibility.java
 *
 * TASK
 *   1. Predict: does the worker exit?
 *   2. Run it. (If it hangs at "waiting for worker", the bug reproduced.)
 *   3. Fix by making `running` volatile. Re-run.
 *   4. Now UNDO the fix and instead add a `System.out.println()` inside the loop.
 *      Does it terminate? Why does that "fix" it, and why is it not a fix?
 *   5. Run with `-Xint` (interpreter only). Does it still reproduce? Explain.
 *
 * WHY IT HAPPENS
 *   The JIT sees that nothing in the loop body writes `running`, so it hoists the
 *   read out of the loop:  if (running) { while (true) n++; }
 *   That is a legal transformation for a non-volatile field, because without a
 *   happens-before edge the JMM does not require the write in main() to ever
 *   become visible to the worker.
 *
 * NOTE
 *   The worker is a daemon thread so the JVM can exit even when the bug reproduces.
 */

public class P02Visibility {

    // BUG: not volatile.
    static boolean running = true;
    // FIX: static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        Thread worker = new Thread(() -> {
            long n = 0;
            while (running) {
                n++;
            }
            System.out.println("worker saw running=false and exited after " + n + " iterations");
        }, "worker");
        worker.setDaemon(true);   // so we can exit even if the bug reproduces
        worker.start();

        Thread.sleep(500);        // let the JIT compile the loop
        System.out.println("main: setting running = false");
        running = false;

        System.out.println("main: waiting for worker (3s timeout)");
        worker.join(3000);

        if (worker.isAlive()) {
            System.out.println("""

                    >>> BUG REPRODUCED: worker is STILL RUNNING.
                    >>> The write to `running` is not visible to it, and never will be.
                    >>> Make the field volatile and run again.""");
        } else {
            System.out.println("""

                    >>> Worker exited. Either the field is volatile (fixed),
                    >>> or you got lucky / the loop was not JIT-compiled.
                    >>> Try -Xcomp, or a longer initial sleep, to make it deterministic.""");
        }
    }
}
