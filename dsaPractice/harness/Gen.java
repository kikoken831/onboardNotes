import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Seeded random input generation.
 *
 * Every generator is deterministic given the seed, so a failing case is always
 * reproducible with `judge run &lt;slug&gt; --seed N`.
 *
 * `scale` runs 0.0 -&gt; 1.0 across a stress run. Early cases are tiny (which is where
 * edge-case bugs live and where shrinking has the least work to do) and later cases
 * approach the problem's stated constraints.
 */
public final class Gen {

    private final Random rnd;
    /** 0.0 at the start of a stress run, 1.0 at the end. */
    public double scale = 1.0;

    /**
     * When >= 0, every call to size() returns exactly this (subject to the caller's
     * minimum). Timing mode sets it to walk a deterministic ladder of input sizes;
     * the caller's `max` is deliberately ignored, because the stress-test bound and
     * the timing bound are different numbers.
     */
    public int forcedSize = -1;
    private boolean forcedUsed;

    public Gen(long seed) { this.rnd = new Random(seed); }

    // ------------------------------------------------------------- primitives

    public int nextInt(int loInclusive, int hiInclusive) {
        if (hiInclusive < loInclusive) throw new IllegalArgumentException("empty range");
        long span = (long) hiInclusive - loInclusive + 1;
        return (int) (loInclusive + Math.floorMod(rnd.nextLong(), span));
    }

    /**
     * A value from [lo,hi], biased toward the values that actually break solutions.
     *
     * Uniform sampling from a wide range is nearly useless for differential testing:
     * it almost never produces 0, duplicates, adjacent values, or boundary values, so
     * whole classes of bug survive thousands of iterations. This narrows the range
     * hard at low `scale` and injects boundary values throughout.
     */
    public int value(int lo, int hi) {
        if (chance(0.12)) {                       // boundary and identity values
            int[] special = { 0, 1, -1, lo, hi, 2, -2 };
            int v = special[nextInt(0, special.length - 1)];
            return Math.max(lo, Math.min(hi, v));
        }
        // Range width grows with scale, so early iterations are dense with collisions.
        double width = 0.02 + 0.98 * scale * scale;
        long span = (long) hi - lo + 1;
        long narrowed = Math.max(2, (long) (span * width));
        long centre = (lo + (long) hi) / 2;
        long half = narrowed / 2;
        int nlo = (int) Math.max(lo, centre - half);
        int nhi = (int) Math.min(hi, centre + half);
        if (nhi < nlo) return lo;
        return nextInt(nlo, nhi);
    }

    public boolean chance(double p) { return rnd.nextDouble() < p; }

    public double nextDouble() { return rnd.nextDouble(); }

    /**
     * Size scaled between min and max, biased small early in a run.
     *
     * The ramp is deliberately fast: `scale` reaches useful sizes within the first
     * few percent of a run rather than climbing linearly. A linear ramp spends the
     * first half of a stress run on inputs of length 0 and 1, which are too small to
     * exercise anything, and bugs that need a handful of elements survive thousands
     * of iterations. sqrt(scale) plus a floor fixes that.
     */
    public int size(int min, int max) {
        // Timing: the first size drawn is the primary dimension and takes n. Later ones
        // (each word's length, each list's length, a second array) stay within their own
        // bounds -- forcing them all to n makes nested inputs n² and runs out of memory.
        if (forcedSize >= 0 && !forcedUsed) { forcedUsed = true; return Math.max(min, forcedSize); }
        if (max <= min) return min;
        double ramp = Math.sqrt(Math.max(0.0, Math.min(1.0, scale)));
        int top = (int) Math.round(min + (max - min) * ramp);
        // Keep a floor of ~4 elements once past the very start of the run, so that
        // degenerate-but-tiny inputs do not crowd out everything else.
        int floor = scale > 0.02 ? Math.min(max, min + 3) : min;
        top = Math.max(top, floor);
        return nextInt(Math.min(min, top), Math.max(min, top));
    }

    // ---------------------------------------------------------------- arrays

