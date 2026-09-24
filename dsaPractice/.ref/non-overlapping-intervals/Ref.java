import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("eraseOverlapIntervals", T.INT_ARRAY_2D).returns(T.INT)
            .example("[[1,2],[2,3],[3,4],[1,3]]", "1").example("[[1,2],[1,2],[1,2]]", "2").example("[[1,2],[2,3]]", "0")
            .example("[[1,100],[11,22],[1,11],[2,12]]", "2")
            .gen(g -> {
                int n = g.size(1, 25);
                int[][] a = new int[n][];
                for (int i = 0; i < n; i++) { int s = g.nextInt(0, 30); a[i] = new int[]{ s, s + g.nextInt(1, 8) }; }
                return new Object[]{ a };
            })
            .patterns("Array", "Dynamic Programming", "Greedy", "Sorting").limitMs(2000).maxN(100000);
    }
    public int eraseOverlapIntervals(int[][] intervals) {
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[1], y[1]));
        int kept = 0;
        long end = Long.MIN_VALUE;
        for (int[] iv : a) if (iv[0] >= end) { kept++; end = iv[1]; }
        return a.length - kept;
    }
}
