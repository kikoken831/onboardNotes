class Ref implements Problem {

    public Spec spec() {
        return Spec.method("maximumWealth", T.INT_ARRAY_2D).returns(T.INT)
            .example("[[1,2,3],[3,2,1]]",         "6")
            .example("[[1,5],[7,3],[3,5]]",       "10")
            .example("[[2,8,7],[7,1,3],[1,9,5]]", "17")
            .example("[[4]]",                     "4")
            .gen(g -> new Object[]{ g.grid(1, 12, 1, 12, 1, 100) })
            .patterns("Array", "Matrix")
            .limitMs(2000)
            .maxN(20000);
    }

    public int maximumWealth(int[][] accounts) {
        Pre.check(accounts.length >= 1, "1 <= m");
        int best = 0;
        for (int[] customer : accounts) {
            int sum = 0;
            for (int x : customer) sum += x;
            best = Math.max(best, sum);
        }
        return best;
    }
}
