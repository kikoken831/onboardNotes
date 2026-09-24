import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("solveNQueens", T.INT).returns(T.LIST_LIST_STRING)
            .compare(Cmp.SET_OF_SEQS)
            .example("4", "[[\".Q..\",\"...Q\",\"Q...\",\"..Q.\"],[\"..Q.\",\"Q...\",\"...Q\",\".Q..\"]]").example("1", "[[\"Q\"]]")
            .example("2", "[]").example("3", "[]")
            .gen(g -> new Object[]{ g.nextInt(1, 8) })
            .patterns("Array", "Backtracking").limitMs(2000).maxN(9);
    }
    public List<List<String>> solveNQueens(int n) {
        Pre.check(n >= 1 && n <= 9, "1 <= n <= 9");
        List<List<String>> out = new ArrayList<>();
        go(0, n, new int[n], new boolean[n], new boolean[2 * n], new boolean[2 * n], out);
        return out;
    }
    private void go(int r, int n, int[] q, boolean[] col, boolean[] d1, boolean[] d2, List<List<String>> out) {
        if (r == n) {
            List<String> b = new ArrayList<>();
            for (int c : q) { char[] row = new char[n]; Arrays.fill(row, '.'); row[c] = 'Q'; b.add(new String(row)); }
            out.add(b); return;
        }
        for (int c = 0; c < n; c++) {
            if (col[c] || d1[r + c] || d2[r - c + n]) continue;
            col[c] = d1[r + c] = d2[r - c + n] = true; q[r] = c;
            go(r + 1, n, q, col, d1, d2, out);
            col[c] = d1[r + c] = d2[r - c + n] = false;
        }
    }
}
