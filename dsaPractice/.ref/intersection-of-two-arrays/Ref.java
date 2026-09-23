import java.util.HashSet;
import java.util.Set;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("intersection", T.INT_ARRAY, T.INT_ARRAY).returns(T.INT_ARRAY)
            .compare(Cmp.MULTISET)                  // any order; each value once
            .example("[1,2,2,1], [2,2]",       "[2]")
            .example("[4,9,5], [9,4,9,8,4]",   "[9,4]")
            .example("[1,2], [3,4]",           "[]")
            .example("[7,7,7], [7]",           "[7]")
            .gen(g -> new Object[]{ g.ints(1, 40, 0, 12), g.ints(1, 40, 0, 12) })
            .patterns("Array", "Hash Table", "Sorting")
            .limitMs(2000)
            .maxN(100000);
    }

    public int[] intersection(int[] nums1, int[] nums2) {
        Pre.check(nums1.length >= 1 && nums2.length >= 1, "1 <= lengths");
        Set<Integer> a = new HashSet<>();
        for (int x : nums1) a.add(x);
        Set<Integer> out = new HashSet<>();
        for (int x : nums2) if (a.contains(x)) out.add(x);
        int[] r = new int[out.size()];
        int i = 0;
        for (int x : out) r[i++] = x;
        return r;
    }
}
