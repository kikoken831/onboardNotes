import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("distanceK", T.TREE_NODE, T.INT, T.INT).returns(T.LIST_INT)
            .compare(Cmp.MULTISET)
            .prepare(a -> a[1] = find((TreeNode) a[0], (Integer) a[1]))     // target is a node of this tree
            .example("[3,5,1,6,2,0,8,null,null,7,4], 5, 2", "[7,4,1]").example("[1], 1, 3", "[]")
            .example("[0,1,null,3,2], 2, 1", "[1]").example("[3,5,1,6,2,0,8,null,null,7,4], 5, 0", "[5]")
            .gen(g -> {
                TreeNode t = g.tree(1, 30, 0, 0);
                List<TreeNode> all = nodes(t);
                int[] v = g.distinctInts(all.size(), all.size(), 0, 500);
                for (int i = 0; i < all.size(); i++) all.get(i).val = v[i];
                return new Object[]{ t, all.get(g.nextInt(0, all.size() - 1)).val, g.nextInt(0, 5) };
            })
            .patterns("Hash Table", "Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    static List<TreeNode> nodes(TreeNode r) { List<TreeNode> a = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r); while (!q.isEmpty()) { TreeNode n = q.poll(); a.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); } return a; }
    static TreeNode find(TreeNode r, int v) {
        TreeNode hit = null;
        for (TreeNode n : nodes(r)) if (n.val == v) { Pre.check(hit == null, "distinct values"); hit = n; }
        Pre.check(hit != null, "target is a node of the tree");
        return hit;
    }
    public List<Integer> distanceK(TreeNode root, TreeNode target, int k) {
        Map<TreeNode, TreeNode> parent = new IdentityHashMap<>();
        for (TreeNode n : nodes(root)) { if (n.left != null) parent.put(n.left, n); if (n.right != null) parent.put(n.right, n); }
        Set<TreeNode> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<TreeNode> q = new ArrayDeque<>();
        q.add(target); seen.add(target);
        for (int d = 0; d < k && !q.isEmpty(); d++) {
            for (int n = q.size(); n > 0; n--) {
                TreeNode x = q.poll();
                for (TreeNode y : new TreeNode[]{ x.left, x.right, parent.get(x) }) if (y != null && seen.add(y)) q.add(y);
            }
        }
        List<Integer> out = new ArrayList<>();
        for (TreeNode x : q) out.add(x.val);
        return out;
    }
}
