class Ref implements Problem {

    public Spec spec() {
        return Spec.method("tribonacci", T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("0",  "0")
            .example("1",  "1")
            .example("2",  "1")
            .example("4",  "4")
            .example("25", "1389537")
            .example("37", "2082876103")
            .gen(g -> new Object[]{ g.nextInt(0, 37) })
            .patterns("Dynamic Programming", "Memoization", "Math")
            .limitMs(2000)
            .maxN(37);
    }

    public int tribonacci(int n) {
        if (n == 0) return 0;
        if (n == 1 || n == 2) return 1;
        int a = 0, b = 1, c = 1;
        for (int i = 3; i <= n; i++) {
            int d = a + b + c;
            a = b; b = c; c = d;
        }
        return c;
    }
}
