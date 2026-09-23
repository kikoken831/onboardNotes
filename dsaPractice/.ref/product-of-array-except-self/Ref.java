class Ref implements Problem {
    public Spec spec() {
        return Spec.method("productExceptSelf", T.INT_ARRAY).returns(T.INT_ARRAY)
            .example("[1,2,3,4]",      "[24,12,8,6]")
            .example("[-1,1,0,-3,3]",  "[0,0,9,0,0]")
            .example("[0,0]",          "[0,0]")
            .example("[2,3]",          "[3,2]")
            .gen(g -> new Object[]{ g.ints(2, 12, -3, 3) })     // keeps every product within int
            .patterns("Array", "Prefix Sum").limitMs(2000).maxN(200000);
    }
    public int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        Pre.check(n >= 2, "2 <= n");
        int[] out = new int[n];
        out[0] = 1;
        for (int i = 1; i < n; i++) out[i] = out[i - 1] * nums[i - 1];
        int right = 1;
        for (int i = n - 1; i >= 0; i--) { out[i] *= right; right *= nums[i]; }
        return out;
    }
}
