class Ref implements Problem {

    public Spec spec() {
        return Spec.method("regionSize", T.INT_ARRAY_2D, T.INT, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[[1,1,0],[0,1,0],[0,0,1]], 0, 0", "3")
            .example("[[1,1,0],[0,1,0],[0,0,1]], 2, 2", "1")
            .example("[[0]], 0, 0",                     "0")
            .example("[[1,1],[1,1]], 0, 0",             "4")
            .example("[[1]], 0, 0",                     "1")
            .gen(g -> {
                int[][] grid = g.grid(1, 12, 1, 12, 0, 1);
                return new Object[]{ grid,
                    g.nextInt(0, grid.length - 1),
                    g.nextInt(0, grid[0].length - 1) };
            })
            .patterns("Depth-First Search", "Matrix", "Breadth-First Search")
            .limitMs(2000)
            .maxN(200);
    }

    public int regionSize(int[][] grid, int r, int c) {
        return dfs(grid, r, c);
    }

    private int dfs(int[][] g, int r, int c) {
        if (r < 0 || r >= g.length || c < 0 || c >= g[0].length) return 0;
        if (g[r][c] != 1) return 0;
        g[r][c] = 2;
        return 1 + dfs(g, r + 1, c) + dfs(g, r - 1, c)
                 + dfs(g, r, c + 1) + dfs(g, r, c - 1);
    }
}
