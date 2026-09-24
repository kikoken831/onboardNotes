import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.driven("Codec", T.TREE_NODE).returns(T.TREE_NODE)
            // LeetCode: Codec ser = new Codec(), deser = new Codec(); deser.deserialize(ser.serialize(root))
            .driver((fresh, a) -> {
                Object ser = fresh.get(), deser = fresh.get();
                Object s = Reflect.call(ser, "serialize", a[0]);
                if (!(s instanceof String)) return "serialize returned " + s + ", not a String";
                return Reflect.call(deser, "deserialize", s);
            })
            // The round trip must build NEW nodes -- returning the input via a static field is a cheat.
            .judgeAs(T.TREE_NODE, (a, ret) -> {
                if (!(ret instanceof TreeNode) && ret != null) return ret;
                Set<TreeNode> in = Collections.newSetFromMap(new IdentityHashMap<>());
                collect((TreeNode) a[0], in);
                Set<TreeNode> out = Collections.newSetFromMap(new IdentityHashMap<>());
                collect((TreeNode) ret, out);
                for (TreeNode n : out) if (in.contains(n)) return "deserialize returned nodes from the original tree, not a rebuilt one";
                return ret;
            })
            .example("[1,2,3,null,null,4,5]", "[1,2,3,null,null,4,5]").example("[]", "[]")
            .example("[-1000,null,1000]", "[-1000,null,1000]").example("[1,1,1]", "[1,1,1]")
            .gen(g -> new Object[]{ g.tree(0, 50, -1000, 1000) })
            .patterns("String", "Tree", "Depth-First Search", "Breadth-First Search", "Design").limitMs(2000).maxN(20000);
    }
    static void collect(TreeNode n, Set<TreeNode> s) { if (n == null || !s.add(n)) return; collect(n.left, s); collect(n.right, s); }
}

class Codec {
    public String serialize(TreeNode root) {
        StringBuilder b = new StringBuilder();
        ser(root, b);
        return b.toString();
    }
    private void ser(TreeNode n, StringBuilder b) {
        if (n == null) { b.append("#,"); return; }
        b.append(n.val).append(',');
        ser(n.left, b);
        ser(n.right, b);
    }
    public TreeNode deserialize(String data) {
        Deque<String> t = new ArrayDeque<>(Arrays.asList(data.split(",")));
        return de(t);
    }
    private TreeNode de(Deque<String> t) {
        String s = t.poll();
        if (s == null || s.equals("#")) return null;
        TreeNode n = new TreeNode(Integer.parseInt(s));
        n.left = de(t);
        n.right = de(t);
        return n;
    }
}
