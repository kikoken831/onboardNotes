class Ref implements Problem {
    public Spec spec() {
        return Spec.method("countSubstrings", T.STRING).returns(T.INT)
            .example("\"abc\"", "3").example("\"aaa\"", "6").example("\"a\"", "1").example("\"abba\"", "6")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.string(1, 40, "ab") : g.lower3(1, 40) })
            .patterns("Two Pointers", "String", "Dynamic Programming").limitMs(2000).maxN(3000);
    }
    public int countSubstrings(String s) {
        int count = 0;
        for (int c = 0; c < 2 * s.length() - 1; c++) {
            int l = c / 2, r = l + c % 2;
            while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { count++; l--; r++; }
        }
        return count;
    }
}
