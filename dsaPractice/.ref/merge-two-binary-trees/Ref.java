class Ref implements Problem {
    public Spec spec() {
        return Spec.method("mergeTrees", T.TREE_NODE, T.TREE_NODE).returns(T.TREE_NODE)
            .example("[1,3,2,5], [2,1,3,null,4,null,7]", "[3,4,5,5,4,null,7]").example("[1], [1,2]", "[2,2]")
            .example("[], []", "[]").example("[], [1]", "[1]")
            .gen(g -> new Object[]{ g.tree(0, 25, -9, 9), g.tree(0, 25, -9, 9) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public TreeNode mergeTrees(TreeNode a, TreeNode b) {
        if (a == null) return b;
        if (b == null) return a;
        TreeNode n = new TreeNode(a.val + b.val);
        n.left = mergeTrees(a.left, b.left);
        n.right = mergeTrees(a.right, b.right);
        return n;
    }
}
