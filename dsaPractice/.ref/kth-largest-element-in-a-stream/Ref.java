import java.util.PriorityQueue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.design("KthLargest", T.INT, T.INT_ARRAY)
            .op("add", T.INT, T.INT)
            .exampleCalls("[\"KthLargest\",\"add\",\"add\",\"add\",\"add\",\"add\"]",
                          "[[3,[4,5,8,2]],[3],[5],[10],[9],[4]]",
                          "[null,4,5,5,8,8]")
            .exampleCalls("[\"KthLargest\",\"add\",\"add\",\"add\",\"add\"]",
                          "[[4,[7,7,7,7,8,3]],[2],[10],[9],[9]]",
                          "[null,7,7,7,8]")
            .exampleCalls("[\"KthLargest\",\"add\",\"add\"]",
                          "[[1,[]],[-3],[-2]]",
                          "[null,-3,-2]")               // starts with fewer than k values
            .gen(g -> {
                int m = g.size(0, 20);
                int k = g.nextInt(1, m + 1);
                int[] nums = g.ints(m, -10, 10);
                Calls.Builder b = new Calls.Builder("KthLargest", k, nums);
                int n = g.size(1, 40);
                for (int i = 0; i < n; i++) b.call("add", g.nextInt(-10, 10));
                return new Object[]{ b.build() };
            })
            .patterns("Design", "Heap (Priority Queue)", "Data Stream")
            .limitMs(2000)
            .maxN(50000);
    }
}

class KthLargest {
    private final int k;
    private final PriorityQueue<Integer> heap = new PriorityQueue<>();   // min-heap of the k largest

    public KthLargest(int k, int[] nums) {
        Pre.check(k >= 1 && k <= nums.length + 1, "1 <= k <= nums.length + 1");
        this.k = k;
        for (int x : nums) offer(x);
    }

    public int add(int val) {
        offer(val);
        return heap.peek();
    }

    private void offer(int x) {
        heap.add(x);
        if (heap.size() > k) heap.poll();
    }
}
