class Ref implements Problem {
    public Spec spec() {
        return Spec.design("WordDictionary")
            .op("addWord", T.VOID, T.STRING).op("search", T.BOOL, T.STRING)
            .exampleCalls("[\"WordDictionary\",\"addWord\",\"addWord\",\"addWord\",\"search\",\"search\",\"search\",\"search\"]",
                          "[[],[\"bad\"],[\"dad\"],[\"mad\"],[\"pad\"],[\"bad\"],[\".ad\"],[\"b..\"]]",
                          "[null,null,null,null,false,true,true,true]")
            .exampleCalls("[\"WordDictionary\",\"addWord\",\"search\",\"search\"]",
                          "[[],[\"a\"],[\".\"],[\"..\"]]",
                          "[null,null,true,false]")                         // '.' matches exactly one letter
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("WordDictionary");
                int n = g.size(1, 50);
                for (int i = 0; i < n; i++) {
                    if (g.chance(0.4)) b.call("addWord", g.string(1, 4, "ab"));
                    else b.call("search", g.string(1, 4, "ab.."));
                }
                return new Object[]{ b.build() };
            })
            .patterns("String", "Depth-First Search", "Design", "Trie").limitMs(2000).maxN(10000);
    }
}

class WordDictionary {
    private final WordDictionary[] next = new WordDictionary[26];
    private boolean word;
    public WordDictionary() {}
    public void addWord(String w) { WordDictionary t = this; for (char c : w.toCharArray()) { if (t.next[c - 'a'] == null) t.next[c - 'a'] = new WordDictionary(); t = t.next[c - 'a']; } t.word = true; }
    public boolean search(String w) { return find(w, 0); }
    private boolean find(String w, int i) {
        if (i == w.length()) return word;
        char c = w.charAt(i);
        if (c == '.') { for (WordDictionary n : next) if (n != null && n.find(w, i + 1)) return true; return false; }
        return next[c - 'a'] != null && next[c - 'a'].find(w, i + 1);
    }
}
