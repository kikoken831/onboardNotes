import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rightSideView", T.TREE_NODE).returns(T.LIST_INT)
            .example("[1,2,3,null,5,null,4]", "[1,3,4]").example("[1,null,3]", "[1,3]").example("[]", "[]")
            .example("[1,2]", "[1,2]")                          // a left node can be visible
            .example("[1,2,3,4]", "[1,3,4]")
            .gen(g -> new Object[]{ g.tree(0, 50, -20, 20) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(20000);
    }
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        if (root != null) q.add(root);
        while (!q.isEmpty()) {
            TreeNode last = null;
            for (int n = q.size(); n > 0; n--) { last = q.poll(); if (last.left != null) q.add(last.left); if (last.right != null) q.add(last.right); }
            out.add(last.val);
        }
        return out;
    }
}
