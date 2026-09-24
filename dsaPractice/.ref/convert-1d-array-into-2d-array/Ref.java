class Ref implements Problem {
    public Spec spec() {
        return Spec.method("construct2DArray", T.INT_ARRAY, T.INT, T.INT).returns(T.INT_ARRAY_2D)
            .example("[1,2,3,4], 2, 2", "[[1,2],[3,4]]").example("[1,2,3], 1, 3", "[[1,2,3]]")
            .example("[1,2], 1, 1", "[]").example("[3], 1, 2", "[]").example("[1,2,3,4], 4, 1", "[[1],[2],[3],[4]]")
            .gen(g -> {
                int[] a = g.ints(1, 24, 1, 9);
                int m = g.nextInt(1, 6), n = g.chance(0.6) && a.length % m == 0 ? a.length / m : g.nextInt(1, 6);
                return new Object[]{ a, m, n };
            })
            .patterns("Array", "Matrix", "Simulation").limitMs(2000).maxN(100000);
    }
    public int[][] construct2DArray(int[] original, int m, int n) {
        Pre.check(m >= 1 && n >= 1, "1 <= m, n");
        if ((long) m * n != original.length) return new int[0][];
        int[][] out = new int[m][n];
        for (int i = 0; i < original.length; i++) out[i / n][i % n] = original[i];
        return out;
    }
}
