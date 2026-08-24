class Ref implements Problem {

    public Spec spec() {
        return Spec.method("bsCountOccurrences", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[5,7,7,8,8,10], 8", "2")
            .example("[5,7,7,8,8,10], 6", "0")
            .example("[2,2,2,2], 2",      "4")
            .example("[], 1",             "0")
            .example("[1], 1",            "1")
            .example("[1,2,3], 4",        "0")
            .gen(g -> {
                int[] a = g.sortedInts(0, 60, -8, 8);   // narrow range -> real duplicates
                int target = (a.length > 0 && g.chance(0.7))
                    ? a[g.nextInt(0, a.length - 1)]
                    : g.value(-10, 10);
                return new Object[]{ a, target };
            })
            .patterns("Array", "Binary Search")
            .limitMs(2000)
            .maxN(500000);
    }

    public int bsCountOccurrences(int[] nums, int target) {
        return firstGT(nums, target) - firstGE(nums, target);
    }

    private int firstGE(int[] a, int x) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] < x) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private int firstGT(int[] a, int x) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] <= x) lo = mid + 1; else hi = mid;
        }
        return lo;
    }
}
