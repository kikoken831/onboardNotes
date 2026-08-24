import java.util.ArrayDeque;
import java.util.Queue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("countEnqueues", T.INT_ARRAY_2D).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[[0,0],[0,0]]",     "4")
            .example("[[0,1],[1,0]]",     "1")
            .example("[[0]]",             "1")
            .example("[[1]]",             "0")
            .example("[[0,0,0],[0,0,0]]", "6")
            .gen(g -> {
                int[][] grid = g.grid(1, 12, 1, 12, 0, 1);
                for (int i = 0; i < grid.length; i++) {
                    for (int j = 0; j < grid[0].length; j++) {
                        if (grid[i][j] == 1 && g.chance(0.5)) grid[i][j] = 0;
                    }
                }
                return new Object[]{ grid };
            })
            .patterns("Breadth-First Search", "Matrix")
            .limitMs(2000)
            .maxN(200);
    }

    private static final int[][] DIRS = {{1,0},{-1,0},{0,1},{0,-1}};

    public int countEnqueues(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        if (grid[0][0] != 0) return 0;

        boolean[][] seen = new boolean[rows][cols];
        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{0, 0});
        seen[0][0] = true;
        int enqueues = 1;

        while (!q.isEmpty()) {
            int[] cell = q.poll();
            for (int[] d : DIRS) {
                int nr = cell[0] + d[0], nc = cell[1] + d[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (seen[nr][nc] || grid[nr][nc] != 0) continue;
                seen[nr][nc] = true;
                q.add(new int[]{nr, nc});
                enqueues++;
            }
        }
        return enqueues;
    }
}
