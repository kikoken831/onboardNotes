class Ref implements Problem {
    public Spec spec() {
        return Spec.method("missingNumber", T.INT_ARRAY).returns(T.INT)
            .example("[3,0,1]",             "2")
            .example("[0,1]",               "2")
            .example("[9,6,4,2,3,5,7,0,1]", "8")
            .example("[1]",                 "0")
            .example("[0]",                 "1")
            .gen(g -> {
                int n = g.size(1, 60), miss = g.nextInt(0, n);
                int[] p = g.permutation(n + 1), a = new int[n];
                int w = 0;
                for (int v : p) if (v != miss) a[w++] = v;
                return new Object[]{ a };
            })
            .patterns("Array", "Hash Table", "Math", "Bit Manipulation").limitMs(2000).maxN(200000);
    }
    public int missingNumber(int[] nums) {
        int n = nums.length;
        boolean[] seen = new boolean[n + 1];
        for (int v : nums) { Pre.check(v >= 0 && v <= n && !seen[v], "distinct values in [0, n]"); seen[v] = true; }
        long expected = (long) n * (n + 1) / 2, sum = 0;
        for (int v : nums) sum += v;
        return (int) (expected - sum);
    }
}
