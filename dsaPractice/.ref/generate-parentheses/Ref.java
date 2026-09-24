import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("generateParenthesis", T.INT).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("3", "[\"((()))\",\"(()())\",\"(())()\",\"()(())\",\"()()()\"]").example("1", "[\"()\"]")
            .example("2", "[\"(())\",\"()()\"]")
            .gen(g -> new Object[]{ g.nextInt(1, 7) })
            .patterns("String", "Dynamic Programming", "Backtracking").limitMs(2000).maxN(8);
    }
    public List<String> generateParenthesis(int n) {
        Pre.check(n >= 1 && n <= 8, "1 <= n <= 8");
        List<String> out = new ArrayList<>();
        go(n, n, new StringBuilder(), out);
        return out;
    }
    private void go(int open, int close, StringBuilder b, List<String> out) {
        if (open == 0 && close == 0) { out.add(b.toString()); return; }
        if (open > 0) { b.append('('); go(open - 1, close, b, out); b.setLength(b.length() - 1); }
        if (close > open) { b.append(')'); go(open, close - 1, b, out); b.setLength(b.length() - 1); }
    }
}
