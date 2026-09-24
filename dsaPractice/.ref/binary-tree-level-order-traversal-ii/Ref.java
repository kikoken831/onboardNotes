import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("levelOrderBottom", T.TREE_NODE).returns(T.LIST_LIST_INT)
            .example("[3,9,20,null,null,15,7]", "[[15,7],[9,20],[3]]").example("[1]", "[[1]]").example("[]", "[]")
            .gen(g -> new Object[]{ g.tree(0, 50, -20, 20) })
            .patterns("Tree", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public List<List<Integer>> levelOrderBottom(TreeNode root) {
        LinkedList<List<Integer>> out = new LinkedList<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        if (root != null) q.add(root);
        while (!q.isEmpty()) {
            List<Integer> lvl = new ArrayList<>();
            for (int n = q.size(); n > 0; n--) { TreeNode x = q.poll(); lvl.add(x.val); if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); }
            out.addFirst(lvl);
        }
        return out;
    }
}
