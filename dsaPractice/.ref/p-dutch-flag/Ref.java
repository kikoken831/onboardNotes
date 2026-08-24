class Ref implements Problem {

    public Spec spec() {
        return Spec.method("dutchFlag", T.INT_ARRAY).returns(T.VOID)
            .inPlace(0)
            .exampleInPlace("[2,0,2,1,1,0]", "[0,0,1,1,2,2]")
            .exampleInPlace("[2,0,1]",       "[0,1,2]")
            .exampleInPlace("[0]",           "[0]")
            .exampleInPlace("[2,2,2]",       "[2,2,2]")
            .exampleInPlace("[1,1,1]",       "[1,1,1]")
            .exampleInPlace("[2,1,0]",       "[0,1,2]")
            .exampleInPlace("[1,2,0,1,2,0]", "[0,0,1,1,2,2]")
            .gen(g -> new Object[]{ g.ints(1, 60, 0, 2) })
            .patterns("Array", "Two Pointers", "Sorting")
            .limitMs(2000)
            .maxN(200000);
    }

    public void dutchFlag(int[] nums) {
        int lo = 0, i = 0, hi = nums.length - 1;
        while (i <= hi) {
            if (nums[i] == 0)      swap(nums, i++, lo++);
            else if (nums[i] == 2) swap(nums, i, hi--);
            else                   i++;
        }
    }

    private void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
}
