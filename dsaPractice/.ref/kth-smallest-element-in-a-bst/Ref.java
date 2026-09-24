import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("kthSmallest", T.TREE_NODE, T.INT).returns(T.INT)
            .example("[3,1,4,null,2], 1", "1").example("[5,3,6,2,4,null,null,1], 3", "3").example("[1], 1", "1")
            .example("[2,1,3], 3", "3")
            .gen(g -> {
                TreeNode t = g.bst(1, 40, -50, 50);
                int n = 0; Deque<TreeNode> q = new ArrayDeque<>(); q.add(t);
                while (!q.isEmpty()) { TreeNode x = q.poll(); n++; if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); }
                return new Object[]{ t, g.nextInt(1, n) };
            })
            .patterns("Tree", "Depth-First Search", "Binary Search Tree").limitMs(2000).maxN(20000);
    }
    public int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode c = root;
        int prev = 0; boolean first = true;
        while (c != null || !st.isEmpty()) {
            while (c != null) { st.push(c); c = c.left; }
            c = st.pop();
            Pre.check(first || c.val > prev, "a valid BST");
            first = false; prev = c.val;
            if (--k == 0) return c.val;
            c = c.right;
        }
        throw new IllegalArgumentException("input violates constraint: 1 <= k <= n");
    }
}
