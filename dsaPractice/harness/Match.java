import java.util.ArrayList;
import java.util.List;

/**
 * Applies a Spec's comparison mode.
 *
 * Every method returns null when the two answers agree, or a short human-readable
 * reason when they do not. Returning the reason rather than a boolean is what lets
 * the judge print "duplicate triplets not skipped" instead of just "WRONG ANSWER".
 */
public final class Match {

    private Match() {}

    public static String disagree(Spec spec, Object[] input, Object yours, Object expected) {
        // judged() reports its own contract violations as a String rather than an array.
        if (yours instanceof String && spec.cmp == Cmp.LENGTH_AND_PREFIX) {
            return (String) yours;
        }
        // A returned list containing a cycle is always wrong, and saying so plainly
        // beats letting the value comparison report a confusing length mismatch. It
        // also stops a cyclic answer from hanging a later traversal.
        String cyc = cycleCheck(yours);
        if (cyc != null) return cyc;

        switch (spec.cmp) {
            case EXACT:             return exact(yours, expected, 0.0);
            case EPSILON:           return exact(yours, expected, spec.eps);
            case MULTISET:          return unordered(yours, expected, false);
            case SET_OF_SEQS:       return unordered(yours, expected, false);
            case SET_OF_SETS:       return unordered(yours, expected, true);
            case LENGTH_AND_PREFIX: return prefix(yours, expected, spec.prefixUnordered);
            case VALIDATE:
                if (spec.validator == null) return "spec declares VALIDATE but supplies no validator";
                return spec.validator.check(input, yours, expected);
            default:                return exact(yours, expected, 0.0);
        }
    }

