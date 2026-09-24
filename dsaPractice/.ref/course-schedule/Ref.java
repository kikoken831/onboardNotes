import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("canFinish", T.INT, T.INT_ARRAY_2D).returns(T.BOOL)
            .example("2, [[1,0]]", "true").example("2, [[1,0],[0,1]]", "false").example("1, []", "true")
            .example("3, [[0,1],[0,2],[1,2]]", "true").example("3, [[0,1],[1,2],[2,0]]", "false")
            .gen(Ref::graph)
            .patterns("Depth-First Search", "Breadth-First Search", "Graph Theory", "Topological Sort").limitMs(2000).maxN(20000);
    }
    /** A DAG half the time; otherwise random edges, which usually contain a cycle. */
    static Object[] graph(Gen g) {
        int n = g.size(1, 12);
        Set<Long> seen = new HashSet<>();
        List<int[]> e = new ArrayList<>();
        int[] order = g.permutation(n);
        boolean dag = g.chance(0.5);
        for (int k = 0; k < n * 2; k++) {
            int a = g.nextInt(0, n - 1), b = g.nextInt(0, n - 1);
            if (a == b) continue;
            if (dag && order[a] < order[b]) { int t = a; a = b; b = t; }
            if (seen.add(a * 1000L + b)) e.add(new int[]{ a, b });
        }
        return new Object[]{ n, e.toArray(new int[0][]) };
    }
    public boolean canFinish(int n, int[][] pre) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        int[] in = new int[n];
        for (int[] p : pre) { adj.get(p[1]).add(p[0]); in[p[0]]++; }
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (in[i] == 0) q.add(i);
        int done = 0;
        while (!q.isEmpty()) { int u = q.poll(); done++; for (int v : adj.get(u)) if (--in[v] == 0) q.add(v); }
        return done == n;
    }
}
