/*
 * P06 — Producer/consumer: find the bugs, then delete the whole thing.
 *
 * RUN:  java problems/java/P06ProducerConsumer.java
 *
 * TASK
 *   1. READ `BuggyQueue` below and find FOUR bugs before running anything.
 *      Write them down. (Hint: one is about `if`, one is about `notify`,
 *      one is about interruption, one is about bounds.)
 *   2. Run the program. Which bug actually manifests?
 *   3. Implement `Exercise1_ConditionQueue` using ReentrantLock + two Conditions.
 *      The test harness will verify it.
 *   4. Then note how short `BlockingQueueVersion` is, and internalise the lesson.
 *
 * WHY IT MATTERS
 *   You will not write wait/notify at work. You WILL be asked about it, because it
 *   is the cleanest way to test whether you understand monitors and lost wakeups.
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class P06ProducerConsumer {

    interface Queue1<T> {
        void put(T item) throws InterruptedException;
        T take() throws InterruptedException;
    }

    // =============================================================
    // THE BUGGY ONE. Four defects. Find them before reading SOLUTIONS.md.
    // =============================================================
    static class BuggyQueue<T> implements Queue1<T> {
        private final LinkedList<T> items = new LinkedList<>();
        private final int capacity;

        BuggyQueue(int capacity) { this.capacity = capacity; }

        public synchronized void put(T item) throws InterruptedException {
            // BUG A: `if` instead of `while` -- no re-check after wakeup.
            if (items.size() == capacity) {
                wait();
            }
            items.addLast(item);
            // BUG B: notify() wakes ONE arbitrary waiter. It may wake another
            //        producer instead of a consumer -> the signal is lost forever.
            notify();
        }

        @SuppressWarnings("unused")
        public synchronized T take() throws InterruptedException {
            // BUG A again.
            if (items.isEmpty()) {
                wait();
            }
            // BUG C: if we woke spuriously, or another consumer took the item,
            //        the list is empty and this throws NoSuchElementException.
            T item = items.removeFirst();
            notify();   // BUG B again
            return item;
        }
        // BUG D (design): a caller who swallows InterruptedException makes this
        //        queue uncancellable. See the harness for how that plays out.

        synchronized int size() { return items.size(); }
    }

    // =============================================================
    // CORRECT with wait/notifyAll.
    // =============================================================
    static class CorrectMonitorQueue<T> implements Queue1<T> {
        private final ArrayDeque<T> items = new ArrayDeque<>();
        private final int capacity;

        CorrectMonitorQueue(int capacity) { this.capacity = capacity; }

        public synchronized void put(T item) throws InterruptedException {
            while (items.size() == capacity) {   // FIX A: loop, always re-check
                wait();
            }
            items.addLast(item);
            notifyAll();                          // FIX B: wake everyone
        }

        public synchronized T take() throws InterruptedException {
            while (items.isEmpty()) {             // FIX A
                wait();
            }
            T item = items.removeFirst();
            notifyAll();                          // FIX B
            return item;
        }
    }

    // =============================================================
    // EXERCISE 1: implement this with ReentrantLock + two Conditions.
    // Why two? So a producer's signal only wakes consumers -- no thundering herd,
    // no wasted wakeups. This is what ArrayBlockingQueue actually does.
    // =============================================================
    static class Exercise1_ConditionQueue<T> implements Queue1<T> {
        private final ArrayDeque<T> items = new ArrayDeque<>();
        private final int capacity;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull  = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        Exercise1_ConditionQueue(int capacity) { this.capacity = capacity; }

        public void put(T item) throws InterruptedException {
            // TODO: lock; while full -> notFull.await(); add; notEmpty.signal(); unlock in finally
            throw new UnsupportedOperationException("EXERCISE: implement me");
        }

        public T take() throws InterruptedException {
            // TODO: lock; while empty -> notEmpty.await(); remove; notFull.signal(); unlock in finally
            throw new UnsupportedOperationException("EXERCISE: implement me");
        }
    }

    // =============================================================
    // What you should actually write in production.
    // =============================================================
    static class BlockingQueueVersion<T> implements Queue1<T> {
        private final BlockingQueue<T> q;
        BlockingQueueVersion(int capacity) { this.q = new ArrayBlockingQueue<>(capacity); }
        public void put(T item) throws InterruptedException { q.put(item); }
        public T take() throws InterruptedException { return q.take(); }
    }

    // =============================================================
    public static void main(String[] args) throws Exception {
        // The random stress test below often does NOT catch the bugs -- which is
        // itself the lesson. So first, force the exact interleaving.
        deterministicBugDemo();

        System.out.println("\n\n########## STRESS TEST ##########");
        System.out.println("(3 producers, 3 consumers, 600 items, capacity 1)");
        test("BuggyQueue", new BuggyQueue<>(1));
        test("CorrectMonitorQueue", new CorrectMonitorQueue<>(1));
        test("BlockingQueueVersion", new BlockingQueueVersion<>(1));
        test("Exercise1_ConditionQueue", new Exercise1_ConditionQueue<>(1));

        System.out.println("""

                LESSON
                  The bounded-buffer problem has been solved correctly since 2004.
                  ArrayBlockingQueue is 300 lines of carefully reviewed code that
                  handles fairness, interruption, and cancellation. Your hand-rolled
                  monitor is not going to beat it -- but you should be able to explain
                  every line of it.

                  Note that BuggyQueue may well PASS the stress test above. A passing
                  concurrency test proves almost nothing: the bug needs a specific
                  interleaving that a fast machine with idle cores rarely produces.
                  This is why you reason about the code, and why jcstress exists.""");
    }

    /**
     * Forces the two interleavings that the stress test only finds by luck.
     * Both scenarios get every waiter parked FIRST, then wake them with one notify().
     */
    static void deterministicBugDemo() throws Exception {
        System.out.println("########## DETERMINISTIC BUG DEMO ##########");

        // ---- Scenario 1: `if` + notify() -> removeFirst() on an empty queue ----
        System.out.println("\n=== Scenario 1: two consumers waiting, ONE item produced ===");
        var q1 = new BuggyQueue<Integer>(10);
        var errors = new CopyOnWriteArrayList<Throwable>();
        var consumers = new ArrayList<Thread>();
        for (int i = 0; i < 2; i++) {
            var t = new Thread(() -> {
                try { q1.take(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (Throwable ex) { errors.add(ex); }
            }, "consumer-" + i);
            t.setDaemon(true);
            consumers.add(t);
            t.start();
        }
        Thread.sleep(300);                      // both consumers are now parked in wait()
        System.out.println("  both consumers parked in wait(); producing exactly 1 item...");
        q1.put(42);
        Thread.sleep(300);

        if (errors.isEmpty()) {
            System.out.println("  (no exception this time -- rerun; but see the reasoning below)");
        } else {
            System.out.println("  >>> BUG A+B REPRODUCED: " + errors.get(0));
            System.out.println("      C1 was woken, took the item, and called notify() again.");
            System.out.println("      That woke C2, which -- using `if`, not `while` -- did NOT");
            System.out.println("      re-check emptiness and called removeFirst() on an empty list.");
        }

        // ---- Scenario 2: `if` + notify() -> capacity violation ----
        System.out.println("\n=== Scenario 2: two producers waiting on a FULL queue of capacity 1 ===");
        var q2 = new BuggyQueue<Integer>(1);
        q2.put(1);                              // now full
        var producers = new ArrayList<Thread>();
        for (int i = 0; i < 2; i++) {
            final int v = 100 + i;
            var t = new Thread(() -> {
                try { q2.put(v); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "producer-" + i);
            t.setDaemon(true);
            producers.add(t);
            t.start();
        }
        Thread.sleep(300);                      // both producers parked in wait()
        System.out.println("  queue full (size=" + q2.size() + ", capacity=1), both producers parked.");
        System.out.println("  taking ONE item to free ONE slot...");
        q2.take();
        Thread.sleep(300);

        int finalSize = q2.size();
        System.out.println("  final size = " + finalSize + " (capacity is 1)");
        if (finalSize > 1) {
            System.out.println("  >>> BUG A+B REPRODUCED: the bound was silently violated.");
            System.out.println("      P1 woke, added, and notified -- waking P2, which did not");
            System.out.println("      re-check fullness and added anyway. A 'bounded' queue that");
            System.out.println("      is not bounded is an unbounded queue: see quiz 02 Q2.");
        } else {
            System.out.println("  (not reproduced this run -- rerun it)");
        }
    }

    /** 3 producers, 3 consumers, 200 items each. Asserts nothing is lost or duplicated. */
    static void test(String name, Queue1<Integer> q) throws Exception {
        System.out.println("\n=== " + name + " ===");
        final int producers = 3, consumers = 3, perProducer = 200;
        final int total = producers * perProducer;

        var produced = new AtomicInteger();
        var consumed = new AtomicInteger();
        // Ticket counter: exactly `total` take() calls happen across all consumers,
        // so no consumer is left blocked on an empty queue at the end.
        var tickets = new AtomicInteger(total);
        var seen = ConcurrentHashMap.<Integer>newKeySet();
        var errors = new CopyOnWriteArrayList<String>();
        var pool = Executors.newFixedThreadPool(producers + consumers);

        for (int p = 0; p < producers; p++) {
            final int base = p * perProducer;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perProducer; i++) { q.put(base + i); produced.incrementAndGet(); }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (Throwable t) { errors.add("producer: " + t); }
            });
        }
        for (int c = 0; c < consumers; c++) {
            pool.submit(() -> {
                try {
                    while (tickets.getAndDecrement() > 0) {
                        Integer v = q.take();
                        consumed.incrementAndGet();
                        if (!seen.add(v)) errors.add("DUPLICATE item " + v);
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (Throwable t) { errors.add("consumer: " + t); }
            });
        }

        pool.shutdown();
        boolean finished = pool.awaitTermination(5, TimeUnit.SECONDS);
        if (!finished) pool.shutdownNow();

        System.out.printf("  produced=%d consumed=%d distinct=%d%n", produced.get(), consumed.get(), seen.size());
        if (!finished)       System.out.println("  >>> HUNG (did not finish in 5s) -- lost wakeup or deadlock");
        if (!errors.isEmpty()) {
            System.out.println("  >>> " + errors.size() + " error(s); first few:");
            errors.stream().distinct().limit(3).forEach(e -> System.out.println("      " + e));
        }
        if (finished && errors.isEmpty() && seen.size() == total) System.out.println("  PASS");
    }
}
