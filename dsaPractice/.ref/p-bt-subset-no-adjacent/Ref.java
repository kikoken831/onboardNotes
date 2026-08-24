class Ref implements Problem {

    public Spec spec() {
        return Spec.method("countNonAdjacentSubsets", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[2,4,6], 8", "1")
            .example("[1,1,1], 2", "1")
            .example("[], 0",      "1")
            .example("[], 1",      "0")
            .example("[5], 5",     "1")
            .example("[3,3], 3",   "2")
            .gen(g -> new Object[]{ g.ints(0, 14, -6, 6), g.value(-12, 12) })
            .patterns("Array", "Backtracking", "Dynamic Programming")
            .limitMs(3000)
            .maxN(18);
    }

    public int countNonAdjacentSubsets(int[] nums, int target) {
        return go(nums, 0, target);
    }

    private int go(int[] nums, int i, int remaining) {
        if (i >= nums.length) return remaining == 0 ? 1 : 0;
        int take = go(nums, i + 2, remaining - nums[i]);
        int skip = go(nums, i + 1, remaining);
        return take + skip;
    }
}
