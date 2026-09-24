class Ref implements Problem {
    public Spec spec() {
        return Spec.method("trap", T.INT_ARRAY).returns(T.INT)
            .example("[0,1,0,2,1,0,1,3,2,1,2,1]", "6").example("[4,2,0,3,2,5]", "9")
            .example("[4,2,3]", "1").example("[5]", "0").example("[1,2,3]", "0")
            .gen(g -> new Object[]{ g.ints(1, 50, 0, 8) })
            .patterns("Array", "Two Pointers", "Dynamic Programming", "Stack", "Monotonic Stack").limitMs(2000).maxN(200000);
    }
    public int trap(int[] h) {
        int i = 0, j = h.length - 1, lmax = 0, rmax = 0, water = 0;
        while (i < j) {
            if (h[i] < h[j]) { lmax = Math.max(lmax, h[i]); water += lmax - h[i++]; }
            else             { rmax = Math.max(rmax, h[j]); water += rmax - h[j--]; }
        }
        return water;
    }
}
