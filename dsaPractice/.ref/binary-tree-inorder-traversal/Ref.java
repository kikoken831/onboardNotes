import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("inorderTraversal", T.TREE_NODE).returns(T.LIST_INT)
            .example("[1,null,2,3]", "[1,3,2]")
            .example("[1,2,3,4,5,null,8,null,null,6,7,9]",         "[4,2,6,5,7,1,3,9,8]")
            .example("[]",           "[]")
            .example("[1]",          "[1]")
            .example("[3,1,null,2]", "[2,1,3]")
            .gen(g -> new Object[]{ g.tree(0, 60, -50, 50) })
            .patterns("Stack", "Tree", "Depth-First Search")
            .limitMs(2000)
            .maxN(20000);
    }

    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        walk(root, out);
        return out;
    }

    private void walk(TreeNode n, List<Integer> out) {
        if (n == null) return;
        walk(n.left, out);
        out.add(n.val);
        walk(n.right, out);
    }
}
