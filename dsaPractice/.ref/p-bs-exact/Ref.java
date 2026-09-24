class Ref implements Problem {

    public Spec spec() {
        return Spec.method("bsExact", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[-1,0,3,5,9,12], 9", "4")
            .example("[-1,0,3,5,9,12], 2", "-1")
            .example("[], 1",              "-1")
            .example("[5], 5",             "0")
            .example("[5], -5",            "-1")
            .example("[1,2], 2",           "1")
            .gen(g -> {
                int[] a = g.strictlySortedInts(0, 60, -Math.max(30, g.forcedSize), Math.max(30, g.forcedSize));
                // Half the searches should hit, or misses dominate and hitting paths
                // go untested.
                int target = (a.length > 0 && g.chance(0.5))
                    ? a[g.nextInt(0, a.length - 1)]
                    : g.value(-35, 35);
                return new Object[]{ a, target };
            })
            .patterns("Array", "Binary Search")
            .limitMs(2000)
            .maxN(500000);
    }

    public int bsExact(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] == target) return mid;
            else if (nums[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }
}
