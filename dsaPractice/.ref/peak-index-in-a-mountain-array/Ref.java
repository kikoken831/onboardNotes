class Ref implements Problem {
    public Spec spec() {
        return Spec.method("peakIndexInMountainArray", T.INT_ARRAY).returns(T.INT)
            .example("[0,1,0]", "1").example("[0,2,1,0]", "1").example("[0,10,5,2]", "1").example("[3,5,3,2,0]", "1")
            .example("[1,2,3,4,1]", "3")
            .gen(g -> {
                int n = g.size(3, 50), peak = g.nextInt(1, n - 2);
                int[] a = new int[n];
                a[peak] = 1000;
                for (int i = peak - 1; i >= 0; i--) a[i] = a[i + 1] - g.nextInt(1, 9);
                for (int i = peak + 1; i < n; i++) a[i] = a[i - 1] - g.nextInt(1, 9);
                return new Object[]{ a };
            })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(200000);
    }
    public int peakIndexInMountainArray(int[] a) {
        int n = a.length, p = 0;
        Pre.check(n >= 3, "3 <= n");
        while (p + 1 < n && a[p + 1] > a[p]) p++;
        for (int i = p; i + 1 < n; i++) Pre.check(a[i + 1] < a[i], "a mountain");
        Pre.check(p > 0 && p < n - 1, "a mountain");
        int lo = 0, hi = n - 1;
        while (lo < hi) { int m = (lo + hi) / 2; if (a[m] < a[m + 1]) lo = m + 1; else hi = m; }
        return lo;
    }
}
