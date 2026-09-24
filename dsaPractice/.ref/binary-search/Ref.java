class Ref implements Problem {
    public Spec spec() {
        return Spec.method("search", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[-1,0,3,5,9,12], 9", "4").example("[-1,0,3,5,9,12], 2", "-1").example("[5], 5", "0")
            .example("[5], -5", "-1").example("[1,3], 3", "1")
            .gen(g -> new Object[]{ g.strictlySortedInts(1, 60, -40, 40), g.value(-45, 45) })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(200000);
    }
    public int search(int[] nums, int target) {
        Pre.sorted(nums, true, "nums");
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int m = lo + (hi - lo) / 2;
            if (nums[m] == target) return m;
            if (nums[m] < target) lo = m + 1; else hi = m - 1;
        }
        return -1;
    }
}
