class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxArea", T.INT_ARRAY).returns(T.INT)
            .example("[1,8,6,2,5,4,8,3,7]", "49")
            .example("[1,1]",               "1")
            .example("[4,3,2,1,4]",         "16")
            .example("[1,2,1]",             "2")
            .gen(g -> new Object[]{ g.ints(2, 60, 0, 30) })
            .patterns("Array", "Two Pointers", "Greedy").limitMs(2000).maxN(200000);
    }
    public int maxArea(int[] h) {
        Pre.length(h, 2, 1_000_000, "height");
        int i = 0, j = h.length - 1, best = 0;
        while (i < j) {
            best = Math.max(best, Math.min(h[i], h[j]) * (j - i));
            if (h[i] < h[j]) i++; else j--;
        }
        return best;
    }
}
