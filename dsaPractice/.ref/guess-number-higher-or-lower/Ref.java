class Ref extends GuessGame implements Problem {

    public Spec spec() {
        return Spec.method("guessNumber", T.INT).returns(T.INT)
            // The pick is a hidden argument: installed behind guess(), never passed in.
            .hidden(a -> {
                int n = (Integer) a[0], pick = (Integer) a[1];
                Pre.check(n >= 1 && pick >= 1 && pick <= n, "1 <= pick <= n");
                GuessGame.install(pick);
            }, T.INT)
            .example("10, 6",                     "6")
            .example("1, 1",                      "1")
            .example("2, 1",                      "1")
            .example("2, 2",                      "2")
            .example("2126753390, 1702766719",    "1702766719")
            .example("2147483647, 2147483647",    "2147483647")   // (lo + hi) overflows
            .gen(g -> {
                int n = g.chance(0.5) ? g.nextInt(1, 100) : g.value(1, Integer.MAX_VALUE);
                int pick = g.chance(0.2) ? (g.chance(0.5) ? 1 : n) : g.nextInt(1, n);
                return new Object[]{ n, pick };
            })
            .patterns("Binary Search", "Interactive")
            .limitMs(2000);
    }

    public int guessNumber(int n) {
        int lo = 1, hi = n;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (guess(mid) <= 0) hi = mid; else lo = mid + 1;
        }
        return lo;
    }
}
