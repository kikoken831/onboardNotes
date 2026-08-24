import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("preorderIterative", T.TREE_NODE).returns(T.LIST_INT)
            .compare(Cmp.EXACT)
            .example("[1,null,2,3]", "[1,2,3]")
            .example("[]",           "[]")
            .example("[1]",          "[1]")
            .example("[1,2,3]",      "[1,2,3]")
            .example("[3,1,2]",      "[3,1,2]")
            .example("[1,2,null,3]", "[1,2,3]")
            .gen(g -> new Object[]{ g.tree(0, 50, -30, 30) })
            .patterns("Tree", "Stack", "Depth-First Search")
            .limitMs(2000)
            .maxN(50000);
    }

    public List<Integer> preorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        if (root != null) st.push(root);
        while (!st.isEmpty()) {
            TreeNode n = st.pop();
            out.add(n.val);
            if (n.right != null) st.push(n.right);
            if (n.left != null) st.push(n.left);
        }
        return out;
    }
}
