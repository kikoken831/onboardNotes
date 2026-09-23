import java.util.HashSet;
import java.util.Set;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("longestConsecutive", T.INT_ARRAY).returns(T.INT)
            .example("[100,4,200,1,3,2]",     "4")
            .example("[0,3,7,2,5,8,4,6,0,1]", "9")
            .example("[]",                    "0")
            .example("[1,0,1,2]",             "3")
            .example("[-1,-2,2147483647,-2147483648]", "2")
            .gen(g -> new Object[]{ g.ints(0, 50, -15, 15) })
            .patterns("Array", "Hash Table", "Union-Find").limitMs(2000).maxN(200000);
    }
    public int longestConsecutive(int[] nums) {
        Set<Integer> s = new HashSet<>();
        for (int x : nums) s.add(x);
        int best = 0;
        for (int x : s) {
            if (x != Integer.MIN_VALUE && s.contains(x - 1)) continue;   // not a run start
            int len = 1;
            long y = x;
            while (y < Integer.MAX_VALUE && s.contains((int) (y + 1))) { y++; len++; }
            best = Math.max(best, len);
        }
        return best;
    }
}
