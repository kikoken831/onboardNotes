class Ref implements Problem {

    public Spec spec() {
        return Spec.method("tribonacci", T.INT).returns(T.INT)
            .example("4",  "4")
            .example("25", "1389537")
            .example("0",  "0")
            .example("1",  "1")
            .example("2",  "1")
            .example("37", "2082876103")
            .gen(g -> new Object[]{ g.nextInt(0, 37) })
            .patterns("Math", "Dynamic Programming", "Memoization")
            .limitMs(2000);
    }

    public int tribonacci(int n) {
        Pre.check(n >= 0 && n <= 37, "0 <= n <= 37");
        int a = 0, b = 1, c = 1;
        if (n == 0) return 0;
        for (int i = 3; i <= n; i++) { int t = a + b + c; a = b; b = c; c = t; }
        return c;
    }
}
