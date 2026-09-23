class Ref implements Problem {
    public Spec spec() {
        return Spec.method("majorityElement", T.INT_ARRAY).returns(T.INT)
            .example("[3,2,3]",         "3")
            .example("[2,2,1,1,1,2,2]", "2")
            .example("[1]",             "1")
            .example("[6,5,5]",         "5")
            .gen(g -> {
                int n = g.size(1, 50), m = g.nextInt(-9, 9), need = n / 2 + 1;
                int[] a = new int[n];
                for (int i = 0; i < n; i++) a[i] = i < need ? m : g.nextInt(-9, 9);
                for (int i = n - 1; i > 0; i--) { int j = g.nextInt(0, i); int t = a[i]; a[i] = a[j]; a[j] = t; }
                return new Object[]{ a };
            })
            .patterns("Array", "Hash Table", "Divide and Conquer", "Sorting").limitMs(2000).maxN(200000);
    }
    public int majorityElement(int[] nums) {
        Pre.length(nums, 1, 1_000_000, "nums");
        int cand = 0, votes = 0;
        for (int x : nums) {
            if (votes == 0) cand = x;
            votes += x == cand ? 1 : -1;
        }
        int count = 0;
        for (int x : nums) if (x == cand) count++;
        Pre.check(count > nums.length / 2, "a majority element exists");
        return cand;
    }
}
