class Ref implements Problem {

    public Spec spec() {
        return Spec.method("firstUniqueChar", T.STRING).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("\"leetcode\"",     "0")
            .example("\"loveleetcode\"", "2")
            .example("\"aabb\"",         "-1")
            .example("\"\"",             "-1")
            .example("\"z\"",            "0")
            .example("\"aab\"",          "2")
            .gen(g -> new Object[]{ g.lower3(0, 60) })
            .patterns("Hash Table", "String", "Counting")
            .limitMs(2000)
            .maxN(500000);
    }

    public int firstUniqueChar(String s) {
        int[] freq = new int[26];
        for (int i = 0; i < s.length(); i++) freq[s.charAt(i) - 'a']++;
        for (int i = 0; i < s.length(); i++) {
            if (freq[s.charAt(i) - 'a'] == 1) return i;
        }
        return -1;
    }
}
