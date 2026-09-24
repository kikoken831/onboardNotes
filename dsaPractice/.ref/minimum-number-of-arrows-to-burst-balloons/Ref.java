import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findMinArrowShots", T.INT_ARRAY_2D).returns(T.INT)
            .example("[[10,16],[2,8],[1,6],[7,12]]", "2").example("[[1,2],[3,4],[5,6],[7,8]]", "4")
            .example("[[1,2],[2,3],[3,4],[4,5]]", "2")                              // touching shares an arrow
            .example("[[-2147483646,-2147483645],[2147483646,2147483647]]", "2")   // a[1] - b[1] overflows
            .gen(g -> {
                if (g.chance(0.15)) return new Object[]{ new int[][]{ { Integer.MIN_VALUE, g.nextInt(-5, 5) }, { g.nextInt(-5, 5), Integer.MAX_VALUE }, { 0, 0 } } };
                int n = g.size(1, 25);
                int[][] a = new int[n][];
                for (int i = 0; i < n; i++) { int s = g.nextInt(0, 30); a[i] = new int[]{ s, s + g.nextInt(0, 8) }; }
                return new Object[]{ a };
            })
            .patterns("Array", "Greedy", "Sorting").limitMs(2000).maxN(100000);
    }
    public int findMinArrowShots(int[][] points) {
        int[][] a = points.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[1], y[1]));
        int arrows = 0;
        long at = Long.MIN_VALUE;
        for (int[] p : a) if (p[0] > at) { arrows++; at = p[1]; }
        return arrows;
    }
}
