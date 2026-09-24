import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("wordSquares", T.STRING_ARRAY).returns(T.LIST_LIST_STRING)
            .compare(Cmp.SET_OF_SEQS)
            .example("[\"area\",\"lead\",\"wall\",\"lady\",\"ball\"]", "[[\"ball\",\"area\",\"lead\",\"lady\"],[\"wall\",\"area\",\"lead\",\"lady\"]]")
            .example("[\"abat\",\"baba\",\"atan\",\"atal\"]", "[[\"baba\",\"abat\",\"baba\",\"atal\"],[\"baba\",\"abat\",\"baba\",\"atan\"]]")
            .example("[\"a\"]", "[[\"a\"]]").example("[\"ab\",\"cd\"]", "[]")
            .gen(g -> {
                int len = g.nextInt(1, 4);
                Set<String> s = new LinkedHashSet<>();
                int n = g.size(1, 12);
                for (int i = 0; i < n * 3 && s.size() < n; i++) s.add(g.string(len, len, "ab"));
                return new Object[]{ s.toArray(new String[0]) };
            })
            .patterns("Array", "String", "Backtracking", "Trie").limitMs(2000).maxN(12);
    }
    public List<List<String>> wordSquares(String[] words) {
        Pre.check(words.length >= 1, "at least one word");
        int len = words[0].length();
        Set<String> seen = new HashSet<>();
        for (String w : words) Pre.check(w.length() == len && seen.add(w), "unique words of equal length");
        Map<String, List<String>> byPrefix = new HashMap<>();
        for (String w : words) for (int i = 0; i <= len; i++) byPrefix.computeIfAbsent(w.substring(0, i), k -> new ArrayList<>()).add(w);
        List<List<String>> out = new ArrayList<>();
        List<String> sq = new ArrayList<>();
        for (String w : words) { sq.add(w); go(len, sq, byPrefix, out); sq.remove(sq.size() - 1); }
        return out;
    }
    private void go(int len, List<String> sq, Map<String, List<String>> byPrefix, List<List<String>> out) {
        int r = sq.size();
        if (r == len) { out.add(new ArrayList<>(sq)); return; }
        StringBuilder p = new StringBuilder();
        for (String w : sq) p.append(w.charAt(r));                // row r must start with column r so far
        for (String w : byPrefix.getOrDefault(p.toString(), List.of())) { sq.add(w); go(len, sq, byPrefix, out); sq.remove(sq.size() - 1); }
    }
}
