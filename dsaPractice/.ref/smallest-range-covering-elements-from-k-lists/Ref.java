import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("smallestRange", T.LIST_LIST_INT).returns(T.INT_ARRAY)
            .example("[[4,10,15,24,26],[0,9,12,20],[5,18,22,30]]", "[20,24]").example("[[1,2,3],[1,2,3],[1,2,3]]", "[1,1]")
            .example("[[1],[5]]", "[1,5]").example("[[1,5],[3]]", "[1,3]")
            .gen(g -> {
                int k = g.size(1, 5);
                List<List<Integer>> ls = new ArrayList<>();
                for (int i = 0; i < k; i++) { List<Integer> l = new ArrayList<>(); for (int x : g.sortedInts(1, 6, -10, 10)) l.add(x); ls.add(l); }
                return new Object[]{ ls };
            })
            .patterns("Array", "Hash Table", "Greedy", "Sliding Window", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(20000);
    }
    public int[] smallestRange(List<List<Integer>> nums) {
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(nums.get(a[0]).get(a[1]), nums.get(b[0]).get(b[1])));
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < nums.size(); i++) { Pre.check(!nums.get(i).isEmpty(), "non-empty lists"); pq.add(new int[]{ i, 0 }); max = Math.max(max, nums.get(i).get(0)); }
        int bestLo = 0, bestHi = Integer.MAX_VALUE;
        boolean first = true;
        while (true) {
            int[] c = pq.poll();
            int lo = nums.get(c[0]).get(c[1]);
            if (first || (long) max - lo < (long) bestHi - bestLo) { bestLo = lo; bestHi = max; first = false; }
            if (c[1] + 1 == nums.get(c[0]).size()) break;           // this list is exhausted: no range can include it further
            int next = nums.get(c[0]).get(c[1] + 1);
            max = Math.max(max, next);
            pq.add(new int[]{ c[0], c[1] + 1 });
        }
        return new int[]{ bestLo, bestHi };
    }
}
