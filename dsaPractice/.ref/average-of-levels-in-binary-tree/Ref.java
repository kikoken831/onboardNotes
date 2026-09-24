import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("averageOfLevels", T.TREE_NODE).returns(T.LIST_DOUBLE)
            .eps(1e-5)
            .example("[3,9,20,null,null,15,7]", "[3.0,14.5,11.0]").example("[3,9,20,15,7]", "[3.0,14.5,11.0]")
            .example("[2147483647,2147483647,2147483647]", "[2147483647.0,2147483647.0]")   // int sum overflows
            .example("[-1,-2]", "[-1.0,-2.0]")
            .gen(g -> new Object[]{ g.chance(0.15) ? g.tree(1, 30, Integer.MAX_VALUE - 5, Integer.MAX_VALUE) : g.tree(1, 40, -20, 20) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public List<Double> averageOfLevels(TreeNode root) {
        List<Double> out = new ArrayList<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            int n = q.size();
            long sum = 0;
            for (int i = 0; i < n; i++) { TreeNode x = q.poll(); sum += x.val; if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); }
            out.add((double) sum / n);
        }
        return out;
    }
}
