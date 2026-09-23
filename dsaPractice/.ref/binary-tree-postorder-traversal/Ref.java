import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("postorderTraversal", T.TREE_NODE).returns(T.LIST_INT)
            .example("[1,null,2,3]", "[3,2,1]")
            .example("[1,2,3,4,5,null,8,null,null,6,7,9]",         "[4,6,7,5,2,9,8,3,1]")
            .example("[]",           "[]")
            .example("[1]",          "[1]")
            .example("[1,2,3]",      "[2,3,1]")
            .gen(g -> new Object[]{ g.tree(0, 60, -50, 50) })
            .patterns("Stack", "Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public List<Integer> postorderTraversal(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root, last = null;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            TreeNode top = st.peek();
            if (top.right != null && top.right != last) {
                cur = top.right;
            } else {
                out.add(top.val);
                last = st.pop();
            }
        }
        return out;
    }
}
