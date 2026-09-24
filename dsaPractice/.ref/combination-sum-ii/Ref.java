import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("combinationSum2", T.INT_ARRAY, T.INT).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("[10,1,2,7,6,1,5], 8", "[[1,1,6],[1,2,5],[1,7],[2,6]]").example("[2,5,2,1,2], 5", "[[1,2,2],[5]]")
            .example("[1,1,1], 2", "[[1,1]]").example("[3], 1", "[]")
            .gen(g -> new Object[]{ g.ints(1, 14, 1, 6), g.nextInt(1, 15) })
            .patterns("Array", "Backtracking").limitMs(2000).maxN(20);
    }
    public List<List<Integer>> combinationSum2(int[] c, int target) {
        int[] a = c.clone(); Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        go(a, 0, target, new ArrayList<>(), out);
        return out;
    }
    private void go(int[] a, int i, int rem, List<Integer> cur, List<List<Integer>> out) {
        if (rem == 0) { out.add(new ArrayList<>(cur)); return; }
        for (int j = i; j < a.length && a[j] <= rem; j++) {
            if (j > i && a[j] == a[j - 1]) continue;              // same value at the same depth
            cur.add(a[j]); go(a, j + 1, rem - a[j], cur, out); cur.remove(cur.size() - 1);
        }
    }
}
