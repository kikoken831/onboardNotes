class Ref implements Problem {

    public Spec spec() {
        return Spec.method("getConcatenation", T.INT_ARRAY).returns(T.INT_ARRAY)
            .example("[1,2,1]",   "[1,2,1,1,2,1]")
            .example("[1,3,2,1]", "[1,3,2,1,1,3,2,1]")
            .example("[7]",       "[7,7]")
            .gen(g -> new Object[]{ g.ints(1, 60, 1, 1000) })
            .patterns("Array")
            .limitMs(2000)
            .maxN(100000);
    }

    public int[] getConcatenation(int[] nums) {
        Pre.length(nums, 1, 1000_000, "nums");
        int n = nums.length;
        int[] ans = new int[2 * n];
        for (int i = 0; i < n; i++) ans[i] = ans[i + n] = nums[i];
        return ans;
    }
}
