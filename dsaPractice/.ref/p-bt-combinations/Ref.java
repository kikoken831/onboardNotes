import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("combinations", T.INT, T.INT).returns(T.LIST_LIST_INT)
            // Combination order is free; within a combination it must be ascending.
            .compare(Cmp.SET_OF_SEQS)
            .example("4, 2", "[[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]")
            .example("1, 1", "[[1]]")
            .example("3, 0", "[[]]")
            .example("3, 3", "[[1,2,3]]")
            .example("2, 3", "[]")
            .gen(g -> {
                int n = g.nextInt(0, 10);
                return new Object[]{ n, g.nextInt(0, Math.min(10, n + 2)) };
            })
            .patterns("Backtracking")
            .limitMs(3000)
            .maxN(12);
    }

    public List<List<Integer>> combinations(int n, int k) {
        List<List<Integer>> out = new ArrayList<>();
        go(n, k, 1, new ArrayList<>(), out);
        return out;
    }

    private void go(int n, int k, int start, List<Integer> cur, List<List<Integer>> out) {
        if (cur.size() == k) { out.add(new ArrayList<>(cur)); return; }
        for (int v = start; v <= n; v++) {
            cur.add(v);
            go(n, k, v + 1, cur, out);
            cur.remove(cur.size() - 1);
        }
    }
}
