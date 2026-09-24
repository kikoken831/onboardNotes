class Ref implements Problem {
    public Spec spec() {
        return Spec.method("lowestCommonAncestor", T.TREE_NODE, T.INT, T.INT).returns(T.TREE_NODE)
            // p and q arrive as values; each call gets the matching node objects of its own copy.
            .prepare(a -> { TreeNode r = (TreeNode) a[0]; a[1] = find(r, (Integer) a[1]); a[2] = find(r, (Integer) a[2]); })
            .judgeAs(T.INT, Ref::valueOf)
            .example("[6,2,8,0,4,7,9,null,null,3,5], 2, 8", "6").example("[6,2,8,0,4,7,9,null,null,3,5], 2, 4", "2")
            .example("[2,1], 2, 1", "2").example("[6,2,8,0,4,7,9,null,null,3,5], 3, 5", "4")
            .gen(g -> {
                TreeNode t = g.bst(2, 40, 0, 200);
                java.util.List<TreeNode> all = nodes(t);
                int i = g.nextInt(0, all.size() - 1), j = g.nextInt(0, all.size() - 1);
                while (j == i) j = g.nextInt(0, all.size() - 1);
                return new Object[]{ t, all.get(i).val, all.get(j).val };
            })
            .patterns("Tree", "Depth-First Search", "Binary Search Tree").limitMs(2000).maxN(20000);
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
        int[] v = g.distinctInts(all.size(), all.size(), 0, 1000);
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
        TreeNode n = root;
        while (true) {
            if (p.val < n.val && q.val < n.val) n = n.left;
            else if (p.val > n.val && q.val > n.val) n = n.right;
            else return n;
        }
    }
}
