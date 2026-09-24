import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("combine", T.INT, T.INT).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("4, 2", "[[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]").example("1, 1", "[[1]]").example("3, 3", "[[1,2,3]]")
            .gen(g -> { int n = g.nextInt(1, 9); return new Object[]{ n, g.nextInt(1, n) }; })
            .patterns("Backtracking").limitMs(2000).maxN(10);
    }
    public List<List<Integer>> combine(int n, int k) {
        Pre.check(1 <= k && k <= n, "1 <= k <= n");
        List<List<Integer>> out = new ArrayList<>();
        go(1, n, k, new ArrayList<>(), out);
        return out;
    }
    private void go(int start, int n, int k, List<Integer> cur, List<List<Integer>> out) {
        if (cur.size() == k) { out.add(new ArrayList<>(cur)); return; }
        for (int v = start; v <= n - (k - cur.size()) + 1; v++) { cur.add(v); go(v + 1, n, k, cur, out); cur.remove(cur.size() - 1); }
    }
}
