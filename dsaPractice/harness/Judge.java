import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** One call of the thing under test: a method on Solution, or a driver over your class. */
    private interface Invoker { Object call(Object[] args) throws Throwable; }

    private final Invoker refCall;
    private final Invoker solCall;

    /** Design problems: the class under test, loaded separately for each side. */
    private final Class<?> refDesign;
    private final Class<?> solDesign;

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
        this.refMethod = spec.isDesign() || spec.isDriven() ? null : find(refClass, spec);
        this.refDesign = spec.isDesign() ? Class.forName(spec.designClass, true, refLoader) : null;
        this.refCall = invoker(refLoader, refInstance, refMethod);

        if (refOnly) {
            this.solInstance = null;
            this.solMethod = null;
            this.solDesign = null;
            this.solCall = null;
        } else {
            URLClassLoader solLoader = new URLClassLoader(
                new URL[]{ new File(solDir).toURI().toURL() }, parent);
            if (spec.isDesign()) {
                this.solInstance = null;
                this.solMethod = null;
                this.solDesign = loadDesign(solLoader);
                this.solCall = null;
            } else if (spec.isDriven()) {
                this.solInstance = null;
                this.solMethod = null;
                this.solDesign = null;
                this.solCall = invoker(solLoader, null, null);
            } else {
                Class<?> solClass = Class.forName("Solution", true, solLoader);
                this.solInstance = instantiate(solClass);
                this.solMethod = find(solClass, spec);
                this.solDesign = null;
                this.solCall = invoker(solLoader, solInstance, solMethod);
            }
        }
    }

    private Invoker invoker(ClassLoader loader, Object instance, Method m) throws Exception {
        if (spec.isDesign()) return null;
        if (spec.isDriven()) {
            Class<?> cls;
            try {
                cls = Class.forName(spec.drivenClass, true, loader);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("declare `class " + spec.drivenClass + "` (not `class Solution`)");
            }
            return a -> spec.driver.run(() -> instantiate(cls), a);
        }
        return a -> {
            try {
                return m.invoke(instance, a);
            } catch (InvocationTargetException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }
        };
    }

    /** Hidden-argument install and prepare, applied to a call's private copy. */
    private Object[] prepared(Object[] copy) {
        if (spec.install != null) spec.install.accept(copy);      // e.g. set GuessGame's pick
        if (spec.prepare != null) spec.prepare.accept(copy);      // e.g. wire LC 142's cycle
        return spec.hidden.length > 0 ? java.util.Arrays.copyOf(copy, spec.params.length) : copy;
    }

    /** Your class, by the name LeetCode gives it -- not Solution. */
    private Class<?> loadDesign(ClassLoader loader) throws Exception {
        try {
            return Class.forName(spec.designClass, true, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("this is a design problem: declare `class "
                + spec.designClass + "` (not `class Solution`)");
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

    private Outcome invoke(Invoker who, Object[] args) {
        Outcome o = new Outcome();
        Object[] copy = Deep.copyAll(args);
        long t0 = System.nanoTime();
        try {
            Object[] callArgs = prepared(copy);
            t0 = System.nanoTime();                       // time the call, not the wiring
            Object ret = who.call(callArgs);
            o.nanos = System.nanoTime() - t0;
            o.judged = spec.judged(copy, ret);
        } catch (Throwable t) {
            o.nanos = System.nanoTime() - t0;
            o.thrown = t;
        }
        return o;
    }

    // ------------------------------------------------------------------ modes

    /** Check the reference against every pinned example. This guards against MY bugs. */
    private void verify() {
        if (spec.isDesign()) { verifyDesign(); return; }
        if (spec.examples.isEmpty()) {
            Report.warn("no pinned examples — nothing guards this problem against a bad reference");
            Report.ok("verify", "0 examples");
            return;
        }
        int n = 0;
        for (Spec.Example ex : spec.examples) {
            Object[] args = LcCodec.parseArgs(ex.args, spec.allParams());
            Outcome out = invoke(refCall, args);
            if (out.thrown != null) {
                Report.fail(RBUG, "reference threw on pinned example " + ex.args
                        + "\n    " + root(out.thrown));
                System.exit(1);
            }
            Object expected = LcCodec.parse(ex.expected, spec.judgedType());
            String why = Match.disagree(spec, args, out.judged, expected);
            if (why != null) {
                Report.fail(RBUG, "reference disagrees with pinned example"
                        + "\n    input     " + ex.args
                        + "\n    pinned    " + ex.expected
                        + "\n    reference " + LcCodec.print(out.judged)
                        + "\n    " + why);
                System.exit(1);
            }
            n++;
        }
        Report.ok("verify", n + " pinned example" + (n == 1 ? "" : "s"));
    }

    private void run(int stressIterations, long seed) {
        if (spec.isDesign()) { runDesign(stressIterations, seed); return; }
        // ---- phase 1: pinned examples, judged against the PIN, not the reference
        int passed = 0;
        for (Spec.Example ex : spec.examples) {
            Object[] args = LcCodec.parseArgs(ex.args, spec.allParams());
            Object expected = LcCodec.parse(ex.expected, spec.judgedType());

            Outcome refOut = invoke(refCall, args);
            if (refOut.thrown != null || Match.disagree(spec, args, refOut.judged, expected) != null) {
                Report.fail(RBUG, "reference fails its own pinned example " + ex.args
                        + "\n    This is a bug in the judge, not in your solution."
                        + "\n    Please report it rather than changing your code.");
                System.exit(1);
            }

            Outcome out = invoke(solCall, args);
            if (out.thrown != null) {
                Report.example(false, ex.args, null, LcCodec.print(expected), root(out.thrown));
                Report.fail(RE, "threw on pinned example " + (passed + 1));
                System.exit(1);
            }
            String why = Match.disagree(spec, args, out.judged, expected);
            if (why != null) {
                Report.example(false, ex.args, LcCodec.print(out.judged), LcCodec.print(expected), why);
                Report.fail(WA, "failed pinned example " + (passed + 1));
                System.exit(1);
            }
            // Pinned examples are time-limited too. Several are chosen precisely because
            // the naive solution is exponential on them (LC 139's "aaa...ab"), and the
            // stress phase's small random inputs would never expose that.
            if (out.nanos / 1_000_000 > spec.limitMs) {
                Report.fail(TLE, "exceeded " + spec.limitMs + "ms on pinned example " + (passed + 1)
                        + " (the reference took " + (refOut.nanos / 1_000_000) + "ms)\n    " + ex.args);
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

            Outcome refOut = invoke(refCall, args);
            if (refOut.thrown != null) {
                Report.fail(RBUG, "reference threw on generated input at iteration " + i
                        + "\n    " + Report.args(args)
                        + "\n    " + root(refOut.thrown));
                System.exit(1);
            }

            Outcome out = invoke(solCall, args);
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
            Outcome r = invoke(refCall, trial);
            if (r.thrown != null) return false;
            Outcome s = invoke(solCall, trial);
            if (s.thrown != null) return true;
            return Match.disagree(spec, trial, s.judged, r.judged) != null;
        });

        Outcome refMin = invoke(refCall, minimal);
        Outcome solMin = invoke(solCall, minimal);

        StringBuilder b = new StringBuilder();
        b.append("minimised input\n      ").append(Report.args(minimal));
        if (solMin.thrown != null) {
            b.append("\n    yours     threw ").append(root(solMin.thrown));
        } else {
            b.append("\n    yours     ").append(LcCodec.print(solMin.judged));
        }
        b.append("\n    expected  ").append(LcCodec.print(refMin.judged));

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
        if (spec.isDesign()) { timeCurveDesign(seed); return; }
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
                Outcome warm = invoke(solCall, args);
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
                Object[] fresh = prepared(Deep.copyAll(args));
                long t0 = System.nanoTime();
                try {
                    solCall.call(fresh);
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

    // =========================================================== design problems

    /** The outcome of replaying one Calls value against one class. */
    private static final class Replay {
        final List<Object> out = new ArrayList<>();   // index 0 is the constructor (always null)
        Throwable thrown;
        int failedAt = -2;                            // -1 constructor, i = call i, -2 none
        long nanos;
    }

    private final Map<Class<?>, Map<String, Method>> opCache = new HashMap<>();

    /** Replays `calls` on a fresh instance. Callers pass their own copy. */
    private Replay replay(Class<?> cls, Calls calls) {
        Replay r = new Replay();
        long t0 = System.nanoTime();
        try {
            r.failedAt = -1;
            Object obj = construct(cls, calls.ctorArgs);
            r.out.add(null);
            for (int i = 0; i < calls.size(); i++) {
                r.failedAt = i;
                String name = calls.ops.get(i);
                Object v = opMethod(cls, name).invoke(obj, calls.args.get(i));
                r.out.add(spec.ops.get(name).ret == T.VOID ? null : v);
            }
            r.failedAt = -2;
        } catch (InvocationTargetException e) {
            r.thrown = e.getCause() != null ? e.getCause() : e;
        } catch (Throwable e) {
            r.thrown = e;
        }
        r.nanos = System.nanoTime() - t0;
        return r;
    }

    private Object construct(Class<?> cls, Object[] args) throws Exception {
        Class<?>[] sig = new Class<?>[spec.ctorParams.length];
        for (int i = 0; i < sig.length; i++) sig[i] = spec.ctorParams[i].javaType();
        Constructor<?> k;
        try {
            k = cls.getDeclaredConstructor(sig);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("no constructor " + spec.designClass + "("
                + java.util.Arrays.toString(spec.ctorParams).replaceAll("[\\[\\]]", "") + ")");
        }
        k.setAccessible(true);
        return k.newInstance(args);
    }

    private Method opMethod(Class<?> cls, String name) throws NoSuchMethodException {
        Map<String, Method> cache = opCache.computeIfAbsent(cls, c -> new HashMap<>());
        Method m = cache.get(name);
        if (m != null) return m;
        Spec.Op op = spec.ops.get(name);
        Class<?>[] sig = new Class<?>[op.params.length];
        for (int i = 0; i < sig.length; i++) sig[i] = op.params[i].javaType();
        try {
            m = cls.getDeclaredMethod(name, sig);
        } catch (NoSuchMethodException primary) {
            for (Method x : cls.getDeclaredMethods()) {
                if (x.getName().equals(name) && x.getParameterCount() == sig.length) { m = x; break; }
            }
            if (m == null) throw new NoSuchMethodException(spec.designClass + "." + name
                + " with " + sig.length + " parameter" + (sig.length == 1 ? "" : "s"));
        }
        m.setAccessible(true);
        cache.put(name, m);
        return m;
    }

    /** Parse a pinned expected-output literal, typed per call. */
    @SuppressWarnings("unchecked")
    private List<Object> expectedOutputs(Calls calls, String literal) {
        List<Object> raw = (List<Object>) LcCodec.parseRaw(literal);
        if (raw.size() != calls.size() + 1) {
            throw new IllegalArgumentException("expected output has " + raw.size()
                + " entries for " + (calls.size() + 1) + " calls");
        }
        List<Object> out = new ArrayList<>();
        out.add(null);
        for (int i = 0; i < calls.size(); i++) {
            T ret = spec.ops.get(calls.ops.get(i)).ret;
            out.add(ret == T.VOID ? null : LcCodec.coerceTo(raw.get(i + 1), ret));
        }
        return out;
    }

    /** First call whose output differs, described; null if all agree. */
    private String designDisagree(Calls calls, List<Object> yours, List<Object> expected) {
        for (int i = 0; i < expected.size(); i++) {
            Object y = i < yours.size() ? yours.get(i) : null, e = expected.get(i);
            if (!Deep.eq(y, e, spec.eps)) {
                return "call " + i + ", " + describe(calls, i - 1) + ": yours "
                     + LcCodec.print(y) + ", expected " + LcCodec.print(e);
            }
        }
        return null;
    }

    private static String describe(Calls c, int i) {
        if (i < 0) return c.className + "(" + joinArgs(c.ctorArgs) + ")";
        return c.ops.get(i) + "(" + joinArgs(c.args.get(i)) + ")";
    }

    private static String joinArgs(Object[] a) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) { if (i > 0) b.append(", "); b.append(LcCodec.print(a[i])); }
        return b.toString();
    }

    private static String thrownAt(Calls c, Replay r) {
        return "threw at " + describe(c, r.failedAt) + ": " + root(r.thrown);
    }

    private void verifyDesign() {
        if (spec.callExamples.isEmpty()) {
            Report.warn("no pinned examples — nothing guards this problem against a bad reference");
            Report.ok("verify", "0 examples");
            return;
        }
        int n = 0;
        for (Spec.CallsExample ex : spec.callExamples) {
            Calls calls = LcCodec.parseCalls(ex.ops, ex.args, spec);
            List<Object> expected = expectedOutputs(calls, ex.expected);
            Replay r = replay(refDesign, calls.deepCopy());
            if (r.thrown != null) {
                Report.fail(RBUG, "reference " + thrownAt(calls, r) + "\n    " + calls);
                System.exit(1);
            }
            String why = designDisagree(calls, r.out, expected);
            if (why != null) {
                Report.fail(RBUG, "reference disagrees with pinned example\n      " + calls + "\n    " + why);
                System.exit(1);
            }
            n++;
        }
        Report.ok("verify", n + " pinned example" + (n == 1 ? "" : "s"));
    }

    private void runDesign(int stressIterations, long seed) {
        int passed = 0;
        for (Spec.CallsExample ex : spec.callExamples) {
            Calls calls = LcCodec.parseCalls(ex.ops, ex.args, spec);
            List<Object> expected = expectedOutputs(calls, ex.expected);

            Replay ref = replay(refDesign, calls.deepCopy());
            if (ref.thrown != null || designDisagree(calls, ref.out, expected) != null) {
                Report.fail(RBUG, "reference fails its own pinned example\n      " + calls
                        + "\n    This is a bug in the judge, not in your solution.");
                System.exit(1);
            }
            Replay mine = replay(solDesign, calls.deepCopy());
            if (mine.thrown != null) {
                Report.example(false, calls.toString(), null, LcCodec.print(expected), thrownAt(calls, mine));
                Report.fail(RE, "threw on pinned example " + (passed + 1));
                System.exit(1);
            }
            String why = designDisagree(calls, mine.out, expected);
            if (why != null) {
                Report.example(false, calls.toString(), LcCodec.print(mine.out), LcCodec.print(expected), why);
                Report.fail(WA, "failed pinned example " + (passed + 1));
                System.exit(1);
            }
            if (mine.nanos / 1_000_000 > spec.limitMs) {
                Report.fail(TLE, "exceeded " + spec.limitMs + "ms on pinned example " + (passed + 1));
                System.exit(1);
            }
            passed++;
        }
        Report.ok("fixed", passed + "/" + spec.callExamples.size());

        if (stressIterations <= 0 || spec.gen == null) {
            Report.verdict(AC, "pinned examples only");
            return;
        }

        Gen gen = new Gen(seed);
        long worstSol = 0, worstRef = 0;
        for (int i = 0; i < stressIterations; i++) {
            gen.scale = stressIterations == 1 ? 1.0 : (double) i / (stressIterations - 1);
            Calls calls;
            try {
                calls = (Calls) spec.gen.apply(gen)[0];
            } catch (Throwable t) {
                Report.fail(RBUG, "generator threw at iteration " + i + ": " + root(t));
                System.exit(1);
                return;
            }
            Replay ref = replay(refDesign, calls.deepCopy());
            if (ref.thrown != null) {
                Report.fail(RBUG, "reference " + thrownAt(calls, ref) + " at iteration " + i + "\n      " + calls);
                System.exit(1);
            }
            Replay mine = replay(solDesign, calls.deepCopy());
            worstSol = Math.max(worstSol, mine.nanos);
            worstRef = Math.max(worstRef, ref.nanos);

            if (mine.thrown != null || designDisagree(calls, mine.out, ref.out) != null) {
                reportDesignFailure(i, stressIterations, calls);
                System.exit(1);
            }
            if (mine.nanos / 1_000_000 > spec.limitMs) {
                Report.fail(TLE, "exceeded " + spec.limitMs + "ms for " + calls.size() + " calls at iteration "
                        + i + " (the reference took " + (ref.nanos / 1_000_000) + "ms)");
                System.exit(1);
            }
        }
        Report.ok("stress", stressIterations + "/" + stressIterations + "  (seed " + seed + ")");
        Report.timing(worstSol, worstRef);
        Report.verdict(AC, null);
    }

    private void reportDesignFailure(int iteration, int total, Calls failing) {
        Report.section("stress", (iteration + 1) + "/" + total + " failed");
        Object[] minimal = Shrink.minimise(new Object[]{ failing }, trial -> {
            Calls c = (Calls) trial[0];
            Replay r = replay(refDesign, c.deepCopy());
            if (r.thrown != null) return false;
            Replay s = replay(solDesign, c.deepCopy());
            return s.thrown != null || designDisagree(c, s.out, r.out) != null;
        });
        Calls c = (Calls) minimal[0];
        Replay ref = replay(refDesign, c.deepCopy());
        Replay mine = replay(solDesign, c.deepCopy());

        StringBuilder b = new StringBuilder();
        b.append("minimised to ").append(c.size()).append(" call").append(c.size() == 1 ? "" : "s")
         .append("\n      ").append(c);
        if (mine.thrown != null) {
            b.append("\n    yours     ").append(thrownAt(c, mine));
        } else {
            b.append("\n    yours     ").append(LcCodec.print(mine.out))
             .append("\n    expected  ").append(LcCodec.print(ref.out))
             .append("\n    ").append(designDisagree(c, mine.out, ref.out));
        }
        Report.fail(mine.thrown != null ? RE : WA, b.toString());
    }

    private void timeCurveDesign(long seed) {
        if (spec.gen == null) { Report.warn("no generator declared — cannot measure a growth curve"); return; }
        System.out.println("  " + Report.label("time") + "growth curve, number of calls doubling");
        int top = Math.max(64, spec.maxN);
        int[] ladder = new int[5];
        for (int i = 4; i >= 0; i--) { ladder[i] = top; top /= 2; }
        long prevNanos = 0;
        int prevSize = 0;
        for (int n : ladder) {
            Gen g = new Gen(seed);
            g.scale = 1.0;
            g.forcedSize = n;
            Calls calls = (Calls) spec.gen.apply(g)[0];
            long best = Long.MAX_VALUE;
            for (int r = 0; r < 7; r++) {
                Replay x = replay(solDesign, calls.deepCopy());
                if (x.thrown != null) { Report.warn(thrownAt(calls, x)); return; }
                if (r >= 2) best = Math.min(best, x.nanos);      // first two are JIT warm-up
            }
            Report.curve(calls.size(), prevSize, best, prevNanos);
            prevNanos = best;
            prevSize = calls.size();
        }
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
