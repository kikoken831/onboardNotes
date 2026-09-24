class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxProduct", T.INT_ARRAY).returns(T.INT)
            .example("[2,3,-2,4]", "6").example("[-2,0,-1]", "0").example("[-2,3,-4]", "24").example("[-2]", "-2").example("[0,2]", "2")
            .gen(g -> new Object[]{ g.ints(1, 14, -4, 4) })              // products stay within int
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int maxProduct(int[] a) {
        int hi = a[0], lo = a[0], best = a[0];
        for (int i = 1; i < a.length; i++) {
            int x = a[i], h = Math.max(x, Math.max(hi * x, lo * x)), l = Math.min(x, Math.min(hi * x, lo * x));
            hi = h; lo = l;
            best = Math.max(best, hi);
        }
        return best;
    }
}
