class Ref implements Problem {

    public Spec spec() {
        return Spec.method("minCostClimb", T.INT_ARRAY).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[10,15,20]",                    "15")
            .example("[1,100,1,1,1,100,1,1,100,1]",   "6")
            .example("[0,0]",                         "0")
            .example("[5,5]",                         "5")
            .example("[1,2]",                         "1")
            .gen(g -> {
                int n = Math.max(2, g.size(2, 40));
                int[] cost = new int[n];
                for (int i = 0; i < n; i++) cost[i] = g.nextInt(0, 30);
                return new Object[]{ cost };
            })
            .patterns("Array", "Dynamic Programming")
            .limitMs(2000)
            .maxN(200000);
    }

    public int minCostClimb(int[] cost) {
        int prev2 = 0, prev1 = 0;          // f(0), f(1)
        for (int i = 2; i <= cost.length; i++) {
            int cur = Math.min(prev1 + cost[i - 1], prev2 + cost[i - 2]);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}
