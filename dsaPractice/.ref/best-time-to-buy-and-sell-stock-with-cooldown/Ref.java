class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxProfit", T.INT_ARRAY).returns(T.INT)
            .example("[1,2,3,0,2]", "3").example("[1]", "0").example("[1,2,4]", "3").example("[2,1]", "0")
            .gen(g -> new Object[]{ g.ints(1, 40, 0, 20) })
            .patterns("Array", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int maxProfit(int[] p) {
        int hold = Integer.MIN_VALUE / 2, sold = 0, rest = 0;   // holding / just sold / free to buy
        for (int x : p) {
            int h = Math.max(hold, rest - x), s = hold + x, r = Math.max(rest, sold);
            hold = h; sold = s; rest = r;
        }
        return Math.max(sold, rest);
    }
}
