/**
 * Preconditions for reference solutions.
 *
 * A reference calls these to reject inputs that violate the problem's stated
 * constraints. That matters to the shrinker, not to correctness: Shrink only accepts a
 * smaller candidate if the reference runs cleanly on it, so a reference that throws on
 * an invalid input is what stops "strictly sorted" being shrunk into duplicates, or a
 * permutation into a non-permutation. Without it a minimised counter-example could
 * violate the very constraints the solution is entitled to assume.
 */
public final class Pre {
    private Pre() {}

    public static void check(boolean ok, String constraint) {
        if (!ok) throw new IllegalArgumentException("input violates constraint: " + constraint);
    }

    public static void sorted(int[] a, boolean strict, String what) {
        for (int i = 1; i < a.length; i++) {
            if (strict ? a[i] <= a[i - 1] : a[i] < a[i - 1]) {
                throw new IllegalArgumentException("input violates constraint: " + what
                        + (strict ? " strictly" : "") + " sorted");
            }
        }
    }

    public static void length(Object arr, int lo, int hi, String what) {
        int n = arr == null ? 0 : java.lang.reflect.Array.getLength(arr);
        if (n < lo || n > hi) {
            throw new IllegalArgumentException("input violates constraint: " + lo + " <= "
                    + what + ".length <= " + hi + " (got " + n + ")");
        }
    }
}
