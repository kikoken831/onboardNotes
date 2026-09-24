import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.design("WordFilter", T.STRING_ARRAY)
            .op("f", T.INT, T.STRING, T.STRING)
            .exampleCalls("[\"WordFilter\",\"f\"]", "[[[\"apple\"]],[\"a\",\"e\"]]", "[null,0]")
            .exampleCalls("[\"WordFilter\",\"f\",\"f\",\"f\"]", "[[[\"apple\",\"ape\"]],[\"a\",\"e\"],[\"ap\",\"le\"],[\"b\",\"c\"]]", "[null,1,0,-1]")   // LARGEST index wins
            .exampleCalls("[\"WordFilter\",\"f\"]", "[[[\"abc\",\"abc\"]],[\"abc\",\"abc\"]]", "[null,1]")
            .gen(g -> {
                String[] w = g.words(1, 10, 1, 4, "ab");
                Calls.Builder b = new Calls.Builder("WordFilter", (Object) w);
                int n = g.size(1, 30);
                for (int i = 0; i < n; i++) b.call("f", g.string(1, 3, "ab"), g.string(1, 3, "ab"));
                return new Object[]{ b.build() };
            })
            .patterns("Array", "Hash Table", "String", "Design", "Trie").limitMs(2000).maxN(2000);
    }
}

class WordFilter {
    private final Map<String, Integer> best = new HashMap<>();
    public WordFilter(String[] words) {
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            for (int p = 1; p <= w.length(); p++) for (int s = 0; s < w.length(); s++)
                best.put(w.substring(0, p) + "#" + w.substring(s), i);        // later index overwrites
        }
    }
    public int f(String pref, String suff) { return best.getOrDefault(pref + "#" + suff, -1); }
}
