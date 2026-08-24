class Ref implements Problem {

    public Spec spec() {
        return Spec.method("dsuOptimised", T.INT, T.INT_ARRAY_2D, T.INT_ARRAY_2D)
            .returns(T.BOOL_ARRAY)
            .compare(Cmp.EXACT)
            .example("5, [[0,1],[1,2]], [[0,2],[0,3]]",      "[true,false]")
            .example("1, [], [[0,0]]",                       "[true]")
            .example("4, [[0,1],[2,3],[1,3]], [[0,2]]",      "[true]")
            .example("2, [], [[0,1]]",                       "[false]")
            .gen(g -> {
                int n = g.nextInt(1, 40);
                return new Object[]{ n, pairs(g, n, 0, 30), pairs(g, n, 1, 30) };
            })
            .patterns("Union-Find", "Graph Theory")
            .limitMs(3000)
            .maxN(20000);
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

    public boolean[] dsuOptimised(int n, int[][] unions, int[][] queries) {
        int[] parent = new int[n], size = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }

        for (int[] u : unions) {
            int ra = find(parent, u[0]), rb = find(parent, u[1]);
            if (ra == rb) continue;
            if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            size[ra] += size[rb];
        }

        boolean[] out = new boolean[queries.length];
        for (int i = 0; i < queries.length; i++) {
            out[i] = find(parent, queries[i][0]) == find(parent, queries[i][1]);
        }
        return out;
    }

    private int find(int[] p, int x) {
        while (p[x] != x) {
            p[x] = p[p[x]];
            x = p[x];
        }
        return x;
    }
}
