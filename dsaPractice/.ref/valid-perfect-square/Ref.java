class Ref implements Problem {

    public Spec spec() {
        return Spec.method("isPerfectSquare", T.INT).returns(T.BOOL)
            .example("16",         "true")
            .example("14",         "false")
            .example("1",          "true")
            .example("808201",     "true")      // 899^2
            .example("2147483647", "false")
            .gen(g -> {
                if (g.chance(0.4)) { int r = g.nextInt(1, 46340); return new Object[]{ r * r }; }
                return new Object[]{ g.chance(0.5) ? g.nextInt(1, 400) : g.value(1, Integer.MAX_VALUE) };
            })
            .patterns("Math", "Binary Search")
            .limitMs(2000);
    }

    public boolean isPerfectSquare(int num) {
        Pre.check(num >= 1, "1 <= num");
        long lo = 1, hi = num;
        while (lo <= hi) {
            long mid = lo + (hi - lo) / 2, sq = mid * mid;
            if (sq == num) return true;
            if (sq < num) lo = mid + 1; else hi = mid - 1;
        }
        return false;
    }
}
