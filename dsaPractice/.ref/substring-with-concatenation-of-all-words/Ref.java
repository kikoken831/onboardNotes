import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findSubstring", T.STRING, T.STRING_ARRAY).returns(T.LIST_INT)
            .compare(Cmp.MULTISET)
            .example("\"barfoothefoobarman\", [\"foo\",\"bar\"]", "[0,9]")
            .example("\"wordgoodgoodgoodbestword\", [\"word\",\"good\",\"best\",\"word\"]", "[]")
            .example("\"barfoofoobarthefoobarman\", [\"bar\",\"foo\",\"the\"]", "[6,9,12]")
            .example("\"aaaa\", [\"aa\",\"aa\"]", "[0]")
            .gen(g -> {
                int len = g.nextInt(1, 3);
                String[] w = g.words(1, 4, len, len, "ab");
                return new Object[]{ g.string(1, 30, "ab"), w };
            })
            .patterns("Hash Table", "String", "Sliding Window").limitMs(2000).maxN(50000);
    }
    public List<Integer> findSubstring(String s, String[] words) {
        Pre.check(words.length >= 1, "1 <= words.length");
        int wl = words[0].length();
        for (String w : words) Pre.check(w.length() == wl && wl >= 1, "all words have the same length");
        Map<String, Integer> need = new HashMap<>();
        for (String w : words) need.merge(w, 1, Integer::sum);
        List<Integer> out = new ArrayList<>();
        int total = wl * words.length;
        for (int off = 0; off < wl; off++) {
            Map<String, Integer> have = new HashMap<>();
            int used = 0;
            for (int l = off, r = off; r + wl <= s.length(); r += wl) {
                String w = s.substring(r, r + wl);
                if (!need.containsKey(w)) { have.clear(); used = 0; l = r + wl; continue; }
                have.merge(w, 1, Integer::sum);
                used++;
                while (have.get(w) > need.get(w)) {
                    String x = s.substring(l, l + wl);
                    have.merge(x, -1, Integer::sum);
                    used--;
                    l += wl;
                }
                if (used == words.length) out.add(l);
            }
        }
        return out;
    }
}
