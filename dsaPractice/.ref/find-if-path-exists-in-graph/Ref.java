import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("validPath", T.INT, T.INT_ARRAY_2D, T.INT, T.INT).returns(T.BOOL)
            .example("3, [[0,1],[1,2],[2,0]], 0, 2",               "true")
            .example("6, [[0,1],[0,2],[3,5],[5,4],[4,3]], 0, 5",   "false")
            .example("1, [], 0, 0",                                "true")
            .example("2, [], 0, 1",                                "false")
            .example("4, [[3,2]], 2, 3",                           "true")   // edges are undirected
            .gen(g -> {
                int n = g.size(1, 30);
                Set<Long> seen = new HashSet<>();
                List<int[]> e = new ArrayList<>();
                int m = g.nextInt(0, Math.min(n * 2, n * (n - 1) / 2));
                for (int k = 0; k < m * 3 && e.size() < m; k++) {
                    int a = g.nextInt(0, n - 1), b = g.nextInt(0, n - 1);
                    if (a == b) continue;
                    long key = (long) Math.min(a, b) * 1000 + Math.max(a, b);
                    if (seen.add(key)) e.add(new int[]{ a, b });
                }
                return new Object[]{ n, e.toArray(new int[0][]), g.nextInt(0, n - 1), g.nextInt(0, n - 1) };
            })
            .patterns("Depth-First Search", "Breadth-First Search", "Union-Find", "Graph Theory")
            .limitMs(2000)
            .maxN(20000);
    }

    public boolean validPath(int n, int[][] edges, int source, int destination) {
        Pre.check(n >= 1 && source >= 0 && source < n && destination >= 0 && destination < n, "0 <= source, destination < n");
        Set<Long> dup = new HashSet<>();
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            Pre.check(e.length == 2 && e[0] != e[1] && e[0] >= 0 && e[1] >= 0 && e[0] < n && e[1] < n, "valid edge, no self-loop");
            Pre.check(dup.add((long) Math.min(e[0], e[1]) * 1_000_000 + Math.max(e[0], e[1])), "no duplicate edges");
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }
        boolean[] seen = new boolean[n];
        Deque<Integer> q = new ArrayDeque<>();
        q.add(source); seen[source] = true;
        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == destination) return true;
            for (int v : adj.get(u)) if (!seen[v]) { seen[v] = true; q.add(v); }
        }
        return false;
    }
}
