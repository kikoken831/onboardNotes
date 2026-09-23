class Ref implements Problem {

    public Spec spec() {
        return Spec.method("removeDuplicates", T.STRING).returns(T.STRING)
            .example("\"abbaca\"", "\"ca\"")
            .example("\"azxxzy\"", "\"ay\"")
            .example("\"a\"",      "\"a\"")
            .example("\"aa\"",     "\"\"")
            .example("\"aaa\"",    "\"a\"")
            .gen(g -> new Object[]{ g.chance(0.6) ? g.string(1, 40, "ab") : g.lower3(1, 40) })
            .patterns("String", "Stack")
            .limitMs(2000)
            .maxN(200000);
    }

    public String removeDuplicates(String s) {
        Pre.check(!s.isEmpty(), "1 <= s.length");
        StringBuilder st = new StringBuilder();
        for (char c : s.toCharArray()) {
            int n = st.length();
            if (n > 0 && st.charAt(n - 1) == c) st.setLength(n - 1);
            else st.append(c);
        }
        return st.toString();
    }
}
