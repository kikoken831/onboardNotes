class Ref implements Problem {
    public Spec spec() {
        return Spec.method("coinChange", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[1,2,5], 11", "3").example("[2], 3", "-1").example("[1], 0", "0").example("[186,419,83,408], 6249", "20")
            .example("[1,3,4], 6", "2")                                           // greedy gives 3
            .gen(g -> new Object[]{ g.distinctInts(1, 5, 1, 12), g.nextInt(0, 60) })
            .patterns("Array", "Dynamic Programming", "Breadth-First Search").limitMs(2000).maxN(10000);
    }
    public int coinChange(int[] coins, int amount) {
        for (int c : coins) Pre.check(c >= 1, "1 <= coins[i]");
        Pre.check(amount >= 0 && amount <= 10_000, "0 <= amount <= 10^4");
        int[] d = new int[amount + 1];
        java.util.Arrays.fill(d, Integer.MAX_VALUE);
        d[0] = 0;
        for (int x = 1; x <= amount; x++) for (int c : coins) if (c <= x && d[x - c] != Integer.MAX_VALUE) d[x] = Math.min(d[x], d[x - c] + 1);
        return d[amount] == Integer.MAX_VALUE ? -1 : d[amount];
    }
}
