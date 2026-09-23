class Ref implements Problem {

    public Spec spec() {
        return Spec.method("shuffle", T.INT_ARRAY, T.INT).returns(T.INT_ARRAY)
            .example("[2,5,1,3,4,7], 3",     "[2,3,5,4,1,7]")
            .example("[1,2,3,4,4,3,2,1], 4", "[1,4,2,3,3,2,4,1]")
            .example("[1,1,2,2], 2",         "[1,2,1,2]")
            .example("[9,8], 1",             "[9,8]")
            .gen(g -> {
                int n = g.size(1, 30);
                return new Object[]{ g.ints(2 * n, 1, 1000), n };
            })
            .patterns("Array")
            .limitMs(2000)
            .maxN(100000);
    }

    public int[] shuffle(int[] nums, int n) {
        Pre.check(n >= 1 && nums.length == 2 * n, "nums.length == 2n");
        int[] ans = new int[2 * n];
        for (int i = 0; i < n; i++) {
            ans[2 * i]     = nums[i];
            ans[2 * i + 1] = nums[n + i];
        }
        return ans;
    }
}
