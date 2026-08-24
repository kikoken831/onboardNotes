import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("inorderIterative", T.TREE_NODE).returns(T.LIST_INT)
            .compare(Cmp.EXACT)
            .example("[1,null,2,3]", "[1,3,2]")
            .example("[]",           "[]")
            .example("[1]",          "[1]")
            .example("[1,2,3]",      "[2,1,3]")
            .example("[3,1,2]",      "[1,3,2]")
            .example("[1,2,null,3]", "[3,2,1]")
            .gen(g -> new Object[]{ g.tree(0, 50, -30, 30) })
            .patterns("Tree", "Stack", "Depth-First Search")
            .limitMs(2000)
            .maxN(50000);
    }

    public List<Integer> inorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            cur = st.pop();
            out.add(cur.val);
            cur = cur.right;
        }
        return out;
    }
}
