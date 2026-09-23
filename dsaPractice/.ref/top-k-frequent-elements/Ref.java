import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("topKFrequent", T.INT_ARRAY, T.INT).returns(T.INT_ARRAY)
            .compare(Cmp.MULTISET)
            .example("[1,1,1,2,2,3], 2", "[1,2]")
            .example("[1], 1",           "[1]")
            .example("[4,4,-1,-1,-1,5], 1", "[-1]")
            .example("[1,2], 2",         "[1,2]")
            .gen(g -> {
                for (;;) {
                    int[] a = g.ints(1, 50, -6, 6);
                    Map<Integer, Integer> c = new HashMap<>();
                    for (int x : a) c.merge(x, 1, Integer::sum);
                    List<Integer> f = new ArrayList<>(c.values());
                    f.sort(Collections.reverseOrder());
                    int k = g.nextInt(1, f.size());
                    if (k == f.size() || f.get(k - 1) > f.get(k)) return new Object[]{ a, k };   // unique answer
                }
            })
            .patterns("Array", "Hash Table", "Heap (Priority Queue)", "Bucket Sort").limitMs(2000).maxN(200000);
    }
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> c = new HashMap<>();
        for (int x : nums) c.merge(x, 1, Integer::sum);
        Pre.check(k >= 1 && k <= c.size(), "1 <= k <= distinct values");
        List<Integer> f = new ArrayList<>(c.values());
        f.sort(Collections.reverseOrder());
        Pre.check(k == f.size() || f.get(k - 1) > f.get(k), "the answer is unique");
        List<Integer>[] bucket = new List[nums.length + 1];
        for (Map.Entry<Integer, Integer> e : c.entrySet()) {
            if (bucket[e.getValue()] == null) bucket[e.getValue()] = new ArrayList<>();
            bucket[e.getValue()].add(e.getKey());
        }
        int[] out = new int[k];
        int w = 0;
        for (int f2 = nums.length; f2 >= 1 && w < k; f2--) if (bucket[f2] != null) for (int v : bucket[f2]) if (w < k) out[w++] = v;
        return out;
    }
}
