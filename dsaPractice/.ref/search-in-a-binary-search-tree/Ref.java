class Ref implements Problem {

    public Spec spec() {
        return Spec.method("searchBST", T.TREE_NODE, T.INT).returns(T.TREE_NODE)
            .example("[4,2,7,1,3], 2", "[2,1,3]")
            .example("[4,2,7,1,3], 5", "[]")
            .example("[4,2,7,1,3], 7", "[7]")
            .example("[1], 1",         "[1]")
            .gen(g -> {
                TreeNode t = g.bst(1, 40, 1, 100);
                int val = g.chance(0.6) ? pick(t, g) : g.nextInt(1, 100);
                return new Object[]{ t, val };
            })
            .patterns("Tree", "Binary Search Tree")
            .limitMs(2000)
            .maxN(20000);
    }

    private static int pick(TreeNode t, Gen g) {
        java.util.List<Integer> v = new java.util.ArrayList<>();
        collect(t, v);
        return v.get(g.nextInt(0, v.size() - 1));
    }

    private static void collect(TreeNode n, java.util.List<Integer> v) {
        if (n == null) return;
        collect(n.left, v); v.add(n.val); collect(n.right, v);
    }

    static boolean isBst(TreeNode n, long lo, long hi) {
        if (n == null) return true;
        return n.val > lo && n.val < hi && isBst(n.left, lo, n.val) && isBst(n.right, n.val, hi);
    }

    public TreeNode searchBST(TreeNode root, int val) {
        Pre.check(root != null && isBst(root, Long.MIN_VALUE, Long.MAX_VALUE), "a valid BST");
        TreeNode n = root;
        while (n != null && n.val != val) n = val < n.val ? n.left : n.right;
        return n;
    }
}
