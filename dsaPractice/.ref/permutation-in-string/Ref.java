class Ref implements Problem {
    public Spec spec() {
        return Spec.method("checkInclusion", T.STRING, T.STRING).returns(T.BOOL)
            .example("\"ab\", \"eidbaooo\"", "true").example("\"ab\", \"eidboaoo\"", "false")
            .example("\"abc\", \"ab\"", "false").example("\"a\", \"a\"", "true").example("\"adc\", \"dcda\"", "true")
            .gen(g -> {
                String s1 = g.lower3(1, 4), s2 = g.lower3(1, 25);
                if (g.chance(0.4) && s2.length() >= s1.length()) {       // plant a permutation
                    char[] c = s1.toCharArray();
                    for (int i = c.length - 1; i > 0; i--) { int j = g.nextInt(0, i); char x = c[i]; c[i] = c[j]; c[j] = x; }
                    int at = g.nextInt(0, s2.length() - s1.length());
                    s2 = s2.substring(0, at) + new String(c) + s2.substring(at + s1.length());
                }
                return new Object[]{ s1, s2 };
            })
            .patterns("Hash Table", "Two Pointers", "String", "Sliding Window").limitMs(2000).maxN(200000);
    }
    public boolean checkInclusion(String s1, String s2) {
        int n = s1.length();
        if (n > s2.length()) return false;
        int[] c = new int[26];
        for (int i = 0; i < n; i++) { c[s1.charAt(i) - 'a']++; c[s2.charAt(i) - 'a']--; }
        int diff = 0;
        for (int x : c) if (x != 0) diff++;
        if (diff == 0) return true;
        for (int r = n; r < s2.length(); r++) {
            int in = s2.charAt(r) - 'a', out = s2.charAt(r - n) - 'a';
            if (c[in] == 0) diff++; if (--c[in] == 0) diff--;
            if (c[out] == 0) diff++; if (++c[out] == 0) diff--;
            if (diff == 0) return true;
        }
        return false;
    }
}
