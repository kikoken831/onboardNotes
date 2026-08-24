class Ref implements Problem {

    public Spec spec() {
        return Spec.method("bsPredicateFlip", T.BOOL_ARRAY).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[false,false,true,true]", "2")
            .example("[true,true]",             "0")
            .example("[false,false]",           "2")
            .example("[]",                      "0")
            .example("[false,true]",            "1")
            .example("[true]",                  "0")
            .gen(g -> {
                int n = g.size(0, 60);
                int flip = g.nextInt(0, Math.max(0, n));   // may be 0 or n
                boolean[] f = new boolean[n];
                for (int i = 0; i < n; i++) f[i] = i >= flip;
                return new Object[]{ f };
            })
            .patterns("Binary Search")
            .limitMs(2000)
            .maxN(500000);
    }

    public int bsPredicateFlip(boolean[] flags) {
        int lo = 0, hi = flags.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (flags[mid]) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }
}
