class Ref implements Problem {
    public Spec spec() {
        return Spec.method("numIslands", T.CHAR_ARRAY_2D).returns(T.INT)
            .example("[[\"1\",\"1\",\"1\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"0\",\"0\"]]", "1")
            .example("[[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"1\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"1\",\"1\"]]", "3")
            .example("[[\"1\",\"0\",\"1\"]]", "2").example("[[\"0\"]]", "0").example("[[\"1\",\"0\"],[\"0\",\"1\"]]", "2")   // diagonal is not connected
            .gen(g -> {
                int r = g.side(1, 12), c = g.side(1, 12);
                char[][] a = new char[r][c];
                for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) a[i][j] = g.chance(0.45) ? '1' : '0';
                return new Object[]{ a };
            })
            .patterns("Array", "Depth-First Search", "Breadth-First Search", "Union-Find", "Matrix").limitMs(2000).maxN(20000);
    }
    public int numIslands(char[][] grid) {
        int n = 0;
        for (int i = 0; i < grid.length; i++) for (int j = 0; j < grid[0].length; j++) if (grid[i][j] == '1') { n++; sink(grid, i, j); }
        return n;
    }
    private void sink(char[][] g, int i, int j) {
        java.util.Deque<int[]> st = new java.util.ArrayDeque<>();
        g[i][j] = '0'; st.push(new int[]{ i, j });
        while (!st.isEmpty()) {
            int[] p = st.pop();
            for (int[] d : new int[][]{ {1,0},{-1,0},{0,1},{0,-1} }) {
                int a = p[0] + d[0], b = p[1] + d[1];
                if (a >= 0 && b >= 0 && a < g.length && b < g[0].length && g[a][b] == '1') { g[a][b] = '0'; st.push(new int[]{ a, b }); }
            }
        }
    }
}
