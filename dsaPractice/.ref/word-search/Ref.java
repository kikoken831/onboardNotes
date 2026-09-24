class Ref implements Problem {
    public Spec spec() {
        return Spec.method("exist", T.CHAR_ARRAY_2D, T.STRING).returns(T.BOOL)
            .example("[[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], \"ABCCED\"", "true")
            .example("[[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], \"SEE\"", "true")
            .example("[[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], \"ABCB\"", "false")   // no reuse
            .example("[[\"a\"]], \"a\"", "true").example("[[\"a\",\"a\"]], \"aaa\"", "false")
            .gen(g -> {
                int r = g.size(1, 5), c = g.size(1, 5);
                char[][] b = new char[r][c];
                for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) b[i][j] = "abc".charAt(g.nextInt(0, 2));
                return new Object[]{ b, g.lower3(1, 7) };
            })
            .patterns("Array", "String", "Backtracking", "Matrix").limitMs(2000).maxN(20);
    }
    public boolean exist(char[][] b, String w) {
        for (int i = 0; i < b.length; i++) for (int j = 0; j < b[0].length; j++) if (dfs(b, w, i, j, 0)) return true;
        return false;
    }
    private boolean dfs(char[][] b, String w, int i, int j, int k) {
        if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || b[i][j] != w.charAt(k)) return false;
        if (k == w.length() - 1) return true;
        char t = b[i][j];
        b[i][j] = '#';                                          // mark on the way in
        boolean ok = dfs(b, w, i + 1, j, k + 1) || dfs(b, w, i - 1, j, k + 1) || dfs(b, w, i, j + 1, k + 1) || dfs(b, w, i, j - 1, k + 1);
        b[i][j] = t;                                            // restore on the way out
        return ok;
    }
}
