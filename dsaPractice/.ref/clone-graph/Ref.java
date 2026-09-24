import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("cloneGraph", T.INT_ARRAY_2D).returns(T.INT_ARRAY_2D)
            // LeetCode's input is an adjacency list; node i+1's neighbours are adjList[i].
            // Each call gets its own real Node graph, and is handed node 1.
            .prepare(a -> a[0] = build((int[][]) a[0]))
            // Judge the clone's adjacency list -- and that it shares no node with the input.
            .judgeAs(T.INT_ARRAY_2D, (a, ret) -> {
                Set<Node> original = Collections.newSetFromMap(new IdentityHashMap<>());
                reach((Node) a[0], original);
                Set<Node> clone = Collections.newSetFromMap(new IdentityHashMap<>());
                reach((Node) ret, clone);
                for (Node n : clone) if (original.contains(n)) return "the result contains node " + n.val + " from the ORIGINAL graph -- it must be a deep copy";
                return adjacency((Node) ret);
            })
            .example("[[2,4],[1,3],[2,4],[1,3]]", "[[2,4],[1,3],[2,4],[1,3]]").example("[[]]", "[[]]").example("[]", "[]")
            .example("[[2],[1]]", "[[2],[1]]")
            .gen(g -> {
                int n = g.size(0, 10);
                List<Set<Integer>> adj = new ArrayList<>();
                for (int i = 0; i < n; i++) adj.add(new TreeSet<>());
                for (int v = 1; v < n; v++) { int u = g.nextInt(0, v - 1); adj.get(u).add(v); adj.get(v).add(u); }   // connected
                for (int k = 0; k < n; k++) { int x = g.nextInt(0, Math.max(0, n - 1)), y = g.nextInt(0, Math.max(0, n - 1)); if (x != y) { adj.get(x).add(y); adj.get(y).add(x); } }
                int[][] list = new int[n][];
                for (int i = 0; i < n; i++) list[i] = adj.get(i).stream().mapToInt(v -> v + 1).toArray();
                return new Object[]{ list };
            })
            .patterns("Hash Table", "Depth-First Search", "Breadth-First Search", "Graph Theory").limitMs(2000).maxN(20000);
    }
    static Node build(int[][] adj) {
        if (adj.length == 0) return null;
        Node[] n = new Node[adj.length];
        for (int i = 0; i < adj.length; i++) n[i] = new Node(i + 1);
        for (int i = 0; i < adj.length; i++) for (int v : adj[i]) { Pre.check(v >= 1 && v <= adj.length && v != i + 1, "valid neighbour"); n[i].neighbors.add(n[v - 1]); }
        return n[0];
    }
    static void reach(Node s, Set<Node> out) {
        if (s == null) return;
        Deque<Node> q = new ArrayDeque<>(); q.add(s); out.add(s);
        while (!q.isEmpty()) for (Node m : q.poll().neighbors) if (out.add(m)) q.add(m);
    }
    static int[][] adjacency(Node s) {
        if (s == null) return new int[0][];
        Set<Node> all = Collections.newSetFromMap(new IdentityHashMap<>());
        reach(s, all);
        int n = 0; for (Node x : all) n = Math.max(n, x.val);
        int[][] out = new int[n][];
        for (int i = 0; i < n; i++) out[i] = new int[0];
        for (Node x : all) out[x.val - 1] = x.neighbors.stream().mapToInt(m -> m.val).toArray();
        return out;
    }

    public Node cloneGraph(Node node) {
        if (node == null) return null;
        Map<Node, Node> copy = new IdentityHashMap<>();
        Deque<Node> q = new ArrayDeque<>();
        copy.put(node, new Node(node.val)); q.add(node);
        while (!q.isEmpty()) {
            Node u = q.poll();
            for (Node v : u.neighbors) {
                if (!copy.containsKey(v)) { copy.put(v, new Node(v.val)); q.add(v); }
                copy.get(u).neighbors.add(copy.get(v));
            }
        }
        return copy.get(node);
    }
}
