class Ref implements Problem {
    public Spec spec() {
        return Spec.method("reverseBits", T.INT).returns(T.INT)
            .example("43261596", "964176192").example("-3", "-1073741825").example("0", "0").example("1", "-2147483648")
            .gen(g -> new Object[]{ g.value(Integer.MIN_VALUE, Integer.MAX_VALUE) })
            .patterns("Divide and Conquer", "Bit Manipulation").limitMs(2000);
    }
    public int reverseBits(int n) {
        int r = 0;
        for (int i = 0; i < 32; i++) { r = (r << 1) | (n & 1); n >>>= 1; }
        return r;
    }
}
