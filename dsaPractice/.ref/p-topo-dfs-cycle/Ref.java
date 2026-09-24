import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("topoDfs", T.INT, T.INT_ARRAY_2D).returns(T.INT_ARRAY)
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    int n = (Integer) input[0];
                    int[][] edges = (int[][]) input[1];
                    int[] order = (int[]) yours;
                    int[] refOrder = (int[]) reference;

                    if (order == null) return "returned null";
                    boolean acyclic = refOrder.length == n;

                    if (!acyclic) {
                        return order.length == 0 ? null
                             : "the graph has a cycle, so the answer must be empty, "
                               + "but you returned " + order.length + " nodes";
                    }
                    if (order.length != n) {
                        return "the graph is acyclic, so all " + n
                             + " nodes must appear; you returned " + order.length;
                    }
                    int[] pos = new int[n];
                    boolean[] seen = new boolean[n];
                    for (int i = 0; i < n; i++) {
                        int v = order[i];
                        if (v < 0 || v >= n) return "node " + v + " is out of range";
                        if (seen[v]) return "node " + v + " appears more than once";
                        seen[v] = true;
                        pos[v] = i;
                    }
                    for (int[] e : edges) {
                        if (pos[e[0]] > pos[e[1]]) {
                            return "edge " + e[0] + "->" + e[1] + " is violated: "
                                 + e[0] + " at index " + pos[e[0]] + " must come before "
                                 + e[1] + " at index " + pos[e[1]];
                        }
                    }
                    return null;
                }
            })
            .example("4, [[0,1],[0,2],[1,3],[2,3]]", "[0,1,2,3]")
            .example("2, [[0,1],[1,0]]",             "[]")
            .example("1, []",                        "[0]")
            .example("2, [[0,0]]",                   "[]")
            .example("3, []",                        "[0,1,2]")
            .gen(g -> {
                int n = g.forcedSize >= 0 ? g.forcedSize : g.nextInt(1, 20);   // timing: n nodes
                int[][] edges = g.chance(0.7) ? g.dag(n, g.size(0, 25)) : randomEdges(g, n);
                return new Object[]{ n, edges };
            })
            .patterns("Topological Sort", "Depth-First Search", "Graph Theory")
            .limitMs(3000)
            .maxN(5000);
    }

    private static int[][] randomEdges(Gen g, int n) {
        int count = g.size(0, 25);
        int[][] out = new int[count][2];
        for (int i = 0; i < count; i++) {
            out[i][0] = g.nextInt(0, n - 1);
            out[i][1] = g.nextInt(0, n - 1);
        }
        return out;
    }

    private List<List<Integer>> adj;
    private int[] colour;
    private List<Integer> order;

    public int[] topoDfs(int n, int[][] edges) {
        adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);

        colour = new int[n];
        order = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            if (colour[i] == 0 && !dfs(i)) return new int[0];
        }

        int[] out = new int[n];
        for (int i = 0; i < n; i++) out[i] = order.get(n - 1 - i);   // reverse
        return out;
    }

    private boolean dfs(int v) {
        colour[v] = 1;
        for (int w : adj.get(v)) {
            if (colour[w] == 1) return false;
            if (colour[w] == 0 && !dfs(w)) return false;
        }
        colour[v] = 2;
        order.add(v);
        return true;
    }
}
