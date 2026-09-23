import java.util.ArrayList;
import java.util.List;

/**
 * Shrinks a failing input to the smallest one that still fails.
 *
 * This is the main thing an offline judge can do that leetcode.com cannot. A stress
 * failure on a 200-element array tells you almost nothing; the same failure reduced
 * to [0,0,0,0] usually tells you exactly which case you forgot.
 *
 * A shrink candidate is accepted only if the reference still runs cleanly on it AND
 * the disagreement persists. That second condition is what keeps planted-answer
 * generators (two-sum, cycle detection) from being shrunk into invalid inputs.
 */
public final class Shrink {

    /** Does this input still exhibit the failure? */
    public interface StillFails {
        boolean test(Object[] args);
    }

    private static final int MAX_PASSES = 60;

    public static Object[] minimise(Object[] failing, StillFails oracle) {
        Object[] best = failing;
        for (int pass = 0; pass < MAX_PASSES; pass++) {
            boolean improved = false;
            for (int i = 0; i < best.length; i++) {
                for (Object candidate : candidates(best[i])) {
                    Object[] trial = best.clone();
                    trial[i] = candidate;
                    if (safeTest(oracle, trial)) { best = trial; improved = true; break; }
                }
            }
            if (!improved) break;
        }
        return best;
    }

    private static boolean safeTest(StillFails oracle, Object[] args) {
        try {
            return oracle.test(Deep.copyAll(args));
        } catch (Throwable t) {
            return false; // reference blew up: not a legal shrink
        }
    }

    /** Smaller candidates for one argument, roughly most-aggressive first. */
    private static List<Object> candidates(Object v) {
        List<Object> out = new ArrayList<>();
        if (v == null) return out;

        if (v instanceof Calls) {
            // Truncating is the big win (a failure at call 40 rarely needs calls 41+);
            // then drop single calls, latest first, so setup calls survive longest.
            Calls c = (Calls) v;
            int n = c.size();
            if (n > 1) out.add(c.prefix(n / 2));
            if (n > 0) out.add(c.prefix(n - 1));
            for (int i = n - 1; i >= 0 && i >= n - 40; i--) out.add(c.without(i));
            return out;
        }

        if (v instanceof int[]) {
            int[] a = (int[]) v;
            if (a.length > 1) {
                out.add(java.util.Arrays.copyOfRange(a, 0, a.length / 2));
                out.add(java.util.Arrays.copyOfRange(a, a.length / 2, a.length));
                for (int i = 0; i < a.length && i < 24; i++) out.add(dropInt(a, i));
            }
            out.add(shrinkMagnitudes(a));
            for (int i = 0; i < a.length && i < 24; i++) {
                if (a[i] != 0) { int[] c = a.clone(); c[i] = 0; out.add(c); }
            }
            return out;
        }

        if (v instanceof String) {
            String s = (String) v;
            if (s.length() > 1) {
                out.add(s.substring(0, s.length() / 2));
                out.add(s.substring(s.length() / 2));
                for (int i = 0; i < s.length() && i < 24; i++) {
                    out.add(s.substring(0, i) + s.substring(i + 1));
                }
            }
            return out;
        }

        if (v instanceof Integer) {
            int x = (Integer) v;
            if (x != 0) { out.add(0); out.add(x / 2); if (x > 0) out.add(x - 1); else out.add(x + 1); }
            return out;
        }

        if (v instanceof int[][]) {
            int[][] g = (int[][]) v;
            if (g.length > 1) {
                out.add(java.util.Arrays.copyOfRange(g, 0, g.length / 2));
                for (int i = 0; i < g.length && i < 12; i++) out.add(dropRow(g, i));
            }
            if (g.length > 0 && g[0].length > 1) out.add(dropColumn(g));
            return out;
        }

        if (v instanceof String[]) {
            String[] a = (String[]) v;
            if (a.length > 1) {
                out.add(java.util.Arrays.copyOfRange(a, 0, a.length / 2));
                for (int i = 0; i < a.length && i < 24; i++) {
                    String[] c = new String[a.length - 1];
                    System.arraycopy(a, 0, c, 0, i);
                    System.arraycopy(a, i + 1, c, i, a.length - i - 1);
                    out.add(c);
                }
            }
            return out;
        }

        if (v instanceof ListNode[]) {
            ListNode[] a = (ListNode[]) v;
            for (int i = 0; i < a.length && i < 24; i++) {
                ListNode[] c = new ListNode[a.length - 1];
                System.arraycopy(a, 0, c, 0, i);
                System.arraycopy(a, i + 1, c, i, a.length - i - 1);
                out.add(c);
            }
            return out;
        }

        if (v instanceof ListNode) {
            // Acyclic only: shrinking a cyclic list would change what is being tested.
            int[] vals = listVals((ListNode) v);
            if (vals == null) return out;
            for (Object c : candidates(vals)) out.add(LcCodec.buildList((int[]) c));
            return out;
        }

        if (v instanceof TreeNode) {
            TreeNode t = (TreeNode) v;
            if (t.left  != null) out.add(t.left);
            if (t.right != null) out.add(t.right);
            out.add(withoutChildren(t));
            return out;
        }

        if (v instanceof List) {
            List<?> l = (List<?>) v;
            if (l.size() > 1) {
                out.add(new ArrayList<>(l.subList(0, l.size() / 2)));
                for (int i = 0; i < l.size() && i < 24; i++) {
                    List<Object> c = new ArrayList<>(l);
                    c.remove(i);
                    out.add(c);
                }
            }
            return out;
        }

        return out;
    }

    private static int[] dropInt(int[] a, int i) {
        int[] c = new int[a.length - 1];
        System.arraycopy(a, 0, c, 0, i);
        System.arraycopy(a, i + 1, c, i, a.length - i - 1);
        return c;
    }

    private static int[] shrinkMagnitudes(int[] a) {
        int[] c = new int[a.length];
        for (int i = 0; i < a.length; i++) c[i] = a[i] / 2;
        return c;
    }

    private static int[][] dropRow(int[][] g, int i) {
        int[][] c = new int[g.length - 1][];
        System.arraycopy(g, 0, c, 0, i);
        System.arraycopy(g, i + 1, c, i, g.length - i - 1);
        return c;
    }

    private static int[][] dropColumn(int[][] g) {
        int[][] c = new int[g.length][];
        for (int i = 0; i < g.length; i++) {
            c[i] = java.util.Arrays.copyOfRange(g[i], 0, Math.max(0, g[i].length - 1));
        }
        return c;
    }

    private static TreeNode withoutChildren(TreeNode t) {
        return new TreeNode(t.val);
    }

    /** @return values in order, or null if the list is cyclic. */
    private static int[] listVals(ListNode head) {
        java.util.IdentityHashMap<ListNode, Boolean> seen = new java.util.IdentityHashMap<>();
        List<Integer> vals = new ArrayList<>();
        for (ListNode p = head; p != null; p = p.next) {
            if (seen.put(p, Boolean.TRUE) != null) return null;
            vals.add(p.val);
        }
        int[] a = new int[vals.size()];
        for (int i = 0; i < a.length; i++) a[i] = vals.get(i);
        return a;
    }
}
