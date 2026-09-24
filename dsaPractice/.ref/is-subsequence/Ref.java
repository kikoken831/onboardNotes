class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isSubsequence", T.STRING, T.STRING).returns(T.BOOL)
            .example("\"abc\", \"ahbgdc\"", "true").example("\"axc\", \"ahbgdc\"", "false")
            .example("\"\", \"abc\"", "true").example("\"abc\", \"\"", "false").example("\"aa\", \"a\"", "false")
            .gen(g -> new Object[]{ g.lower3(0, 5), g.lower3(0, 25) })
            .patterns("Two Pointers", "String", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public boolean isSubsequence(String s, String t) {
        int i = 0;
        for (int j = 0; j < t.length() && i < s.length(); j++) if (s.charAt(i) == t.charAt(j)) i++;
        return i == s.length();
    }
}
