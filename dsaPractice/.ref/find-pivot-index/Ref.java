class Ref implements Problem {

    public Spec spec() {
        return Spec.method("pivotIndex", T.INT_ARRAY).returns(T.INT)
            .example("[1,7,3,6,5,6]",     "3")
            .example("[1,2,3]",           "-1")
            .example("[2,1,-1]",          "0")
            .example("[0]",               "0")
            .example("[-1,-1,0,1,1,0]",   "5")
            .example("[0,0,0]",           "0")
            .gen(g -> new Object[]{ g.ints(1, 60, -4, 4) })
            .patterns("Array", "Prefix Sum")
            .limitMs(2000)
            .maxN(200000);
    }

    public int pivotIndex(int[] nums) {
        Pre.length(nums, 1, 1000_000, "nums");
        long total = 0;
        for (int x : nums) total += x;
        long left = 0;
        for (int i = 0; i < nums.length; i++) {
            if (left == total - left - nums[i]) return i;
            left += nums[i];
        }
        return -1;
    }
}
