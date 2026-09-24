class Ref implements Problem {
    public Spec spec() {
        return Spec.method("canJump", T.INT_ARRAY).returns(T.BOOL)
            .example("[2,3,1,1,4]", "true").example("[3,2,1,0,4]", "false").example("[0]", "true").example("[0,1]", "false")
            .gen(g -> new Object[]{ g.ints(1, 40, 0, 3) })
            .patterns("Array", "Dynamic Programming", "Greedy").limitMs(2000).maxN(200000);
    }
    public boolean canJump(int[] a) {
        int reach = 0;
        for (int i = 0; i < a.length; i++) { if (i > reach) return false; reach = Math.max(reach, i + a[i]); }
        return true;
    }
}
