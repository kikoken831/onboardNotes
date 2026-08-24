class Ref implements Problem {

    public Spec spec() {
        return Spec.method("removeValue", T.INT_ARRAY, T.INT).returns(T.INT)
            // Judges the returned k together with nums[0..k-1]; anything past k is
            // explicitly undefined, which is LeetCode's own convention for LC 26/27/80.
            .lengthAndPrefix(0)
            .example("[3,2,2,3], 3",            "[2,2]")
            .example("[0,1,2,2,3,0,4,2], 2",    "[0,1,3,0,4]")
            .example("[], 1",                   "[]")
            .example("[2,2,2], 2",              "[]")
            .example("[1,2,3], 9",              "[1,2,3]")
            .gen(g -> new Object[]{ g.ints(0, 60, 0, 6), g.value(0, 6) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public int removeValue(int[] nums, int val) {
        int w = 0;
        for (int r = 0; r < nums.length; r++) {
            if (nums[r] != val) nums[w++] = nums[r];
        }
        return w;
    }
}
