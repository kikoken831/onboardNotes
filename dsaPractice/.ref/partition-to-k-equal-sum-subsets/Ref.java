class Ref implements Problem {
    public Spec spec() {
        return Spec.method("canPartitionKSubsets", T.INT_ARRAY, T.INT).returns(T.BOOL)
            .example("[4,3,2,3,5,2,1], 4", "true").example("[1,2,3,4], 3", "false").example("[2,2,2,2,3,4,5], 4", "false")
            .example("[1,1,1,1], 4", "true").example("[5], 1", "true")
            .gen(g -> {
                int[] a = new int[g.size(1, 12)], seen = new int[9];
                for (int i = 0; i < a.length; i++) {             // no value more than 4 times, as LeetCode promises
                    int v; do v = g.value(1, 8); while (seen[v] >= 4);
                    seen[v]++; a[i] = v;
                }
                return new Object[]{ a, g.nextInt(1, Math.min(6, a.length)) };
            })
            .patterns("Array", "Dynamic Programming", "Backtracking", "Bitmask").limitMs(2000).maxN(16);
    }
    public boolean canPartitionKSubsets(int[] a, int k) {
        int n = a.length, sum = 0;
        Pre.check(k >= 1 && k <= n && n <= 16, "1 <= k <= n <= 16");
        java.util.Map<Integer, Integer> freq = new java.util.HashMap<>();
        for (int x : a) {
            Pre.check(1 <= x && x <= 10000, "1 <= nums[i] <= 10^4");
            Pre.check(freq.merge(x, 1, Integer::sum) <= 4, "each value occurs at most 4 times");
            sum += x;
        }
        if (sum % k != 0) return false;
        int t = sum / k;
        int[] fill = new int[1 << n];                    // fill[mask] = sum of mask modulo t, or -1 if unreachable
        java.util.Arrays.fill(fill, -1);
        fill[0] = 0;
        for (int m = 0; m < 1 << n; m++) {
            if (fill[m] < 0) continue;
            for (int i = 0; i < n; i++) if ((m >> i & 1) == 0 && fill[m] + a[i] <= t) fill[m | 1 << i] = (fill[m] + a[i]) % t;
        }
        return fill[(1 << n) - 1] == 0;
    }
}
