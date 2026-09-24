class Ref implements Problem {
    public Spec spec() {
        return Spec.method("minDepth", T.TREE_NODE).returns(T.INT)
            .example("[3,9,20,null,null,15,7]", "2").example("[2,null,3,null,4,null,5,null,6]", "5").example("[]", "0")
            .example("[1,2]", "2")                              // the root is not a leaf
            .gen(g -> new Object[]{ g.tree(0, 50, -20, 20) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public int minDepth(TreeNode root) {
        if (root == null) return 0;
        if (root.left == null) return 1 + minDepth(root.right);
        if (root.right == null) return 1 + minDepth(root.left);
        return 1 + Math.min(minDepth(root.left), minDepth(root.right));
    }
}
