class Ref implements Problem {
    public Spec spec() {
        return Spec.method("searchMatrix", T.INT_ARRAY_2D, T.INT).returns(T.BOOL)
            .example("[[1,4,7,11,15],[2,5,8,12,19],[3,6,9,16,22],[10,13,14,17,24],[18,21,23,26,30]], 5", "true")
            .example("[[1,4,7,11,15],[2,5,8,12,19],[3,6,9,16,22],[10,13,14,17,24],[18,21,23,26,30]], 20", "false")
            .example("[[-5]], -5", "true").example("[[1,1]], 2", "false")
            .gen(g -> {
                int m = g.size(1, 8), n = g.size(1, 8);
                int[][] a = new int[m][n];
                for (int i = 0; i < m; i++) for (int j = 0; j < n; j++)
                    a[i][j] = Math.max(i > 0 ? a[i - 1][j] : -30, j > 0 ? a[i][j - 1] : -30) + g.nextInt(0, 4);
                int t = g.chance(0.5) ? a[g.nextInt(0, m - 1)][g.nextInt(0, n - 1)] : g.nextInt(-30, 80);
                return new Object[]{ a, t };
            })
            .patterns("Array", "Binary Search", "Divide and Conquer", "Matrix").limitMs(2000).maxN(20000);
    }
    public boolean searchMatrix(int[][] a, int t) {
        for (int i = 0; i < a.length; i++) for (int j = 0; j < a[0].length; j++) {
            if (i > 0) Pre.check(a[i][j] >= a[i - 1][j], "columns sorted");
            if (j > 0) Pre.check(a[i][j] >= a[i][j - 1], "rows sorted");
        }
        int r = 0, c = a[0].length - 1;
        while (r < a.length && c >= 0) {
            if (a[r][c] == t) return true;
            if (a[r][c] > t) c--; else r++;
        }
        return false;
    }
}
