class Ref implements Problem {
    public Spec spec() {
        return Spec.method("sortedSquares", T.INT_ARRAY).returns(T.INT_ARRAY)
            .example("[-4,-1,0,3,10]", "[0,1,9,16,100]")
            .example("[-7,-3,2,3,11]", "[4,9,9,49,121]")
            .example("[-5,-3,-1]",     "[1,9,25]")
            .example("[2]",            "[4]")
            .gen(g -> new Object[]{ g.sortedInts(1, 60, -40, 40) })
            .patterns("Array", "Two Pointers", "Sorting").limitMs(2000).maxN(200000);
    }
    public int[] sortedSquares(int[] nums) {
        Pre.length(nums, 1, 1_000_000, "nums");
        Pre.sorted(nums, false, "nums");
        int n = nums.length, i = 0, j = n - 1;
        int[] out = new int[n];
        for (int w = n - 1; w >= 0; w--) {
            if (Math.abs(nums[i]) > Math.abs(nums[j])) out[w] = nums[i] * nums[i++];
            else out[w] = nums[j] * nums[j--];
        }
        return out;
    }
}
