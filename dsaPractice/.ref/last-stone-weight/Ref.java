import java.util.Collections;
import java.util.PriorityQueue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("lastStoneWeight", T.INT_ARRAY).returns(T.INT)
            .example("[2,7,4,1,8,1]", "1")
            .example("[1]",           "1")
            .example("[2,2]",         "0")
            .example("[3,1]",         "2")
            .gen(g -> new Object[]{ g.ints(1, 30, 1, 1000) })
            .patterns("Array", "Heap (Priority Queue)")
            .limitMs(2000)
            .maxN(50000);
    }

    public int lastStoneWeight(int[] stones) {
        Pre.length(stones, 1, 1000_000, "stones");
        PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
        for (int s : stones) pq.add(s);
        while (pq.size() > 1) {
            int y = pq.poll(), x = pq.poll();
            if (y != x) pq.add(y - x);
        }
        return pq.isEmpty() ? 0 : pq.peek();
    }
}
