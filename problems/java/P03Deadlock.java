/*
 * P03 — Deadlock: reproduce, detect, fix.
 *
 * RUN:  java problems/java/P03Deadlock.java            # reproduces (hangs ~5s, then reports)
 *       java problems/java/P03Deadlock.java ordered    # fixed by lock ordering
 *       java problems/java/P03Deadlock.java trylock    # fixed by tryLock + backoff
 *
 * TASK
 *   1. Explain why the default mode deadlocks. Draw the wait-for graph.
 *   2. While it is hung, in another terminal:  jstack <pid> | grep -A 20 "deadlock"
 *      (find the pid with `jps`). Read what the JVM tells you.
 *   3. Implement `transferOrdered` yourself before looking at it.
 *   4. Why is the `trylock` fix vulnerable to LIVELOCK, and what makes the backoff
 *      randomised rather than fixed?
 *   5. What is the *best* fix here, that uses no locks at all? (Hint: what if the
 *      balance were immutable and the transfer produced a new pair of accounts?)
 */

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

public class P03Deadlock {

    static class Account {
        final int id;
        final ReentrantLock lock = new ReentrantLock();
        long balance;
        Account(int id, long balance) { this.id = id; this.balance = balance; }
    }

    // ---------- BUGGY: acquires locks in call order ----------
    static void transferDeadlocking(Account from, Account to, long amount) {
        synchronized (from) {
            sleep(20);                       // widen the window
            synchronized (to) {
                from.balance -= amount;
                to.balance += amount;
            }
        }
    }

    // ---------- FIX 1: global lock ordering by id ----------
    static void transferOrdered(Account from, Account to, long amount) {
        Account first  = from.id < to.id ? from : to;
        Account second = from.id < to.id ? to : from;
        // NOTE: if ids can be equal you must handle self-transfer separately.
        synchronized (first) {
            sleep(20);
            synchronized (second) {
                from.balance -= amount;
                to.balance += amount;
            }
        }
    }

    // ---------- FIX 2: tryLock with timeout + randomised backoff ----------
    static final AtomicInteger retries = new AtomicInteger();

    static boolean transferTryLock(Account from, Account to, long amount) {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (from.lock.tryLock()) {
                try {
                    sleep(20);
                    if (to.lock.tryLock()) {
                        try {
                            from.balance -= amount;
                            to.balance += amount;
                            return true;
                        } finally { to.lock.unlock(); }
                    }
                } finally { from.lock.unlock(); }
            }
            retries.incrementAndGet();
            // Randomised so two threads do not retry in lockstep forever (livelock).
            sleep(1 + ThreadLocalRandom.current().nextInt(10));
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "deadlock";
        var a = new Account(1, 1000);
        var b = new Account(2, 1000);

        var done = new CountDownLatch(2);
        Runnable aToB = () -> { run(mode, a, b, 100); done.countDown(); };
        Runnable bToA = () -> { run(mode, b, a, 100); done.countDown(); };

        var t1 = new Thread(aToB, "a->b");
        var t2 = new Thread(bToA, "b->a");
        t1.setDaemon(true); t2.setDaemon(true);
        t1.start(); t2.start();

        System.out.println("mode = " + mode + "; waiting up to 15s...");
        boolean finished = done.await(15, TimeUnit.SECONDS);

        if (finished) {
            System.out.printf("COMPLETED. a=%d b=%d total=%d (invariant: total must be 2000)%n",
                    a.balance, b.balance, a.balance + b.balance);
            if (mode.equals("trylock")) System.out.println("tryLock retries = " + retries.get());
        } else {
            System.out.println("""

                    >>> DEADLOCK. Neither thread finished in 15 seconds.
                    >>> Thread states below; then try `jstack <pid>` in another terminal
                    >>> while this is hung -- the JVM detects Java-level deadlocks for you.""");
            System.out.println("  " + t1.getName() + " state=" + t1.getState());
            System.out.println("  " + t2.getName() + " state=" + t2.getState());
            System.out.println("  (BLOCKED on a monitor = classic deadlock signature)");

            // Programmatic detection -- the same thing jstack reports.
            var mx = java.lang.management.ManagementFactory.getThreadMXBean();
            long[] deadlocked = mx.findDeadlockedThreads();
            if (deadlocked != null) {
                System.out.println("\nThreadMXBean.findDeadlockedThreads() found " + deadlocked.length + " threads:");
                for (var info : mx.getThreadInfo(deadlocked, true, true)) {
                    System.out.printf("  %s is blocked on %s owned by %s%n",
                            info.getThreadName(), info.getLockName(), info.getLockOwnerName());
                }
            }
        }
    }

    static void run(String mode, Account from, Account to, long amt) {
        for (int i = 0; i < 20; i++) {
            switch (mode) {
                case "ordered" -> transferOrdered(from, to, amt);
                case "trylock" -> transferTryLock(from, to, amt);
                default        -> transferDeadlocking(from, to, amt);
            }
        }
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
