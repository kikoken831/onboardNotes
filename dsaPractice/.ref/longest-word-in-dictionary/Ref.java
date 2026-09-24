import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("longestWord", T.STRING_ARRAY).returns(T.STRING)
            .example("[\"w\",\"wo\",\"wor\",\"worl\",\"world\"]", "\"world\"")
            .example("[\"a\",\"banana\",\"app\",\"appl\",\"ap\",\"apply\",\"apple\"]", "\"apple\"")   // tie -> smaller
            .example("[\"b\",\"ba\",\"a\"]", "\"ba\"").example("[\"ab\"]", "\"\"")
            .gen(g -> new Object[]{ g.words(1, 20, 1, 4, "ab") })
            .patterns("Array", "Hash Table", "String", "Trie", "Sorting").limitMs(2000).maxN(1000);
    }
    public String longestWord(String[] words) {
        Set<String> all = new HashSet<>(Arrays.asList(words));
        String best = "";
        for (String w : words) {
            if (w.length() < best.length() || (w.length() == best.length() && w.compareTo(best) >= 0)) continue;
            boolean ok = true;
            for (int i = 1; i < w.length() && ok; i++) ok = all.contains(w.substring(0, i));
            if (ok) best = w;
        }
        return best;
    }
}
