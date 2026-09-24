import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("insert", T.INT_ARRAY_2D, T.INT_ARRAY).returns(T.INT_ARRAY_2D)
            .example("[[1,3],[6,9]], [2,5]", "[[1,5],[6,9]]")
            .example("[[1,2],[3,5],[6,7],[8,10],[12,16]], [4,8]", "[[1,2],[3,10],[12,16]]")
            .example("[], [5,7]", "[[5,7]]").example("[[1,5]], [6,8]", "[[1,5],[6,8]]").example("[[3,5]], [1,2]", "[[1,2],[3,5]]")
            .example("[[1,5]], [5,7]", "[[1,7]]")
            .gen(g -> {
                int n = g.size(0, 12), x = 0;
                int[][] a = new int[n][];
                for (int i = 0; i < n; i++) { x += g.nextInt(1, 4); int e = x + g.nextInt(0, 3); a[i] = new int[]{ x, e }; x = e; }
                int s = g.nextInt(0, x + 3);
                return new Object[]{ a, new int[]{ s, s + g.nextInt(0, 6) } };
            })
            .patterns("Array").limitMs(2000).maxN(100000);
    }
    public int[][] insert(int[][] intervals, int[] nw) {
        Pre.check(nw.length == 2 && nw[0] <= nw[1], "newInterval start <= end");
        for (int[] iv : intervals) Pre.check(iv.length == 2 && iv[0] <= iv[1], "start_i <= end_i");
        for (int i = 1; i < intervals.length; i++) Pre.check(intervals[i][0] > intervals[i - 1][1], "sorted, non-overlapping");
        List<int[]> out = new ArrayList<>();
        int s = nw[0], e = nw[1], i = 0, n = intervals.length;
        while (i < n && intervals[i][1] < s) out.add(intervals[i++]);
        while (i < n && intervals[i][0] <= e) { s = Math.min(s, intervals[i][0]); e = Math.max(e, intervals[i][1]); i++; }
        out.add(new int[]{ s, e });
        while (i < n) out.add(intervals[i++]);
        return out.toArray(new int[0][]);
    }
}
