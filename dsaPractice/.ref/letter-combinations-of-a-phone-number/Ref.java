import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("letterCombinations", T.STRING).returns(T.LIST_STRING)
            .compare(Cmp.MULTISET)
            .example("\"23\"", "[\"ad\",\"ae\",\"af\",\"bd\",\"be\",\"bf\",\"cd\",\"ce\",\"cf\"]").example("\"\"", "[]")
            .example("\"2\"", "[\"a\",\"b\",\"c\"]").example("\"79\"", "[\"pw\",\"px\",\"py\",\"pz\",\"qw\",\"qx\",\"qy\",\"qz\",\"rw\",\"rx\",\"ry\",\"rz\",\"sw\",\"sx\",\"sy\",\"sz\"]")
            .gen(g -> new Object[]{ g.string(0, 4, "23456789") })
            .patterns("Hash Table", "String", "Backtracking").limitMs(2000).maxN(8);
    }
    static final String[] KEYS = { "", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz" };
    public List<String> letterCombinations(String digits) {
        List<String> out = new ArrayList<>();
        if (!digits.isEmpty()) go(digits, 0, new StringBuilder(), out);
        return out;
    }
    private void go(String d, int i, StringBuilder b, List<String> out) {
        if (i == d.length()) { out.add(b.toString()); return; }
        for (char c : KEYS[d.charAt(i) - '0'].toCharArray()) { b.append(c); go(d, i + 1, b, out); b.setLength(b.length() - 1); }
    }
}
