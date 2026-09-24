class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isSameTree", T.TREE_NODE, T.TREE_NODE).returns(T.BOOL)
            .example("[1,2,3], [1,2,3]", "true").example("[1,2], [1,null,2]", "false")
            .example("[1,2,1], [1,1,2]", "false").example("[], []", "true").example("[1], []", "false")
            .gen(g -> {
                TreeNode a = g.tree(0, 20, 0, 3);
                TreeNode b = g.chance(0.5) ? copy(a) : g.tree(0, 20, 0, 3);
                if (b != null && g.chance(0.3)) b.val++;
                return new Object[]{ a, b };
            })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    static TreeNode copy(TreeNode n) { if (n == null) return null; TreeNode c = new TreeNode(n.val); c.left = copy(n.left); c.right = copy(n.right); return c; }
    public boolean isSameTree(TreeNode p, TreeNode q) {
        if (p == null || q == null) return p == q;
        return p.val == q.val && isSameTree(p.left, q.left) && isSameTree(p.right, q.right);
    }
}
