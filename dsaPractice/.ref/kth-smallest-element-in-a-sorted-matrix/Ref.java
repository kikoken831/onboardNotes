class Ref implements Problem {
    public Spec spec() {
        return Spec.method("kthSmallest", T.INT_ARRAY_2D, T.INT).returns(T.INT)
            .example("[[1,5,9],[10,11,13],[12,13,15]], 8", "13").example("[[-5]], 1", "-5")
            .example("[[1,2],[1,3]], 2", "1").example("[[1,2],[3,4]], 4", "4")
            .gen(g -> {
                int n = g.size(1, 8);
                int[][] a = new int[n][n];
                for (int i = 0; i < n; i++) for (int j = 0; j < n; j++)
                    a[i][j] = Math.max(i > 0 ? a[i - 1][j] : -20, j > 0 ? a[i][j - 1] : -20) + g.nextInt(0, 3);
                return new Object[]{ a, g.nextInt(1, n * n) };
            })
            .patterns("Array", "Binary Search", "Sorting", "Heap (Priority Queue)", "Matrix").limitMs(2000).maxN(20000);
    }
    public int kthSmallest(int[][] a, int k) {
        int n = a.length;
        Pre.check(n >= 1, "n >= 1");
        for (int[] row : a) Pre.check(row.length == n, "matrix is n x n");
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            if (i > 0) Pre.check(a[i - 1][j] <= a[i][j], "columns sorted ascending");
            if (j > 0) Pre.check(a[i][j - 1] <= a[i][j], "rows sorted ascending");
        }
        Pre.check(k >= 1 && k <= n * n, "1 <= k <= n^2");
        int lo = a[0][0], hi = a[n - 1][n - 1];
        while (lo < hi) {                                   // binary search on the value
            int mid = lo + (hi - lo) / 2, count = 0;
            for (int i = n - 1, j = 0; i >= 0 && j < n; ) { if (a[i][j] <= mid) { count += i + 1; j++; } else i--; }
            if (count < k) lo = mid + 1; else hi = mid;
        }
        return lo;
    }
}
