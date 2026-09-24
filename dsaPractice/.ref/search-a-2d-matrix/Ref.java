class Ref implements Problem {
    public Spec spec() {
        return Spec.method("searchMatrix", T.INT_ARRAY_2D, T.INT).returns(T.BOOL)
            .example("[[1,3,5,7],[10,11,16,20],[23,30,34,60]], 3", "true")
            .example("[[1,3,5,7],[10,11,16,20],[23,30,34,60]], 13", "false").example("[[1]], 1", "true").example("[[1],[3]], 2", "false")
            .gen(g -> {
                int m = g.size(1, 8), n = g.size(1, 8);
                int[] v = g.strictlySortedInts(m * n, m * n, -200, 200);
                if (v.length < m * n) { m = 1; n = v.length; }
                int[][] a = new int[m][n];
                for (int i = 0; i < m * n; i++) a[i / n][i % n] = v[i];
                return new Object[]{ a, g.chance(0.5) ? v[g.nextInt(0, v.length - 1)] : g.nextInt(-210, 210) };
            })
            .patterns("Array", "Binary Search", "Matrix").limitMs(2000).maxN(20000);
    }
    public boolean searchMatrix(int[][] a, int t) {
        int m = a.length, n = a[0].length, lo = 0, hi = m * n - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2, v = a[mid / n][mid % n];
            if (v == t) return true;
            if (v < t) lo = mid + 1; else hi = mid - 1;
        }
        return false;
    }
}
