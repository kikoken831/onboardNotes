class Ref implements Problem {
    public Spec spec() {
        return Spec.method("canPartition", T.INT_ARRAY).returns(T.BOOL)
            .example("[1,5,11,5]", "true").example("[1,2,3,5]", "false").example("[1,1]", "true").example("[2,2,3,5]", "false").example("[1]", "false")
            .gen(g -> new Object[]{ g.ints(1, 20, 1, 12) })
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(200);
    }
    public boolean canPartition(int[] a) {
        int s = 0;
        for (int x : a) s += x;
        if (s % 2 == 1) return false;
        boolean[] can = new boolean[s / 2 + 1];
        can[0] = true;
        for (int x : a) for (int t = s / 2; t >= x; t--) can[t] |= can[t - x];   // backwards: each item once
        return can[s / 2];
    }
}
