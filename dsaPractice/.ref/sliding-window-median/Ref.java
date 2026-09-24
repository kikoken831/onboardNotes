import java.util.TreeMap;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("medianSlidingWindow", T.INT_ARRAY, T.INT).returns(T.DOUBLE_ARRAY)
            .eps(1e-5)
            .example("[1,3,-1,-3,5,3,6,7], 3", "[1.0,-1.0,-1.0,3.0,5.0,6.0]")
            .example("[1,2,3,4,2,3,1,4,2], 3", "[2.0,3.0,3.0,3.0,2.0,3.0,2.0]")
            .example("[2147483647,2147483647], 2", "[2147483647.0]")      // sum overflows int
            .example("[1,4,2,3], 4", "[2.5]")
            .gen(g -> {
                int[] a = g.chance(0.2) ? g.ints(1, 30, Integer.MIN_VALUE, Integer.MAX_VALUE) : g.ints(1, 30, -9, 9);
                return new Object[]{ a, g.nextInt(1, a.length) };
            })
            .patterns("Array", "Hash Table", "Sliding Window", "Heap (Priority Queue)").limitMs(2000).maxN(50000);
    }
    public double[] medianSlidingWindow(int[] nums, int k) {
        Pre.check(k >= 1 && k <= nums.length, "1 <= k <= n");
        double[] out = new double[nums.length - k + 1];
        // Reference by brute force on a sorted multiset: correct first, fast second.
        TreeMap<Integer, Integer> w = new TreeMap<>();
        for (int i = 0; i < nums.length; i++) {
            w.merge(nums[i], 1, Integer::sum);
            if (i >= k) { if (w.merge(nums[i - k], -1, Integer::sum) == 0) w.remove(nums[i - k]); }
            if (i >= k - 1) out[i - k + 1] = median(w, k);
        }
        return out;
    }
    private static double median(TreeMap<Integer, Integer> w, int k) {
        int lo = (k - 1) / 2, hi = k / 2, seen = 0;
        Integer a = null, b = null;
        for (java.util.Map.Entry<Integer, Integer> e : w.entrySet()) {
            int next = seen + e.getValue();
            if (a == null && lo < next) a = e.getKey();
            if (b == null && hi < next) { b = e.getKey(); break; }
            seen = next;
        }
        return ((long) a + (long) b) / 2.0;
    }
}
