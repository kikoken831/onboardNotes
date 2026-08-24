class Ref implements Problem {

    public Spec spec() {
        return Spec.method("countComponents", T.INT, T.INT_ARRAY_2D).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("5, [[0,1],[1,2],[3,4]]", "2")
            .example("5, []",                  "5")
            .example("1, []",                  "1")
            .example("3, [[0,1],[1,2],[0,2]]", "1")
            .example("2, [[0,0]]",             "2")
            .gen(g -> {
                int n = g.nextInt(1, 30);
                int count = g.size(0, 25);
                int[][] edges = new int[count][2];
                for (int i = 0; i < count; i++) {
                    edges[i][0] = g.nextInt(0, n - 1);
                    edges[i][1] = g.nextInt(0, n - 1);
                }
                return new Object[]{ n, edges };
            })
            .patterns("Union-Find", "Graph Theory", "Depth-First Search")
            .limitMs(3000)
            .maxN(20000);
    }

    private int[] parent, size;

    public int countComponents(int n, int[][] edges) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }

        int components = n;
        for (int[] e : edges) {
            int ra = find(e[0]), rb = find(e[1]);
            if (ra == rb) continue;
            if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            size[ra] += size[rb];
            components--;
        }
        return components;
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }
}
