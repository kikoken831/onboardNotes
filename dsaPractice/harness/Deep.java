import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Deep copy and deep comparison of judged values.
 *
 * Deep copy matters more than it looks: your solution and the reference are handed the
 * SAME logical input, and several problems mutate it in place (rotate-image, sort-colors,
 * move-zeroes). Without a fresh copy per invocation, whichever runs second sees the
 * other's mutations and the judge silently reports nonsense.
 */
public final class Deep {

    private Deep() {}

    // ------------------------------------------------------------------ copy

    public static Object[] copyAll(Object[] args) {
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) out[i] = copy(args[i]);
        return out;
    }

    public static Object copy(Object v) {
        return copy(v, new IdentityHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static Object copy(Object v, IdentityHashMap<Object, Object> seen) {
        if (v == null) return null;
        if (v instanceof String || v instanceof Integer || v instanceof Long
                || v instanceof Double || v instanceof Boolean || v instanceof Character) {
            return v; // immutable
        }
        Object hit = seen.get(v);
        if (hit != null) return hit;

        if (v instanceof Calls)     return ((Calls) v).deepCopy();
        if (v instanceof int[])     { int[] a = (int[]) v;         return a.clone(); }
        if (v instanceof long[])    { long[] a = (long[]) v;       return a.clone(); }
        if (v instanceof double[])  { double[] a = (double[]) v;   return a.clone(); }
        if (v instanceof boolean[]) { boolean[] a = (boolean[]) v; return a.clone(); }
        if (v instanceof char[])    { char[] a = (char[]) v;       return a.clone(); }

        if (v.getClass().isArray()) {
            int n = java.lang.reflect.Array.getLength(v);
            Object out = java.lang.reflect.Array.newInstance(v.getClass().getComponentType(), n);
            seen.put(v, out);
            for (int i = 0; i < n; i++) {
                java.lang.reflect.Array.set(out, i, copy(java.lang.reflect.Array.get(v, i), seen));
            }
            return out;
        }

        if (v instanceof List) {
            List<Object> src = (List<Object>) v;
            List<Object> out = new ArrayList<>(src.size());
            seen.put(v, out);
            for (Object x : src) out.add(copy(x, seen));
            return out;
        }

        if (v instanceof ListNode) {
            // Handles cycles: 141/142 hand the solution a genuinely cyclic list.
            ListNode src = (ListNode) v;
            ListNode out = new ListNode(src.val);
            seen.put(src, out);
            out.next = (ListNode) copy(src.next, seen);
            return out;
        }

        if (v instanceof TreeNode) {
            TreeNode src = (TreeNode) v;
            TreeNode out = new TreeNode(src.val);
            seen.put(src, out);
            out.left  = (TreeNode) copy(src.left, seen);
            out.right = (TreeNode) copy(src.right, seen);
            return out;
        }

        return v; // unknown immutable-ish type; nothing safe to do but pass through
    }

    // ------------------------------------------------------------------ equals

    public static boolean eq(Object a, Object b) { return eq(a, b, 0.0); }

    @SuppressWarnings("unchecked")
    public static boolean eq(Object a, Object b, double eps) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a instanceof Double || b instanceof Double) {
            double x = ((Number) a).doubleValue(), y = ((Number) b).doubleValue();
            if (Double.isNaN(x) || Double.isNaN(y)) return Double.isNaN(x) && Double.isNaN(y);
            return eps > 0 ? Math.abs(x - y) <= eps : x == y;
        }
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).longValue() == ((Number) b).longValue();
        }
        if (a instanceof ListNode && b instanceof ListNode) {
            ListNode p = (ListNode) a, q = (ListNode) b;
            int guard = 0;
            while (p != null && q != null) {
                if (p.val != q.val) return false;
                p = p.next; q = q.next;
                if (++guard > 2_000_000) throw new IllegalStateException("cycle while comparing lists");
            }
            return p == null && q == null;
        }
        if (a instanceof TreeNode && b instanceof TreeNode) {
            TreeNode p = (TreeNode) a, q = (TreeNode) b;
            return p.val == q.val && eq(p.left, q.left, eps) && eq(p.right, q.right, eps);
        }
        if (a.getClass().isArray() && b.getClass().isArray()) {
            int n = java.lang.reflect.Array.getLength(a);
            if (n != java.lang.reflect.Array.getLength(b)) return false;
            for (int i = 0; i < n; i++) {
                if (!eq(java.lang.reflect.Array.get(a, i), java.lang.reflect.Array.get(b, i), eps)) return false;
            }
            return true;
        }
        if (a instanceof List && b instanceof List) {
            List<Object> x = (List<Object>) a, y = (List<Object>) b;
            if (x.size() != y.size()) return false;
            for (int i = 0; i < x.size(); i++) if (!eq(x.get(i), y.get(i), eps)) return false;
            return true;
        }
        // Cross-shape: int[] vs List<Integer> is a common and legitimate signature difference.
        if (a.getClass().isArray() && b instanceof List) return eq(toList(a), b, eps);
        if (a instanceof List && b.getClass().isArray()) return eq(a, toList(b), eps);

        return a.equals(b);
    }

    public static List<Object> toList(Object array) {
        int n = java.lang.reflect.Array.getLength(array);
        List<Object> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) out.add(java.lang.reflect.Array.get(array, i));
        return out;
    }

    /**
     * Canonical string form used to compare unordered collections. Sorting the
     * rendered form is O(n log n) and, unlike hashing, gives a stable diff to print.
     */
    public static String canonical(Object v, boolean sortInner, boolean sortOuter) {
        List<Object> outer = asList(v);
        List<String> rendered = new ArrayList<>(outer.size());
        for (Object row : outer) {
            if (sortInner && isCollection(row)) {
                List<String> inner = new ArrayList<>();
                for (Object x : asList(row)) inner.add(Codec.print(x));
                java.util.Collections.sort(inner);
                rendered.add("[" + String.join(",", inner) + "]");
            } else {
                rendered.add(Codec.print(row));
            }
        }
        if (sortOuter) java.util.Collections.sort(rendered);
        return "[" + String.join(",", rendered) + "]";
    }

    static boolean isCollection(Object v) {
        return v != null && (v instanceof List || v.getClass().isArray());
    }

    @SuppressWarnings("unchecked")
    static List<Object> asList(Object v) {
        if (v == null) return new ArrayList<>();
        if (v instanceof List) return (List<Object>) v;
        if (v.getClass().isArray()) return toList(v);
        List<Object> one = new ArrayList<>(1);
        one.add(v);
        return one;
    }
}
