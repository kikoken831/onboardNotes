class Ref implements Problem {

    public Spec spec() {
        return Spec.method("searchInsert", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[1,3,5,6], 5", "2")
            .example("[1,3,5,6], 2", "1")
            .example("[1,3,5,6], 7", "4")
            .example("[1,3,5,6], 0", "0")
            .example("[1], 1",       "0")
            .example("[1], 2",       "1")
            .gen(g -> new Object[]{ g.strictlySortedInts(1, 60, -Math.max(40, g.forcedSize), Math.max(40, g.forcedSize)), g.value(-45, 45) })
            .patterns("Array", "Binary Search")
            .limitMs(2000)
            .maxN(10000);
    }

    public int searchInsert(int[] nums, int target) {
        Pre.length(nums, 1, 1000_000, "nums");
        Pre.sorted(nums, true, "nums");
        int lo = 0, hi = nums.length;            // answer lies in [lo, hi]
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] < target) lo = mid + 1; else hi = mid;
        }
        return lo;
    }
}
