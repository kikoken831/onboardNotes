import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isSubtree", T.TREE_NODE, T.TREE_NODE).returns(T.BOOL)
            .example("[3,4,5,1,2], [4,1,2]", "true").example("[3,4,5,1,2,null,null,null,null,0], [4,1,2]", "false")
            .example("[1,1], [1]", "true").example("[12], [2]", "false")              // string-matching trap
            .example("[1,2,3], [1,2]", "false")                                      // must be a whole subtree
            .gen(g -> {
                TreeNode t = g.tree(1, 30, 0, 2);
                TreeNode s;
                if (g.chance(0.5)) {
                    List<TreeNode> a = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); q.add(t);
                    while (!q.isEmpty()) { TreeNode n = q.poll(); a.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); }
                    s = copy(a.get(g.nextInt(0, a.size() - 1)));
                    if (g.chance(0.3)) s.val++;
                } else s = g.tree(1, 5, 0, 2);
                return new Object[]{ t, s };
            })
            .patterns("Tree", "Depth-First Search", "String Matching", "Hash Function").limitMs(2000).maxN(20000);
    }
    static TreeNode copy(TreeNode n) { if (n == null) return null; TreeNode c = new TreeNode(n.val); c.left = copy(n.left); c.right = copy(n.right); return c; }
    public boolean isSubtree(TreeNode root, TreeNode sub) {
        if (root == null) return false;
        return same(root, sub) || isSubtree(root.left, sub) || isSubtree(root.right, sub);
    }
    private boolean same(TreeNode a, TreeNode b) {
        if (a == null || b == null) return a == b;
        return a.val == b.val && same(a.left, b.left) && same(a.right, b.right);
    }
}
