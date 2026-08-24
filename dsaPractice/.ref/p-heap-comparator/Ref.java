import java.util.Arrays;
import java.util.PriorityQueue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("kClosest", T.INT_ARRAY_2D, T.INT).returns(T.INT_ARRAY_2D)
            .compare(Cmp.EXACT)
            .example("[[1,3],[-2,2]], 1",        "[[-2,2]]")
            .example("[[3,3],[5,-1],[-2,4]], 2", "[[3,3],[-2,4]]")
            .example("[], 0",                    "[]")
            .example("[[0,0]], 1",               "[[0,0]]")
            .example("[[1,0],[0,1]], 2",         "[[0,1],[1,0]]")
            .gen(g -> {
                int n = g.size(0, 40);
                int[][] pts = new int[n][2];
                for (int i = 0; i < n; i++) {
                    pts[i][0] = g.value(-6, 6);   // narrow -> distance ties are common
                    pts[i][1] = g.value(-6, 6);
                }
                return new Object[]{ pts, g.nextInt(0, n) };
            })
            .patterns("Array", "Heap (Priority Queue)", "Sorting", "Geometry")
            .limitMs(3000)
            .maxN(50000);
    }

    private static long dist(int[] p) {
        return (long) p[0] * p[0] + (long) p[1] * p[1];
    }

    private static int order(int[] a, int[] b) {
        int c = Long.compare(dist(a), dist(b));
        if (c != 0) return c;
        c = Integer.compare(a[0], b[0]);
        if (c != 0) return c;
        return Integer.compare(a[1], b[1]);
    }

    public int[][] kClosest(int[][] points, int k) {
        if (k <= 0) return new int[0][];
        PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> order(b, a));
        for (int[] p : points) {
            heap.offer(p);
            if (heap.size() > k) heap.poll();
        }
        int[][] out = heap.toArray(new int[0][]);
        Arrays.sort(out, (a, b) -> order(a, b));
        return out;
    }
}
