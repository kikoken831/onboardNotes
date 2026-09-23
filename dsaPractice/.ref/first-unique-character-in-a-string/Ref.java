class Ref implements Problem {

    public Spec spec() {
        return Spec.method("firstUniqChar", T.STRING).returns(T.INT)
            .example("\"leetcode\"",     "0")
            .example("\"loveleetcode\"", "2")
            .example("\"aabb\"",         "-1")
            .example("\"z\"",            "0")
            .example("\"aabbc\"",        "4")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.lower3(1, 30) : g.string(1, 30, "abcdefgh") })
            .patterns("Hash Table", "String", "Counting")
            .limitMs(2000)
            .maxN(100000);
    }

    public int firstUniqChar(String s) {
        Pre.check(!s.isEmpty(), "1 <= s.length");
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) count[s.charAt(i) - 'a']++;
        for (int i = 0; i < s.length(); i++) if (count[s.charAt(i) - 'a'] == 1) return i;
        return -1;
    }
}
