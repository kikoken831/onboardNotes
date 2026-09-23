class Ref implements Problem {

    public Spec spec() {
        return Spec.method("rangeSumBST", T.TREE_NODE, T.INT, T.INT).returns(T.INT)
            .example("[10,5,15,3,7,null,18], 7, 15",        "32")
            .example("[10,5,15,3,7,13,18,1,null,6], 6, 10", "23")
            .example("[5], 5, 5",                           "5")
            .example("[5], 6, 9",                           "0")
            .gen(g -> {
                TreeNode t = g.bst(1, 50, 1, 100);
                int a = g.nextInt(1, 100), b = g.nextInt(1, 100);
                return new Object[]{ t, Math.min(a, b), Math.max(a, b) };
            })
            .patterns("Tree", "Depth-First Search", "Binary Search Tree")
            .limitMs(2000)
            .maxN(20000);
    }

    static boolean isBst(TreeNode n, long lo, long hi) {
        if (n == null) return true;
        return n.val > lo && n.val < hi && isBst(n.left, lo, n.val) && isBst(n.right, n.val, hi);
    }

    public int rangeSumBST(TreeNode root, int low, int high) {
        Pre.check(root != null && isBst(root, Long.MIN_VALUE, Long.MAX_VALUE), "a valid BST");
        Pre.check(low <= high, "low <= high");
        return sum(root, low, high);
    }

    private int sum(TreeNode n, int lo, int hi) {
        if (n == null) return 0;
        if (n.val < lo) return sum(n.right, lo, hi);     // whole left side is too small
        if (n.val > hi) return sum(n.left, lo, hi);      // whole right side is too big
        return n.val + sum(n.left, lo, hi) + sum(n.right, lo, hi);
    }
}
