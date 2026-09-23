class Ref implements Problem {

    public Spec spec() {
        return Spec.method("runningSum", T.INT_ARRAY).returns(T.INT_ARRAY)
            .example("[1,2,3,4]",    "[1,3,6,10]")
            .example("[1,1,1,1,1]",  "[1,2,3,4,5]")
            .example("[3,1,2,10,1]", "[3,4,6,16,17]")
            .example("[-1]",         "[-1]")
            .example("[5,-5,5]",     "[5,0,5]")
            .gen(g -> new Object[]{ g.ints(1, 60, -1000, 1000) })
            .patterns("Array", "Prefix Sum")
            .limitMs(2000)
            .maxN(200000);
    }

    public int[] runningSum(int[] nums) {
        Pre.length(nums, 1, 1000_000, "nums");
        int[] out = new int[nums.length];
        int s = 0;
        for (int i = 0; i < nums.length; i++) out[i] = s += nums[i];
        return out;
    }
}
