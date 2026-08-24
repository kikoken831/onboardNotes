class Ref implements Problem {

    public Spec spec() {
        return Spec.method("rectangleSums", T.INT_ARRAY_2D, T.INT_ARRAY_2D).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[[1,2],[3,4]], [[0,0,1,1],[0,0,0,0],[1,0,1,1]]", "[10,1,7]")
            .example("[[5]], [[0,0,0,0]]",                             "[5]")
            .example("[[1,2],[3,4]], []",                              "[]")
            .gen(g -> {
                int[][] m = g.grid(1, 12, 1, 12, -10, 10);
                int q = g.size(0, 15);
                int[][] queries = new int[q][4];
                for (int k = 0; k < q; k++) {
                    int r1 = g.nextInt(0, m.length - 1), r2 = g.nextInt(0, m.length - 1);
                    int c1 = g.nextInt(0, m[0].length - 1), c2 = g.nextInt(0, m[0].length - 1);
                    queries[k][0] = Math.min(r1, r2);
                    queries[k][1] = Math.min(c1, c2);
                    queries[k][2] = Math.max(r1, r2);
                    queries[k][3] = Math.max(c1, c2);
                }
                return new Object[]{ m, queries };
            })
            .patterns("Array", "Matrix", "Prefix Sum")
            .limitMs(2000)
            .maxN(20000);
    }

    public int[] rectangleSums(int[][] matrix, int[][] queries) {
        int rows = matrix.length, cols = matrix[0].length;
        int[][] p = new int[rows + 1][cols + 1];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                p[r + 1][c + 1] = matrix[r][c] + p[r][c + 1] + p[r + 1][c] - p[r][c];
            }
        }

        int[] out = new int[queries.length];
        for (int k = 0; k < queries.length; k++) {
            int r1 = queries[k][0], c1 = queries[k][1];
            int r2 = queries[k][2], c2 = queries[k][3];
            out[k] = p[r2 + 1][c2 + 1] - p[r1][c2 + 1] - p[r2 + 1][c1] + p[r1][c1];
        }
        return out;
    }
}
