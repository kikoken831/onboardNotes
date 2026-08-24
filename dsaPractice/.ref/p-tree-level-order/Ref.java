import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("levelOrder", T.TREE_NODE).returns(T.LIST_LIST_INT)
            .compare(Cmp.EXACT)
            .example("[3,9,20,null,null,15,7]", "[[3],[9,20],[15,7]]")
            .example("[]",                      "[]")
            .example("[1]",                     "[[1]]")
            .example("[1,2,3,4]",               "[[1],[2,3],[4]]")
            .example("[1,null,2,null,3]",       "[[1],[2],[3]]")
            .gen(g -> new Object[]{ g.tree(0, 50, -30, 30) })
            .patterns("Tree", "Breadth-First Search")
            .limitMs(2000)
            .maxN(50000);
    }

    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        Queue<TreeNode> q = new ArrayDeque<>();
        if (root != null) q.add(root);
        while (!q.isEmpty()) {
            int size = q.size();
            List<Integer> level = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                TreeNode n = q.poll();
                level.add(n.val);
                if (n.left != null) q.add(n.left);
                if (n.right != null) q.add(n.right);
            }
            out.add(level);
        }
        return out;
    }
}
