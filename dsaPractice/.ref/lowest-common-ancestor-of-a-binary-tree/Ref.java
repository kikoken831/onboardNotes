class Ref implements Problem {
    public Spec spec() {
        return Spec.method("lowestCommonAncestor", T.TREE_NODE, T.INT, T.INT).returns(T.TREE_NODE)
            .prepare(a -> { Pre.check(!a[1].equals(a[2]), "p != q"); TreeNode r = (TreeNode) a[0]; a[1] = find(r, (Integer) a[1]); a[2] = find(r, (Integer) a[2]); })
            .judgeAs(T.INT, Ref::valueOf)
            .example("[3,5,1,6,2,0,8,null,null,7,4], 5, 1", "3").example("[3,5,1,6,2,0,8,null,null,7,4], 5, 4", "5")
            .example("[1,2], 1, 2", "1").example("[3,5,1,6,2,0,8,null,null,7,4], 7, 8", "3")
            .gen(g -> {
                TreeNode t = distinct(g.tree(2, 40, 0, 0), g);
                java.util.List<TreeNode> all = nodes(t);
                int i = g.nextInt(0, all.size() - 1), j = g.nextInt(0, all.size() - 1);
                while (j == i) j = g.nextInt(0, all.size() - 1);
                return new Object[]{ t, all.get(i).val, all.get(j).val };
            })
            .patterns("Tree", "Depth-First Search", "Binary Tree").limitMs(2000).maxN(20000);
    }
    /** Values in the tree, BFS order. */
    static java.util.List<TreeNode> nodes(TreeNode r) {
        java.util.List<TreeNode> all = new java.util.ArrayList<>();
        java.util.Deque<TreeNode> q = new java.util.ArrayDeque<>();
        if (r != null) q.add(r);
        while (!q.isEmpty()) { TreeNode n = q.poll(); all.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); }
        return all;
    }

    /** Relabel with distinct values: LCA and distance-K identify nodes by value. */
    static TreeNode distinct(TreeNode t, Gen g) {
        java.util.List<TreeNode> all = nodes(t);
        int[] v = g.distinctInts(all.size(), all.size(), 0, Math.max(1000, 4 * all.size()));
        for (int i = 0; i < all.size(); i++) all.get(i).val = v[i];
        return t;
    }

    /** The node with value v inside THIS copy of the tree -- the object you must be handed. */
    static TreeNode find(TreeNode r, int v) {
        TreeNode hit = null;
        for (TreeNode n : nodes(r)) if (n.val == v) { Pre.check(hit == null, "distinct values"); hit = n; }
        Pre.check(hit != null, v + " is a node of the tree");
        return hit;
    }

    static Object valueOf(Object[] a, Object ret) {
        if (ret == null) return "returned null";
        for (TreeNode n : nodes((TreeNode) a[0])) if (n == ret) return n.val;
        return "returned a node that is not in the input tree (a copy?)";
    }

    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) return root;
        TreeNode l = lowestCommonAncestor(root.left, p, q), r = lowestCommonAncestor(root.right, p, q);
        return l != null && r != null ? root : l != null ? l : r;
    }
}
