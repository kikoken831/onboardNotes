import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("subsetsWithDup", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("[1,2,2]", "[[],[1],[1,2],[1,2,2],[2],[2,2]]").example("[0]", "[[],[0]]").example("[4,4,4]", "[[],[4],[4,4],[4,4,4]]")
            .gen(g -> new Object[]{ g.ints(1, 9, 0, 3) })
            .patterns("Array", "Backtracking", "Bit Manipulation").limitMs(2000).maxN(10);
    }
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        int[] a = nums.clone(); Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        go(a, 0, new ArrayList<>(), out);
        return out;
    }
    private void go(int[] a, int i, List<Integer> cur, List<List<Integer>> out) {
        out.add(new ArrayList<>(cur));
        for (int j = i; j < a.length; j++) {
            if (j > i && a[j] == a[j - 1]) continue;
            cur.add(a[j]); go(a, j + 1, cur, out); cur.remove(cur.size() - 1);
        }
    }
}
