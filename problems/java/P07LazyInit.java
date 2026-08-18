/*
 * P07 — Lazy initialisation: five approaches, four problems.
 *
 * RUN:  java problems/java/P07LazyInit.java
 *
 * TASK
 *   1. For each approach, decide: correct? lazy? how much synchronisation on the
 *      hot (already-initialised) read path?
 *   2. Explain precisely why `BrokenDCL` is broken. What reordering is legal?
 *   3. Why can't we just test our way to confidence about `BrokenDCL`?
 *   4. Which approach would you use in a Spring singleton? In a Scala object?
 *   5. BONUS: what does Scala's `lazy val` compile to, and what is its failure mode?
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Supplier;

public class P07LazyInit {

    /**
     * The thing being lazily created.
     *
     * IMPORTANT SUBTLETY: these fields are deliberately NOT final.
     *
     * If every field were `final`, JSR-133 final-field "freeze" semantics would
     * guarantee that any thread seeing a reference to a correctly-constructed
     * object also sees the correct final-field values -- EVEN IF the reference was
     * published through a data race. So broken DCL over an all-final-fields object
     * happens to be safe for those fields. That is a real subtlety worth knowing,
     * and it is NOT a reason to omit volatile: the moment someone adds one
     * non-final field, or a final field referencing a mutable object, it breaks.
     */
    static class Heavy {
        int a;
        int b;
        int[] data;

        Heavy() {
            // Pretend this is expensive.
            this.a = 1;
            this.b = 2;
            this.data = new int[]{1, 2, 3};
        }

        boolean isFullyConstructed() { return a == 1 && b == 2 && data != null && data.length == 3; }
    }

    // ============================================================
    // 1. EAGER. Correct, thread-safe (class-init lock), but not lazy.
    // ============================================================
    static class Eager {
        static final Heavy INSTANCE = new Heavy();
        static Heavy get() { return INSTANCE; }
    }

    // ============================================================
    // 2. SYNCHRONIZED ACCESSOR. Correct and lazy, but every read pays for a lock.
    // ============================================================
    static class SyncAccessor {
        private static Heavy instance;
        static synchronized Heavy get() {
            if (instance == null) instance = new Heavy();
            return instance;
        }
    }

    // ============================================================
    // 3. BROKEN double-checked locking. THE classic bug.
    // ============================================================
    static class BrokenDCL {
        private static Heavy instance;         // <-- missing volatile
        static Heavy get() {
            if (instance == null) {                       // 1st check, unsynchronised
                synchronized (BrokenDCL.class) {
                    if (instance == null) {               // 2nd check
                        instance = new Heavy();
                        // `new Heavy()` is really:
                        //    1. allocate memory
                        //    2. run the constructor (write a, b, data)
                        //    3. publish the reference into `instance`
                        // The JMM permits 3 to become visible BEFORE 2. Another thread
                        // can then pass the 1st check and return a HALF-BUILT object.
                    }
                }
            }
            return instance;
        }
    }

    // ============================================================
    // 4. CORRECT double-checked locking (volatile).
    // ============================================================
    static class CorrectDCL {
        private static volatile Heavy instance;
        static Heavy get() {
            Heavy result = instance;                 // read the volatile once
            if (result == null) {
                synchronized (CorrectDCL.class) {
                    result = instance;
                    if (result == null) {
                        result = new Heavy();
                        instance = result;           // volatile write = release fence
                    }
                }
            }
            return result;
        }
    }

    // ============================================================
    // 5. HOLDER IDIOM. Correct, lazy, zero synchronisation on the read path.
    //    THE ANSWER for a static singleton.
    // ============================================================
    static class Holder {
        private Holder() {}
        private static class Lazy {
            // The JVM guarantees class initialisation happens exactly once,
            // under its own lock, with proper happens-before to every reader.
            static final Heavy INSTANCE = new Heavy();
        }
        static Heavy get() { return Lazy.INSTANCE; }
    }

    // ============================================================
    // 6. For an INSTANCE field (not static), the holder trick does not apply.
    //    Use a memoizing supplier: one volatile read on the hot path.
    // ============================================================
    static class Memoized<T> implements Supplier<T> {
        private final Supplier<T> delegate;
        private volatile T value;                 // volatile: safe publication

        Memoized(Supplier<T> delegate) { this.delegate = delegate; }

        public T get() {
            T result = value;                     // one volatile read
            if (result == null) {
                synchronized (this) {
                    result = value;
                    if (result == null) {
                        result = delegate.get();
                        value = result;           // volatile write = release
                    }
                }
            }
            return result;
        }
    }

    // ============================================================
    public static void main(String[] args) throws Exception {
        System.out.println("=== Correctness / laziness / hot-path cost ===");
        System.out.printf("%-22s %-9s %-7s %s%n", "approach", "correct?", "lazy?", "hot read path");
        System.out.printf("%-22s %-9s %-7s %s%n", "Eager",         "yes", "NO",  "plain static read (free)");
        System.out.printf("%-22s %-9s %-7s %s%n", "SyncAccessor",  "yes", "yes", "monitor acquire EVERY read");
        System.out.printf("%-22s %-9s %-7s %s%n", "BrokenDCL",     "NO",  "yes", "plain read (but unsafe)");
        System.out.printf("%-22s %-9s %-7s %s%n", "CorrectDCL",    "yes", "yes", "one volatile read (cheap)");
        System.out.printf("%-22s %-9s %-7s %s%n", "Holder",        "yes", "yes", "plain static read (free)");

        System.out.println("\n=== Attempting to observe the BrokenDCL bug ===");
        int rounds = 200_000;
        int torn = raceForTornObject(rounds);
        System.out.println("  " + rounds + " racing initialisations, torn objects observed: " + torn);
        System.out.println("""
                  On x86/ARM with HotSpot you will almost certainly see ZERO. That does
                  NOT mean the code is correct:
                    - x86 is strongly ordered (TSO), so the store reordering is rare.
                    - The JIT needs to be warm, and each class initialises only once,
                      so you get one shot per classloader.
                    - The window is nanoseconds wide.
                  It is legal per the JMM, it has been observed on weakly-ordered
                  hardware (Alpha, some ARM/PowerPC), and "I could not reproduce it"
                  is not an argument. Use jcstress if you want to argue about the JMM
                  with evidence. THIS is why you reason instead of testing.""");

        System.out.println("\n=== All approaches produce a valid object (single-threaded sanity) ===");
        check("Eager", Eager.get());
        check("SyncAccessor", SyncAccessor.get());
        check("BrokenDCL", BrokenDCL.get());
        check("CorrectDCL", CorrectDCL.get());
        check("Holder", Holder.get());
        var memo = new Memoized<>(Heavy::new);
        check("Memoized", memo.get());
        System.out.println("  Memoized returns the same instance twice: " + (memo.get() == memo.get()));

        System.out.println("\n=== Laziness proof: Holder does not initialise until first get() ===");
        System.out.println("  (see LazyProof output below)");
        System.out.println("  about to call LazyProof.get()...");
        LazyProof.get();
    }

    static class LazyProof {
        private static class Inner {
            static { System.out.println("  >>> Inner class initialised NOW (not at JVM startup)"); }
            static final Object INSTANCE = new Object();
        }
        static Object get() { return Inner.INSTANCE; }
    }

    static void check(String name, Heavy h) {
        System.out.printf("  %-14s fully constructed: %s%n", name, h.isFullyConstructed());
    }

    /**
     * Races many threads through a fresh BrokenDCL-shaped holder, looking for a
     * partially-constructed object. Uses a new holder per round so initialisation
     * actually races each time.
     */
    static int raceForTornObject(int rounds) throws Exception {
        var torn = new AtomicInteger();
        int threads = Runtime.getRuntime().availableProcessors();
        var pool = Executors.newFixedThreadPool(threads);

        for (int r = 0; r < rounds; r++) {
            var box = new UnsafeBox();
            var gate = new CountDownLatch(1);
            var done = new CountDownLatch(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        gate.await();
                        Heavy h = box.get();
                        if (h != null && !h.isFullyConstructed()) torn.incrementAndGet();
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    finally { done.countDown(); }
                });
            }
            gate.countDown();
            done.await();
        }
        pool.shutdown();
        return torn.get();
    }

    /** Instance-level version of BrokenDCL so we can create a fresh one per round. */
    static class UnsafeBox {
        private Heavy instance;              // not volatile -- the bug
        Heavy get() {
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) instance = new Heavy();
                }
            }
            return instance;
        }
    }
}
