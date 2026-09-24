class Ref implements Problem {
    public Spec spec() {
        return Spec.method("longestPalindrome", T.STRING).returns(T.STRING)
            // Several answers can be correct ("bab" and "aba"): check the property.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    String s = (String) input[0], y = (String) yours, r = (String) reference;
                    if (y == null) return "returned null";
                    if (!s.contains(y)) return "\"" + y + "\" is not a substring of the input";
                    if (!new StringBuilder(y).reverse().toString().equals(y)) return "\"" + y + "\" is not a palindrome";
                    if (y.length() != r.length()) return "length " + y.length() + ", but a palindrome of length " + r.length() + " exists (\"" + r + "\")";
                    return null;
                }
            })
            .example("\"babad\"", "\"bab\"").example("\"cbbd\"", "\"bb\"").example("\"a\"", "\"a\"")
            .example("\"ac\"", "\"a\"").example("\"aaaa\"", "\"aaaa\"")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.string(1, 40, "ab") : g.lower3(1, 40) })
            .patterns("Two Pointers", "String", "Dynamic Programming").limitMs(2000).maxN(3000);
    }
    public String longestPalindrome(String s) {
        Pre.check(!s.isEmpty(), "1 <= s.length");
        int bs = 0, bl = 1;
        for (int c = 0; c < s.length(); c++) {
            for (int odd = 0; odd <= 1; odd++) {
                int l = c, r = c + odd;
                while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }
                if (r - l - 1 > bl) { bl = r - l - 1; bs = l + 1; }
            }
        }
        return s.substring(bs, bs + bl);
    }
}
