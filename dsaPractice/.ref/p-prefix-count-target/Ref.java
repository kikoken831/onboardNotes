import java.util.HashMap;
import java.util.Map;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("countSubarraysSumK", T.INT_ARRAY, T.INT).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[1,1,1], 2",   "2")
            .example("[1,2,3], 3",   "2")
            .example("[1], 0",       "0")
            .example("[0,0,0], 0",   "6")
            .example("[1,-1,0], 0",  "3")
            .example("[-1,-1,1], 0", "1")
            .gen(g -> new Object[]{ g.ints(1, 50, -4, 4), g.value(-8, 8) })
            .patterns("Array", "Hash Table", "Prefix Sum")
            .limitMs(2000)
            .maxN(100000);
    }

    public int countSubarraysSumK(int[] nums, int k) {
        Map<Integer, Integer> seen = new HashMap<>();
        seen.put(0, 1);
        int running = 0, count = 0;
        for (int x : nums) {
            running += x;
            count += seen.getOrDefault(running - k, 0);
            seen.merge(running, 1, Integer::sum);
        }
        return count;
    }
}
