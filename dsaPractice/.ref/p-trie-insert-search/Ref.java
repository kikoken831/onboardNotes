import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("trieQueries", T.STRING_ARRAY, T.STRING_ARRAY).returns(T.LIST_STRING)
            .compare(Cmp.EXACT)
            .example("[\"app\",\"apple\"], [\"app\",\"appl\",\"apple\",\"b\",\"\"]",
                     "[\"word\",\"prefix\",\"word\",\"none\",\"prefix\"]")
            .example("[], [\"a\"]",     "[\"none\"]")
            .example("[\"a\"], [\"\"]", "[\"prefix\"]")
            .example("[\"\"], [\"\"]",  "[\"word\"]")
            .example("[], [\"\"]", "[\"none\"]")
            .gen(g -> new Object[]{
                g.words(0, 20, 0, 5, "abc"),
                g.words(1, 20, 0, 5, "abc") })
            .patterns("Trie", "String", "Design")
            .limitMs(3000)
            .maxN(20000);
    }

    private static final class Node {
        final Node[] next = new Node[26];
        boolean isWord;
    }

    public List<String> trieQueries(String[] words, String[] queries) {
        Node root = new Node();
        for (String w : words) {
            Node cur = root;
            for (int i = 0; i < w.length(); i++) {
                int c = w.charAt(i) - 'a';
                if (cur.next[c] == null) cur.next[c] = new Node();
                cur = cur.next[c];
            }
            cur.isWord = true;
        }

        List<String> out = new ArrayList<>(queries.length);
        for (String q : queries) {
            Node n = walk(root, q);
            if (n == null) out.add("none");
            else if (n.isWord) out.add("word");
            else if (hasChild(n)) out.add("prefix");
            else out.add("none");
        }
        return out;
    }

    private Node walk(Node root, String s) {
        Node cur = root;
        for (int i = 0; i < s.length(); i++) {
            cur = cur.next[s.charAt(i) - 'a'];
            if (cur == null) return null;
        }
        return cur;
    }

    private boolean hasChild(Node n) {
        for (Node c : n.next) if (c != null) return true;
        return false;
    }
}
