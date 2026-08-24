class Ref implements Problem {

    public Spec spec() {
        return Spec.method("rotateLeftK", T.INT_ARRAY, T.INT).returns(T.VOID)
            .inPlace(0)
            .exampleInPlace("[1,2,3,4,5], 2", "[3,4,5,1,2]")
            .exampleInPlace("[1,2,3], 0",     "[1,2,3]")
            .exampleInPlace("[1,2,3], 3",     "[1,2,3]")
            .exampleInPlace("[1,2,3], 4",     "[2,3,1]")
            .exampleInPlace("[], 5",          "[]")
            .exampleInPlace("[1], 7",         "[1]")
            .gen(g -> new Object[]{ g.ints(0, 60, -20, 20), g.value(0, 200) })
            .patterns("Array", "Math", "Two Pointers")
            .limitMs(2000)
            .maxN(500000);
    }

    public void rotateLeftK(int[] nums, int k) {
        int n = nums.length;
        if (n == 0) return;
        k %= n;
        if (k == 0) return;
        reverse(nums, 0, k - 1);
        reverse(nums, k, n - 1);
        reverse(nums, 0, n - 1);
    }

    private void reverse(int[] a, int lo, int hi) {
        while (lo < hi) {
            int t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            lo++; hi--;
        }
    }
}
