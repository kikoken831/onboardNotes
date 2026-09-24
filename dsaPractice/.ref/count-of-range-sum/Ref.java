class Ref implements Problem {
    public Spec spec() {
        return Spec.method("countRangeSum", T.INT_ARRAY, T.INT, T.INT).returns(T.INT)
            .example("[-2,5,-1], -2, 2", "3").example("[0], 0, 0", "1")
            .example("[2147483647,-2147483648,-1,0], -1, 0", "4")
            .example("[2147483647,2147483647,2], 0, 0", "0")                    // int prefix sums wrap to exactly 0
            .gen(g -> {
                int[] a = g.chance(0.2) ? g.ints(1, 30, Integer.MIN_VALUE, Integer.MAX_VALUE) : g.ints(1, 40, -10, 10);
                int x = g.nextInt(-15, 15), y = g.nextInt(-15, 15);
                return new Object[]{ a, Math.min(x, y), Math.max(x, y) };
            })
            .patterns("Array", "Binary Search", "Divide and Conquer", "Binary Indexed Tree", "Merge Sort").limitMs(2000).maxN(100000);
    }
    public int countRangeSum(int[] nums, int lower, int upper) {
        Pre.check(lower <= upper, "lower <= upper");
        long[] pre = new long[nums.length + 1];
        for (int i = 0; i < nums.length; i++) pre[i + 1] = pre[i] + nums[i];
        return count(pre, new long[pre.length], 0, pre.length, lower, upper);
    }
    /** Merge sort over prefix sums, counting pairs i < j with lower <= pre[j] - pre[i] <= upper. */
    private int count(long[] p, long[] tmp, int lo, int hi, int lower, int upper) {
        if (hi - lo <= 1) return 0;
        int mid = (lo + hi) / 2, c = count(p, tmp, lo, mid, lower, upper) + count(p, tmp, mid, hi, lower, upper);
        for (int i = lo, a = mid, b = mid; i < mid; i++) {
            while (a < hi && p[a] - p[i] < lower) a++;
            while (b < hi && p[b] - p[i] <= upper) b++;
            c += b - a;
        }
        System.arraycopy(p, lo, tmp, lo, hi - lo);
        for (int i = lo, x = lo, y = mid; i < hi; i++) p[i] = y >= hi || (x < mid && tmp[x] <= tmp[y]) ? tmp[x++] : tmp[y++];
        return c;
    }
}
