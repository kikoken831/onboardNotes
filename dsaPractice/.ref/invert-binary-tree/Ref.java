class Ref implements Problem {
    public Spec spec() {
        return Spec.method("invertTree", T.TREE_NODE).returns(T.TREE_NODE)
            .example("[4,2,7,1,3,6,9]", "[4,7,2,9,6,3,1]").example("[2,1,3]", "[2,3,1]").example("[]", "[]")
            .example("[1,2]", "[1,null,2]")
            .gen(g -> new Object[]{ g.tree(0, 50, -20, 20) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public TreeNode invertTree(TreeNode root) {
        if (root == null) return null;
        TreeNode l = invertTree(root.left);
        root.left = invertTree(root.right);
        root.right = l;
        return root;
    }
}
