class Ref implements Problem {
    public Spec spec() {
        return Spec.method("countBits", T.INT).returns(T.INT_ARRAY)
            .example("2", "[0,1,1]").example("5", "[0,1,1,2,1,2]").example("0", "[0]")
            .gen(g -> new Object[]{ g.nextInt(0, 300) })
            .patterns("Dynamic Programming", "Bit Manipulation").limitMs(2000).maxN(100000);
    }
    public int[] countBits(int n) {
        int[] b = new int[n + 1];
        for (int i = 1; i <= n; i++) b[i] = b[i >> 1] + (i & 1);
        return b;
    }
}
