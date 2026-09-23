class Ref implements Problem {

    public Spec spec() {
        return Spec.method("buildArray", T.INT_ARRAY).returns(T.INT_ARRAY)
            .example("[0,2,1,5,3,4]", "[0,1,2,4,5,3]")
            .example("[5,0,1,2,3,4]", "[4,5,0,1,2,3]")
            .example("[0]",           "[0]")
            .example("[1,0]",         "[0,1]")
            .gen(g -> new Object[]{ g.permutation(g.size(1, 60)) })
            .patterns("Array", "Simulation")
            .limitMs(2000)
            .maxN(100000);
    }

    public int[] buildArray(int[] nums) {
        int n = nums.length;
        Pre.check(n >= 1, "1 <= nums.length");
        boolean[] seen = new boolean[n];
        for (int v : nums) {
            Pre.check(v >= 0 && v < n && !seen[v], "nums is a permutation of 0..n-1");
            seen[v] = true;
        }
        int[] ans = new int[n];
        for (int i = 0; i < n; i++) ans[i] = nums[nums[i]];
        return ans;
    }
}
