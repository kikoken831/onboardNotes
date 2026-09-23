class Ref implements Problem {

    public Spec spec() {
        return Spec.method("transpose", T.INT_ARRAY_2D).returns(T.INT_ARRAY_2D)
            .example("[[1,2,3],[4,5,6],[7,8,9]]", "[[1,4,7],[2,5,8],[3,6,9]]")
            .example("[[1,2,3],[4,5,6]]",         "[[1,4],[2,5],[3,6]]")
            .example("[[1]]",                     "[[1]]")
            .example("[[1,2,3]]",                 "[[1],[2],[3]]")
            .gen(g -> new Object[]{ g.grid(1, 10, 1, 10, -9, 9) })
            .patterns("Array", "Matrix", "Simulation")
            .limitMs(2000)
            .maxN(20000);
    }

    public int[][] transpose(int[][] matrix) {
        int m = matrix.length;
        Pre.check(m >= 1 && matrix[0].length >= 1, "non-empty matrix");
        int n = matrix[0].length;
        int[][] t = new int[n][m];
        for (int i = 0; i < m; i++) {
            Pre.check(matrix[i].length == n, "rectangular");
            for (int j = 0; j < n; j++) t[j][i] = matrix[i][j];
        }
        return t;
    }
}
