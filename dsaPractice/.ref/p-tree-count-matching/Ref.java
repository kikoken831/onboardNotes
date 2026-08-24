class Ref implements Problem {

    public Spec spec() {
        return Spec.method("rangeSum", T.TREE_NODE, T.INT, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[10,5,15,3,7,null,18], 7, 15",          "32")
            .example("[10,5,15,3,7,13,18,1,null,6], 6, 10",   "23")
            .example("[], 1, 2",                              "0")
            .example("[5], 5, 5",                             "5")
            .example("[5], 6, 9",                             "0")
            .gen(g -> {
                int a = g.value(-30, 30), b = g.value(-30, 30);
                return new Object[]{ g.tree(0, 50, -30, 30), Math.min(a, b), Math.max(a, b) };
            })
            .patterns("Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public int rangeSum(TreeNode root, int low, int high) {
        if (root == null) return 0;
        int self = (root.val >= low && root.val <= high) ? root.val : 0;
        return self + rangeSum(root.left, low, high) + rangeSum(root.right, low, high);
    }
}
