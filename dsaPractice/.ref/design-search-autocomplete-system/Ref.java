import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.design("AutocompleteSystem", T.STRING_ARRAY, T.INT_ARRAY)
            .op("input", T.LIST_STRING, T.CHAR)
            .exampleCalls("[\"AutocompleteSystem\",\"input\",\"input\",\"input\",\"input\"]",
                          "[[[\"i love you\",\"island\",\"iroman\",\"i love leetcode\"],[5,3,2,2]],[\"i\"],[\" \"],[\"a\"],[\"#\"]]",
                          "[null,[\"i love you\",\"island\",\"i love leetcode\"],[\"i love you\",\"i love leetcode\"],[],[]]")
            .exampleCalls("[\"AutocompleteSystem\",\"input\",\"input\",\"input\",\"input\",\"input\",\"input\",\"input\",\"input\",\"input\",\"input\"]",
                          "[[[\"abc\",\"abbc\",\"a\"],[3,3,3]],[\"b\"],[\"c\"],[\"#\"],[\"b\"],[\"c\"],[\"#\"],[\"a\"],[\"b\"],[\"c\"],[\"#\"]]",
                          "[null,[],[],[],[\"bc\"],[\"bc\"],[],[\"a\",\"abbc\",\"abc\"],[\"abbc\",\"abc\"],[\"abc\"],[]]")   // ties by ASCII; typed sentences count
            .gen(g -> {
                int n = g.size(1, 6);
                Set<String> s = new LinkedHashSet<>();
                for (int i = 0; i < n * 3 && s.size() < n; i++) s.add(g.string(1, 4, "ab "));
                String[] sent = s.toArray(new String[0]);
                int[] times = g.ints(sent.length, 1, 4);
                Calls.Builder b = new Calls.Builder("AutocompleteSystem", sent, times);
                int k = g.size(1, 30);
                for (int i = 0, typed = 0; i < k; i++) {                    // '#' only ends a non-empty sentence
                    if (typed > 0 && g.chance(0.2)) { b.call("input", '#'); typed = 0; }
                    else { b.call("input", "ab ".charAt(g.nextInt(0, 2))); typed++; }
                }
                return new Object[]{ b.build() };
            })
            .patterns("String", "Depth-First Search", "Design", "Trie", "Sorting").limitMs(2000).maxN(2000);
    }
}

class AutocompleteSystem {
    private final Map<String, Integer> hot = new HashMap<>();
    private final StringBuilder cur = new StringBuilder();
    public AutocompleteSystem(String[] sentences, int[] times) {
        Pre.check(sentences.length == times.length, "equal lengths");
        for (int i = 0; i < sentences.length; i++) Pre.check(sentences[i].length() >= 1 && times[i] >= 1, "1 <= sentences[i].length, 1 <= times[i]");
        for (int i = 0; i < sentences.length; i++) hot.merge(sentences[i], times[i], Integer::sum);
    }
    public List<String> input(char c) {
        if (c == '#') { Pre.check(cur.length() > 0, "'#' ends a non-empty sentence"); hot.merge(cur.toString(), 1, Integer::sum); cur.setLength(0); return new ArrayList<>(); }
        cur.append(c);
        String p = cur.toString();
        List<String> m = new ArrayList<>();
        for (String s : hot.keySet()) if (s.startsWith(p)) m.add(s);
        m.sort((x, y) -> hot.get(x).equals(hot.get(y)) ? x.compareTo(y) : hot.get(y) - hot.get(x));
        return new ArrayList<>(m.subList(0, Math.min(3, m.size())));
    }
}
