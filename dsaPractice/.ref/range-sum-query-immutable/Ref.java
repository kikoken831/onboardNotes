class Ref implements Problem {
    public Spec spec() {
        return Spec.design("NumArray", T.INT_ARRAY)
            .op("sumRange", T.INT, T.INT, T.INT)
            .exampleCalls("[\"NumArray\",\"sumRange\",\"sumRange\",\"sumRange\"]", "[[[-2,0,3,-5,2,-1]],[0,2],[2,5],[0,5]]", "[null,1,-1,-3]")
            .exampleCalls("[\"NumArray\",\"sumRange\"]", "[[[7]],[0,0]]", "[null,7]")
            .gen(g -> {
                int[] a = g.ints(1, 30, -20, 20);
                Calls.Builder b = new Calls.Builder("NumArray", (Object) a);
                int n = g.size(1, 40);
                for (int i = 0; i < n; i++) { int l = g.nextInt(0, a.length - 1); b.call("sumRange", l, g.nextInt(l, a.length - 1)); }
                return new Object[]{ b.build() };
            })
            .patterns("Array", "Design", "Prefix Sum").limitMs(2000).maxN(50000);
    }
}

class NumArray {
    private final int[] pre;
    public NumArray(int[] nums) { pre = new int[nums.length + 1]; for (int i = 0; i < nums.length; i++) pre[i + 1] = pre[i] + nums[i]; }
    public int sumRange(int left, int right) {
        Pre.check(0 <= left && left <= right && right < pre.length - 1, "0 <= left <= right < n");
        return pre[right + 1] - pre[left];
    }
}
