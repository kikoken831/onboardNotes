import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("generateAbbreviations", T.STRING).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("\"word\"", "[\"4\",\"3d\",\"2r1\",\"2rd\",\"1o2\",\"1o1d\",\"1or1\",\"1ord\",\"w3\",\"w2d\",\"w1r1\",\"w1rd\",\"wo2\",\"wo1d\",\"wor1\",\"word\"]")
            .example("\"a\"", "[\"1\",\"a\"]").example("\"ab\"", "[\"2\",\"1b\",\"a1\",\"ab\"]")
            .gen(g -> new Object[]{ g.lower(1, 8) })
            .patterns("String", "Backtracking", "Bit Manipulation").limitMs(2000).maxN(12);
    }
    public List<String> generateAbbreviations(String w) {
        List<String> out = new ArrayList<>();
        go(w, 0, 0, new StringBuilder(), out);
        return out;
    }
    private void go(String w, int i, int run, StringBuilder b, List<String> out) {
        int len = b.length();
        if (i == w.length()) { if (run > 0) b.append(run); out.add(b.toString()); b.setLength(len); return; }
        go(w, i + 1, run + 1, b, out);                         // abbreviate w[i]
        if (run > 0) b.append(run);                            // keep w[i]: flush the pending count first
        b.append(w.charAt(i));
        go(w, i + 1, 0, b, out);
        b.setLength(len);
    }
}
