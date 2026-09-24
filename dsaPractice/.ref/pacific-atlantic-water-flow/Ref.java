import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("pacificAtlantic", T.INT_ARRAY_2D).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SEQS)
            .example("[[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]", "[[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]")
            .example("[[1]]", "[[0,0]]").example("[[2,1],[1,2]]", "[[0,0],[0,1],[1,0],[1,1]]")
            .gen(g -> new Object[]{ g.grid(1, 8, 1, 8, 0, 5) })
            .patterns("Array", "Depth-First Search", "Breadth-First Search", "Matrix").limitMs(2000).maxN(20000);
    }
    public List<List<Integer>> pacificAtlantic(int[][] h) {
        int R = h.length, C = h[0].length;
        boolean[][] pac = new boolean[R][C], atl = new boolean[R][C];
        Deque<int[]> p = new ArrayDeque<>(), a = new ArrayDeque<>();
        for (int i = 0; i < R; i++) { mark(pac, p, i, 0); mark(atl, a, i, C - 1); }
        for (int j = 0; j < C; j++) { mark(pac, p, 0, j); mark(atl, a, R - 1, j); }
        climb(h, pac, p); climb(h, atl, a);
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) if (pac[i][j] && atl[i][j]) out.add(Arrays.asList(i, j));
        return out;
    }
    private static void mark(boolean[][] s, Deque<int[]> q, int i, int j) { if (!s[i][j]) { s[i][j] = true; q.add(new int[]{ i, j }); } }
    /** Water flows downhill, so search UPHILL from each ocean's shore. */
    private static void climb(int[][] h, boolean[][] s, Deque<int[]> q) {
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int[] d : new int[][]{ {1,0},{-1,0},{0,1},{0,-1} }) {
                int i = c[0] + d[0], j = c[1] + d[1];
                if (i >= 0 && j >= 0 && i < h.length && j < h[0].length && !s[i][j] && h[i][j] >= h[c[0]][c[1]]) { s[i][j] = true; q.add(new int[]{ i, j }); }
            }
        }
    }
}
