class Ref implements Problem {

    public Spec spec() {
        return Spec.method("findContentChildren", T.INT_ARRAY, T.INT_ARRAY).returns(T.INT)
            .example("[1,2,3], [1,1]",   "1")
            .example("[1,2], [1,2,3]",   "2")
            .example("[1], []",          "0")
            .example("[10,9,8,7], [5,6,7,8]", "2")
            .gen(g -> new Object[]{ g.ints(1, 30, 1, 20), g.ints(0, 30, 1, 20) })
            .patterns("Array", "Greedy", "Sorting")
            .limitMs(2000)
            .maxN(100000);
    }

    public int findContentChildren(int[] g, int[] s) {
        Pre.length(g, 1, 1000_000, "g");
        int[] greed = g.clone(), size = s.clone();
        java.util.Arrays.sort(greed);
        java.util.Arrays.sort(size);
        int child = 0;
        for (int c = 0; c < size.length && child < greed.length; c++) {
            if (size[c] >= greed[child]) child++;
        }
        return child;
    }
}
