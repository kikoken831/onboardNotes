class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findPeakElement", T.INT_ARRAY).returns(T.INT)
            // Any peak is correct: check the property, not one index.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    int[] a = (int[]) input[0];
                    int i = (Integer) yours;
                    if (i < 0 || i >= a.length) return "index " + i + " is out of range";
                    long left = i == 0 ? Long.MIN_VALUE : a[i - 1], right = i == a.length - 1 ? Long.MIN_VALUE : a[i + 1];
                    if (a[i] <= left || a[i] <= right) return "index " + i + " (value " + a[i] + ") is not a peak";
                    return null;
                }
            })
            .example("[1,2,3,1]", "2").example("[1,2,1,3,5,6,4]", "5").example("[1]", "0")
            .example("[1,2]", "1").example("[2,1]", "0")
            .gen(g -> {
                int n = g.size(1, 50);
                int[] a = new int[n];
                for (int i = 0; i < n; i++) do { a[i] = g.nextInt(-9, 9); } while (i > 0 && a[i] == a[i - 1]);
                return new Object[]{ a };
            })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(200000);
    }
    public int findPeakElement(int[] a) {
        for (int i = 1; i < a.length; i++) Pre.check(a[i] != a[i - 1], "adjacent values differ");
        int lo = 0, hi = a.length - 1;
        while (lo < hi) { int m = (lo + hi) / 2; if (a[m] < a[m + 1]) lo = m + 1; else hi = m; }
        return lo;
    }
}
