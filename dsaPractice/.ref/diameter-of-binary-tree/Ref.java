class Ref implements Problem {

    public Spec spec() {
        return Spec.method("diameterOfBinaryTree", T.TREE_NODE).returns(T.INT)
            .example("[1,2,3,4,5]",               "3")
            .example("[1,2]",                     "1")
            .example("[1]",                       "0")
            .example("[1,2,null,3,4,5,null,null,6]", "4")   // longest path avoids the root
            .gen(g -> new Object[]{ g.tree(1, 60, -50, 50) })
            .patterns("Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    private int best;

    public int diameterOfBinaryTree(TreeNode root) {
        Pre.check(root != null, "at least one node");
        best = 0;
        height(root);
        return best;
    }

    private int height(TreeNode n) {
        if (n == null) return 0;
        int l = height(n.left), r = height(n.right);
        best = Math.max(best, l + r);          // edges through n
        return 1 + Math.max(l, r);
    }
}
