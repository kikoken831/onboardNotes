import java.util.ArrayDeque;
import java.util.Queue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("shortestPath", T.INT_ARRAY_2D).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[[0,0],[1,0]]",             "3")
            .example("[[0,1],[1,0]]",             "-1")
            .example("[[0]]",                     "1")
            .example("[[1]]",                     "-1")
            .example("[[0,0,0],[1,1,0],[0,0,0]]", "5")
            .gen(g -> {
                // Bias toward open cells so a path exists reasonably often; a grid of
                // uniform 0/1 is unreachable most of the time and only tests -1.
                int[][] grid = g.grid(1, 12, 1, 12, 0, 1);
                for (int i = 0; i < grid.length; i++) {
                    for (int j = 0; j < grid[0].length; j++) {
                        if (grid[i][j] == 1 && g.chance(0.45)) grid[i][j] = 0;
                    }
                }
                return new Object[]{ grid };
            })
            .patterns("Breadth-First Search", "Matrix")
            .limitMs(2000)
            .maxN(200);
    }

    private static final int[][] DIRS = {{1,0},{-1,0},{0,1},{0,-1}};

    public int shortestPath(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        if (grid[0][0] != 0 || grid[rows-1][cols-1] != 0) return -1;

        boolean[][] seen = new boolean[rows][cols];
        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{0, 0});
        seen[0][0] = true;

        int dist = 1;
        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                int[] cell = q.poll();
                if (cell[0] == rows - 1 && cell[1] == cols - 1) return dist;
                for (int[] d : DIRS) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                    if (seen[nr][nc] || grid[nr][nc] != 0) continue;
                    seen[nr][nc] = true;          // mark at ENQUEUE time
                    q.add(new int[]{nr, nc});
                }
            }
            dist++;
        }
        return -1;
    }
}
