import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("rootToLeafPaths", T.TREE_NODE).returns(T.LIST_LIST_INT)
            // Left-to-right order is required, so this is EXACT, not a set comparison.
            .compare(Cmp.EXACT)
            .example("[1,2,3]",           "[[1,2],[1,3]]")
            .example("[]",                "[]")
            .example("[1]",               "[[1]]")
            .example("[1,2,null,3]",      "[[1,2,3]]")
            .example("[1,null,2,null,3]", "[[1,2,3]]")
            .gen(g -> new Object[]{ g.tree(0, 40, -20, 20) })
            .patterns("Tree", "Backtracking", "Depth-First Search")
            .limitMs(3000)
            .maxN(5000);
    }

    public List<List<Integer>> rootToLeafPaths(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        go(root, new ArrayList<>(), out);
        return out;
    }

    private void go(TreeNode n, List<Integer> path, List<List<Integer>> out) {
        if (n == null) return;
        path.add(n.val);
        if (n.left == null && n.right == null) {
            out.add(new ArrayList<>(path));
        } else {
            go(n.left, path, out);
            go(n.right, path, out);
        }
        path.remove(path.size() - 1);
    }
}
