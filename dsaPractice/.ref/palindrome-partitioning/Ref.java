import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("partition", T.STRING).returns(T.LIST_LIST_STRING)
            .compare(Cmp.SET_OF_SEQS)
            .example("\"aab\"", "[[\"a\",\"a\",\"b\"],[\"aa\",\"b\"]]").example("\"a\"", "[[\"a\"]]")
            .example("\"aba\"", "[[\"a\",\"b\",\"a\"],[\"aba\"]]")
            .gen(g -> new Object[]{ g.string(1, 10, "ab") })
            .patterns("String", "Dynamic Programming", "Backtracking").limitMs(2000).maxN(16);
    }
    public List<List<String>> partition(String s) {
        List<List<String>> out = new ArrayList<>();
        go(s, 0, new ArrayList<>(), out);
        return out;
    }
    private void go(String s, int i, List<String> cur, List<List<String>> out) {
        if (i == s.length()) { out.add(new ArrayList<>(cur)); return; }
        for (int j = i + 1; j <= s.length(); j++) {
            String p = s.substring(i, j);
            if (!new StringBuilder(p).reverse().toString().equals(p)) continue;
            cur.add(p); go(s, j, cur, out); cur.remove(cur.size() - 1);
        }
    }
}
