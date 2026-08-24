class Ref implements Problem {

    public Spec spec() {
        return Spec.method("countGoodNodes", T.TREE_NODE).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[3,1,4,3,null,1,5]", "4")
            .example("[3,3,null,4,2]",     "3")
            .example("[1]",                "1")
            .example("[2,null,1]",         "1")
            .example("[]",                 "0")
            .gen(g -> new Object[]{ g.tree(0, 50, -6, 6) })   // narrow range -> ties
            .patterns("Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public int countGoodNodes(TreeNode root) {
        return go(root, Integer.MIN_VALUE);
    }

    private int go(TreeNode n, int maxSoFar) {
        if (n == null) return 0;
        int self = n.val >= maxSoFar ? 1 : 0;
        int next = Math.max(maxSoFar, n.val);
        return self + go(n.left, next) + go(n.right, next);
    }
}