    public int[] ints(int n, int lo, int hi) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = value(lo, hi);
        return a;
    }

    public int[] ints(int minLen, int maxLen, int lo, int hi) {
        return ints(size(minLen, maxLen), lo, hi);
    }

    /** Values drawn from a deliberately narrow band, so duplicates are common. */
    public int[] intsWithDuplicates(int minLen, int maxLen, int distinct) {
        int n = size(minLen, maxLen);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = nextInt(0, Math.max(0, distinct - 1));
        return a;
    }

    public int[] sortedInts(int minLen, int maxLen, int lo, int hi) {
        int[] a = ints(minLen, maxLen, lo, hi);
        Arrays.sort(a);
        return a;
    }

    public int[] strictlySortedInts(int minLen, int maxLen, int lo, int hi) {
        int n = size(minLen, maxLen);
        java.util.TreeSet<Integer> s = new java.util.TreeSet<>();
        int guard = 0;
        while (s.size() < n && guard++ < n * 40) s.add(nextInt(lo, hi));
        int[] a = new int[s.size()];
        int i = 0;
        for (int v : s) a[i++] = v;
        return a;
    }

    /** Distinct values from [lo, hi] in random order. */
    public int[] distinctInts(int minLen, int maxLen, int lo, int hi) {
        int[] a = strictlySortedInts(minLen, maxLen, lo, hi);
        shuffle(a);
        return a;
    }

    /** A permutation of 0..n-1. */
    public int[] permutation(int n) {
        List<Integer> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) l.add(i);
        Collections.shuffle(l, rnd);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = l.get(i);
        return a;
    }

    /** One side of a 2-D input: like size(), but at a forced timing size n the grid gets ~n cells, not n². */
    public int side(int min, int max) {
        if (forcedSize >= 0) return Math.max(min, (int) Math.ceil(Math.sqrt(forcedSize)));
        return size(min, max);
    }

    public int[][] grid(int minR, int maxR, int minC, int maxC, int lo, int hi) {
        int r = side(minR, maxR), c = side(minC, maxC);
        int[][] g = new int[r][c];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) g[i][j] = nextInt(lo, hi);
        return g;
    }

    // --------------------------------------------------------------- strings

    public String string(int minLen, int maxLen, String alphabet) {
        int n = size(minLen, maxLen);
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) b.append(alphabet.charAt(nextInt(0, alphabet.length() - 1)));
        return b.toString();
    }

    public String lower(int minLen, int maxLen)  { return string(minLen, maxLen, "abcdefghijklmnopqrstuvwxyz"); }
    public String lower3(int minLen, int maxLen) { return string(minLen, maxLen, "abc"); }
    public String binary(int minLen, int maxLen) { return string(minLen, maxLen, "01"); }

    public String[] words(int minCount, int maxCount, int minLen, int maxLen, String alphabet) {
        int n = size(minCount, maxCount);
        String[] w = new String[n];
        for (int i = 0; i < n; i++) w[i] = string(minLen, maxLen, alphabet);
        return w;
    }

    // --------------------------------------------------------------- structures

    public ListNode list(int minLen, int maxLen, int lo, int hi) {
        return LcCodec.buildList(ints(minLen, maxLen, lo, hi));
    }

    public ListNode sortedList(int minLen, int maxLen, int lo, int hi) {
        return LcCodec.buildList(sortedInts(minLen, maxLen, lo, hi));
    }

    /** Random-shaped binary tree (not necessarily balanced). Returns null sometimes. */
    public TreeNode tree(int minNodes, int maxNodes, int lo, int hi) {
        int n = size(minNodes, maxNodes);
        if (n == 0) return null;
        TreeNode root = new TreeNode(nextInt(lo, hi));
        List<TreeNode> open = new ArrayList<>();
        open.add(root);
        for (int i = 1; i < n; i++) {
            int k = nextInt(0, open.size() - 1);
            TreeNode parent = open.get(k);
            TreeNode child = new TreeNode(nextInt(lo, hi));
            if (parent.left == null && (parent.right != null || chance(0.5))) {
                parent.left = child;
            } else if (parent.right == null) {
                parent.right = child;
            } else {
                open.remove(k);
                i--;
                continue;
            }
            if (parent.left != null && parent.right != null) open.remove(k);
            open.add(child);
        }
        return root;
    }

    /** A valid BST over distinct values. */
    public TreeNode bst(int minNodes, int maxNodes, int lo, int hi) {
        if (forcedSize >= 0) hi = Math.max(hi, lo + 2 * forcedSize);   // timing: room for n distinct keys
        int[] vals = strictlySortedInts(minNodes, maxNodes, lo, hi);
        return bstFrom(vals, 0, vals.length - 1);
    }

    private TreeNode bstFrom(int[] v, int lo, int hi) {
        if (lo > hi) return null;
        int mid = lo + (hi - lo) / 2;
        TreeNode n = new TreeNode(v[mid]);
        n.left  = bstFrom(v, lo, mid - 1);
        n.right = bstFrom(v, mid + 1, hi);
        return n;
    }

    /** Directed acyclic edge list over n nodes, as {from, to} pairs. */
    public int[][] dag(int n, int maxEdges) {
        int[] order = permutation(n);
        List<int[]> edges = new ArrayList<>();
        int target = Math.min(maxEdges, n * 2);
        for (int e = 0; e < target; e++) {
            int i = nextInt(0, n - 1), j = nextInt(0, n - 1);
            if (i == j) continue;
            if (i > j) { int t = i; i = j; j = t; }
            edges.add(new int[]{order[i], order[j]});
        }
        return edges.toArray(new int[0][]);
    }

    // ------------------------------------------------- answer-planting helpers

    /**
     * An array plus a target that is the sum of exactly one pair.
     * Returned as {int[] nums, int target}.
     *
     * Guaranteeing a unique answer is why this has to be code rather than config.
     * Note it deliberately permits zeros and duplicate values — a generator that
     * draws only distinct values from a wide range never produces the degenerate
     * inputs where two-sum solutions actually break.
     */
    public Object[] twoSumInput(int minLen, int maxLen, int lo, int hi) {
        int n = Math.max(2, size(minLen, maxLen));

        // Large n only happens in timing mode. Two requirements differ from stress mode:
        // the O(n^2) uniqueness check below would dominate the measurement, and the
        // answer must sit at the very end or an early-returning solution's runtime
        // measures where the answer landed rather than how the algorithm scales.
        if (n > 300) {
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) nums[i] = i - n / 2;
            shuffle(nums);
            // Force worst case: put the unique maximal pair in the last two slots.
            int m1 = 0, m2 = 1;
            for (int i = 0; i < n; i++) {
                if (nums[i] > nums[m1]) { m2 = m1; m1 = i; }
                else if (i != m1 && nums[i] > nums[m2]) { m2 = i; }
            }
            swap(nums, m1, n - 1);
            swap(nums, m2 == n - 1 ? m1 : m2, n - 2);
            return new Object[]{ nums, nums[n - 1] + nums[n - 2] };
        }

        for (int attempt = 0; attempt < 12; attempt++) {
            int[] nums = ints(n, lo, hi);
            Integer target = uniquePairSum(nums);
            if (target != null) return new Object[]{ nums, target };
        }
        // Fallback (the range is too narrow for random values to have a unique pair sum).
        // Plant a, b >= 0 and make every other value >= a + b + 1. Then any pair that
        // includes a filler sums to more than a + b (its partner is >= 0), so (a, b) is
        // the ONLY pair hitting the target -- duplicates among the fillers included.
        int base = Math.max(0, lo);
        int a = nextInt(base, Math.max(base, Math.min(hi, base + 3)));
        int b = nextInt(base, Math.max(base, Math.min(hi, base + 3)));
        int target = a + b;
        int fLo = target + 1;
        int fHi = Math.max(fLo, Math.min(hi, fLo + 3 * n));   // may exceed hi if the range is tiny
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = nextInt(fLo, fHi);
        int i1 = nextInt(0, n - 1), i2 = nextInt(0, n - 1);
        while (i2 == i1) i2 = nextInt(0, n - 1);
        nums[i1] = a;
        nums[i2] = b;
        if (pairsSumming(nums, target) != 1) {
            throw new IllegalStateException("twoSumInput fallback planted a non-unique pair");
        }
        return new Object[]{ nums, target };
    }

    /** Number of unordered index pairs summing to `target`. */
    static int pairsSumming(int[] nums, int target) {
        int c = 0;
        for (int i = 0; i < nums.length; i++)
            for (int j = i + 1; j < nums.length; j++)
                if ((long) nums[i] + nums[j] == target) c++;
        return c;
    }

    private void shuffle(int[] a) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = nextInt(0, i);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    private void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

    /** A pair sum achieved by exactly one unordered pair, or null if there is none. */
    private Integer uniquePairSum(int[] nums) {
        java.util.HashMap<Long, Integer> counts = new java.util.HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                long s = (long) nums[i] + nums[j];
                counts.merge(s, 1, Integer::sum);
            }
        }
        List<Long> singles = new ArrayList<>();
        for (java.util.Map.Entry<Long, Integer> e : counts.entrySet()) {
            if (e.getValue() == 1 && e.getKey() == (int) (long) e.getKey()) singles.add(e.getKey());
        }
        if (singles.isEmpty()) return null;
        Collections.sort(singles);
        return (int) (long) singles.get(nextInt(0, singles.size() - 1));
    }


    /** A linked list that may contain a cycle. Returns {head, expectedCycleIndex}. */
    public Object[] listMaybeCycle(int minLen, int maxLen, int lo, int hi) {
        int n = Math.max(1, size(minLen, maxLen));
        int[] vals = ints(n, lo, hi);
        ListNode head = LcCodec.buildList(vals);
        int pos = chance(0.5) ? nextInt(0, n - 1) : -1;
        if (pos >= 0) {
            ListNode tail = head, at = head;
            while (tail.next != null) tail = tail.next;
            for (int i = 0; i < pos; i++) at = at.next;
            tail.next = at;
        }
        return new Object[]{ head, pos };
    }
}
