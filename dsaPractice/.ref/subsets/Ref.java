import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("subsets", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("[1,2,3]", "[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]").example("[0]", "[[],[0]]")
            .gen(g -> new Object[]{ g.distinctInts(1, 8, -10, 10) })
            .patterns("Array", "Backtracking", "Bit Manipulation").limitMs(2000).maxN(10);
    }
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < 1 << nums.length; mask++) {
            List<Integer> s = new ArrayList<>();
            for (int i = 0; i < nums.length; i++) if ((mask >> i & 1) == 1) s.add(nums[i]);
            out.add(s);
        }
        return out;
    }
}
