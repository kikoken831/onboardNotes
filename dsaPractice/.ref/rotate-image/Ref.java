class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rotate", T.INT_ARRAY_2D).returns(T.VOID).inPlace(0)
            .exampleInPlace("[[1,2,3],[4,5,6],[7,8,9]]", "[[7,4,1],[8,5,2],[9,6,3]]")
            .exampleInPlace("[[5,1,9,11],[2,4,8,10],[13,3,6,7],[15,14,12,16]]", "[[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]")
            .exampleInPlace("[[1]]", "[[1]]").exampleInPlace("[[1,2],[3,4]]", "[[3,1],[4,2]]")
            .gen(g -> { int n = g.side(1, 10); return new Object[]{ g.grid(n, n, n, n, -9, 9) }; })
            .patterns("Array", "Math", "Matrix").limitMs(2000).maxN(20000);
    }
    public void rotate(int[][] m) {
        int n = m.length;
        for (int[] r : m) Pre.check(r.length == n, "square");
        for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) { int t = m[i][j]; m[i][j] = m[j][i]; m[j][i] = t; }   // transpose
        for (int[] r : m) for (int i = 0, j = n - 1; i < j; i++, j--) { int t = r[i]; r[i] = r[j]; r[j] = t; }              // mirror
    }
}
