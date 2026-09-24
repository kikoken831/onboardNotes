import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("pathSum", T.TREE_NODE, T.INT).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SEQS)
            .example("[5,4,8,11,null,13,4,7,2,null,null,5,1], 22", "[[5,4,11,2],[5,8,4,5]]")
            .example("[1,2,3], 5", "[]").example("[1,2], 0", "[]").example("[1,2,2], 3", "[[1,2],[1,2]]")
            .gen(g -> { TreeNode t = g.tree(0, 30, -3, 3); return new Object[]{ t, g.nextInt(-6, 6) }; })
            .patterns("Backtracking", "Tree", "Depth-First Search").limitMs(2000).maxN(20000);
    }
    public List<List<Integer>> pathSum(TreeNode root, int target) {
        List<List<Integer>> out = new ArrayList<>();
        go(root, target, new ArrayList<>(), out);
        return out;
    }
    private void go(TreeNode n, int rem, List<Integer> path, List<List<Integer>> out) {
        if (n == null) return;
        path.add(n.val);
        if (n.left == null && n.right == null && rem == n.val) out.add(new ArrayList<>(path));
        go(n.left, rem - n.val, path, out);
        go(n.right, rem - n.val, path, out);
        path.remove(path.size() - 1);
    }
}
