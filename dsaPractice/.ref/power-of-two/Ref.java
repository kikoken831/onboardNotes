class Ref implements Problem {

    public Spec spec() {
        return Spec.method("isPowerOfTwo", T.INT).returns(T.BOOL)
            .example("1",           "true")
            .example("16",          "true")
            .example("3",           "false")
            .example("0",           "false")
            .example("-16",         "false")
            .example("-2147483648", "false")       // one bit set, but negative
            .example("1073741824",  "true")
            .gen(g -> {
                if (g.chance(0.4)) return new Object[]{ 1 << g.nextInt(0, 31) };   // includes MIN_VALUE
                return new Object[]{ g.chance(0.5) ? g.nextInt(-40, 40) : g.value(Integer.MIN_VALUE, Integer.MAX_VALUE) };
            })
            .patterns("Math", "Bit Manipulation", "Recursion")
            .limitMs(2000);
    }

    public boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}
