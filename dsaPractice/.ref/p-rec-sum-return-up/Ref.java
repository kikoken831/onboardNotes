class Ref implements Problem {

    public Spec spec() {
        return Spec.method("sumTree", T.TREE_NODE).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[1,2,3]",           "6")
            .example("[]",                "0")
            .example("[5]",               "5")
            .example("[1,null,2,null,3]", "6")
            .example("[-1,-2,-3]",        "-6")
            .gen(g -> new Object[]{ g.tree(0, 60, -50, 50) })
            .patterns("Tree", "Depth-First Search", "Recursion")
            .limitMs(2000)
            .maxN(20000);
    }

    public int sumTree(TreeNode root) {
        if (root == null) return 0;
        return root.val + sumTree(root.left) + sumTree(root.right);
    }
}
