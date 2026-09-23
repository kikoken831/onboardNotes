class Ref implements Problem {
    public Spec spec() {
        return Spec.method("sortColors", T.INT_ARRAY).returns(T.VOID).inPlace(0)
            .exampleInPlace("[2,0,2,1,1,0]", "[0,0,1,1,2,2]")
            .exampleInPlace("[2,0,1]",       "[0,1,2]")
            .exampleInPlace("[0]",           "[0]")
            .exampleInPlace("[2,2,0]",       "[0,2,2]")
            .gen(g -> new Object[]{ g.ints(1, 60, 0, 2) })
            .patterns("Array", "Two Pointers", "Sorting").limitMs(2000).maxN(200000);
    }
    public void sortColors(int[] nums) {
        Pre.length(nums, 1, 1_000_000, "nums");
        int lo = 0, mid = 0, hi = nums.length - 1;
        while (mid <= hi) {
            Pre.check(nums[mid] >= 0 && nums[mid] <= 2, "values are 0, 1, 2");
            if (nums[mid] == 0) swap(nums, lo++, mid++);
            else if (nums[mid] == 2) swap(nums, mid, hi--);      // don't advance mid: unseen value
            else mid++;
        }
    }
    private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
}
