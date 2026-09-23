class Ref implements Problem {

    public Spec spec() {
        return Spec.method("removeDuplicates", T.INT_ARRAY).returns(T.INT)
            .lengthAndPrefix(0)
            .example("[1,1,2]",               "[1,2]")
            .example("[0,0,1,1,1,2,2,3,3,4]", "[0,1,2,3,4]")
            .example("[1]",                   "[1]")
            .example("[2,2,2]",               "[2]")
            .example("[-3,-1,0,5]",           "[-3,-1,0,5]")
            .gen(g -> new Object[]{ g.sortedInts(1, 60, -8, 8) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public int removeDuplicates(int[] nums) {
        Pre.length(nums, 1, 1000_000, "nums");
        Pre.sorted(nums, false, "nums");
        int w = 1;
        for (int r = 1; r < nums.length; r++) {
            if (nums[r] != nums[w - 1]) nums[w++] = nums[r];
        }
        return w;
    }
}
