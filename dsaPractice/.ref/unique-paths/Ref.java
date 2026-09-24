class Ref implements Problem {
    public Spec spec() {
        return Spec.method("uniquePaths", T.INT, T.INT).returns(T.INT)
            .example("3, 7", "28").example("3, 2", "3").example("1, 1", "1").example("23, 12", "193536720")
            .gen(g -> new Object[]{ g.nextInt(1, 16), g.nextInt(1, 16) })
            .patterns("Math", "Dynamic Programming", "Combinatorics").limitMs(2000).maxN(20);
    }
    public int uniquePaths(int m, int n) {
        int[] d = new int[n];
        java.util.Arrays.fill(d, 1);
        for (int i = 1; i < m; i++) for (int j = 1; j < n; j++) d[j] += d[j - 1];
        return d[n - 1];
    }
}
