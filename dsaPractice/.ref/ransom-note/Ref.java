class Ref implements Problem {

    public Spec spec() {
        return Spec.method("canConstruct", T.STRING, T.STRING).returns(T.BOOL)
            .example("\"a\", \"b\"",     "false")
            .example("\"aa\", \"ab\"",   "false")
            .example("\"aa\", \"aab\"",  "true")
            .example("\"abc\", \"cba\"", "true")
            .example("\"b\", \"aab\"",   "true")
            .gen(g -> new Object[]{ g.lower3(1, 12), g.lower3(1, 20) })
            .patterns("Hash Table", "String", "Counting")
            .limitMs(2000)
            .maxN(100000);
    }

    public boolean canConstruct(String ransomNote, String magazine) {
        Pre.check(!ransomNote.isEmpty() && !magazine.isEmpty(), "1 <= lengths");
        int[] count = new int[26];
        for (char c : magazine.toCharArray()) count[c - 'a']++;
        for (char c : ransomNote.toCharArray()) {
            if (--count[c - 'a'] < 0) return false;
        }
        return true;
    }
}
