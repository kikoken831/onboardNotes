import java.util.Arrays;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("threeSumClosest", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[-1,2,1,-4], 1", "2")
            .example("[0,0,0], 1",     "0")
            .example("[1,1,1,0], 100", "3")
            .example("[4,0,5,-5,3,3,0,-4,-5], -2", "-2")
            .gen(g -> {
                for (int attempt = 0; ; attempt++) {
                    int[] a = g.ints(3, 30, -20, 20);
                    int t = g.nextInt(-40, 40);
                    if (uniqueClosest(a, t) || attempt > 50) return new Object[]{ a, t };
                }
            })
            .patterns("Array", "Two Pointers", "Sorting").limitMs(2000).maxN(1000);
    }

    /** LeetCode guarantees one answer; reject inputs where t-d and t+d are both achievable. */
    static boolean uniqueClosest(int[] a, int t) {
        long best = Long.MAX_VALUE; java.util.Set<Integer> sums = new java.util.HashSet<>();
        for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) for (int k = j + 1; k < a.length; k++) {
            int s = a[i] + a[j] + a[k]; long d = Math.abs((long) s - t);
            if (d < best) { best = d; sums.clear(); }
            if (d == best) sums.add(s);
        }
        return sums.size() == 1;
    }

    public int threeSumClosest(int[] nums, int target) {
        Pre.length(nums, 3, 1_000_000, "nums");
        Pre.check(nums.length > 60 || uniqueClosest(nums, target), "exactly one closest sum");
        int[] a = nums.clone();
        Arrays.sort(a);
        int best = a[0] + a[1] + a[2];
        for (int i = 0; i < a.length - 2; i++) {
            int l = i + 1, r = a.length - 1;
            while (l < r) {
                int s = a[i] + a[l] + a[r];
                if (Math.abs(s - target) < Math.abs(best - target)) best = s;
                if (s < target) l++; else if (s > target) r--; else return s;
            }
        }
        return best;
    }
}
