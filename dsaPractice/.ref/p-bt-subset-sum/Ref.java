class Ref implements Problem {

    public Spec spec() {
        return Spec.method("subsetSum", T.INT_ARRAY, T.INT).returns(T.BOOL)
            .compare(Cmp.EXACT)
            .example("[2,4,8], 10", "true")
            .example("[2,4,8], 5",  "false")
            .example("[], 0",       "true")
            .example("[], 1",       "false")
            .example("[1,-1], 0",   "true")
            .example("[5], 5",      "true")
            .gen(g -> new Object[]{ g.ints(0, 14, -12, 12), g.value(-25, 25) })
            .patterns("Array", "Backtracking", "Dynamic Programming")
            .limitMs(3000)
            .maxN(18);
    }

    public boolean subsetSum(int[] nums, int target) {
        return go(nums, 0, target);
    }

    private boolean go(int[] nums, int i, int remaining) {
        if (remaining == 0) return true;
        if (i == nums.length) return false;
        return go(nums, i + 1, remaining - nums[i]) || go(nums, i + 1, remaining);
    }
}
