import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("combinationSum", T.INT_ARRAY, T.INT).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("[2,3,6,7], 7", "[[2,2,3],[7]]").example("[2,3,5], 8", "[[2,2,2,2],[2,3,3],[3,5]]").example("[2], 1", "[]")
            .example("[1], 2", "[[1,1]]")
            .gen(g -> new Object[]{ g.distinctInts(1, 6, 2, 12), g.nextInt(1, 20) })
            .patterns("Array", "Backtracking").limitMs(2000).maxN(20);
    }
    public List<List<Integer>> combinationSum(int[] c, int target) {
        Set<Integer> s = new HashSet<>(); for (int x : c) Pre.check(x >= 1 && s.add(x), "distinct positive candidates");
        int[] a = c.clone(); Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        go(a, 0, target, new ArrayList<>(), out);
        return out;
    }
    private void go(int[] a, int i, int rem, List<Integer> cur, List<List<Integer>> out) {
        if (rem == 0) { out.add(new ArrayList<>(cur)); return; }
        for (int j = i; j < a.length && a[j] <= rem; j++) { cur.add(a[j]); go(a, j, rem - a[j], cur, out); cur.remove(cur.size() - 1); }
    }
}
