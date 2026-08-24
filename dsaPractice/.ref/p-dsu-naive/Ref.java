class Ref implements Problem {

    public Spec spec() {
        return Spec.method("dsuNaive", T.INT, T.INT_ARRAY_2D, T.INT_ARRAY_2D)
            .returns(T.BOOL_ARRAY)
            .compare(Cmp.EXACT)
            .example("5, [[0,1],[1,2]], [[0,2],[0,3]]", "[true,false]")
            .example("1, [], [[0,0]]",                  "[true]")
            .example("3, [[0,1],[0,1]], [[1,0]]",       "[true]")
            .example("2, [], [[0,1]]",                  "[false]")
            .gen(g -> {
                int n = g.nextInt(1, 12);
                return new Object[]{ n, pairs(g, n, 0, 10), pairs(g, n, 1, 10) };
            })
            .patterns("Union-Find", "Graph Theory")
            .limitMs(2000)
            .maxN(2000);
    }

    private static int[][] pairs(Gen g, int n, int min, int max) {
        int count = g.size(min, max);
        int[][] out = new int[count][2];
        for (int i = 0; i < count; i++) {
            out[i][0] = g.nextInt(0, n - 1);
            out[i][1] = g.nextInt(0, n - 1);
        }
        return out;
    }

    public boolean[] dsuNaive(int n, int[][] unions, int[][] queries) {
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int[] u : unions) {
            int ra = find(parent, u[0]), rb = find(parent, u[1]);
            if (ra != rb) parent[ra] = rb;
        }

        boolean[] out = new boolean[queries.length];
        for (int i = 0; i < queries.length; i++) {
            out[i] = find(parent, queries[i][0]) == find(parent, queries[i][1]);
        }
        return out;
    }

    private int find(int[] p, int x) {
        while (p[x] != x) x = p[x];
        return x;
    }
}
