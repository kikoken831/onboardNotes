class Ref implements Problem {

    public Spec spec() {
        return Spec.method("mySqrt", T.INT).returns(T.INT)
            .example("4",          "2")
            .example("8",          "2")
            .example("0",          "0")
            .example("1",          "1")
            .example("2147483647", "46340")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.nextInt(0, 400) : g.value(0, Integer.MAX_VALUE) })
            .patterns("Math", "Binary Search")
            .limitMs(2000);
    }

    public int mySqrt(int x) {
        Pre.check(x >= 0, "0 <= x");
        long lo = 0, hi = x;                     // largest r with r*r <= x
        while (lo < hi) {
            long mid = lo + (hi - lo + 1) / 2;
            if (mid * mid <= x) lo = mid; else hi = mid - 1;
        }
        return (int) lo;
    }
}
