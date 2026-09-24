class Ref implements Problem {
    public Spec spec() {
        return Spec.method("validTree", T.INT, T.INT_ARRAY_2D).returns(T.BOOL)
            .example("5, [[0,1],[0,2],[0,3],[1,4]]", "true").example("5, [[0,1],[1,2],[2,3],[1,3],[1,4]]", "false")
            .example("1, []", "true").example("4, [[0,1],[2,3]]", "false").example("4, [[0,1],[1,2],[2,0],[1,3]]", "false")
            .gen(g -> {
                int n = g.size(1, 12);
                java.util.Set<Long> seen = new java.util.HashSet<>();
                java.util.List<int[]> e = new java.util.ArrayList<>();
                int m = g.nextInt(0, Math.min(n * (n - 1) / 2, n + 2));
                if (g.chance(0.4)) for (int v = 1; v < n; v++) { int u = g.nextInt(0, v - 1); seen.add(Math.min(u, v) * 1000L + Math.max(u, v)); e.add(new int[]{ u, v }); }   // a tree
                for (int k = 0; k < m * 3 && e.size() < m; k++) { int a = g.nextInt(0, n - 1), b = g.nextInt(0, n - 1); if (a != b && seen.add(Math.min(a, b) * 1000L + Math.max(a, b))) e.add(new int[]{ a, b }); }
                return new Object[]{ n, e.toArray(new int[0][]) };
            })
            .patterns("Depth-First Search", "Breadth-First Search", "Union-Find", "Graph Theory").limitMs(2000).maxN(20000);
    }
    static int find(int[] p, int x) { while (p[x] != x) x = p[x] = p[p[x]]; return x; }
    public boolean validTree(int n, int[][] edges) {
        Pre.check(n >= 1, "1 <= n");
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (int[] e : edges) {
            Pre.check(e.length == 2 && e[0] >= 0 && e[0] < n && e[1] >= 0 && e[1] < n && e[0] != e[1], "0 <= a_i, b_i < n, no self-loops");
            Pre.check(seen.add(Math.min(e[0], e[1]) * 1_000_000L + Math.max(e[0], e[1])), "no repeated edges");
        }
        if (edges.length != n - 1) return false;             // a tree has exactly n - 1 edges
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int[] e : edges) {
            int a = find(p, e[0]), b = find(p, e[1]);
            if (a == b) return false;                         // this edge closes a cycle
            p[a] = b;
        }
        return true;
    }
}
