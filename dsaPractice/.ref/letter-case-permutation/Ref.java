import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("letterCasePermutation", T.STRING).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("\"a1b2\"", "[\"a1b2\",\"a1B2\",\"A1b2\",\"A1B2\"]").example("\"3z4\"", "[\"3z4\",\"3Z4\"]")
            .example("\"12345\"", "[\"12345\"]").example("\"C\"", "[\"c\",\"C\"]")
            .gen(g -> new Object[]{ g.string(1, 8, "aB1c2D") })
            .patterns("String", "Backtracking", "Bit Manipulation").limitMs(2000).maxN(12);
    }
    public List<String> letterCasePermutation(String s) {
        List<String> out = new ArrayList<>();
        go(s.toCharArray(), 0, out);
        return out;
    }
    private void go(char[] c, int i, List<String> out) {
        if (i == c.length) { out.add(new String(c)); return; }
        if (Character.isLetter(c[i])) {
            c[i] = Character.toLowerCase(c[i]); go(c, i + 1, out);
            c[i] = Character.toUpperCase(c[i]); go(c, i + 1, out);
        } else go(c, i + 1, out);
    }
}
