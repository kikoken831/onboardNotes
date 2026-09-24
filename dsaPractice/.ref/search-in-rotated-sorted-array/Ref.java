class Ref implements Problem {
    public Spec spec() {
        return Spec.method("search", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[4,5,6,7,0,1,2], 0", "4").example("[4,5,6,7,0,1,2], 3", "-1").example("[1], 0", "-1")
            .example("[3,1], 1", "1").example("[5,1,3], 5", "0")
            .gen(g -> { int[] a = rotated(g); return new Object[]{ a, g.chance(0.6) ? a[g.nextInt(0, a.length - 1)] : g.nextInt(-45, 45) }; })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(5000);
    }
    static int[] rotated(Gen g) {
        int span = Math.max(40, 2 * g.forcedSize);                // distinct values need room at timing sizes
        int[] s = g.strictlySortedInts(1, 40, -span, span);
        int k = g.nextInt(0, s.length - 1);
        int[] a = new int[s.length];
        for (int i = 0; i < s.length; i++) a[i] = s[(i + k) % s.length];
        return a;
    }
    public int search(int[] a, int t) {
        Pre.check(a.length >= 1, "1 <= nums.length");
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (int x : a) Pre.check(seen.add(x), "all values of nums are unique");
        int descents = 0;
        for (int i = 1; i < a.length; i++) if (a[i] < a[i - 1]) descents++;
        Pre.check(descents == 0 || (descents == 1 && a[a.length - 1] < a[0]), "nums is a rotated ascending array");
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) / 2;
            if (a[m] == t) return m;
            if (a[lo] <= a[m]) { if (a[lo] <= t && t < a[m]) hi = m - 1; else lo = m + 1; }   // left half sorted
            else               { if (a[m] < t && t <= a[hi]) lo = m + 1; else hi = m - 1; }
        }
        return -1;
    }
}
