class Ref implements Problem {

    public Spec spec() {
        return Spec.method("bsFirstGT", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[1,3,5,6], 5", "3")
            .example("[1,3,5,6], 2", "1")
            .example("[1,3,5,6], 6", "4")
            .example("[2,2,2], 2",   "3")
            .example("[2,2,2], 1",   "0")
            .example("[], 1",        "0")
            .gen(g -> {
                int[] a = g.sortedInts(0, 60, -12, 12);
                int x = (a.length > 0 && g.chance(0.6))
                    ? a[g.nextInt(0, a.length - 1)]
                    : g.value(-15, 15);
                return new Object[]{ a, x };
            })
            .patterns("Array", "Binary Search")
            .limitMs(2000)
            .maxN(500000);
    }

    public int bsFirstGT(int[] nums, int x) {
        int lo = 0, hi = nums.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] <= x) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
