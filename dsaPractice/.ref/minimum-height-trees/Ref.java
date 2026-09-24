import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findMinHeightTrees", T.INT, T.INT_ARRAY_2D).returns(T.LIST_INT)
            .compare(Cmp.MULTISET)
            .example("4, [[1,0],[1,2],[1,3]]", "[1]").example("6, [[3,0],[3,1],[3,2],[3,4],[5,4]]", "[3,4]")
            .example("1, []", "[0]").example("2, [[0,1]]", "[0,1]").example("3, [[0,1],[1,2]]", "[1]")
            .gen(g -> {
                int n = g.size(1, 20);
                int[][] e = new int[n - 1][];
                for (int v = 1; v < n; v++) e[v - 1] = new int[]{ g.nextInt(0, v - 1), v };
                return new Object[]{ n, e };
            })
            .patterns("Depth-First Search", "Breadth-First Search", "Graph Theory", "Topological Sort").limitMs(2000).maxN(20000);
    }
    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        Pre.check(edges.length == n - 1, "a tree");
        if (n == 1) return new ArrayList<>(List.of(0));
        List<Set<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new HashSet<>());
        for (int[] e : edges) { adj.get(e[0]).add(e[1]); adj.get(e[1]).add(e[0]); }
        List<Integer> leaves = new ArrayList<>();
        for (int i = 0; i < n; i++) if (adj.get(i).size() == 1) leaves.add(i);
        int left = n;
        while (left > 2) {                                    // peel leaves until 1 or 2 centres remain
            left -= leaves.size();
            List<Integer> next = new ArrayList<>();
            for (int l : leaves) { int p = adj.get(l).iterator().next(); adj.get(p).remove(l); if (adj.get(p).size() == 1) next.add(p); }
            leaves = next;
        }
        return leaves;
    }
}
