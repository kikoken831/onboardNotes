class Ref implements Problem {

    public Spec spec() {
        return Spec.method("floodFill", T.INT_ARRAY_2D, T.INT, T.INT, T.INT)
            .returns(T.INT_ARRAY_2D)
            .compare(Cmp.EXACT)
            .example("[[1,1,1],[1,1,0],[1,0,1]], 1, 1, 2", "[[2,2,2],[2,2,0],[2,0,1]]")
            .example("[[0,0,0],[0,0,0]], 0, 0, 0",         "[[0,0,0],[0,0,0]]")
            .example("[[1]], 0, 0, 5",                     "[[5]]")
            .example("[[0,0,0],[0,1,1]], 1, 1, 1",         "[[0,0,0],[0,1,1]]")
            .gen(g -> {
                int[][] img = g.grid(1, 10, 1, 10, 0, 2);
                return new Object[]{ img,
                    g.nextInt(0, img.length - 1),
                    g.nextInt(0, img[0].length - 1),
                    g.nextInt(0, 2) };        // may equal the start colour, on purpose
            })
            .patterns("Depth-First Search", "Breadth-First Search", "Matrix")
            .limitMs(2000)
            .maxN(200);
    }

    public int[][] floodFill(int[][] image, int sr, int sc, int newColor) {
        int start = image[sr][sc];
        if (start == newColor) return image;
        fill(image, sr, sc, start, newColor);
        return image;
    }

    private void fill(int[][] g, int r, int c, int from, int to) {
        if (r < 0 || r >= g.length || c < 0 || c >= g[0].length) return;
        if (g[r][c] != from) return;
        g[r][c] = to;
        fill(g, r + 1, c, from, to);
        fill(g, r - 1, c, from, to);
        fill(g, r, c + 1, from, to);
        fill(g, r, c - 1, from, to);
    }
}
