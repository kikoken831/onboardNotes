class Ref implements Problem {
    public Spec spec() {
        return Spec.method("minWindow", T.STRING, T.STRING).returns(T.STRING)
            .example("\"ADOBECODEBANC\", \"ABC\"", "\"BANC\"")
            .example("\"a\", \"a\"",   "\"a\"")
            .example("\"a\", \"aa\"",  "\"\"")               // t has more copies than s
            .example("\"ab\", \"b\"",  "\"b\"")
            .example("\"aab\", \"aab\"", "\"aab\"")
            .gen(g -> {
                for (int attempt = 0; ; attempt++) {
                    String s = g.string(1, 30, "ABCa"), t = g.string(1, 4, "ABCa");
                    if (uniqueMin(s, t) || attempt > 40) return new Object[]{ s, t };
                }
            })
            .patterns("Hash Table", "String", "Sliding Window").limitMs(2000).maxN(100000);
    }

    /** LeetCode guarantees a unique answer: at most one window of minimal length. */
    static boolean uniqueMin(String s, String t) {
        int best = Integer.MAX_VALUE, count = 0;
        for (int i = 0; i < s.length(); i++) for (int j = i + 1; j <= s.length(); j++) {
            if (covers(s.substring(i, j), t)) {
                if (j - i < best) { best = j - i; count = 1; } else if (j - i == best) count++;
                break;                               // longer windows from i are never minimal
            }
        }
        return count <= 1;
    }

    static boolean covers(String w, String t) {
        int[] c = new int[128];
        for (char ch : w.toCharArray()) c[ch]++;
        for (char ch : t.toCharArray()) if (--c[ch] < 0) return false;
        return true;
    }

    public String minWindow(String s, String t) {
        Pre.check(!s.isEmpty() && !t.isEmpty(), "1 <= lengths");
        Pre.check(s.length() > 60 || uniqueMin(s, t), "the answer is unique");
        int[] need = new int[128];
        for (char c : t.toCharArray()) need[c]++;
        int missing = t.length(), bestL = 0, bestLen = Integer.MAX_VALUE, l = 0;
        for (int r = 0; r < s.length(); r++) {
            if (need[s.charAt(r)]-- > 0) missing--;
            while (missing == 0) {
                if (r - l + 1 < bestLen) { bestLen = r - l + 1; bestL = l; }
                if (++need[s.charAt(l++)] > 0) missing++;
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestL, bestL + bestLen);
    }
}
