import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findDuplicates", T.INT_ARRAY).returns(T.LIST_INT)
            .compare(Cmp.MULTISET)
            .example("[4,3,2,7,8,2,3,1]", "[2,3]")
            .example("[1,1,2]",           "[1]")
            .example("[1]",               "[]")
            .gen(g -> {
                int n = g.size(1, 50);
                int[] a = new int[n];
                int[] perm = g.permutation(n);
                int w = 0;
                while (w < n) {                           // each value at most twice
                    int v = perm[w % n] + 1;
                    a[w++] = v;
                    if (w < n && g.chance(0.3)) a[w++] = v;
                }
                for (int i = n - 1; i > 0; i--) { int j = g.nextInt(0, i); int t = a[i]; a[i] = a[j]; a[j] = t; }
                return new Object[]{ a };
            })
            .patterns("Array", "Hash Table").limitMs(2000).maxN(200000);
    }
    public List<Integer> findDuplicates(int[] nums) {
        int n = nums.length;
        int[] c = new int[n + 1];
        for (int v : nums) { Pre.check(v >= 1 && v <= n, "1 <= nums[i] <= n"); Pre.check(++c[v] <= 2, "each value at most twice"); }
        List<Integer> out = new ArrayList<>();
        for (int v = 1; v <= n; v++) if (c[v] == 2) out.add(v);
        return out;
    }
}
