class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findTargetSumWays", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[1,1,1,1,1], 3", "5").example("[1], 1", "1").example("[0,0,1], 1", "4").example("[1], 2", "0")
            .example("[100], -200", "0")
            .gen(g -> new Object[]{ g.ints(1, 16, 0, 6), g.nextInt(-20, 20) })
            .patterns("Array", "Dynamic Programming", "Backtracking").limitMs(2000).maxN(20);
    }
    public int findTargetSumWays(int[] a, int target) {
        int s = 0;
        for (int x : a) s += x;
        if (Math.abs(target) > s || (s + target) % 2 != 0) return 0;
        int want = (s + target) / 2;                        // P - N = target, P + N = s
        int[] d = new int[want + 1];
        d[0] = 1;
        for (int x : a) for (int t = want; t >= x; t--) d[t] += d[t - x];
        return d[want];
    }
}
