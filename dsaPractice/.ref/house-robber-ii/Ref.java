class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rob", T.INT_ARRAY).returns(T.INT)
            .example("[2,3,2]", "3").example("[1,2,3,1]", "4").example("[1,2,3]", "3").example("[5]", "5").example("[1,3]", "3")
            .gen(g -> new Object[]{ g.ints(1, 50, 0, 40) })
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int rob(int[] a) {
        if (a.length == 1) return a[0];
        return Math.max(line(a, 0, a.length - 2), line(a, 1, a.length - 1));
    }
    private int line(int[] a, int lo, int hi) {
        int prev = 0, cur = 0;
        for (int i = lo; i <= hi; i++) { int t = Math.max(cur, prev + a[i]); prev = cur; cur = t; }
        return cur;
    }
}
