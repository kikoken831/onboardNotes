class Ref implements Problem {
    public Spec spec() {
        return Spec.method("minSubArrayLen", T.INT, T.INT_ARRAY).returns(T.INT)
            .example("7, [2,3,1,2,4,3]", "2").example("4, [1,4,4]", "1").example("11, [1,1,1,1,1,1,1,1]", "0")
            .example("15, [1,2,3,4,5]", "5")
            .gen(g -> new Object[]{ g.nextInt(1, 60), g.ints(1, 40, 1, 10) })
            .patterns("Array", "Binary Search", "Sliding Window", "Prefix Sum").limitMs(2000).maxN(200000);
    }
    public int minSubArrayLen(int target, int[] nums) {
        Pre.check(target >= 1, "1 <= target");
        for (int x : nums) Pre.check(x >= 1, "positive values");
        int l = 0, best = Integer.MAX_VALUE;
        long sum = 0;
        for (int r = 0; r < nums.length; r++) {
            sum += nums[r];
            while (sum >= target) { best = Math.min(best, r - l + 1); sum -= nums[l++]; }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }
}
