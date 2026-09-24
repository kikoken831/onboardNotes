class Ref implements Problem {
    public Spec spec() {
        return Spec.design("Trie")
            .op("insert", T.VOID, T.STRING).op("search", T.BOOL, T.STRING).op("startsWith", T.BOOL, T.STRING)
            .exampleCalls("[\"Trie\",\"insert\",\"search\",\"search\",\"startsWith\",\"insert\",\"search\"]",
                          "[[],[\"apple\"],[\"apple\"],[\"app\"],[\"app\"],[\"app\"],[\"app\"]]",
                          "[null,null,true,false,true,null,true]")
            .exampleCalls("[\"Trie\",\"search\",\"startsWith\",\"insert\",\"insert\",\"search\"]",
                          "[[],[\"a\"],[\"a\"],[\"ab\"],[\"ab\"],[\"a\"]]",
                          "[null,false,false,null,null,false]")          // a prefix is not a word
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("Trie");
                int n = g.size(1, 60);
                for (int i = 0; i < n; i++) {
                    String w = g.string(1, 4, "abc");
                    int r = g.nextInt(0, 2);
                    b.call(r == 0 ? "insert" : r == 1 ? "search" : "startsWith", w);
                }
                return new Object[]{ b.build() };
            })
            .patterns("Hash Table", "String", "Design", "Trie").limitMs(2000).maxN(20000);
    }
}

class Trie {
    private final Trie[] next = new Trie[26];
    private boolean word;
    public Trie() {}
    public void insert(String w) { Trie t = this; for (char c : w.toCharArray()) { if (t.next[c - 'a'] == null) t.next[c - 'a'] = new Trie(); t = t.next[c - 'a']; } t.word = true; }
    public boolean search(String w) { Trie t = walk(w); return t != null && t.word; }
    public boolean startsWith(String p) { return walk(p) != null; }
    private Trie walk(String s) { Trie t = this; for (char c : s.toCharArray()) { t = t.next[c - 'a']; if (t == null) return null; } return t; }
}
