class Ref implements Problem {

    public Spec spec() {
        return Spec.method("knapsackReachable", T.INT_ARRAY, T.INT).returns(T.BOOL)
            .compare(Cmp.EXACT)
            .example("[1,5,11,5], 11", "true")
            .example("[1,2,3,5], 5",   "true")
            .example("[1,2,5], 4",     "false")
            .example("[1], 0",         "true")
            .example("[0], 0",         "true")
            .example("[100], 100",     "true")
            .example("[1], 2",         "false")   // catches the upward-loop bug
            .gen(g -> {
                int n = g.size(1, 14);
                int[] nums = new int[n];
                for (int i = 0; i < n; i++) nums[i] = g.nextInt(0, 12);
                return new Object[]{ nums, g.nextInt(0, 30) };
            })
            .patterns("Array", "Dynamic Programming", "0-1 Knapsack")
            .limitMs(3000)
            .maxN(2000);
    }

    public boolean knapsackReachable(int[] nums, int target) {
        boolean[] reachable = new boolean[target + 1];
        reachable[0] = true;
        for (int x : nums) {
            for (int s = target; s >= x; s--) {
                if (reachable[s - x]) reachable[s] = true;
            }
        }
        return reachable[target];
    }
}
