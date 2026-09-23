class Ref implements Problem {

    public Spec spec() {
        return Spec.method("isSymmetric", T.TREE_NODE).returns(T.BOOL)
            .example("[1,2,2,3,4,4,3]",        "true")
            .example("[1,2,2,null,3,null,3]",  "false")
            .example("[1]",                    "true")
            .example("[1,2,3]",                "false")
            .example("[1,2,2,2,null,2]",       "false")   // same values, wrong shape
            .gen(g -> new Object[]{ mirrored(g) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    /** A symmetric tree, perturbed half the time. Random trees are ~never symmetric. */
    private static TreeNode mirrored(Gen g) {
        TreeNode root = new TreeNode(g.nextInt(-3, 3));
        TreeNode half = g.tree(0, 20, -3, 3);
        root.left = half;
        root.right = mirror(copy(half));
        if (g.chance(0.5)) perturb(root, g);
        return root;
    }

    private static TreeNode copy(TreeNode n) {
        if (n == null) return null;
        TreeNode c = new TreeNode(n.val);
        c.left = copy(n.left);
        c.right = copy(n.right);
        return c;
    }

    private static TreeNode mirror(TreeNode n) {
        if (n == null) return null;
        TreeNode t = n.left; n.left = mirror(n.right); n.right = mirror(t);
        return n;
    }

    private static void perturb(TreeNode root, Gen g) {
        java.util.List<TreeNode> all = new java.util.ArrayList<>();
        java.util.Deque<TreeNode> q = new java.util.ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode n = q.poll();
            all.add(n);
            if (n.left != null) q.add(n.left);
            if (n.right != null) q.add(n.right);
        }
        TreeNode n = all.get(g.nextInt(0, all.size() - 1));
        switch (g.nextInt(0, 2)) {
            case 0:  n.val += 1; break;                                     // value asymmetry
            case 1:  if (n.left == null) n.left = new TreeNode(n.val); else n.left = null; break;
            default: TreeNode t = n.left; n.left = n.right; n.right = t;    // shape asymmetry
        }
    }

    public boolean isSymmetric(TreeNode root) {
        Pre.check(root != null, "at least one node");
        return mirrorOf(root.left, root.right);
    }

    private boolean mirrorOf(TreeNode a, TreeNode b) {
        if (a == null || b == null) return a == b;
        return a.val == b.val && mirrorOf(a.left, b.right) && mirrorOf(a.right, b.left);
    }
}
