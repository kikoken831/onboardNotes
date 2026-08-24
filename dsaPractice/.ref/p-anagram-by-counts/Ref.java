class Ref implements Problem {

    public Spec spec() {
        return Spec.method("anagramByCounts", T.STRING, T.STRING).returns(T.BOOL)
            .compare(Cmp.EXACT)
            .example("\"anagram\", \"nagaram\"", "true")
            .example("\"rat\", \"car\"",         "false")
            .example("\"\", \"\"",               "true")
            .example("\"a\", \"ab\"",            "false")
            .example("\"aacc\", \"ccac\"",       "false")
            .example("\"ab\", \"ba\"",           "true")
            .gen(g -> {
                String a = g.lower3(0, 30);
                // Half the time hand back a genuine permutation, so `true` cases are
                // not vanishingly rare. Uniform random strings are almost never anagrams.
                if (g.chance(0.5)) {
                    char[] c = a.toCharArray();
                    for (int i = c.length - 1; i > 0; i--) {
                        int j = g.nextInt(0, i);
                        char t = c[i]; c[i] = c[j]; c[j] = t;
                    }
                    return new Object[]{ a, new String(c) };
                }
                return new Object[]{ a, g.lower3(0, 30) };
            })
            .patterns("Hash Table", "String", "Sorting")
            .limitMs(2000)
            .maxN(200000);
    }

    public boolean anagramByCounts(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] freq = new int[26];
        for (int i = 0; i < s.length(); i++) {
            freq[s.charAt(i) - 'a']++;
            freq[t.charAt(i) - 'a']--;
        }
        for (int f : freq) if (f != 0) return false;
        return true;
    }
}
