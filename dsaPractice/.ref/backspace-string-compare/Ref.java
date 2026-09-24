class Ref implements Problem {
    public Spec spec() {
        return Spec.method("backspaceCompare", T.STRING, T.STRING).returns(T.BOOL)
            .example("\"ab#c\", \"ad#c\"", "true").example("\"ab##\", \"c#d#\"", "true")
            .example("\"a#c\", \"b\"", "false").example("\"a##c\", \"#a#c\"", "true").example("\"bxj##tw\", \"bxo#j##tw\"", "true")
            .gen(g -> new Object[]{ g.string(1, 20, "ab##"), g.string(1, 20, "ab##") })
            .patterns("Two Pointers", "String", "Stack", "Simulation").limitMs(2000).maxN(200000);
    }
    public boolean backspaceCompare(String s, String t) {
        int i = s.length() - 1, j = t.length() - 1;
        while (i >= 0 || j >= 0) {
            i = skip(s, i); j = skip(t, j);
            if (i < 0 || j < 0) return i < 0 && j < 0;
            if (s.charAt(i--) != t.charAt(j--)) return false;
        }
        return true;
    }
    private static int skip(String s, int i) {
        int back = 0;
        while (i >= 0) {
            if (s.charAt(i) == '#') { back++; i--; }
            else if (back > 0) { back--; i--; }
            else break;
        }
        return i;
    }
}
