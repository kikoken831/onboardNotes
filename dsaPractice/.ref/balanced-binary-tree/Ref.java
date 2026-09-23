class Ref implements Problem {

    public Spec spec() {
        return Spec.method("isBalanced", T.TREE_NODE).returns(T.BOOL)
            .example("[3,9,20,null,null,15,7]",        "true")
            .example("[1,2,2,3,3,null,null,4,4]",      "false")
            .example("[]",                             "true")
            .example("[1,2,2,3,null,null,3,4,null,null,4]", "false")   // root looks balanced
            .example("[1,null,2]",                     "true")
            .example("[1,null,2,null,3]",              "false")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.bst(0, 60, -200, 200) : g.tree(0, 30, -50, 50) })
            .patterns("Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public boolean isBalanced(TreeNode root) {
        return height(root) >= 0;
    }

    /** Height, or -1 if any subtree is unbalanced. */
    private int height(TreeNode n) {
        if (n == null) return 0;
        int l = height(n.left);
        if (l < 0) return -1;
        int r = height(n.right);
        if (r < 0 || Math.abs(l - r) > 1) return -1;
        return 1 + Math.max(l, r);
    }
}
