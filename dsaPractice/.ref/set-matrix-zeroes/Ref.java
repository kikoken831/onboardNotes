class Ref implements Problem {
    public Spec spec() {
        return Spec.method("setZeroes", T.INT_ARRAY_2D).returns(T.VOID).inPlace(0)
            .exampleInPlace("[[1,1,1],[1,0,1],[1,1,1]]", "[[1,0,1],[0,0,0],[1,0,1]]")
            .exampleInPlace("[[0,1,2,0],[3,4,5,2],[1,3,1,5]]", "[[0,0,0,0],[0,4,5,0],[0,3,1,0]]")
            .exampleInPlace("[[1]]", "[[1]]").exampleInPlace("[[1,0]]", "[[0,0]]").exampleInPlace("[[1],[0]]", "[[0],[0]]")
            .gen(g -> {
                int[][] a = g.grid(1, 8, 1, 8, 1, 9);
                boolean wide = g.chance(0.4);            // full int range: breaks in-band sentinel markers
                if (wide) {
                    int[] special = { Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 1, Integer.MIN_VALUE + 1, -1000000000 };
                    for (int[] row : a) for (int j = 0; j < row.length; j++)
                        row[j] = g.chance(0.3) ? special[g.nextInt(0, special.length - 1)] : g.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
                }
                int z = g.nextInt(wide ? 1 : 0, 3);
                for (int k = 0; k < z; k++) a[g.nextInt(0, a.length - 1)][g.nextInt(0, a[0].length - 1)] = 0;
                return new Object[]{ a };
            })
            .patterns("Array", "Hash Table", "Matrix").limitMs(2000).maxN(20000);
    }
    public void setZeroes(int[][] m) {
        Pre.check(m.length >= 1 && m[0].length >= 1, "1 <= m, n");
        for (int[] row : m) Pre.check(row.length == m[0].length, "rectangular matrix");
        int r = m.length, c = m[0].length;
        boolean[] rows = new boolean[r], cols = new boolean[c];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) if (m[i][j] == 0) { rows[i] = true; cols[j] = true; }
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) if (rows[i] || cols[j]) m[i][j] = 0;
    }
}
