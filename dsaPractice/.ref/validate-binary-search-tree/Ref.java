class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isValidBST", T.TREE_NODE).returns(T.BOOL)
            .example("[2,1,3]", "true").example("[5,1,4,null,null,3,6]", "false")
            .example("[5,4,6,null,null,3,7]", "false")          // 3 is right of 5's subtree root but < 5
            .example("[2,2,2]", "false")                       // strictly less / greater
            .example("[2147483647]", "true").example("[-2147483648,null,2147483647]", "true")
            .gen(g -> {
                TreeNode t = g.bst(1, 40, -50, 50);
                if (g.chance(0.5)) {                              // break it somewhere
                    java.util.List<TreeNode> all = new java.util.ArrayList<>();
                    java.util.Deque<TreeNode> q = new java.util.ArrayDeque<>(); q.add(t);
                    while (!q.isEmpty()) { TreeNode n = q.poll(); all.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); }
                    TreeNode n = all.get(g.nextInt(0, all.size() - 1));
                    n.val += g.nextInt(-30, 30);
                }
                return new Object[]{ t };
            })
            .patterns("Tree", "Depth-First Search", "Binary Search Tree").limitMs(2000).maxN(20000);
    }
    public boolean isValidBST(TreeNode root) { return ok(root, Long.MIN_VALUE, Long.MAX_VALUE); }
    private boolean ok(TreeNode n, long lo, long hi) {
        if (n == null) return true;
        return n.val > lo && n.val < hi && ok(n.left, lo, n.val) && ok(n.right, n.val, hi);
    }
}
