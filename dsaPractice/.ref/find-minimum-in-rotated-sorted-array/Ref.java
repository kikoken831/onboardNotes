class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findMin", T.INT_ARRAY).returns(T.INT)
            .example("[3,4,5,1,2]", "1").example("[4,5,6,7,0,1,2]", "0").example("[11,13,15,17]", "11")
            .example("[2,1]", "1").example("[1]", "1")
            .gen(g -> {
                int[] s = g.strictlySortedInts(1, 40, -40, 40);
                int k = g.nextInt(0, s.length - 1);
                int[] a = new int[s.length];
                for (int i = 0; i < s.length; i++) a[i] = s[(i + k) % s.length];
                return new Object[]{ a };
            })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(200000);
    }
    public int findMin(int[] a) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) { int m = (lo + hi) / 2; if (a[m] > a[hi]) lo = m + 1; else hi = m; }
        return a[lo];
    }
}
