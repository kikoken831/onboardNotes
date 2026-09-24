class Ref implements Problem {

    public Spec spec() {
        return Spec.method("mergeAlternately", T.STRING, T.STRING).returns(T.STRING)
            .example("\"abc\", \"pqr\"",  "\"apbqcr\"")
            .example("\"ab\", \"pqrs\"",  "\"apbqrs\"")
            .example("\"abcd\", \"pq\"",  "\"apbqcd\"")
            .example("\"a\", \"z\"",      "\"az\"")
            .gen(g -> new Object[]{ g.lower(1, 20), g.string(1, 20, "wxyz") })
            .patterns("Two Pointers", "String")
            .limitMs(2000)
            .maxN(100000);
    }

    public String mergeAlternately(String word1, String word2) {
        Pre.check(!word1.isEmpty() && !word2.isEmpty(), "1 <= lengths");
        StringBuilder b = new StringBuilder(word1.length() + word2.length());
        int i = 0;
        for (; i < word1.length() || i < word2.length(); i++) {
            if (i < word1.length()) b.append(word1.charAt(i));
            if (i < word2.length()) b.append(word2.charAt(i));
        }
        return b.toString();
    }
}
