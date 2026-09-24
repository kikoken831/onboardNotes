import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("combinationSum3", T.INT, T.INT).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("3, 7", "[[1,2,4]]").example("3, 9", "[[1,2,6],[1,3,5],[2,3,4]]").example("4, 1", "[]")
            .example("9, 45", "[[1,2,3,4,5,6,7,8,9]]")
            .gen(g -> new Object[]{ g.nextInt(2, 9), g.nextInt(1, 60) })
            .patterns("Array", "Backtracking").limitMs(2000).maxN(10);
    }
    public List<List<Integer>> combinationSum3(int k, int n) {
        Pre.check(k >= 2 && k <= 9 && n >= 1 && n <= 60, "2 <= k <= 9, 1 <= n <= 60");
        List<List<Integer>> out = new ArrayList<>();
        go(1, k, n, new ArrayList<>(), out);
        return out;
    }
    private void go(int start, int k, int rem, List<Integer> cur, List<List<Integer>> out) {
        if (cur.size() == k) { if (rem == 0) out.add(new ArrayList<>(cur)); return; }
        for (int v = start; v <= 9 && v <= rem; v++) { cur.add(v); go(v + 1, k, rem - v, cur, out); cur.remove(cur.size() - 1); }
    }
}
