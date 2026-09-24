import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("islandPerimeter", T.INT_ARRAY_2D).returns(T.INT)
            .example("[[0,1,0,0],[1,1,1,0],[0,1,0,0],[1,1,0,0]]", "16")
            .example("[[1]]",         "4")
            .example("[[1,0]]",       "4")
            .example("[[1,1],[1,1]]", "8")
            .example("[[0,0,0],[0,1,0],[0,0,0]]", "4")
            .gen(Ref::island)
            .patterns("Array", "Depth-First Search", "Matrix")
            .limitMs(2000)
            .maxN(20000);
    }

    /** One 4-connected island, no lakes, grown by random walk. */
    private static Object[] island(Gen g) {
        int r = g.side(1, 12), c = g.side(1, 12);
        int[][] a = new int[r][c];
        int x = g.nextInt(0, r - 1), y = g.nextInt(0, c - 1);
        a[x][y] = 1;
        int steps = g.nextInt(0, r * c * 2);
        int[][] d = { {1,0},{-1,0},{0,1},{0,-1} };
        for (int s = 0; s < steps; s++) {
            int[] m = d[g.nextInt(0, 3)];
            int nx = x + m[0], ny = y + m[1];
            if (nx >= 0 && ny >= 0 && nx < r && ny < c) { x = nx; y = ny; a[x][y] = 1; }
        }
        fillLakes(a);
        return new Object[]{ a };
    }

    /** Water not reachable from outside the grid is a lake; LeetCode says there are none. */
    private static void fillLakes(int[][] a) {
        int r = a.length, c = a[0].length;
        boolean[][] sea = new boolean[r][c];
        Deque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++)
            if ((i == 0 || j == 0 || i == r - 1 || j == c - 1) && a[i][j] == 0) { sea[i][j] = true; q.add(new int[]{i, j}); }
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int[] m : new int[][]{ {1,0},{-1,0},{0,1},{0,-1} }) {
                int i = p[0] + m[0], j = p[1] + m[1];
                if (i >= 0 && j >= 0 && i < r && j < c && a[i][j] == 0 && !sea[i][j]) { sea[i][j] = true; q.add(new int[]{i, j}); }
            }
        }
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) if (a[i][j] == 0 && !sea[i][j]) a[i][j] = 1;
    }

    private static boolean oneIslandNoLakes(int[][] a) {
        int r = a.length, c = a[0].length, land = 0, si = -1, sj = -1;
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) if (a[i][j] == 1) { land++; si = i; sj = j; }
        if (land == 0) return false;
        boolean[][] seen = new boolean[r][c];
        Deque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{si, sj}); seen[si][sj] = true;
        int reached = 0;
        while (!q.isEmpty()) {
            int[] p = q.poll(); reached++;
            for (int[] m : new int[][]{ {1,0},{-1,0},{0,1},{0,-1} }) {
                int i = p[0] + m[0], j = p[1] + m[1];
                if (i >= 0 && j >= 0 && i < r && j < c && a[i][j] == 1 && !seen[i][j]) { seen[i][j] = true; q.add(new int[]{i, j}); }
            }
        }
        if (reached != land) return false;
        int[][] b = new int[r][];
        for (int i = 0; i < r; i++) b[i] = a[i].clone();
        fillLakes(b);
        for (int i = 0; i < r; i++) if (!java.util.Arrays.equals(a[i], b[i])) return false;
        return true;
    }

    public int islandPerimeter(int[][] grid) {
        Pre.check(grid.length >= 1 && grid[0].length >= 1, "non-empty grid");
        for (int[] row : grid) for (int v : row) Pre.check(v == 0 || v == 1, "cells are 0 or 1");
        Pre.check(oneIslandNoLakes(grid), "exactly one island, no lakes");
        int land = 0, shared = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != 1) continue;
                land++;
                if (i + 1 < grid.length && grid[i + 1][j] == 1) shared++;
                if (j + 1 < grid[0].length && grid[i][j + 1] == 1) shared++;
            }
        }
        return 4 * land - 2 * shared;
    }
}
