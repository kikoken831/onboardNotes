import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("permuteUnique", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SEQS)
            .example("[1,1,2]", "[[1,1,2],[1,2,1],[2,1,1]]").example("[1,2,3]", "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]")
            .example("[2,2,2]", "[[2,2,2]]")
            .gen(g -> new Object[]{ g.ints(1, 7, 0, 3) })
            .patterns("Array", "Backtracking", "Sorting").limitMs(2000).maxN(8);
    }
    public List<List<Integer>> permuteUnique(int[] nums) {
        int[] a = nums.clone(); Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        go(a, new boolean[a.length], new ArrayList<>(), out);
        return out;
    }
    private void go(int[] a, boolean[] used, List<Integer> cur, List<List<Integer>> out) {
        if (cur.size() == a.length) { out.add(new ArrayList<>(cur)); return; }
        for (int i = 0; i < a.length; i++) {
            if (used[i] || (i > 0 && a[i] == a[i - 1] && !used[i - 1])) continue;   // use equal values left to right
            used[i] = true; cur.add(a[i]); go(a, used, cur, out); cur.remove(cur.size() - 1); used[i] = false;
        }
    }
}
