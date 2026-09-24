class Ref implements Problem {
    public Spec spec() {
        return Spec.method("climbStairs", T.INT).returns(T.INT)
            .example("2", "2").example("3", "3").example("1", "1").example("45", "1836311903")
            .gen(g -> new Object[]{ g.nextInt(1, 45) })
            .patterns("Math", "Dynamic Programming", "Memoization").limitMs(2000);
    }
    public int climbStairs(int n) {
        Pre.check(n >= 1 && n <= 45, "1 <= n <= 45");
        int a = 1, b = 1;
        for (int i = 2; i <= n; i++) { int t = a + b; a = b; b = t; }
        return b;
    }
}
