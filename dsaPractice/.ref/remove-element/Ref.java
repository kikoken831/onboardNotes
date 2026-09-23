class Ref implements Problem {

    public Spec spec() {
        return Spec.method("removeElement", T.INT_ARRAY, T.INT).returns(T.INT)
            .lengthAndPrefixAnyOrder(0)
            .example("[3,2,2,3], 3",           "[2,2]")
            .example("[0,1,2,2,3,0,4,2], 2",   "[0,1,4,0,3]")
            .example("[], 0",                  "[]")
            .example("[1], 1",                 "[]")
            .example("[4,4,4], 5",             "[4,4,4]")
            .gen(g -> new Object[]{ g.ints(0, 60, 0, 6), g.value(0, 6) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public int removeElement(int[] nums, int val) {
        int w = 0;
        for (int r = 0; r < nums.length; r++) {
            if (nums[r] != val) nums[w++] = nums[r];
        }
        return w;
    }
}
