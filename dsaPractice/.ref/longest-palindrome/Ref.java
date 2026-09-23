class Ref implements Problem {

    public Spec spec() {
        return Spec.method("longestPalindrome", T.STRING).returns(T.INT)
            .example("\"abccccdd\"", "7")
            .example("\"a\"",        "1")
            .example("\"Aa\"",       "1")
            .example("\"aaa\"",      "3")
            .example("\"ab\"",       "1")
            .example("\"aabb\"",     "4")
            .gen(g -> new Object[]{ g.string(1, 40, "aAbBcd") })
            .patterns("Hash Table", "String", "Greedy")
            .limitMs(2000)
            .maxN(100000);
    }

    public int longestPalindrome(String s) {
        Pre.check(!s.isEmpty(), "1 <= s.length");
        int[] count = new int[128];
        for (char c : s.toCharArray()) count[c]++;
        int len = 0;
        boolean odd = false;
        for (int c : count) {
            len += c / 2 * 2;
            if (c % 2 == 1) odd = true;
        }
        return len + (odd ? 1 : 0);
    }
}
