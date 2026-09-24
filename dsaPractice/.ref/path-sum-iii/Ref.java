import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("pathSum", T.TREE_NODE, T.INT).returns(T.INT)
            .example("[10,5,-3,3,2,null,11,3,-2,null,1], 8", "3").example("[5,4,8,11,null,13,4,7,2,null,null,5,1], 22", "3")
            .example("[1000000000,1000000000,null,294967296,null,1000000000,null,1000000000,null,1000000000], 0", "0")   // sums overflow int
            .example("[1,-1,1], 0", "1").example("[], 0", "0")
            .gen(g -> {
                TreeNode t = g.tree(0, 30, -3, 3);
                if (g.chance(0.1)) for (TreeNode n : all(t)) n.val = g.chance(0.5) ? 1_000_000_000 : -1_000_000_000;
                return new Object[]{ t, g.nextInt(-5, 5) };
            })
            .patterns("Tree", "Depth-First Search").limitMs(2000).maxN(20000);
    }
    static List<TreeNode> all(TreeNode r) { List<TreeNode> a = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r); while (!q.isEmpty()) { TreeNode n = q.poll(); a.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); } return a; }
    public int pathSum(TreeNode root, int target) {
        Map<Long, Integer> seen = new HashMap<>();
        seen.put(0L, 1);
        return go(root, 0L, target, seen);
    }
    private int go(TreeNode n, long sum, int target, Map<Long, Integer> seen) {
        if (n == null) return 0;
        sum += n.val;
        int count = seen.getOrDefault(sum - target, 0);
        seen.merge(sum, 1, Integer::sum);
        count += go(n.left, sum, target, seen) + go(n.right, sum, target, seen);
        seen.merge(sum, -1, Integer::sum);                  // backtrack: leave only ancestors
        return count;
    }
}
