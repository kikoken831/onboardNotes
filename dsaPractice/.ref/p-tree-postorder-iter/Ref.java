import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("postorderIterative", T.TREE_NODE).returns(T.LIST_INT)
            .compare(Cmp.EXACT)
            .example("[1,null,2,3]", "[3,2,1]")
            .example("[]",           "[]")
            .example("[1]",          "[1]")
            .example("[1,2,3]",      "[2,3,1]")
            .example("[3,1,2]",      "[1,2,3]")
            .example("[1,2,null,3]", "[3,2,1]")
            .gen(g -> new Object[]{ g.tree(0, 50, -30, 30) })
            .patterns("Tree", "Stack", "Depth-First Search")
            .limitMs(2000)
            .maxN(50000);
    }

    // The lastVisited form rather than the reversal trick, so that the reference
    // exercises the harder path the drill is really about.
    public List<Integer> postorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root, lastVisited = null;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            TreeNode peek = st.peek();
            if (peek.right != null && lastVisited != peek.right) {
                cur = peek.right;
            } else {
                out.add(peek.val);
                lastVisited = st.pop();
            }
        }
        return out;
    }
}
