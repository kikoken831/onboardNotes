class Ref implements Problem {

    public Spec spec() {
        return Spec.method("lisLength", T.INT_ARRAY).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[10,9,2,5,3,7,101,18]", "4")
            .example("[0,1,0,3,2,3]",         "4")
            .example("[7,7,7,7]",             "1")
            .example("[]",                    "0")
            .example("[5]",                   "1")
            .example("[3,2,1]",               "1")
            .gen(g -> new Object[]{ g.ints(0, 50, -10, 10) })
            .patterns("Array", "Dynamic Programming", "Longest Increasing Subsequence")
            .limitMs(3000)
            .maxN(2500);
    }

    public int lisLength(int[] nums) {
        int[] f = new int[nums.length];
        int best = 0;
        for (int i = 0; i < nums.length; i++) {
            f[i] = 1;
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) f[i] = Math.max(f[i], f[j] + 1);
            }
            best = Math.max(best, f[i]);
        }
        return best;
    }
}
