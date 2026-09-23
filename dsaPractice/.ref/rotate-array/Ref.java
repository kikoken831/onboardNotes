class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rotate", T.INT_ARRAY, T.INT).returns(T.VOID).inPlace(0)
            .exampleInPlace("[1,2,3,4,5,6,7], 3", "[5,6,7,1,2,3,4]")
            .exampleInPlace("[-1,-100,3,99], 2",  "[3,99,-1,-100]")
            .exampleInPlace("[1,2], 3",           "[2,1]")          // k > n
            .exampleInPlace("[1,2,3], 0",         "[1,2,3]")
            .exampleInPlace("[1], 100000",        "[1]")
            .gen(g -> new Object[]{ g.ints(1, 40, -9, 9), g.chance(0.2) ? g.nextInt(0, 100000) : g.nextInt(0, 50) })
            .patterns("Array", "Math", "Two Pointers").limitMs(2000).maxN(200000);
    }
    public void rotate(int[] nums, int k) {
        Pre.length(nums, 1, 1_000_000, "nums");
        Pre.check(k >= 0, "0 <= k");
        int n = nums.length;
        k %= n;
        reverse(nums, 0, n - 1); reverse(nums, 0, k - 1); reverse(nums, k, n - 1);
    }
    private static void reverse(int[] a, int i, int j) { for (; i < j; i++, j--) { int t = a[i]; a[i] = a[j]; a[j] = t; } }
}
