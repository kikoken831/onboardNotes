class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxSubArray", T.INT_ARRAY).returns(T.INT)
            .example("[-2,1,-3,4,-1,2,1,-5,4]", "6").example("[1]", "1").example("[5,4,-1,7,8]", "23").example("[-3,-1,-2]", "-1")
            .gen(g -> new Object[]{ g.ints(1, 60, -20, 20) })
            .patterns("Array", "Divide and Conquer", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int maxSubArray(int[] a) {
        int best = a[0], cur = a[0];
        for (int i = 1; i < a.length; i++) { cur = Math.max(a[i], cur + a[i]); best = Math.max(best, cur); }
        return best;
    }
}
