import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findClosestElements", T.INT_ARRAY, T.INT, T.INT).returns(T.LIST_INT)
            .example("[1,2,3,4,5], 4, 3", "[1,2,3,4]").example("[1,1,2,3,4,5], 4, -1", "[1,1,2,3]")
            .example("[1,2,3,4,5], 4, -1", "[1,2,3,4]").example("[0,0,1,2,3,3,4,7,7,8], 3, 5", "[3,3,4]")
            .example("[1,3], 1, 2", "[1]")                         // tie goes to the smaller value
            .gen(g -> { int[] a = g.sortedInts(1, 40, -10, 10); return new Object[]{ a, g.nextInt(1, a.length), g.nextInt(-14, 14) }; })
            .patterns("Array", "Two Pointers", "Binary Search", "Sliding Window").limitMs(2000).maxN(200000);
    }
    public List<Integer> findClosestElements(int[] arr, int k, int x) {
        Pre.check(k >= 1 && k <= arr.length, "1 <= k <= n");
        Pre.sorted(arr, false, "arr");
        int lo = 0, hi = arr.length - k;                 // window start
        while (lo < hi) {
            int m = (lo + hi) / 2;
            if (x - arr[m] > arr[m + k] - x) lo = m + 1; else hi = m;
        }
        List<Integer> out = new ArrayList<>();
        for (int i = lo; i < lo + k; i++) out.add(arr[i]);
        return out;
    }
}
