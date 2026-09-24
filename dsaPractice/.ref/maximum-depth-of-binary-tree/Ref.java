class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxDepth", T.TREE_NODE).returns(T.INT)
            .example("[3,9,20,null,null,15,7]", "3").example("[1,null,2]", "2").example("[]", "0")
            .gen(g -> new Object[]{ g.tree(0, 60, -20, 20) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public int maxDepth(TreeNode root) { return root == null ? 0 : 1 + Math.max(maxDepth(root.left), maxDepth(root.right)); }
}
