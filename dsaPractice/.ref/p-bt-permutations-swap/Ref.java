import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("permutations", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            // Order of the permutations is free; order WITHIN one is the answer.
            .compare(Cmp.SET_OF_SEQS)
            .example("[1,2,3]", "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]")
            .example("[]",      "[[]]")
            .example("[1]",     "[[1]]")
            .example("[1,2]",   "[[1,2],[2,1]]")
            .gen(g -> new Object[]{ g.strictlySortedInts(0, 6, -15, 15) })
            .patterns("Array", "Backtracking")
            .limitMs(3000)
            .maxN(7);
    }

    public List<List<Integer>> permutations(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        go(nums.clone(), 0, out);
        return out;
    }

    private void go(int[] nums, int i, List<List<Integer>> out) {
        if (i == nums.length) {
            List<Integer> one = new ArrayList<>(nums.length);
            for (int v : nums) one.add(v);
            out.add(one);
            return;
        }
        for (int j = i; j < nums.length; j++) {
            swap(nums, i, j);
            go(nums, i + 1, out);
            swap(nums, i, j);
        }
    }

    private void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
}
