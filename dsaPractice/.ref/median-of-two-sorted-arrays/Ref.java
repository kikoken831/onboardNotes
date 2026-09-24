class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findMedianSortedArrays", T.INT_ARRAY, T.INT_ARRAY).returns(T.DOUBLE)
            .eps(1e-5)
            .example("[1,3], [2]", "2.0").example("[1,2], [3,4]", "2.5").example("[], [1]", "1.0")
            .example("[2], []", "2.0").example("[1,1,1], [1,1,1]", "1.0").example("[-5,3,6,12,15], [-12,-10,-6,-3,4,10]", "3.0")
            .gen(g -> {
                int[] a = g.sortedInts(0, 25, -30, 30), b = g.sortedInts(a.length == 0 ? 1 : 0, 25, -30, 30);
                return new Object[]{ a, b };
            })
            .patterns("Array", "Binary Search", "Divide and Conquer").limitMs(2000).maxN(200000);
    }
    public double findMedianSortedArrays(int[] a, int[] b) {
        Pre.sorted(a, false, "nums1"); Pre.sorted(b, false, "nums2");
        Pre.check(a.length + b.length >= 1, "at least one value");
        if (a.length > b.length) { int[] t = a; a = b; b = t; }
        int m = a.length, n = b.length, half = (m + n + 1) / 2, lo = 0, hi = m;
        while (true) {
            int i = (lo + hi) / 2, j = half - i;                  // i from a, j from b on the left
            long aL = i == 0 ? Long.MIN_VALUE : a[i - 1], aR = i == m ? Long.MAX_VALUE : a[i];
            long bL = j == 0 ? Long.MIN_VALUE : b[j - 1], bR = j == n ? Long.MAX_VALUE : b[j];
            if (aL <= bR && bL <= aR) {
                long left = Math.max(aL, bL);
                if ((m + n) % 2 == 1) return left;
                return (left + Math.min(aR, bR)) / 2.0;
            }
            if (aL > bR) hi = i - 1; else lo = i + 1;
        }
    }
}
