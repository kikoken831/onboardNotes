import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("binaryTreePaths", T.TREE_NODE).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("[1,2,3,null,5]", "[\"1->2->5\",\"1->3\"]").example("[1]", "[\"1\"]")
            .example("[-1,2]", "[\"-1->2\"]")
            .gen(g -> new Object[]{ g.tree(1, 30, -9, 9) })
            .patterns("String", "Backtracking", "Tree", "Depth-First Search").limitMs(2000).maxN(20000);
    }
    public List<String> binaryTreePaths(TreeNode root) {
        List<String> out = new ArrayList<>();
        go(root, "", out);
        return out;
    }
    private void go(TreeNode n, String prefix, List<String> out) {
        if (n == null) return;
        String s = prefix.isEmpty() ? String.valueOf(n.val) : prefix + "->" + n.val;
        if (n.left == null && n.right == null) { out.add(s); return; }
        go(n.left, s, out);
        go(n.right, s, out);
    }
}
