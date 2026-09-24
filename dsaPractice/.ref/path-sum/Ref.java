class Ref implements Problem {
    public Spec spec() {
        return Spec.method("hasPathSum", T.TREE_NODE, T.INT).returns(T.BOOL)
            .example("[5,4,8,11,null,13,4,7,2,null,null,null,1], 22", "true").example("[1,2,3], 5", "false")
            .example("[], 0", "false")                          // no path at all
            .example("[1,2], 1", "false")                        // 1 is not a leaf sum
            .example("[-2,null,-3], -5", "true")
            .gen(g -> { TreeNode t = g.tree(0, 30, -5, 5); return new Object[]{ t, g.nextInt(-12, 12) }; })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public boolean hasPathSum(TreeNode root, int target) {
        if (root == null) return false;
        if (root.left == null && root.right == null) return root.val == target;
        return hasPathSum(root.left, target - root.val) || hasPathSum(root.right, target - root.val);
    }
}
