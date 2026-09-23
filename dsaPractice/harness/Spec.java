import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The test specification for one problem, declared inside its Ref class.
 *
 * Written as Java rather than JSON for two reasons: the JDK has no stdlib JSON parser,
 * and generators genuinely have to be code. Two Sum's generator must PLANT a valid
 * answer; linked-list-cycle must build a real cycle; tree problems must emit valid
 * trees. None of that survives being expressed as configuration.
 */
public final class Spec {

    /** A pinned example. Expected output comes from LeetCode, not from the reference. */
    public static final class Example {
        public final String args;
        public final String expected;
        Example(String args, String expected) { this.args = args; this.expected = expected; }
    }

    /** Predicate for problems with more than one correct answer. */
    public interface Validator {
        /** @return null if the answer is acceptable, else a human-readable reason it is not. */
        String check(Object[] input, Object yours, Object reference);
    }

    public final String method;
    public final T[] params;
    public T ret = T.VOID;

    public Cmp cmp = Cmp.EXACT;
    public Validator validator;
    public double eps = 1e-5;

    /** Index of the argument to judge after the call, or -1 to judge the return value. */
    public int inPlaceArg = -1;

    /**
     * For Cmp.LENGTH_AND_PREFIX: which argument the method mutates. The judge then
     * compares the returned length AND the first `length` elements of that argument,
     * ignoring everything past it, per LeetCode's "don't care beyond k" convention.
     */
    public int prefixArg = -1;

    /**
     * For LENGTH_AND_PREFIX: compare the first k elements as a multiset rather than a
     * sequence. LC 27 (Remove Element) explicitly allows any order, and LeetCode's own
     * checker sorts the prefix before comparing.
     */
    public boolean prefixUnordered = false;

    public final List<Example> examples = new ArrayList<>();

    // ------------------------------------------------------------ design problems

    /** One operation of a design class: its return type and parameter types. */
    public static final class Op {
        public final T ret;
        public final T[] params;
        Op(T ret, T[] params) { this.ret = ret; this.params = params; }
    }

    /**
     * Non-null for a design problem (LC 232, 705, 703...): the name of the class you
     * implement, instead of a method on Solution. Examples are LeetCode's
     * ops/args/expected triple, and the generator returns a single Calls value.
     */
    public String designClass;
    public T[] ctorParams = new T[0];
    public final Map<String, Op> ops = new LinkedHashMap<>();

    /** A pinned LeetCode design example: ops, args and expected output, as literals. */
    public static final class CallsExample {
        public final String ops, args, expected;
        CallsExample(String ops, String args, String expected) {
            this.ops = ops; this.args = args; this.expected = expected;
        }
    }
    public final List<CallsExample> callExamples = new ArrayList<>();

    // --------------------------------------------------------- interactive problems

    /**
     * For interactive problems (LC 374): the trailing `hidden.length` generated
     * arguments are NOT passed to the method. They are handed to `install` before
     * every call instead -- e.g. to set the pick behind GuessGame.guess().
     */
    public T[] hidden = new T[0];
    public Consumer<Object[]> install;

    /**
     * Rewires the (already copied) arguments before every call. For problems whose
     * real arguments can't be written as a literal: LC 142's cycle is `pos`, which must
     * be turned into an actual back-pointer; LC 236's `p` and `q` must be the very node
     * objects inside the tree being passed, found by value. Runs on each call's private
     * copy, so the reference and your solution each get their own wiring.
     */
    public Consumer<Object[]> prepare;

    /**
     * Converts what the method returned into what the judge compares. Receives the
     * prepared arguments too, so it can map a returned node back to its index in the
     * input (LC 142) or check that a "clone" shares no node with the original (LC 133).
     */
    public java.util.function.BiFunction<Object[], Object, Object> judgeAs;
    public T judgeAsType;

    // ---------------------------------------------------------------- driven problems

    /** Runs a problem that exercises several methods of your class, e.g. a codec round trip. */
    public interface Driver { Object run(Fresh fresh, Object[] args) throws Throwable; }

    /** A new instance of the class under test (LeetCode builds separate ser and deser objects). */
    public interface Fresh { Object get() throws Exception; }

    /** Non-null for a driven problem: the class you implement (e.g. "Codec"), not Solution. */
    public String drivenClass;
    public Driver driver;
    public Function<Gen, Object[]> gen;
    public String[] patterns = new String[0];
    public long limitMs = 4000;

    /** Largest n the generator should reach, used by `judge time`. */
    public int maxN = 100_000;

    private Spec(String method, T[] params) {
        this.method = method;
        this.params = params;
    }

    public static Spec method(String name, T... params) { return new Spec(name, params); }

    /** A design problem: you implement class `className`, constructed with `ctorParams`. */
    public static Spec design(String className, T... ctorParams) {
        Spec s = new Spec(className, new T[0]);
        s.designClass = className;
        s.ctorParams = ctorParams;
        return s;
    }

