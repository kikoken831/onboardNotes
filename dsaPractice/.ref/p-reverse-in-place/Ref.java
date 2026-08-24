class Ref implements Problem {

    public Spec spec() {
        return Spec.method("reverseInPlace", T.INT_ARRAY).returns(T.VOID)
            .inPlace(0)
            .exampleInPlace("[1,2,3,4]", "[4,3,2,1]")
            .exampleInPlace("[1,2,3]",   "[3,2,1]")
            .exampleInPlace("[]",        "[]")
            .exampleInPlace("[9]",       "[9]")
            .exampleInPlace("[1,1,2]",   "[2,1,1]")
            .gen(g -> new Object[]{ g.ints(0, 60, -20, 20) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(500000);
    }

    public void reverseInPlace(int[] nums) {
        for (int lo = 0, hi = nums.length - 1; lo < hi; lo++, hi--) {
            int t = nums[lo]; nums[lo] = nums[hi]; nums[hi] = t;
        }
    }
}
