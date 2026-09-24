class Ref implements Problem {
    public Spec spec() {
        return Spec.method("numSubarrayProductLessThanK", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[10,5,2,6], 100", "8").example("[1,2,3], 0", "0").example("[1,1,1], 1", "0")
            .example("[1,1,1], 2", "6").example("[5], 6", "1")
            .gen(g -> new Object[]{ g.ints(1, 40, 1, 12), g.nextInt(0, 200) })
            .patterns("Array", "Binary Search", "Sliding Window", "Prefix Sum").limitMs(2000).maxN(200000);
    }
    public int numSubarrayProductLessThanK(int[] nums, int k) {
        for (int x : nums) Pre.check(x >= 1, "1 <= nums[i]");
        if (k <= 1) return 0;
        long prod = 1;
        int l = 0, count = 0;
        for (int r = 0; r < nums.length; r++) {
            prod *= nums[r];
            while (prod >= k) prod /= nums[l++];
            count += r - l + 1;                       // subarrays ending at r
        }
        return count;
    }
}
