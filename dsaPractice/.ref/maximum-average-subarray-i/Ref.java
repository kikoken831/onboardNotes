class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findMaxAverage", T.INT_ARRAY, T.INT).returns(T.DOUBLE)
            .eps(1e-5)
            .example("[1,12,-5,-6,50,3], 4", "12.75").example("[5], 1", "5.0")
            .example("[-1], 1", "-1.0").example("[0,4,0,3,2], 1", "4.0")
            .gen(g -> { int[] a = g.ints(1, 50, -10000, 10000); return new Object[]{ a, g.nextInt(1, a.length) }; })
            .patterns("Array", "Sliding Window").limitMs(2000).maxN(200000);
    }
    public double findMaxAverage(int[] nums, int k) {
        Pre.check(k >= 1 && k <= nums.length, "1 <= k <= n");
        long s = 0;
        for (int i = 0; i < k; i++) s += nums[i];
        long best = s;
        for (int i = k; i < nums.length; i++) { s += nums[i] - nums[i - k]; best = Math.max(best, s); }
        return (double) best / k;
    }
}
