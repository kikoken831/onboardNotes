import java.util.ArrayList;
import java.util.List;
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
        if (cmp == Cmp.LENGTH_AND_PREFIX) return params[prefixArg];
        return inPlaceArg >= 0 ? params[inPlaceArg] : ret;
    }
}
