import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("preorderTraversal", T.TREE_NODE).returns(T.LIST_INT)
            .example("[1,null,2,3]", "[1,2,3]")
            .example("[1,2,3,4,5,null,8,null,null,6,7,9]",         "[1,2,4,5,6,7,3,8,9]")
            .example("[]",           "[]")
            .example("[1]",          "[1]")
            .example("[1,2]",        "[1,2]")
            .gen(g -> new Object[]{ g.tree(0, 60, -50, 50) })
            .patterns("Stack", "Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        if (root != null) st.push(root);
        while (!st.isEmpty()) {
            TreeNode n = st.pop();
            out.add(n.val);
            if (n.right != null) st.push(n.right);   // right first so left pops first
            if (n.left  != null) st.push(n.left);
        }
        return out;
    }
}
