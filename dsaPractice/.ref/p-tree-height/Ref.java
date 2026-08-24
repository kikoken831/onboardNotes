class Ref implements Problem {

    public Spec spec() {
        return Spec.method("treeHeight", T.TREE_NODE).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[3,9,20,null,null,15,7]", "3")
            .example("[]",                      "0")
            .example("[1]",                     "1")
            .example("[1,2,null,3,null,4]",     "4")
            .example("[1,null,2]",              "2")
            .gen(g -> new Object[]{ g.tree(0, 60, -30, 30) })
            .patterns("Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public int treeHeight(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(treeHeight(root.left), treeHeight(root.right));
    }
}
