import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("buildTree", T.INT_ARRAY, T.INT_ARRAY).returns(T.TREE_NODE)
            .example("[3,9,20,15,7], [9,3,15,20,7]", "[3,9,20,null,null,15,7]").example("[-1], [-1]", "[-1]")
            .example("[1,2], [2,1]", "[1,2]").example("[1,2], [1,2]", "[1,null,2]")
            .gen(g -> {
                TreeNode t = distinct(g.tree(1, 40, 0, 0), g);
                List<Integer> pre = new ArrayList<>(), in = new ArrayList<>();
                walk(t, pre, in);
                return new Object[]{ pre.stream().mapToInt(i -> i).toArray(), in.stream().mapToInt(i -> i).toArray() };
            })
            .patterns("Array", "Hash Table", "Divide and Conquer", "Tree").limitMs(2000).maxN(3000);
    }
    static TreeNode distinct(TreeNode t, Gen g) {
        List<TreeNode> all = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); q.add(t);
        while (!q.isEmpty()) { TreeNode n = q.poll(); all.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); }
        int[] v = g.distinctInts(all.size(), all.size(), -3000, 3000);
        for (int i = 0; i < all.size(); i++) all.get(i).val = v[i];
        return t;
    }
    static void walk(TreeNode t, List<Integer> pre, List<Integer> in) { preorder(t, pre); inorder(t, in); }
    static void preorder(TreeNode n, List<Integer> pre) { if (n == null) return; pre.add(n.val); preorder(n.left, pre); preorder(n.right, pre); }
    static void inorder(TreeNode n, List<Integer> in) { if (n == null) return; inorder(n.left, in); in.add(n.val); inorder(n.right, in); }

    private int preAt;
    private Map<Integer, Integer> where;

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        Pre.check(preorder.length == inorder.length && preorder.length >= 1, "same non-zero length");
        where = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) Pre.check(where.put(inorder[i], i) == null, "distinct values");
        for (int v : preorder) Pre.check(where.containsKey(v), "same values in both");
        preAt = 0;
        TreeNode t = build(preorder, 0, inorder.length - 1);
        Pre.check(preAt == preorder.length, "consistent traversals");
        List<Integer> in = new ArrayList<>(); inorder(t, in);
        for (int i = 0; i < inorder.length; i++) Pre.check(in.get(i) == inorder[i], "consistent traversals");
        return t;
    }
    private TreeNode build(int[] pre, int lo, int hi) {
        if (lo > hi) return null;
        TreeNode n = new TreeNode(pre[preAt++]);
        int m = where.get(n.val);
        Pre.check(m >= lo && m <= hi, "consistent traversals");
        n.left = build(pre, lo, m - 1);
        n.right = build(pre, m + 1, hi);
        return n;
    }
}
