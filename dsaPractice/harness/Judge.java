import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The judge.
 *
 * Usage (the `judge` CLI supplies these; you never type it by hand):
 *   java Judge &lt;slug&gt; &lt;refDir&gt; &lt;solDir&gt; &lt;mode&gt; &lt;iterations&gt; &lt;seed&gt;
 *
 * modes: fixed | stress | verify | time
 *
 * Reference and solution get SEPARATE classloaders, both delegating to the harness
 * loader as parent. Without that, a helper class you happen to call `Node` and one the
 * reference calls `Node` are the same name in the same default package, and whichever
 * loads first silently wins. Parent-first delegation keeps ListNode/TreeNode shared
 * (they must be, they cross the boundary) while keeping everything else separate.
 */
public final class Judge {

    static final String AC   = "ACCEPTED";
    static final String WA   = "WRONG ANSWER";
    static final String RE   = "RUNTIME ERROR";
    static final String TLE  = "TIME LIMIT EXCEEDED";
    static final String CE   = "COMPILE/LOAD ERROR";
    static final String RBUG = "REFERENCE BUG";

    private final Spec spec;
    private final Object refInstance;
    private final Object solInstance;
    private final Method refMethod;
    private final Method solMethod;

    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("usage: Judge <slug> <refDir> <solDir> <mode> <iters> <seed>");
            System.exit(2);
        }
        String slug   = args[0];
        String refDir = args[1];
        String solDir = args[2];
        String mode   = args[3];
        int iters     = Integer.parseInt(args[4]);
        long seed     = Long.parseLong(args[5]);

        Judge judge;
        try {
            judge = new Judge(refDir, solDir, "verify".equals(mode));
        } catch (Throwable t) {
            Report.fail(CE, "could not load classes for " + slug + ": " + root(t));
            System.exit(1);
            return;
        }

        switch (mode) {
            case "verify": judge.verify();         break;
            case "fixed":  judge.run(0, seed);     break;
            case "time":   judge.timeCurve(seed);  break;
            default:       judge.run(iters, seed); break;
        }
    }

    // ------------------------------------------------------------------ setup

    private Judge(String refDir, String solDir, boolean refOnly) throws Exception {
        ClassLoader parent = Judge.class.getClassLoader();

        URLClassLoader refLoader = new URLClassLoader(
            new URL[]{ new File(refDir).toURI().toURL() }, parent);
        Class<?> refClass = Class.forName("Ref", true, refLoader);
        this.refInstance = instantiate(refClass);
        this.spec = ((Problem) refInstance).spec();
        this.refMethod = find(refClass, spec);

        if (refOnly) {
            this.solInstance = null;
            this.solMethod = null;
        } else {
            URLClassLoader solLoader = new URLClassLoader(
                new URL[]{ new File(solDir).toURI().toURL() }, parent);
            Class<?> solClass = Class.forName("Solution", true, solLoader);
            this.solInstance = instantiate(solClass);
            this.solMethod = find(solClass, spec);
        }
    }

    /**
     * LeetCode's convention is a package-private `class Solution`, and the reference
     * follows suit. Because each side has its own classloader they land in different
     * runtime packages from Judge, so package-private access is denied unless we open
     * the constructor explicitly.
     */
    private static Object instantiate(Class<?> c) throws Exception {
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    private static Method find(Class<?> c, Spec spec) throws NoSuchMethodException {
        Class<?>[] sig = new Class<?>[spec.params.length];
        for (int i = 0; i < sig.length; i++) sig[i] = spec.params[i].javaType();
        try {
            Method m = c.getDeclaredMethod(spec.method, sig);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException primary) {
            // Fall back to name + arity. List<Integer> vs int[] is a legitimate variation
            // and erasure means the reflective signature cannot distinguish list types.
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(spec.method) && m.getParameterCount() == sig.length) {
                    m.setAccessible(true);
                    return m;
                }
            }
            throw primary;
        }
    }

    // ------------------------------------------------------------- invocation

    private static final class Outcome {
        Object judged;
        Throwable thrown;
        long nanos;
    }

    private Outcome invoke(Method m, Object target, Object[] args) {
        Outcome o = new Outcome();
        Object[] copy = Deep.copyAll(args);
        long t0 = System.nanoTime();
        try {
            Object ret = m.invoke(target, copy);
            o.nanos = System.nanoTime() - t0;
            o.judged = spec.judged(copy, ret);
        } catch (InvocationTargetException e) {
            o.nanos = System.nanoTime() - t0;
            o.thrown = e.getCause() != null ? e.getCause() : e;
        } catch (Throwable t) {
            o.nanos = System.nanoTime() - t0;
            o.thrown = t;
        }
        return o;
    }

    // ------------------------------------------------------------------ modes

    /** Check the reference against every pinned example. This guards against MY bugs. */
    private void verify() {
        if (spec.examples.isEmpty()) {
            Report.warn("no pinned examples — nothing guards this problem against a bad reference");
            Report.ok("verify", "0 examples");
            return;
        }
        int n = 0;
        for (Spec.Example ex : spec.examples) {
            Object[] args = Codec.parseArgs(ex.args, spec.params);
            Outcome out = invoke(refMethod, refInstance, args);
            if (out.thrown != null) {
                Report.fail(RBUG, "reference threw on pinned example " + ex.args
                        + "\n    " + root(out.thrown));
                System.exit(1);
            }
            Object expected = Codec.parse(ex.expected, spec.judgedType());
            String why = Match.disagree(spec, args, out.judged, expected);
            if (why != null) {
                Report.fail(RBUG, "reference disagrees with pinned example"
                        + "\n    input     " + ex.args
                        + "\n    pinned    " + ex.expected
                        + "\n    reference " + Codec.print(out.judged)
                        + "\n    " + why);
                System.exit(1);
            }
            n++;
        }
        Report.ok("verify", n + " pinned example" + (n == 1 ? "" : "s"));
    }

    private void run(int stressIterations, long seed) {
        // ---- phase 1: pinned examples, judged against the PIN, not the reference
        int passed = 0;
        for (Spec.Example ex : spec.examples) {
            Object[] args = Codec.parseArgs(ex.args, spec.params);
            Object expected = Codec.parse(ex.expected, spec.judgedType());

            Outcome refOut = invoke(refMethod, refInstance, args);
            if (refOut.thrown != null || Match.disagree(spec, args, refOut.judged, expected) != null) {
                Report.fail(RBUG, "reference fails its own pinned example " + ex.args
                        + "\n    This is a bug in the judge, not in your solution."
                        + "\n    Please report it rather than changing your code.");
                System.exit(1);
            }

            Outcome out = invoke(solMethod, solInstance, args);
            if (out.thrown != null) {
                Report.example(false, ex.args, null, Codec.print(expected), root(out.thrown));
                Report.fail(RE, "threw on pinned example " + (passed + 1));
                System.exit(1);
            }
            String why = Match.disagree(spec, args, out.judged, expected);
            if (why != null) {
                Report.example(false, ex.args, Codec.print(out.judged), Codec.print(expected), why);
                Report.fail(WA, "failed pinned example " + (passed + 1));
                System.exit(1);
            }
            passed++;
        }
        Report.ok("fixed", passed + "/" + spec.examples.size());

        if (stressIterations <= 0) {
            Report.verdict(AC, "pinned examples only — run with -n to stress test");
            return;
        }
        if (spec.gen == null) {
            Report.warn("no generator declared — stress testing skipped");
            Report.verdict(AC, "pinned examples only");
            return;
        }

        // ---- phase 2: randomised differential testing against the reference
        Gen gen = new Gen(seed);
        long worstSol = 0, worstRef = 0;
        for (int i = 0; i < stressIterations; i++) {
            gen.scale = stressIterations == 1 ? 1.0 : (double) i / (stressIterations - 1);
            Object[] args;
            try {
                args = spec.gen.apply(gen);
            } catch (Throwable t) {
                Report.fail(RBUG, "generator threw at iteration " + i + ": " + root(t));
                System.exit(1);
                return;
            }

            Outcome refOut = invoke(refMethod, refInstance, args);
            if (refOut.thrown != null) {
                Report.fail(RBUG, "reference threw on generated input at iteration " + i
                        + "\n    " + Report.args(args)
                        + "\n    " + root(refOut.thrown));
                System.exit(1);
            }

            Outcome out = invoke(solMethod, solInstance, args);
            worstSol = Math.max(worstSol, out.nanos);
            worstRef = Math.max(worstRef, refOut.nanos);

            boolean threw = out.thrown != null;
            String why = threw ? root(out.thrown)
                               : Match.disagree(spec, args, out.judged, refOut.judged);

            if (threw || why != null) {
                reportStressFailure(i, stressIterations, args, threw);
                System.exit(1);
            }

            if (out.nanos / 1_000_000 > spec.limitMs) {
                Report.fail(TLE, "exceeded " + spec.limitMs + "ms at iteration " + i
                        + " (the reference took " + (refOut.nanos / 1_000_000) + "ms)"
                        + "\n    " + Report.args(args));
                System.exit(1);
            }
        }

        Report.ok("stress", stressIterations + "/" + stressIterations + "  (seed " + seed + ")");
        Report.timing(worstSol, worstRef);
        Report.verdict(AC, null);
    }

    private void reportStressFailure(int iteration, int total, Object[] args, boolean threw) {
        Report.section("stress", (iteration + 1) + "/" + total + " failed");

        Object[] minimal = Shrink.minimise(args, trial -> {
            Outcome r = invoke(refMethod, refInstance, trial);
            if (r.thrown != null) return false;
            Outcome s = invoke(solMethod, solInstance, trial);
            if (s.thrown != null) return true;
            return Match.disagree(spec, trial, s.judged, r.judged) != null;
        });

        Outcome refMin = invoke(refMethod, refInstance, minimal);
        Outcome solMin = invoke(solMethod, solInstance, minimal);

        StringBuilder b = new StringBuilder();
        b.append("minimised input\n      ").append(Report.args(minimal));
        if (solMin.thrown != null) {
            b.append("\n    yours     threw ").append(root(solMin.thrown));
        } else {
            b.append("\n    yours     ").append(Codec.print(solMin.judged));
        }
        b.append("\n    expected  ").append(Codec.print(refMin.judged));

        String reason = solMin.thrown != null ? null
                      : Match.disagree(spec, minimal, solMin.judged, refMin.judged);
        if (reason != null) b.append("\n    ").append(reason);

        Report.fail(solMin.thrown != null ? RE : WA, b.toString());
    }

    /**
     * Growth curve. Runs the solution at a doubling ladder of input sizes and reports
     * how the time responds, so you can see whether you wrote the linear solution or
     * the quadratic one.
     *
     * This is an indication, not a measurement. It is single-JVM, JIT-warmed but not
     * isolated, and the label it prints is a rough fit. Read it as "that shape is
     * consistent with O(n log n)", never as proof.
     */
    private void timeCurve(long seed) {
        if (spec.gen == null) {
            Report.warn("no generator declared — cannot measure a growth curve");
            return;
        }
        System.out.println("  " + Report.label("time") + "growth curve, n doubling");

        int top = Math.max(64, spec.maxN);
        int[] ladder = new int[5];
        for (int i = 4; i >= 0; i--) { ladder[i] = top; top /= 2; }

        long prevNanos = 0;
        int prevSize = 0;
        boolean any = false;

        for (int n : ladder) {
            Object[] args;
            try {
                Gen g = new Gen(seed);
                g.scale = 1.0;
                g.forcedSize = n;
                args = spec.gen.apply(g);
            } catch (Throwable t) {
                Report.warn("generator failed at n=" + n + ": " + root(t));
                continue;
            }

            for (int w = 0; w < 3; w++) {
                Outcome warm = invoke(solMethod, solInstance, args);
                if (warm.thrown != null) {
                    Report.warn("threw at n=" + n + ": " + root(warm.thrown));
                    return;
                }
            }

            // Time only the call. The deep copy is harness overhead and is itself O(n),
            // which would otherwise swamp a genuinely linear solution and flatten the
            // curve for a quadratic one. Copy first, measure second.
            long best = Long.MAX_VALUE;
            for (int r = 0; r < 7; r++) {
                Object[] fresh = Deep.copyAll(args);
                long t0 = System.nanoTime();
                try {
                    solMethod.invoke(solInstance, fresh);
                } catch (Throwable t) {
                    Report.warn("threw at n=" + n + ": " + root(t));
                    return;
                }
                best = Math.min(best, System.nanoTime() - t0);
            }

            int size = Report.sizeOf(args);
            Report.curve(size, prevSize, best, prevNanos);
            prevNanos = best;
            prevSize = size;
            any = true;
        }
        if (!any) Report.warn("no measurable sizes — check the generator's size bounds");
    }

    static String root(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null && c.getCause() != c) c = c.getCause();
        String msg = c.getMessage();
        String name = c.getClass().getSimpleName();
        StackTraceElement where = null;
        for (StackTraceElement e : c.getStackTrace()) {
            if (e.getClassName().equals("Solution") || e.getClassName().startsWith("Solution$")) {
                where = e;
                break;
            }
        }
        String at = where != null
            ? "  (Solution." + where.getMethodName() + " line " + where.getLineNumber() + ")"
            : "";
        return name + (msg != null ? ": " + msg : "") + at;
    }
}
