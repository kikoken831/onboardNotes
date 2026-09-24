class Ref implements Problem {
    public Spec spec() {
        return Spec.method("hammingWeight", T.INT).returns(T.INT)
            .example("11", "3").example("128", "1").example("2147483645", "30").example("1", "1")
            .gen(g -> new Object[]{ g.value(1, Integer.MAX_VALUE) })
            .patterns("Divide and Conquer", "Bit Manipulation").limitMs(2000);
    }
    public int hammingWeight(int n) {
        int c = 0;
        while (n != 0) { n &= n - 1; c++; }                    // clear the lowest set bit
        return c;
    }
}
