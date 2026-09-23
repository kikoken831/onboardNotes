class Ref implements Problem {

    public Spec spec() {
        return Spec.method("minCostClimbingStairs", T.INT_ARRAY).returns(T.INT)
            .example("[10,15,20]",                   "15")
            .example("[1,100,1,1,1,100,1,1,100,1]",  "6")
            .example("[0,0]",                        "0")
            .example("[5,1]",                        "1")
            .gen(g -> new Object[]{ g.ints(2, 60, 0, 999) })
            .patterns("Array", "Dynamic Programming")
            .limitMs(2000)
            .maxN(200000);
    }

    public int minCostClimbingStairs(int[] cost) {
        Pre.length(cost, 2, 1000_000, "cost");
        int a = 0, b = 0;                 // min cost to stand on step i-2, i-1
        for (int i = 2; i <= cost.length; i++) {
            int c = Math.min(b + cost[i - 1], a + cost[i - 2]);
            a = b;
            b = c;
        }
        return b;
    }
}
