class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isAnagram", T.STRING, T.STRING).returns(T.BOOL)
            .example("\"anagram\", \"nagaram\"", "true")
            .example("\"rat\", \"car\"",         "false")
            .example("\"a\", \"ab\"",            "false")
            .example("\"ab\", \"a\"",            "false")
            .example("\"aab\", \"abb\"",         "false")
            .gen(g -> {
                String s = g.lower3(1, 20);
                String t;
                if (g.chance(0.5)) {
                    char[] c = s.toCharArray();
                    for (int i = c.length - 1; i > 0; i--) { int j = g.nextInt(0, i); char x = c[i]; c[i] = c[j]; c[j] = x; }
                    t = new String(c);
                } else t = g.lower3(1, 20);
                return new Object[]{ s, t };
            })
            .patterns("Hash Table", "String", "Sorting").limitMs(2000).maxN(200000);
    }
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] c = new int[26];
        for (int i = 0; i < s.length(); i++) { c[s.charAt(i) - 'a']++; c[t.charAt(i) - 'a']--; }
        for (int x : c) if (x != 0) return false;
        return true;
    }
}
