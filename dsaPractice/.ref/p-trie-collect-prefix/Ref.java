import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("wordsWithPrefix", T.STRING_ARRAY, T.STRING).returns(T.LIST_STRING)
            .compare(Cmp.EXACT)     // lexicographic order required
            .example("[\"app\",\"apple\",\"apply\",\"banana\"], \"app\"",
                     "[\"app\",\"apple\",\"apply\"]")
            .example("[\"b\",\"a\"], \"\"",   "[\"a\",\"b\"]")
            .example("[\"abc\"], \"x\"",      "[]")
            .example("[\"a\",\"a\"], \"a\"",  "[\"a\"]")
            .gen(g -> new Object[]{ g.words(0, 20, 0, 5, "abc"), g.string(0, 3, "abc") })
            .patterns("Trie", "String", "Depth-First Search")
            .limitMs(3000)
            .maxN(20000);
    }

    private static final class Node {
        final Node[] next = new Node[26];
        boolean isWord;
    }

    public List<String> wordsWithPrefix(String[] words, String prefix) {
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

        Node cur = root;
        for (int i = 0; i < prefix.length(); i++) {
            cur = cur.next[prefix.charAt(i) - 'a'];
            if (cur == null) return new ArrayList<>();
        }

        List<String> out = new ArrayList<>();
        collect(cur, new StringBuilder(prefix), out);
        return out;
    }

    private void collect(Node n, StringBuilder path, List<String> out) {
        if (n == null) return;
        if (n.isWord) out.add(path.toString());
        for (int c = 0; c < 26; c++) {
            if (n.next[c] == null) continue;
            path.append((char) ('a' + c));
            collect(n.next[c], path, out);
            path.deleteCharAt(path.length() - 1);
        }
    }
}
