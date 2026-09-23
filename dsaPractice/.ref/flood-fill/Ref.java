import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("floodFill", T.INT_ARRAY_2D, T.INT, T.INT, T.INT).returns(T.INT_ARRAY_2D)
            .example("[[1,1,1],[1,1,0],[1,0,1]], 1, 1, 2", "[[2,2,2],[2,2,0],[2,0,1]]")
            .example("[[0,0,0],[0,0,0]], 0, 0, 0",         "[[0,0,0],[0,0,0]]")   // same colour
            .example("[[0,0,0],[0,1,1]], 1, 1, 1",         "[[0,0,0],[0,1,1]]")
            .example("[[1]], 0, 0, 5",                     "[[5]]")
            .example("[[1,2],[2,1]], 0, 0, 3",             "[[3,2],[2,1]]")   // no diagonal spread
            .gen(g -> {
                int[][] img = g.grid(1, 10, 1, 10, 0, 2);
                return new Object[]{ img, g.nextInt(0, img.length - 1), g.nextInt(0, img[0].length - 1), g.nextInt(0, 2) };
            })
            .patterns("Array", "Depth-First Search", "Breadth-First Search", "Matrix")
            .limitMs(2000)
            .maxN(20000);
    }

    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        Pre.check(image.length >= 1 && image[0].length >= 1, "non-empty image");
        Pre.check(sr >= 0 && sr < image.length && sc >= 0 && sc < image[0].length, "start in bounds");
        int[][] a = new int[image.length][];
        for (int i = 0; i < a.length; i++) a[i] = image[i].clone();
        int from = a[sr][sc];
        if (from == color) return a;
        Deque<int[]> q = new ArrayDeque<>();
        a[sr][sc] = color;                    // mark on enqueue
        q.add(new int[]{sr, sc});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int[] d : new int[][]{ {1,0},{-1,0},{0,1},{0,-1} }) {
                int i = p[0] + d[0], j = p[1] + d[1];
                if (i >= 0 && j >= 0 && i < a.length && j < a[0].length && a[i][j] == from) {
                    a[i][j] = color;
                    q.add(new int[]{i, j});
                }
            }
        }
        return a;
    }
}
