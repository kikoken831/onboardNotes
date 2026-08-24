class Ref implements Problem {

    public Spec spec() {
        return Spec.method("rangeSums", T.INT_ARRAY, T.INT_ARRAY_2D).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[1,2,3,4], [[0,0],[0,3],[1,2]]", "[1,10,5]")
            .example("[5], [[0,0]]",                   "[5]")
            .example("[-1,-2,-3], [[0,2]]",            "[-6]")
            .example("[1,2,3], []",                    "[]")
            .gen(g -> {
                int[] nums = g.ints(1, 50, -20, 20);
                int q = g.size(0, 20);
                int[][] queries = new int[q][2];
                for (int k = 0; k < q; k++) {
                    int a = g.nextInt(0, nums.length - 1), b = g.nextInt(0, nums.length - 1);
                    queries[k][0] = Math.min(a, b);
                    queries[k][1] = Math.max(a, b);
                }
                return new Object[]{ nums, queries };
            })
            .patterns("Array", "Prefix Sum", "Design")
            .limitMs(2000)
            .maxN(200000);
    }

    public int[] rangeSums(int[] nums, int[][] queries) {
        int[] p = new int[nums.length + 1];
        for (int i = 0; i < nums.length; i++) p[i + 1] = p[i] + nums[i];

        int[] out = new int[queries.length];
        for (int k = 0; k < queries.length; k++) {
            out[k] = p[queries[k][1] + 1] - p[queries[k][0]];
        }
        return out;
    }
}
