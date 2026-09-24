import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("kClosest", T.INT_ARRAY_2D, T.INT).returns(T.INT_ARRAY_2D)
            .compare(Cmp.SET_OF_SEQS)
            .example("[[1,3],[-2,2]], 1", "[[-2,2]]").example("[[3,3],[5,-1],[-2,4]], 2", "[[3,3],[-2,4]]").example("[[0,1]], 1", "[[0,1]]")
            .gen(g -> {
                for (;;) {
                    int n = g.size(1, 20);
                    int[][] p = new int[n][];
                    for (int i = 0; i < n; i++) p[i] = new int[]{ g.nextInt(-10, 10), g.nextInt(-10, 10) };
                    int k = g.nextInt(1, n);
                    long[] d = new long[n];
                    for (int i = 0; i < n; i++) d[i] = (long) p[i][0] * p[i][0] + (long) p[i][1] * p[i][1];
                    long[] s = d.clone(); Arrays.sort(s);
                    if (k == n || s[k - 1] < s[k]) return new Object[]{ p, k };       // LeetCode: the answer is unique
                }
            })
            .patterns("Array", "Math", "Divide and Conquer", "Geometry", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(200000);
    }
    public int[][] kClosest(int[][] points, int k) {
        Pre.check(1 <= k && k <= points.length, "1 <= k <= points.length");
        long[] s = new long[points.length];
        for (int i = 0; i < points.length; i++) s[i] = d(points[i]);
        Arrays.sort(s);
        Pre.check(k == points.length || s[k - 1] < s[k], "the answer is unique");
        PriorityQueue<int[]> far = new PriorityQueue<>((a, b) -> Long.compare(d(b), d(a)));   // max-heap of the k closest
        for (int[] p : points) { far.add(p); if (far.size() > k) far.poll(); }
        return far.toArray(new int[0][]);
    }
    private static long d(int[] p) { return (long) p[0] * p[0] + (long) p[1] * p[1]; }
}
