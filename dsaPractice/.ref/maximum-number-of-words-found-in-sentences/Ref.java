class Ref implements Problem {

    public Spec spec() {
        return Spec.method("mostWordsFound", T.STRING_ARRAY).returns(T.INT)
            .example("[\"alice and bob love leetcode\",\"i think so too\",\"this is great thanks very much\"]", "6")
            .example("[\"please wait\",\"continue to fight\",\"continue to win\"]", "3")
            .example("[\"one\"]", "1")
            .gen(g -> {
                int n = g.size(1, 20);
                String[] s = new String[n];
                for (int i = 0; i < n; i++) {
                    int w = g.nextInt(1, 10);
                    StringBuilder b = new StringBuilder();
                    for (int k = 0; k < w; k++) {
                        if (k > 0) b.append(' ');
                        b.append(g.lower(1, 5));
                    }
                    s[i] = b.toString();
                }
                return new Object[]{ s };
            })
            .patterns("Array", "String")
            .limitMs(2000)
            .maxN(20000);
    }

    public int mostWordsFound(String[] sentences) {
        Pre.length(sentences, 1, 1000_000, "sentences");
        int best = 0;
        for (String s : sentences) {
            Pre.check(!s.isEmpty() && s.charAt(0) != ' ' && s.charAt(s.length() - 1) != ' '
                      && !s.contains("  "), "single-spaced, no leading/trailing space");
            int words = 1;
            for (int i = 0; i < s.length(); i++) if (s.charAt(i) == ' ') words++;
            best = Math.max(best, words);
        }
        return best;
    }
}