    /**
     * Detect a cycle in a returned linked list.
     *
     * The usual cause is forgetting to null-terminate a list you rebuilt by splicing
     * nodes out of the input — the last node still points at whatever followed it
     * originally. Naming that failure directly saves a long debugging session.
     */
    private static String cycleCheck(Object v) {
        if (!(v instanceof ListNode)) return null;
        ListNode slow = (ListNode) v, fast = (ListNode) v;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                return "the list you returned contains a CYCLE — "
                     + "did you forget to null-terminate a list you rebuilt? "
                     + Codec.printList((ListNode) v);
            }
        }
        return null;
    }

    /** Length-and-prefix: both sides are already truncated to their returned length. */
    private static String prefix(Object yours, Object expected, boolean anyOrder) {
        if (anyOrder && yours instanceof int[] && expected instanceof int[]) {
            int[] y = ((int[]) yours).clone(), e = ((int[]) expected).clone();
            java.util.Arrays.sort(y);
            java.util.Arrays.sort(e);
            if (java.util.Arrays.equals(y, e)) return null;
            if (y.length != e.length) return "you returned k=" + y.length + ", expected k=" + e.length;
            return "returned the right length but the first " + e.length
                 + " elements are not the right values (any order is fine here)";
        }
        if (Deep.eq(yours, expected)) return null;
        int ly = yours == null ? -1 : java.lang.reflect.Array.getLength(yours);
        int le = expected == null ? -1 : java.lang.reflect.Array.getLength(expected);
        if (ly != le) {
            return "you returned k=" + ly + ", expected k=" + le;
        }
        return "returned the right length but the first " + le
             + " element" + (le == 1 ? "" : "s") + " are wrong"
             + " (order must match the original)";
    }

    private static String exact(Object yours, Object expected, double eps) {
        if (Deep.eq(yours, expected, eps)) return null;
        // shapeHint returns null when it has nothing clever to say. That null must
        // NEVER escape: callers read null as "the answers agree". Before this guard, any
        // wrong answer of the right length that was not a reversal or reordering of the
        // expected one -- [7,0] for [7,7], a wrong cell in a grid -- was ACCEPTED.
        String hint = shapeHint(yours, expected);
        return hint != null ? hint : firstDifference(yours, expected, eps);
    }

    /** Where two same-shaped answers first diverge. Always returns a non-null reason. */
    private static String firstDifference(Object yours, Object expected, double eps) {
        Integer ly = lengthOf(yours), le = lengthOf(expected);
        if (ly != null && le != null && ly.equals(le)
                && !(yours instanceof String) && !(expected instanceof String)) {
            List<Object> a = Deep.asList(yours), b = Deep.asList(expected);
            for (int i = 0; i < a.size(); i++) {
                if (!Deep.eq(a.get(i), b.get(i), eps)) {
                    return "first difference at index " + i + ": yours "
                         + Codec.print(a.get(i)) + ", expected " + Codec.print(b.get(i));
                }
            }
        }
        return "values differ";
    }

    /**
     * Order-insensitive comparison. `sortInner` also ignores order within each element,
     * which is what subsets/3sum/combination-sum need but permutations/n-queens must not.
     */
    private static String unordered(Object yours, Object expected, boolean sortInner) {
        String a = Deep.canonical(yours, sortInner, true);
        String b = Deep.canonical(expected, sortInner, true);
        if (a.equals(b)) return null;

        List<String> ya = elements(yours, sortInner);
        List<String> ea = elements(expected, sortInner);

        List<String> missing = new ArrayList<>(ea);
        for (String s : ya) missing.remove(s);
        List<String> extra = new ArrayList<>(ya);
        for (String s : ea) extra.remove(s);

        StringBuilder b2 = new StringBuilder();
        if (ya.size() != ea.size()) {
            b2.append("count differs: yours ").append(ya.size())
              .append(", expected ").append(ea.size());
        }
        if (!missing.isEmpty()) {
            if (b2.length() > 0) b2.append("; ");
            b2.append("missing ").append(preview(missing));
        }
        if (!extra.isEmpty()) {
            if (b2.length() > 0) b2.append("; ");
            b2.append("unexpected ").append(preview(extra));
            if (missing.isEmpty() && ya.size() > ea.size()) {
                b2.append("  (duplicates not de-duplicated?)");
            }
        }
        return b2.length() > 0 ? b2.toString() : "same elements, different arrangement";
    }

    private static List<String> elements(Object v, boolean sortInner) {
        List<String> out = new ArrayList<>();
        for (Object row : Deep.asList(v)) {
            if (sortInner && Deep.isCollection(row)) {
                List<String> inner = new ArrayList<>();
                for (Object x : Deep.asList(row)) inner.add(Codec.print(x));
                java.util.Collections.sort(inner);
                out.add("[" + String.join(",", inner) + "]");
            } else {
                out.add(Codec.print(row));
            }
        }
        java.util.Collections.sort(out);
        return out;
    }

    private static String preview(List<String> items) {
        int show = Math.min(3, items.size());
        String s = String.join(", ", items.subList(0, show));
        return items.size() > show ? s + " (+" + (items.size() - show) + " more)" : s;
    }

    /** Cheap diagnosis of the common near-misses, so the message says something useful. */
    private static String shapeHint(Object yours, Object expected) {
        if (yours == null) return "returned null";
        if (expected == null) return "expected null";

        Integer ly = lengthOf(yours), le = lengthOf(expected);
        if (ly != null && le != null && !ly.equals(le)) {
            return "length differs: yours " + ly + ", expected " + le;
        }
        if (ly != null && le != null && ly.equals(le)) {
            String rev = Deep.eq(reversed(yours), expected) ? "  (your answer is reversed)" : "";
            if (!rev.isEmpty()) return "same elements, opposite order" + rev;

            String sorted = sameMultiset(yours, expected)
                ? "  (same elements, wrong order — did you forget to sort?)" : "";
            if (!sorted.isEmpty()) return "order differs" + sorted;
        }
        if (yours instanceof Boolean && expected instanceof Boolean) {
            return "returned " + yours + ", expected " + expected;
        }
        if (yours instanceof Number && expected instanceof Number) {
            double d = ((Number) yours).doubleValue() - ((Number) expected).doubleValue();
            if (Math.abs(d) == 1) return "off by one (" + (d > 0 ? "+1" : "-1") + ")";
            return "off by " + Codec.print(d);
        }
        return null; // no useful hint; the printed values speak for themselves
    }

    private static Integer lengthOf(Object v) {
        if (v == null) return null;
        if (v.getClass().isArray()) return java.lang.reflect.Array.getLength(v);
        if (v instanceof List) return ((List<?>) v).size();
        if (v instanceof String) return ((String) v).length();
        return null;
    }

    private static Object reversed(Object v) {
        List<Object> l = new ArrayList<>(Deep.asList(v));
        java.util.Collections.reverse(l);
        return l;
    }

    private static boolean sameMultiset(Object a, Object b) {
        List<String> x = new ArrayList<>(), y = new ArrayList<>();
        for (Object o : Deep.asList(a)) x.add(Codec.print(o));
        for (Object o : Deep.asList(b)) y.add(Codec.print(o));
        java.util.Collections.sort(x);
        java.util.Collections.sort(y);
        return x.equals(y);
    }
}
