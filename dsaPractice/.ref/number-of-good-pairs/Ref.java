class Ref implements Problem {

    public Spec spec() {
        return Spec.method("numIdenticalPairs", T.INT_ARRAY).returns(T.INT)
            .example("[1,2,3,1,1,3]", "4")
            .example("[1,1,1,1]",     "6")
            .example("[1,2,3]",       "0")
            .example("[5]",           "0")
            .example("[2,2]",         "1")
            .gen(g -> new Object[]{ g.ints(1, 60, 1, 5) })
            .patterns("Array", "Hash Table", "Counting")
            .limitMs(2000)
            .maxN(100000);
    }

    public int numIdenticalPairs(int[] nums) {
        Pre.length(nums, 1, 1000_000, "nums");
        java.util.Map<Integer, Integer> seen = new java.util.HashMap<>();
        int pairs = 0;
        for (int x : nums) {
            int c = seen.getOrDefault(x, 0);
            pairs += c;                 // x pairs with every earlier copy of itself
            seen.put(x, c + 1);
        }
        return pairs;
    }
}
