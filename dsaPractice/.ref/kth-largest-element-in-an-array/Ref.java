import java.util.PriorityQueue;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findKthLargest", T.INT_ARRAY, T.INT).returns(T.INT)
            .example("[3,2,1,5,6,4], 2", "5").example("[3,2,3,1,2,4,5,5,6], 4", "4").example("[1], 1", "1").example("[2,2], 2", "2")
            .gen(g -> { int[] a = g.ints(1, 50, -20, 20); return new Object[]{ a, g.nextInt(1, a.length) }; })
            .patterns("Array", "Divide and Conquer", "Sorting", "Heap (Priority Queue)", "Quickselect").limitMs(2000).maxN(200000);
    }
    public int findKthLargest(int[] a, int k) {
        Pre.check(k >= 1 && k <= a.length, "1 <= k <= n");
        PriorityQueue<Integer> h = new PriorityQueue<>();
        for (int x : a) { h.add(x); if (h.size() > k) h.poll(); }
        return h.peek();
    }
}
