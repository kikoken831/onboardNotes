class Ref implements Problem {

    public Spec spec() {
        return Spec.method("fib", T.INT).returns(T.INT)
            .example("2",  "1")
            .example("3",  "2")
            .example("4",  "3")
            .example("0",  "0")
            .example("1",  "1")
            .example("30", "832040")
            .gen(g -> new Object[]{ g.nextInt(0, 30) })
            .patterns("Math", "Dynamic Programming", "Memoization")
            .limitMs(2000);
    }

    public int fib(int n) {
        Pre.check(n >= 0 && n <= 30, "0 <= n <= 30");
        int a = 0, b = 1;
        for (int i = 0; i < n; i++) { int t = a + b; a = b; b = t; }
        return a;
    }
}
