class Ref implements Problem {

    public Spec spec() {
        return Spec.method("isBalanced", T.TREE_NODE).returns(T.BOOL)
            .compare(Cmp.EXACT)
            .example("[3,9,20,null,null,15,7]",   "true")
            .example("[1,2,2,3,3,null,null,4,4]", "false")
            .example("[]",                        "true")
            .example("[1,2,null,3]",              "false")
            .example("[1,2,3]",                   "true")
            .example("[1,2,null,3,null,4]",       "false")
            .gen(g -> new Object[]{
                // Mix random-shaped trees (often unbalanced) with BSTs built by
                // midpoint split (always balanced), so both verdicts occur often.
                g.chance(0.5) ? g.tree(0, 40, -20, 20) : g.bst(0, 40, -50, 50) })
            .patterns("Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(5000);
    }

    public boolean isBalanced(TreeNode root) {
        return height(root) != -1;
    }

    private int height(TreeNode n) {
        if (n == null) return 0;
        int l = height(n.left);
        if (l == -1) return -1;
        int r = height(n.right);
        if (r == -1) return -1;
        if (Math.abs(l - r) > 1) return -1;
        return 1 + Math.max(l, r);
    }
}
