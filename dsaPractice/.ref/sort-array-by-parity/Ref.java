class Ref implements Problem {

    public Spec spec() {
        return Spec.method("sortArrayByParity", T.INT_ARRAY).returns(T.INT_ARRAY)
            // Any evens-then-odds arrangement is accepted, as on LeetCode.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    int[] before = (int[]) input[0];
                    if (yours == null) return "returned null";
                    int[] after = (int[]) yours;
                    if (after.length != before.length) {
                        return "length changed from " + before.length + " to " + after.length;
                    }
                    boolean seenOdd = false;
                    for (int i = 0; i < after.length; i++) {
                        if (after[i] % 2 != 0) seenOdd = true;
                        else if (seenOdd) {
                            return "even value " + after[i] + " at index " + i
                                 + " appears after an odd value";
                        }
                    }
                    int[] a = before.clone(), b = after.clone();
                    java.util.Arrays.sort(a);
                    java.util.Arrays.sort(b);
                    if (!java.util.Arrays.equals(a, b)) {
                        return "not a rearrangement of the input — a value was lost or duplicated";
                    }
                    return null;
                }
            })
            .example("[3,1,2,4]", "[2,4,3,1]")
            .example("[0]",       "[0]")
            .example("[1,3]",     "[1,3]")
            .example("[2,1]",     "[2,1]")
            .gen(g -> new Object[]{ g.ints(1, 60, 0, 5000) })
            .patterns("Array", "Two Pointers", "Sorting")
            .limitMs(2000)
            .maxN(200000);
    }

    public int[] sortArrayByParity(int[] nums) {
        Pre.length(nums, 1, 1000_000, "nums");
        int[] a = nums.clone();
        int w = 0;
        for (int r = 0; r < a.length; r++) {
            if (a[r] % 2 == 0) { int t = a[w]; a[w] = a[r]; a[r] = t; w++; }
        }
        return a;
    }
}
