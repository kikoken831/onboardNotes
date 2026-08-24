class Ref implements Problem {

    public Spec spec() {
        return Spec.method("bsFirstGE", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[1,3,5,6], 5", "2")
            .example("[1,3,5,6], 2", "1")
            .example("[1,3,5,6], 7", "4")
            .example("[1,3,5,6], 0", "0")
            .example("[2,2,2], 2",   "0")
            .example("[], 1",        "0")
            .example("[1,1,2,2,3], 2", "2")
            .gen(g -> {
                int[] a = g.sortedInts(0, 60, -12, 12);   // duplicates on purpose
                int x = (a.length > 0 && g.chance(0.6))
                    ? a[g.nextInt(0, a.length - 1)]
                    : g.value(-15, 15);
                return new Object[]{ a, x };
            })
            .patterns("Array", "Binary Search")
            .limitMs(2000)
            .maxN(500000);
    }

    public int bsFirstGE(int[] nums, int x) {
        int lo = 0, hi = nums.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] < x) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
