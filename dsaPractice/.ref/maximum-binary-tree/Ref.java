import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("constructMaximumBinaryTree", T.INT_ARRAY).returns(T.TREE_NODE)
            .example("[3,2,1,6,0,5]", "[6,3,5,null,2,0,null,null,1]").example("[3,2,1]", "[3,null,2,null,1]")
            .example("[1]", "[1]").example("[1,2,3]", "[3,2,null,1]")
            .gen(g -> new Object[]{ g.distinctInts(1, 40, 0, 1000) })
            .patterns("Array", "Divide and Conquer", "Stack", "Tree", "Monotonic Stack").limitMs(2000).maxN(1000);
    }
    public TreeNode constructMaximumBinaryTree(int[] nums) {
        Set<Integer> s = new HashSet<>(); for (int x : nums) Pre.check(s.add(x), "distinct values");
        Deque<TreeNode> st = new ArrayDeque<>();              // values decreasing from bottom to top
        for (int x : nums) {
            TreeNode n = new TreeNode(x), last = null;
            while (!st.isEmpty() && st.peek().val < x) last = st.pop();
            n.left = last;
            if (!st.isEmpty()) st.peek().right = n;
            st.push(n);
        }
        return st.peekLast();
    }
}
