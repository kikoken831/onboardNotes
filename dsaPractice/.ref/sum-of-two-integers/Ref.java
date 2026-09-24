class Ref implements Problem {
    public Spec spec() {
        return Spec.method("getSum", T.INT, T.INT).returns(T.INT)
            .example("1, 2", "3").example("2, 3", "5").example("-1, 1", "0").example("-1000, -1000", "-2000").example("0, -7", "-7")
            .gen(g -> new Object[]{ g.nextInt(-1000, 1000), g.nextInt(-1000, 1000) })
            .patterns("Math", "Bit Manipulation").limitMs(2000);
    }
    public int getSum(int a, int b) {
        while (b != 0) { int carry = (a & b) << 1; a ^= b; b = carry; }
        return a;
    }
}
