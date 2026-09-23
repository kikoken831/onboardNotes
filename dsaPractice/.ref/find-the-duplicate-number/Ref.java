class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findDuplicate", T.INT_ARRAY).returns(T.INT)
            .example("[1,3,4,2,2]", "2")
            .example("[3,1,3,4,2]", "3")
            .example("[3,3,3,3,3]", "3")
            .example("[1,1]",       "1")
            .example("[2,5,9,6,9,3,8,9,7,1]", "9")
            .gen(g -> {
                int n = g.size(1, 40), dup = g.nextInt(1, n), extra = g.nextInt(1, n);   // n+1 slots
                int[] perm = g.permutation(n);
                int[] a = new int[n + 1];
                int w = 0;
                java.util.List<Integer> vals = new java.util.ArrayList<>();
                for (int v : perm) vals.add(v + 1);
                java.util.Collections.shuffle(vals, new java.util.Random(g.nextInt(0, 1 << 30)));
                int keep = n - extra + 1;                 // distinct values kept, dup among them
                java.util.List<Integer> pool = new java.util.ArrayList<>();
                pool.add(dup);
                for (int v : vals) if (v != dup && pool.size() < keep) pool.add(v);
                for (int v : pool) a[w++] = v;
                while (w < n + 1) a[w++] = dup;           // the one repeated value, repeated extra times
                for (int i = a.length - 1; i > 0; i--) { int j = g.nextInt(0, i); int t = a[i]; a[i] = a[j]; a[j] = t; }
                return new Object[]{ a };
            })
            .patterns("Array", "Two Pointers", "Binary Search", "Bit Manipulation").limitMs(2000).maxN(200000);
    }
    public int findDuplicate(int[] nums) {
        int n = nums.length - 1;
        Pre.check(n >= 1, "n >= 1");
        int[] c = new int[n + 1];
        int repeated = 0, which = -1;
        for (int v : nums) {
            Pre.check(v >= 1 && v <= n, "1 <= nums[i] <= n");
            if (++c[v] == 2) { repeated++; which = v; }
        }
        Pre.check(repeated == 1, "exactly one repeated value");
        int slow = nums[0], fast = nums[0];                 // Floyd on i -> nums[i]
        do { slow = nums[slow]; fast = nums[nums[fast]]; } while (slow != fast);
        slow = nums[0];
        while (slow != fast) { slow = nums[slow]; fast = nums[fast]; }
        return slow;
    }
}
