import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("permute", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SEQS)
            .example("[1,2,3]", "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]").example("[0,1]", "[[0,1],[1,0]]").example("[1]", "[[1]]")
            .gen(g -> new Object[]{ g.distinctInts(1, 6, -10, 10) })
            .patterns("Array", "Backtracking").limitMs(2000).maxN(7);
    }
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        go(nums, new boolean[nums.length], new ArrayList<>(), out);
        return out;
    }
    private void go(int[] a, boolean[] used, List<Integer> cur, List<List<Integer>> out) {
        if (cur.size() == a.length) { out.add(new ArrayList<>(cur)); return; }
        for (int i = 0; i < a.length; i++) if (!used[i]) { used[i] = true; cur.add(a[i]); go(a, used, cur, out); cur.remove(cur.size() - 1); used[i] = false; }
    }
}
