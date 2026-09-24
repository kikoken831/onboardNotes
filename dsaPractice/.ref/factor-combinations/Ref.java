import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("getFactors", T.INT).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("1", "[]").example("12", "[[2,6],[3,4],[2,2,3]]").example("37", "[]")
            .example("32", "[[2,16],[4,8],[2,2,8],[2,4,4],[2,2,2,4],[2,2,2,2,2]]").example("4", "[[2,2]]")
            .gen(g -> new Object[]{ g.chance(0.3) ? 1 << g.nextInt(0, 10) : g.nextInt(1, 400) })
            .patterns("Backtracking").limitMs(2000).maxN(10);
    }
    public List<List<Integer>> getFactors(int n) {
        Pre.check(n >= 1 && n <= 10_000_000, "1 <= n <= 10^7");
        List<List<Integer>> out = new ArrayList<>();
        go(n, 2, new ArrayList<>(), out);
        return out;
    }
    private void go(int n, int lo, List<Integer> cur, List<List<Integer>> out) {
        for (int f = lo; (long) f * f <= n; f++) {
            if (n % f != 0) continue;
            cur.add(f);
            List<Integer> c = new ArrayList<>(cur); c.add(n / f); out.add(c);   // f × (n / f)
            go(n / f, f, cur, out);                                          // split n / f further, factors >= f
            cur.remove(cur.size() - 1);
        }
    }
}
