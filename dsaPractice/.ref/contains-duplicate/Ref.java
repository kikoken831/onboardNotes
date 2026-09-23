import java.util.HashSet;
import java.util.Set;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("containsDuplicate", T.INT_ARRAY).returns(T.BOOL)
            .example("[1,2,3,1]",             "true")
            .example("[1,2,3,4]",             "false")
            .example("[1,1,1,3,3,4,3,2,4,2]", "true")
            .example("[7]",                   "false")
            .gen(g -> new Object[]{ g.chance(0.5) ? g.distinctInts(1, 40, -30, 30) : g.ints(1, 40, -30, 30) })
            .patterns("Array", "Hash Table", "Sorting").limitMs(2000).maxN(200000);
    }
    public boolean containsDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int x : nums) if (!seen.add(x)) return true;
        return false;
    }
}
