class Ref implements Problem {
    public Spec spec() {
        return Spec.method("singleNumber", T.INT_ARRAY).returns(T.INT)
            .example("[2,2,1]",     "1")
            .example("[4,1,2,1,2]", "4")
            .example("[1]",         "1")
            .example("[-3,5,5]",    "-3")
            .gen(g -> {
                int pairs = g.size(0, 25);
                int[] vals = g.distinctInts(pairs + 1, pairs + 1, -30000, 30000);
                int[] a = new int[2 * pairs + 1];
                int w = 0;
                for (int i = 0; i < pairs; i++) { a[w++] = vals[i]; a[w++] = vals[i]; }
                a[w] = vals[pairs];
                for (int i = a.length - 1; i > 0; i--) { int j = g.nextInt(0, i); int t = a[i]; a[i] = a[j]; a[j] = t; }
                return new Object[]{ a };
            })
            .patterns("Array", "Bit Manipulation").limitMs(2000).maxN(200000);
    }
    public int singleNumber(int[] nums) {
        Pre.check(nums.length % 2 == 1, "odd length: pairs plus one");
        java.util.Map<Integer, Integer> c = new java.util.HashMap<>();
        for (int x : nums) c.merge(x, 1, Integer::sum);
        int single = 0, singles = 0;
        for (java.util.Map.Entry<Integer, Integer> e : c.entrySet()) {
            Pre.check(e.getValue() <= 2, "every other value appears exactly twice");
            if (e.getValue() == 1) { single = e.getKey(); singles++; }
        }
        Pre.check(singles == 1, "exactly one value appears once");
        int x = 0;
        for (int v : nums) x ^= v;
        return x;
    }
}
