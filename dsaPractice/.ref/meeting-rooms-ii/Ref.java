import java.util.Arrays;
import java.util.PriorityQueue;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("minMeetingRooms", T.INT_ARRAY_2D).returns(T.INT)
            .example("[[0,30],[5,10],[15,20]]", "2").example("[[7,10],[2,4]]", "1").example("[[1,5],[5,10]]", "1")
            .example("[[1,10],[2,3],[4,5],[6,7]]", "2").example("[[1,2],[1,2],[1,2]]", "3")
            .gen(g -> {
                int n = g.size(1, 20);
                int[][] a = new int[n][];
                for (int i = 0; i < n; i++) { int s = g.nextInt(0, 40); a[i] = new int[]{ s, s + g.nextInt(1, 10) }; }
                return new Object[]{ a };
            })
            .patterns("Array", "Two Pointers", "Greedy", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(100000);
    }
    public int minMeetingRooms(int[][] intervals) {
        Pre.check(intervals.length >= 1, "1 <= intervals.length");
        for (int[] m : intervals) Pre.check(m.length == 2 && 0 <= m[0] && m[0] < m[1], "0 <= start < end");
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        PriorityQueue<Integer> ends = new PriorityQueue<>();
        for (int[] m : a) {
            if (!ends.isEmpty() && ends.peek() <= m[0]) ends.poll();          // reuse the room freed earliest
            ends.add(m[1]);
        }
        return ends.size();
    }
}
