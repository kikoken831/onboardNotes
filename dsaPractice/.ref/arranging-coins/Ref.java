class Ref implements Problem {

    public Spec spec() {
        return Spec.method("arrangeCoins", T.INT).returns(T.INT)
            .example("5",          "2")
            .example("8",          "3")
            .example("1",          "1")
            .example("3",          "2")
            .example("2147483647", "65535")     // k(k+1)/2 overflows int here
            .gen(g -> new Object[]{ g.chance(0.5) ? g.nextInt(1, 200) : g.value(1, Integer.MAX_VALUE) })
            .patterns("Math", "Binary Search")
            .limitMs(2000);
    }

    public int arrangeCoins(int n) {
        Pre.check(n >= 1, "1 <= n");
        long lo = 1, hi = n;                     // largest k with k(k+1)/2 <= n
        while (lo < hi) {
            long mid = lo + (hi - lo + 1) / 2;
            if (mid * (mid + 1) / 2 <= n) lo = mid; else hi = mid - 1;
        }
        return (int) lo;
    }
}
