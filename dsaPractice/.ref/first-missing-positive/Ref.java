class Ref implements Problem {
    public Spec spec() {
        return Spec.method("firstMissingPositive", T.INT_ARRAY).returns(T.INT)
            .example("[1,2,0]",        "3")
            .example("[3,4,-1,1]",     "2")
            .example("[7,8,9,11,12]",  "1")
            .example("[1]",            "2")
            .example("[1,1]",          "2")          // duplicates must not loop forever
            .example("[2147483647]",   "1")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.ints(1, 40, -3, 12) : g.ints(1, 40, Integer.MIN_VALUE, Integer.MAX_VALUE) })
            .patterns("Array", "Hash Table").limitMs(2000).maxN(200000);
    }
    public int firstMissingPositive(int[] nums) {
        int[] a = nums.clone();
        int n = a.length;
        for (int i = 0; i < n; i++) {
            while (a[i] >= 1 && a[i] <= n && a[a[i] - 1] != a[i]) {
                int j = a[i] - 1, t = a[i]; a[i] = a[j]; a[j] = t;
            }
        }
        for (int i = 0; i < n; i++) if (a[i] != i + 1) return i + 1;
        return n + 1;
    }
}
