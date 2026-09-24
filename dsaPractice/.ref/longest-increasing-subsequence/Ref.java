class Ref implements Problem {
    public Spec spec() {
        return Spec.method("lengthOfLIS", T.INT_ARRAY).returns(T.INT)
            .example("[10,9,2,5,3,7,101,18]", "4").example("[0,1,0,3,2,3]", "4").example("[7,7,7,7,7,7,7]", "1").example("[5]", "1")
            .gen(g -> new Object[]{ g.ints(1, 50, -10, 10) })
            .patterns("Array", "Binary Search", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int lengthOfLIS(int[] a) {
        int[] tails = new int[a.length];                 // tails[k] = smallest tail of an increasing run of length k+1
        int len = 0;
        for (int x : a) {
            int lo = 0, hi = len;
            while (lo < hi) { int m = (lo + hi) / 2; if (tails[m] < x) lo = m + 1; else hi = m; }
            tails[lo] = x;
            if (lo == len) len++;
        }
        return len;
    }
}
