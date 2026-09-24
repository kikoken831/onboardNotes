class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxPathSum", T.TREE_NODE).returns(T.INT)
            .example("[1,2,3]", "6").example("[-10,9,20,null,null,15,7]", "42").example("[-3]", "-3")
            .example("[2,-1]", "2").example("[-1,-2,-3]", "-1").example("[1,-2,3]", "4")
            .gen(g -> new Object[]{ g.tree(1, 40, -10, 10) })
            .patterns("Dynamic Programming", "Tree", "Depth-First Search").limitMs(2000).maxN(20000);
    }
    private int best;
    public int maxPathSum(TreeNode root) {
        Pre.check(root != null, "at least one node");
        best = Integer.MIN_VALUE;
        gain(root);
        return best;
    }
    private int gain(TreeNode n) {
        if (n == null) return 0;
        int l = Math.max(0, gain(n.left)), r = Math.max(0, gain(n.right));
        best = Math.max(best, n.val + l + r);
        return n.val + Math.max(l, r);
    }
}
