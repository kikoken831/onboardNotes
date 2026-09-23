import java.util.ArrayList;
import java.util.List;

/**
 * One test case for a design problem: a constructor call followed by a sequence of
 * method calls, exactly as LeetCode presents them:
 *
 *   ["MyQueue","push","push","peek","pop","empty"]
 *   [[],[1],[2],[],[],[]]
 *
 * It is a value, not a script: the judge replays it against a fresh instance of the
 * reference class and a fresh instance of yours, and compares the outputs call by call.
 * Being a value is also what lets the shrinker minimise it -- dropping calls one at a
 * time until the failure is as short as it gets.
 */
public final class Calls {

    public final String className;
    public final Object[] ctorArgs;
    public final List<String> ops;
    public final List<Object[]> args;

    public Calls(String className, Object[] ctorArgs, List<String> ops, List<Object[]> args) {
        if (ops.size() != args.size()) throw new IllegalArgumentException("ops/args length mismatch");
        this.className = className;
        this.ctorArgs = ctorArgs;
        this.ops = ops;
        this.args = args;
    }

    /** Builder used by generators. */
    public static final class Builder {
        private final String className;
        private final Object[] ctorArgs;
        private final List<String> ops = new ArrayList<>();
        private final List<Object[]> args = new ArrayList<>();

        public Builder(String className, Object... ctorArgs) {
            this.className = className;
            this.ctorArgs = ctorArgs;
        }

        public Builder call(String op, Object... a) { ops.add(op); args.add(a); return this; }

        public int size() { return ops.size(); }

        public Calls build() { return new Calls(className, ctorArgs, ops, args); }
    }

    public int size() { return ops.size(); }

    /** A copy without call `i`. The constructor is never dropped. */
    public Calls without(int i) {
        List<String> o = new ArrayList<>(ops);
        List<Object[]> a = new ArrayList<>(args);
        o.remove(i);
        a.remove(i);
        return new Calls(className, ctorArgs, o, a);
    }

    /** A copy keeping only the first `n` calls. */
    public Calls prefix(int n) {
        return new Calls(className, ctorArgs, new ArrayList<>(ops.subList(0, n)),
                         new ArrayList<>(args.subList(0, n)));
    }

    public Calls deepCopy() {
        List<Object[]> a = new ArrayList<>(args.size());
        for (Object[] x : args) a.add(Deep.copyAll(x));
        return new Calls(className, Deep.copyAll(ctorArgs), new ArrayList<>(ops), a);
    }

    /** LeetCode's two-line format, so a failing case can be pasted straight into leetcode.com. */
    @Override
    public String toString() {
        StringBuilder o = new StringBuilder("[").append(Codec.print(className));
        StringBuilder a = new StringBuilder("[").append(argList(ctorArgs));
        for (int i = 0; i < ops.size(); i++) {
            o.append(',').append(Codec.print(ops.get(i)));
            a.append(',').append(argList(args.get(i)));
        }
        return o.append(']') + "\n      " + a.append(']');
    }

    private static String argList(Object[] xs) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < xs.length; i++) { if (i > 0) b.append(','); b.append(Codec.print(xs[i])); }
        return b.append(']').toString();
    }
}
