class Ref implements Problem {

    public Spec spec() {
        return Spec.method("flipAndInvertImage", T.INT_ARRAY_2D).returns(T.INT_ARRAY_2D)
            .example("[[1,1,0],[1,0,1],[0,0,0]]",                 "[[1,0,0],[0,1,0],[1,1,1]]")
            .example("[[1,1,0,0],[1,0,0,1],[0,1,1,1],[1,0,1,0]]", "[[1,1,0,0],[0,1,1,0],[0,0,0,1],[1,0,1,0]]")
            .example("[[0]]",                                     "[[1]]")
            .gen(g -> { int n = g.size(1, 12); return new Object[]{ g.grid(n, n, n, n, 0, 1) }; })
            .patterns("Array", "Two Pointers", "Matrix")
            .limitMs(2000)
            .maxN(20000);
    }

    public int[][] flipAndInvertImage(int[][] image) {
        int n = image.length;
        Pre.check(n >= 1, "1 <= n");
        int[][] out = new int[n][];
        for (int i = 0; i < n; i++) {
            Pre.check(image[i].length == n, "square n x n");
            out[i] = new int[n];
            for (int j = 0; j < n; j++) out[i][j] = 1 - image[i][n - 1 - j];
        }
        return out;
    }
}
