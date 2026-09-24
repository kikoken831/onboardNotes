import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("zigzagLevelOrder", T.TREE_NODE).returns(T.LIST_LIST_INT)
            .example("[3,9,20,null,null,15,7]", "[[3],[20,9],[15,7]]").example("[1]", "[[1]]").example("[]", "[]")
            .example("[1,2,3,4,null,null,5]", "[[1],[3,2],[4,5]]")
            .gen(g -> new Object[]{ g.tree(0, 50, -20, 20) })
            .patterns("Tree", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        if (root != null) q.add(root);
        boolean rev = false;
        while (!q.isEmpty()) {
            LinkedList<Integer> lvl = new LinkedList<>();
            for (int n = q.size(); n > 0; n--) {
                TreeNode x = q.poll();
                if (rev) lvl.addFirst(x.val); else lvl.addLast(x.val);
                if (x.left != null) q.add(x.left);
                if (x.right != null) q.add(x.right);
            }
            out.add(lvl);
            rev = !rev;
        }
        return out;
    }
}
