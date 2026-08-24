class Ref implements Problem {

    public Spec spec() {
        return Spec.method("bsOnAnswer", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[1,2,3,4,5,6,7,8,9,10], 5", "15")
            .example("[3,2,2,4,1,4], 3",          "6")
            .example("[1,2,3,1,1], 4",            "3")
            .example("[5], 1",                    "5")
            .example("[1,1,1,1], 4",              "1")
            .example("[10,10], 1",                "20")
            .gen(g -> {
                int n = g.size(1, 40);
                int[] w = new int[n];
                for (int i = 0; i < n; i++) w[i] = g.nextInt(1, 20);
                return new Object[]{ w, g.nextInt(1, n) };
            })
            .patterns("Array", "Binary Search")
            .limitMs(3000)
            .maxN(100000);
    }

    public int bsOnAnswer(int[] weights, int days) {
        int lo = 0, hi = 0;
        for (int w : weights) { lo = Math.max(lo, w); hi += w; }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (daysNeeded(weights, mid) <= days) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    private int daysNeeded(int[] w, int cap) {
        int d = 1, load = 0;
        for (int x : w) {
            if (load + x > cap) { d++; load = 0; }
            load += x;
        }
        return d;
    }
}
