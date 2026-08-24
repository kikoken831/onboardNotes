import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("subsets", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            // Any order, inside and out.
            .compare(Cmp.SET_OF_SETS)
            .example("[1,2,3]", "[[],[1],[2],[3],[1,2],[1,3],[2,3],[1,2,3]]")
            .example("[]",      "[[]]")
            .example("[0]",     "[[],[0]]")
            .example("[1,2]",   "[[],[1],[2],[1,2]]")
            .gen(g -> new Object[]{ g.strictlySortedInts(0, 10, -20, 20) })
            .patterns("Array", "Backtracking", "Bit Manipulation")
            .limitMs(3000)
            .maxN(14);
    }

    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        go(nums, 0, new ArrayList<>(), out);
        return out;
    }

    private void go(int[] nums, int i, List<Integer> cur, List<List<Integer>> out) {
        if (i == nums.length) { out.add(new ArrayList<>(cur)); return; }
        go(nums, i + 1, cur, out);
        cur.add(nums[i]);
        go(nums, i + 1, cur, out);
        cur.remove(cur.size() - 1);
    }
}
