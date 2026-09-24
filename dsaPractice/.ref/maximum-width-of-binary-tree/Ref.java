import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("widthOfBinaryTree", T.TREE_NODE).returns(T.INT)
            .example("[1,3,2,5,3,null,9]", "4").example("[1,3,2,5,null,null,9,6,null,7]", "7")
            .example("[1,3,2,5]", "2").example("[1]", "1")
            .gen(g -> new Object[]{ g.tree(1, 40, 0, 9) })
            .patterns("Tree", "Depth-First Search", "Breadth-First Search").limitMs(2000).maxN(3000);
    }
    public int widthOfBinaryTree(TreeNode root) {
        long best = 0;
        Deque<Object[]> q = new ArrayDeque<>();
        q.add(new Object[]{ root, 0L });
        while (!q.isEmpty()) {
            int n = q.size();
            long first = (Long) q.peek()[1], last = first;
            for (int i = 0; i < n; i++) {
                Object[] e = q.poll();
                TreeNode x = (TreeNode) e[0];
                long pos = (Long) e[1] - first;                    // re-base each level: no overflow
                last = pos;
                if (x.left != null) q.add(new Object[]{ x.left, 2 * pos });
                if (x.right != null) q.add(new Object[]{ x.right, 2 * pos + 1 });
            }
            best = Math.max(best, last + 1);
        }
        Pre.check(best <= Integer.MAX_VALUE, "the answer fits in 32 bits");
        return (int) best;
    }
}
