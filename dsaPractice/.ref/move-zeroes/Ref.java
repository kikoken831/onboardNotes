class Ref implements Problem {
    public Spec spec() {
        return Spec.method("moveZeroes", T.INT_ARRAY).returns(T.VOID).inPlace(0)
            .exampleInPlace("[0,1,0,3,12]", "[1,3,12,0,0]")
            .exampleInPlace("[0]",          "[0]")
            .exampleInPlace("[1,0]",        "[1,0]")
            .exampleInPlace("[0,0,1]",      "[1,0,0]")
            .gen(g -> new Object[]{ g.ints(1, 60, -3, 3) })
            .patterns("Array", "Two Pointers").limitMs(2000).maxN(200000);
    }
    public void moveZeroes(int[] nums) {
        Pre.length(nums, 1, 1_000_000, "nums");
        int w = 0;
        for (int r = 0; r < nums.length; r++) if (nums[r] != 0) { int t = nums[w]; nums[w++] = nums[r]; nums[r] = t; }
    }
}
