import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("topoKahn", T.INT, T.INT_ARRAY_2D).returns(T.INT_ARRAY)
            // Many orders are valid, so validate the property instead of comparing.
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
                                 + e[0] + " appears at index " + pos[e[0]]
                                 + " but must come before " + e[1]
                                 + " at index " + pos[e[1]];
                        }
                    }
                    return null;
                }
            })
            .example("4, [[0,1],[0,2],[1,3],[2,3]]", "[0,1,2,3]")
            .example("2, [[0,1],[1,0]]",             "[]")
            .example("1, []",                        "[0]")
            .example("3, []",                        "[0,1,2]")
            .example("2, [[0,0]]",                   "[]")
            .gen(g -> {
                int n = g.nextInt(1, 20);
                // Mostly acyclic (built from a random topological order), sometimes
                // not, so both branches are exercised.
                int[][] edges = g.chance(0.7) ? g.dag(n, g.size(0, 25)) : randomEdges(g, n);
                return new Object[]{ n, edges };
            })
            .patterns("Topological Sort", "Breadth-First Search", "Graph Theory")
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

    public int[] topoKahn(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        int[] indeg = new int[n];
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            indeg[e[1]]++;
        }

        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);

        int[] order = new int[n];
        int count = 0;
        while (!q.isEmpty()) {
            int v = q.poll();
            order[count++] = v;
            for (int w : adj.get(v)) {
                if (--indeg[w] == 0) q.add(w);
            }
        }
        return count < n ? new int[0] : order;
    }
}
