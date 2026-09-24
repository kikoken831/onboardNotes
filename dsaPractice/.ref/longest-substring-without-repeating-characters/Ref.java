class Ref implements Problem {
    public Spec spec() {
        return Spec.method("lengthOfLongestSubstring", T.STRING).returns(T.INT)
            .example("\"abcabcbb\"", "3").example("\"bbbbb\"", "1").example("\"pwwkew\"", "3")
            .example("\"\"", "0").example("\" \"", "1")
            .example("\"abba\"", "2")                      // left edge must never move backwards
            .gen(g -> new Object[]{ g.chance(0.5) ? g.lower3(0, 40) : g.string(0, 40, "abcdefg ") })
            .patterns("Hash Table", "String", "Sliding Window").limitMs(2000).maxN(200000);
    }
    public int lengthOfLongestSubstring(String s) {
        int[] last = new int[128];
        java.util.Arrays.fill(last, -1);
        int best = 0, left = 0;
        for (int r = 0; r < s.length(); r++) {
            char c = s.charAt(r);
            if (last[c] >= left) left = last[c] + 1;
            last[c] = r;
            best = Math.max(best, r - left + 1);
        }
        return best;
    }
}
