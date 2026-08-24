class Ref implements Problem {

    public Spec spec() {
        return Spec.method("depthWeightedSum", T.TREE_NODE).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[1,2,3]",      "11")
            .example("[]",           "0")
            .example("[5]",          "5")
            .example("[1,2,null,3]", "14")
            .example("[1,null,2]",   "5")
            .gen(g -> new Object[]{ g.tree(0, 50, -30, 30) })
            .patterns("Tree", "Depth-First Search", "Recursion")
            .limitMs(2000)
            .maxN(20000);
    }

    public int depthWeightedSum(TreeNode root) {
        return go(root, 1);
    }

    private int go(TreeNode n, int depth) {
        if (n == null) return 0;
        return depth * n.val + go(n.left, depth + 1) + go(n.right, depth + 1);
    }
}
