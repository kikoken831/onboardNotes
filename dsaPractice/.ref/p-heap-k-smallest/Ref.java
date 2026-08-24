import java.util.Comparator;
import java.util.PriorityQueue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("kSmallest", T.INT_ARRAY, T.INT).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[3,1,4,1,5], 2", "[1,1]")
            .example("[3,1,4,1,5], 0", "[]")
            .example("[3,1,4], 9",     "[1,3,4]")
            .example("[], 3",          "[]")
            .example("[5], 1",         "[5]")
            .example("[2,2,2], 2",     "[2,2]")
            .gen(g -> {
                int[] nums = g.ints(0, 50, -20, 20);
                return new Object[]{ nums, g.nextInt(0, nums.length + 2) };
            })
            .patterns("Array", "Heap (Priority Queue)", "Sorting")
            .limitMs(3000)
            .maxN(200000);
    }

    public int[] kSmallest(int[] nums, int k) {
        if (k <= 0) return new int[0];
        PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.reverseOrder());
        for (int x : nums) {
            heap.offer(x);
            if (heap.size() > k) heap.poll();
        }
        int n = heap.size();
        int[] out = new int[n];
        for (int i = n - 1; i >= 0; i--) out[i] = heap.poll();   // max-heap: fill backwards
        return out;
    }
}