    public Spec op(String name, T ret, T... params) { ops.put(name, new Op(ret, params)); return this; }

    public Spec exampleCalls(String ops, String args, String expected) {
        callExamples.add(new CallsExample(ops, args, expected));
        return this;
    }

    /** Trailing arguments installed before each call rather than passed to it. */
    public Spec hidden(Consumer<Object[]> install, T... types) {
        this.install = install;
        this.hidden = types;
        return this;
    }

    /** Visible parameters followed by hidden ones: the shape of a pinned example's args. */
    public T[] allParams() {
        T[] all = java.util.Arrays.copyOf(params, params.length + hidden.length);
        System.arraycopy(hidden, 0, all, params.length, hidden.length);
        return all;
    }

    public boolean isDesign() { return designClass != null; }

    public boolean isDriven() { return drivenClass != null; }

    public Spec prepare(Consumer<Object[]> p) { this.prepare = p; return this; }

    /** Judge `fn(preparedArgs, returnValue)` instead of the return value, parsed as `type`. */
    public Spec judgeAs(T type, java.util.function.BiFunction<Object[], Object, Object> fn) {
        this.judgeAs = fn;
        this.judgeAsType = type;
        return this;
    }

    /**
     * A problem where you implement class `className` and the judge drives several of
     * its methods itself -- LC 297's `deser.deserialize(ser.serialize(root))`. `params`
     * are the literal types of the test input; `driver` performs the calls.
     */
    public static Spec driven(String className, T... params) {
        Spec s = new Spec(className, params);
        s.drivenClass = className;
        return s;
    }

    public Spec driver(Driver d) { this.driver = d; return this; }

    public Spec returns(T t)              { this.ret = t; return this; }
    public Spec compare(Cmp c)            { this.cmp = c; return this; }
    public Spec eps(double e)             { this.eps = e; this.cmp = Cmp.EPSILON; return this; }
    public Spec validate(Validator v)     { this.validator = v; this.cmp = Cmp.VALIDATE; return this; }
    public Spec inPlace(int argIndex)     { this.inPlaceArg = argIndex; return this; }

    /**
     * Judge the returned length together with the first `length` elements of the
     * given argument. Everything beyond the length is treated as undefined.
     */
    public Spec lengthAndPrefix(int argIndex) {
        this.prefixArg = argIndex;
        this.cmp = Cmp.LENGTH_AND_PREFIX;
        return this;
    }
    /** As lengthAndPrefix, but the order of the first k elements does not matter. */
    public Spec lengthAndPrefixAnyOrder(int argIndex) {
        lengthAndPrefix(argIndex);
        this.prefixUnordered = true;
        return this;
    }
    public Spec gen(Function<Gen, Object[]> g) { this.gen = g; return this; }
    public Spec patterns(String... p)     { this.patterns = p; return this; }
    public Spec limitMs(long ms)          { this.limitMs = ms; return this; }
    public Spec maxN(int n)               { this.maxN = n; return this; }

    /** Pin a LeetCode example. `args` is the literal arg list, `expected` the literal answer. */
    public Spec example(String args, String expected) {
        examples.add(new Example(args, expected));
        return this;
    }

    /** Pin an example for a void / in-place method: `expected` is the mutated argument. */
    public Spec exampleInPlace(String args, String expectedMutatedArg) {
        examples.add(new Example(args, expectedMutatedArg));
        return this;
    }

    /**
     * The value the judge actually compares.
     *
     * For LENGTH_AND_PREFIX this is the mutated argument truncated to the returned
     * length, which checks both at once: a wrong length gives a wrong-length array,
     * and wrong contents give wrong contents. Elements past the length are dropped,
     * which is exactly LeetCode's "don't care beyond k" rule.
     */
    Object judged(Object[] argsAfterCall, Object returnValue) {
        if (judgeAs != null) return judgeAs.apply(argsAfterCall, returnValue);
        if (cmp == Cmp.LENGTH_AND_PREFIX) {
            int k = returnValue instanceof Number ? ((Number) returnValue).intValue() : -1;
            Object arr = argsAfterCall[prefixArg];
            int len = arr == null ? 0 : java.lang.reflect.Array.getLength(arr);
            if (k < 0)   return "returned a negative length: " + k;
            if (k > len) return "returned length " + k + " but the array only has " + len;
            Object out = java.lang.reflect.Array.newInstance(
                arr.getClass().getComponentType(), k);
            System.arraycopy(arr, 0, out, 0, k);
            return out;
        }
        return inPlaceArg >= 0 ? argsAfterCall[inPlaceArg] : returnValue;
    }

    /** Type of the judged value, for parsing pinned examples. */
    T judgedType() {
        if (judgeAs != null) return judgeAsType;
        if (cmp == Cmp.LENGTH_AND_PREFIX) return params[prefixArg];
        return inPlaceArg >= 0 ? params[inPlaceArg] : ret;
    }
}
