import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findWords", T.CHAR_ARRAY_2D, T.STRING_ARRAY).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("[[\"o\",\"a\",\"a\",\"n\"],[\"e\",\"t\",\"a\",\"e\"],[\"i\",\"h\",\"k\",\"r\"],[\"i\",\"f\",\"l\",\"v\"]], [\"oath\",\"pea\",\"eat\",\"rain\"]", "[\"eat\",\"oath\"]")
            .example("[[\"a\",\"b\"],[\"c\",\"d\"]], [\"abcb\"]", "[]")
            .example("[[\"a\",\"a\"]], [\"aaa\",\"aa\"]", "[\"aa\"]")           // each found word reported once
            .gen(g -> {
                int r = g.side(1, 5), c = g.side(1, 5);
                char[][] b = new char[r][c];
                for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) b[i][j] = "abc".charAt(g.nextInt(0, 2));
                Set<String> w = new LinkedHashSet<>(Arrays.asList(g.words(1, 10, 1, 5, "abc")));
                return new Object[]{ b, w.toArray(new String[0]) };
            })
            .patterns("Array", "String", "Backtracking", "Trie", "Matrix").limitMs(4000).maxN(20);
    }
    static final class Node { Node[] next = new Node[26]; String word; }
    public List<String> findWords(char[][] board, String[] words) {
        Node root = new Node();
        for (String w : words) { Node t = root; for (char c : w.toCharArray()) { if (t.next[c - 'a'] == null) t.next[c - 'a'] = new Node(); t = t.next[c - 'a']; } t.word = w; }
        List<String> out = new ArrayList<>();
        for (int i = 0; i < board.length; i++) for (int j = 0; j < board[0].length; j++) dfs(board, i, j, root, out);
        return out;
    }
    private void dfs(char[][] b, int i, int j, Node t, List<String> out) {
        if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || b[i][j] == '#') return;
        Node n = t.next[b[i][j] - 'a'];
        if (n == null) return;
        if (n.word != null) { out.add(n.word); n.word = null; }        // report once
        char c = b[i][j];
        b[i][j] = '#';
        dfs(b, i + 1, j, n, out); dfs(b, i - 1, j, n, out); dfs(b, i, j + 1, n, out); dfs(b, i, j - 1, n, out);
        b[i][j] = c;
    }
}
