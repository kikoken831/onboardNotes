class Ref implements Problem {
    public Spec spec() {
        return Spec.method("combinationSum4", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[1,2,3], 4", "7").example("[9], 3", "0").example("[1], 1", "1").example("[2,1], 3", "3")
            .gen(g -> new Object[]{ g.distinctInts(1, 5, 1, 8), g.nextInt(1, 25) })
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(1000);
    }
    public int combinationSum4(int[] nums, int target) {
        long[] d = new long[target + 1];
        d[0] = 1;
        for (int t = 1; t <= target; t++) for (int x : nums) if (x <= t) d[t] += d[t - x];   // target outer: order matters
        return (int) d[target];
    }
}
