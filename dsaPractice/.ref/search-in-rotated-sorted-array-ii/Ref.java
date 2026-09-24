class Ref implements Problem {
    public Spec spec() {
        return Spec.method("search", T.INT_ARRAY, T.INT).returns(T.BOOL)
            .example("[2,5,6,0,0,1,2], 0", "true").example("[2,5,6,0,0,1,2], 3", "false")
            .example("[1,0,1,1,1], 0", "true").example("[1,1,1,1], 2", "false").example("[1], 1", "true")
            .gen(g -> {
                int[] s = g.sortedInts(1, 40, 0, 6);
                int k = g.nextInt(0, s.length - 1);
                int[] a = new int[s.length];
                for (int i = 0; i < s.length; i++) a[i] = s[(i + k) % s.length];
                return new Object[]{ a, g.nextInt(-1, 7) };
            })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(200000);
    }
    public boolean search(int[] a, int t) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) / 2;
            if (a[m] == t) return true;
            if (a[lo] == a[m] && a[m] == a[hi]) { lo++; hi--; }          // can't tell which half is sorted
            else if (a[lo] <= a[m]) { if (a[lo] <= t && t < a[m]) hi = m - 1; else lo = m + 1; }
            else                    { if (a[m] < t && t <= a[hi]) lo = m + 1; else hi = m - 1; }
        }
        return false;
    }
}
