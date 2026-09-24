class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxProfit", T.INT_ARRAY).returns(T.INT)
            .example("[7,1,5,3,6,4]", "5").example("[7,6,4,3,1]", "0").example("[1]", "0").example("[2,4,1]", "2")
            .gen(g -> new Object[]{ g.ints(1, 60, 0, 30) })
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int maxProfit(int[] p) {
        int low = Integer.MAX_VALUE, best = 0;
        for (int x : p) { low = Math.min(low, x); best = Math.max(best, x - low); }
        return best;
    }
}
