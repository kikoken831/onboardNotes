class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rob", T.INT_ARRAY).returns(T.INT)
            .example("[1,2,3,1]", "4").example("[2,7,9,3,1]", "12").example("[2,1,1,2]", "4").example("[5]", "5")
            .gen(g -> new Object[]{ g.ints(1, 50, 0, 40) })
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int rob(int[] a) {
        int prev = 0, cur = 0;
        for (int x : a) { int t = Math.max(cur, prev + x); prev = cur; cur = t; }
        return cur;
    }
}
