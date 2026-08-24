import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("binaryStrings", T.INT).returns(T.LIST_STRING)
            .compare(Cmp.EXACT)        // lexicographic order is required
            .example("2", "[\"00\",\"01\",\"10\",\"11\"]")
            .example("0", "[\"\"]")
            .example("1", "[\"0\",\"1\"]")
            .example("3", "[\"000\",\"001\",\"010\",\"011\",\"100\",\"101\",\"110\",\"111\"]")
            .gen(g -> new Object[]{ g.nextInt(0, 12) })
            .patterns("Backtracking")
            .limitMs(3000)
            .maxN(14);
    }

    public List<String> binaryStrings(int n) {
        List<String> out = new ArrayList<>();
        go(n, new StringBuilder(), out);
        return out;
    }

    private void go(int n, StringBuilder cur, List<String> out) {
        if (cur.length() == n) { out.add(cur.toString()); return; }
        cur.append('0');
        go(n, cur, out);
        cur.deleteCharAt(cur.length() - 1);
        cur.append('1');
        go(n, cur, out);
        cur.deleteCharAt(cur.length() - 1);
    }
}
