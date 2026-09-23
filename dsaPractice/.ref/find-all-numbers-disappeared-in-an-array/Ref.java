import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findDisappearedNumbers", T.INT_ARRAY).returns(T.LIST_INT)
            .compare(Cmp.MULTISET)
            .example("[4,3,2,7,8,2,3,1]", "[5,6]")
            .example("[1,1]",             "[2]")
            .example("[1]",               "[]")
            .example("[2,2,2]",           "[1,3]")
            .gen(g -> { int n = g.size(1, 50); return new Object[]{ g.ints(n, 1, n) }; })
            .patterns("Array", "Hash Table").limitMs(2000).maxN(200000);
    }
    public List<Integer> findDisappearedNumbers(int[] nums) {
        int n = nums.length;
        boolean[] seen = new boolean[n + 1];
        for (int v : nums) { Pre.check(v >= 1 && v <= n, "1 <= nums[i] <= n"); seen[v] = true; }
        List<Integer> out = new ArrayList<>();
        for (int v = 1; v <= n; v++) if (!seen[v]) out.add(v);
        return out;
    }
}
