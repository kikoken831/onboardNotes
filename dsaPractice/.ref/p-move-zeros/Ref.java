class Ref implements Problem {

    public Spec spec() {
        return Spec.method("moveZeros", T.INT_ARRAY).returns(T.VOID)
            .inPlace(0)
            .exampleInPlace("[0,1,0,3,12]", "[1,3,12,0,0]")
            .exampleInPlace("[0]",          "[0]")
            .exampleInPlace("[1,2,3]",      "[1,2,3]")
            .exampleInPlace("[0,0,1]",      "[1,0,0]")
            .exampleInPlace("[0,0,0]",      "[0,0,0]")
            .exampleInPlace("[1,0,2,0,3]",  "[1,2,3,0,0]")
            .gen(g -> new Object[]{ g.ints(1, 60, -4, 4) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public void moveZeros(int[] nums) {
        int w = 0;
        for (int r = 0; r < nums.length; r++) {
            if (nums[r] != 0) nums[w++] = nums[r];
        }
        while (w < nums.length) nums[w++] = 0;
    }
}
