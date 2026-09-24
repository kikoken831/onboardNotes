import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findOrder", T.INT, T.INT_ARRAY_2D).returns(T.INT_ARRAY)
            // Any valid order is accepted; [] exactly when there is none.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    int n = (Integer) input[0];
                    int[][] pre = (int[][]) input[1];
                    int[] y = (int[]) yours, r = (int[]) reference;
                    if (y == null) return "returned null";
                    if (r.length == 0) return y.length == 0 ? null : "there is a cycle, so the answer is [] -- you returned " + y.length + " courses";
                    if (y.length != n) return "returned " + y.length + " courses, expected all " + n;
                    int[] pos = new int[n];
                    Arrays.fill(pos, -1);
                    for (int i = 0; i < n; i++) { if (y[i] < 0 || y[i] >= n || pos[y[i]] >= 0) return "not a permutation of 0.." + (n - 1); pos[y[i]] = i; }
                    for (int[] p : pre) if (pos[p[1]] > pos[p[0]]) return "course " + p[0] + " comes before its prerequisite " + p[1];
                    return null;
                }
            })
            .example("2, [[1,0]]", "[0,1]").example("4, [[1,0],[2,0],[3,1],[3,2]]", "[0,2,1,3]").example("1, []", "[0]")
            .example("2, [[0,1],[1,0]]", "[]").example("3, []", "[0,1,2]")
            .gen(Ref::graph)
            .patterns("Depth-First Search", "Breadth-First Search", "Graph Theory", "Topological Sort").limitMs(2000).maxN(20000);
    }
    static Object[] graph(Gen g) {
        int n = g.size(1, 12);
        Set<Long> seen = new HashSet<>();
        List<int[]> e = new ArrayList<>();
        int[] order = g.permutation(n);
        boolean dag = g.chance(0.6);
        for (int k = 0; k < n * 2; k++) {
            int a = g.nextInt(0, n - 1), b = g.nextInt(0, n - 1);
            if (a == b) continue;
            if (dag && order[a] < order[b]) { int t = a; a = b; b = t; }
            if (seen.add(a * 1000L + b)) e.add(new int[]{ a, b });
        }
        return new Object[]{ n, e.toArray(new int[0][]) };
    }
    public int[] findOrder(int n, int[][] pre) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        int[] in = new int[n];
        for (int[] p : pre) { adj.get(p[1]).add(p[0]); in[p[0]]++; }
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (in[i] == 0) q.add(i);
        int[] out = new int[n];
        int k = 0;
        while (!q.isEmpty()) { int u = q.poll(); out[k++] = u; for (int v : adj.get(u)) if (--in[v] == 0) q.add(v); }
        return k == n ? out : new int[0];
    }
}
