class Ref implements Problem {

    public Spec spec() {
        return Spec.method("merge", T.INT_ARRAY, T.INT, T.INT_ARRAY, T.INT).returns(T.VOID)
            .inPlace(0)
            .exampleInPlace("[1,2,3,0,0,0], 3, [2,5,6], 3", "[1,2,2,3,5,6]")
            .exampleInPlace("[1], 1, [], 0",                "[1]")
            .exampleInPlace("[0], 0, [1], 1",               "[1]")
            .exampleInPlace("[4,5,6,0,0,0], 3, [1,2,3], 3", "[1,2,3,4,5,6]")
            .exampleInPlace("[2,0], 1, [1], 1",             "[1,2]")
            .gen(g -> {
                int m = g.size(0, 30), n = g.size(0, 30);
                if (m + n == 0) { if (g.chance(0.5)) m = 1; else n = 1; }       // 1 <= m + n
                int[] a = g.sortedInts(m, m, -20, 20);
                int[] b = g.sortedInts(n, n, -20, 20);
                int[] nums1 = new int[m + n];
                System.arraycopy(a, 0, nums1, 0, m);   // tail stays 0, as on LeetCode
                return new Object[]{ nums1, m, b, n };
            })
            .patterns("Array", "Two Pointers", "Sorting")
            .limitMs(2000)
            .maxN(100000);
    }

    public void merge(int[] nums1, int m, int[] nums2, int n) {
        Pre.check(m >= 0 && n >= 0 && m + n >= 1 && nums1.length == m + n && nums2.length == n,
                  "1 <= m + n, nums1.length == m + n, nums2.length == n");
        Pre.sorted(java.util.Arrays.copyOf(nums1, m), false, "nums1[0..m)");
        Pre.sorted(nums2, false, "nums2");
        int i = m - 1, j = n - 1, w = m + n - 1;
        while (j >= 0) {
            if (i >= 0 && nums1[i] > nums2[j]) nums1[w--] = nums1[i--];
            else                               nums1[w--] = nums2[j--];
        }
    }
}
