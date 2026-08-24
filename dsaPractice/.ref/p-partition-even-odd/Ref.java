class Ref implements Problem {

    public Spec spec() {
        return Spec.method("partitionEvenOdd", T.INT_ARRAY).returns(T.VOID)
            .inPlace(0)
            // Any evens-then-odds arrangement is correct, so this validates the
            // property rather than comparing against one particular arrangement.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    int[] before = (int[]) input[0];
                    int[] after = (int[]) yours;
                    if (after == null) return "the array is null";
                    if (after.length != before.length) {
                        return "length changed from " + before.length + " to " + after.length;
                    }
                    boolean seenOdd = false;
                    for (int i = 0; i < after.length; i++) {
                        boolean odd = Math.abs(after[i] % 2) == 1;
                        if (odd) seenOdd = true;
                        else if (seenOdd) {
                            return "even value " + after[i] + " at index " + i
                                 + " appears after an odd value";
                        }
                    }
                    int[] a = before.clone(), b = after.clone();
                    java.util.Arrays.sort(a);
                    java.util.Arrays.sort(b);
                    if (!java.util.Arrays.equals(a, b)) {
                        return "the multiset of values changed — you lost or duplicated something";
                    }
                    return null;
                }
            })
            .exampleInPlace("[3,1,2,4]", "[2,4,3,1]")
            .exampleInPlace("[0]",       "[0]")
            .exampleInPlace("[1,3,5]",   "[1,3,5]")
            .exampleInPlace("[2,4,6]",   "[2,4,6]")
            .gen(g -> new Object[]{ g.ints(1, 60, 0, 12) })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public void partitionEvenOdd(int[] nums) {
        int w = 0;
        for (int r = 0; r < nums.length; r++) {
            if (nums[r] % 2 == 0) {
                int t = nums[w]; nums[w] = nums[r]; nums[r] = t;
                w++;
            }
        }
    }
}
