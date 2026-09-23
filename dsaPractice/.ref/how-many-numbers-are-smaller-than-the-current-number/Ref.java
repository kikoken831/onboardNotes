class Ref implements Problem {

    public Spec spec() {
        return Spec.method("smallerNumbersThanCurrent", T.INT_ARRAY).returns(T.INT_ARRAY)
            .example("[8,1,2,2,3]", "[4,0,1,1,3]")
            .example("[6,5,4,8]",   "[2,1,0,3]")
            .example("[7,7,7,7]",   "[0,0,0,0]")
            .example("[0,100]",     "[0,1]")
            .gen(g -> new Object[]{ g.ints(2, 50, 0, 100) })
            .patterns("Array", "Hash Table", "Counting")
            .limitMs(2000)
            .maxN(100000);
    }

    public int[] smallerNumbersThanCurrent(int[] nums) {
        Pre.length(nums, 2, 1000_000, "nums");
        int[] count = new int[102];
        for (int x : nums) {
            Pre.check(x >= 0 && x <= 100, "0 <= nums[i] <= 100");
            count[x + 1]++;
        }
        for (int v = 1; v < count.length; v++) count[v] += count[v - 1];   // count[v] = #values < v
        int[] out = new int[nums.length];
        for (int i = 0; i < nums.length; i++) out[i] = count[nums[i]];
        return out;
    }
}
