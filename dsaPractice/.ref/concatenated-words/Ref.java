import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findAllConcatenatedWordsInADict", T.STRING_ARRAY).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("[\"cat\",\"cats\",\"catsdogcats\",\"dog\",\"dogcatsdog\",\"hippopotamuses\",\"rat\",\"ratcatdogcat\"]", "[\"catsdogcats\",\"dogcatsdog\",\"ratcatdogcat\"]")
            .example("[\"cat\",\"dog\",\"catdog\"]", "[\"catdog\"]").example("[\"a\"]", "[]")
            .example("[\"a\",\"aa\",\"aaa\"]", "[\"aa\",\"aaa\"]")
            .gen(g -> {
                Set<String> s = new LinkedHashSet<>(Arrays.asList(g.words(1, 10, 1, 3, "ab")));
                List<String> l = new ArrayList<>(s);
                for (int i = 0; i < 4 && l.size() >= 2; i++) if (g.chance(0.5)) s.add(l.get(g.nextInt(0, l.size() - 1)) + l.get(g.nextInt(0, l.size() - 1)));
                return new Object[]{ s.toArray(new String[0]) };
            })
            .patterns("Array", "String", "Dynamic Programming", "Depth-First Search", "Trie").limitMs(2000).maxN(1000);
    }
    public List<String> findAllConcatenatedWordsInADict(String[] words) {
        Set<String> seen = new HashSet<>();
        for (String w : words) Pre.check(!w.isEmpty() && seen.add(w), "unique non-empty words");
        String[] sorted = words.clone();
        Arrays.sort(sorted, Comparator.comparingInt(String::length));
        Set<String> dict = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (String w : sorted) {                          // shorter words only can build longer ones
            if (breaks(w, dict)) out.add(w);
            dict.add(w);
        }
        return out;
    }
    private boolean breaks(String w, Set<String> dict) {
        if (dict.isEmpty()) return false;
        boolean[] ok = new boolean[w.length() + 1];
        ok[0] = true;
        for (int i = 1; i <= w.length(); i++) for (int j = 0; j < i && !ok[i]; j++) ok[i] = ok[j] && dict.contains(w.substring(j, i));
        return ok[w.length()];
    }
}
