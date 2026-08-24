class Ref implements Problem {

    public Spec spec() {
        return Spec.method("dedupeSorted", T.INT_ARRAY).returns(T.INT)
            .lengthAndPrefix(0)
            .example("[1,1,2]",                "[1,2]")
            .example("[0,0,1,1,1,2,2,3,3,4]",  "[0,1,2,3,4]")
            .example("[]",                     "[]")
            .example("[7]",                    "[7]")
            .example("[5,5,5,5]",              "[5]")
            .example("[-3,-3,0,0,0,9]",        "[-3,0,9]")
            .gen(g -> new Object[]{ g.sortedInts(0, 60, -8, 8) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public int dedupeSorted(int[] nums) {
        if (nums.length == 0) return 0;
        int w = 1;
        for (int r = 1; r < nums.length; r++) {
            if (nums[r] != nums[w - 1]) nums[w++] = nums[r];
        }
        return w;
    }
}
