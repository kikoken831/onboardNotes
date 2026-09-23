class Ref implements Problem {

    public Spec spec() {
        return Spec.method("matrixReshape", T.INT_ARRAY_2D, T.INT, T.INT).returns(T.INT_ARRAY_2D)
            .example("[[1,2],[3,4]], 1, 4", "[[1,2,3,4]]")
            .example("[[1,2],[3,4]], 2, 4", "[[1,2],[3,4]]")      // impossible: return as-is
            .example("[[1,2,3,4]], 2, 2",   "[[1,2],[3,4]]")
            .example("[[1,2],[3,4]], 4, 1", "[[1],[2],[3],[4]]")
            .example("[[1,2],[3,4]], 2, 2", "[[1,2],[3,4]]")
            .gen(g -> {
                int[][] a = g.grid(1, 8, 1, 8, -9, 9);
                int total = a.length * a[0].length, r, c;
                if (g.chance(0.6)) {                       // a valid target shape
                    java.util.List<Integer> div = new java.util.ArrayList<>();
                    for (int d = 1; d <= total; d++) if (total % d == 0) div.add(d);
                    r = div.get(g.nextInt(0, div.size() - 1));
                    c = total / r;
                } else {
                    r = g.nextInt(1, 10); c = g.nextInt(1, 10);
                }
                return new Object[]{ a, r, c };
            })
            .patterns("Array", "Matrix", "Simulation")
            .limitMs(2000)
            .maxN(20000);
    }

    public int[][] matrixReshape(int[][] mat, int r, int c) {
        int m = mat.length;
        Pre.check(m >= 1 && mat[0].length >= 1 && r >= 1 && c >= 1, "non-empty, r, c >= 1");
        int n = mat[0].length;
        for (int[] row : mat) Pre.check(row.length == n, "rectangular");
        if ((long) r * c != (long) m * n) return mat;
        int[][] out = new int[r][c];
        for (int k = 0; k < m * n; k++) out[k / c][k % c] = mat[k / n][k % n];
        return out;
    }
}
