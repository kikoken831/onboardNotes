import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.design("MedianFinder")
            .op("addNum", T.VOID, T.INT).op("findMedian", T.DOUBLE)
            .exampleCalls("[\"MedianFinder\",\"addNum\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
                          "[[],[1],[2],[],[3],[]]", "[null,null,null,1.5,null,2.0]")
            .exampleCalls("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
                          "[[],[-100000],[],[100000],[]]", "[null,null,-100000.0,null,0.0]")
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("MedianFinder");
                int n = g.size(1, 60), added = 0;
                for (int i = 0; i < n; i++) {
                    if (added == 0 || g.chance(0.6)) { b.call("addNum", g.nextInt(-20, 20)); added++; }
                    else b.call("findMedian");
                }
                return new Object[]{ b.build() };
            })
            .patterns("Two Pointers", "Design", "Sorting", "Heap (Priority Queue)", "Data Stream").limitMs(2000).maxN(50000);
    }
}

class MedianFinder {
    private final PriorityQueue<Integer> lo = new PriorityQueue<>(Collections.reverseOrder()), hi = new PriorityQueue<>();
    public MedianFinder() {}
    public void addNum(int x) {
        lo.add(x);
        hi.add(lo.poll());                                   // keep every lo <= every hi
        if (hi.size() > lo.size()) lo.add(hi.poll());        // lo holds the extra one
    }
    public double findMedian() {
        Pre.check(!lo.isEmpty(), "at least one number added");
        return lo.size() > hi.size() ? lo.peek() : ((long) lo.peek() + hi.peek()) / 2.0;
    }
}
