class Ref implements Problem {
    public Spec spec() {
        return Spec.method("solveSudoku", T.CHAR_ARRAY_2D).returns(T.VOID).inPlace(0)
            // Validate the property: complete, valid, and every given digit unchanged.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    char[][] in = (char[][]) input[0], b = (char[][]) yours;
                    if (b == null || b.length != 9) return "the board must stay 9x9";
                    for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) {
                        if (b[i][j] < '1' || b[i][j] > '9') return "cell (" + i + "," + j + ") is '" + b[i][j] + "', not a digit";
                        if (in[i][j] != '.' && in[i][j] != b[i][j]) return "you changed the given digit at (" + i + "," + j + ")";
                    }
                    for (int k = 0; k < 9; k++) {
                        boolean[] r = new boolean[10], c = new boolean[10], s = new boolean[10];
                        for (int t = 0; t < 9; t++) {
                            if (r[b[k][t] - '0']) return "row " + k + " repeats " + b[k][t];
                            if (c[b[t][k] - '0']) return "column " + k + " repeats " + b[t][k];
                            char x = b[k / 3 * 3 + t / 3][k % 3 * 3 + t % 3];
                            if (s[x - '0']) return "box " + k + " repeats " + x;
                            r[b[k][t] - '0'] = c[b[t][k] - '0'] = s[x - '0'] = true;
                        }
                    }
                    return null;
                }
            })
            .exampleInPlace("[[\"5\",\"3\",\".\",\".\",\"7\",\".\",\".\",\".\",\".\"],[\"6\",\".\",\".\",\"1\",\"9\",\"5\",\".\",\".\",\".\"],[\".\",\"9\",\"8\",\".\",\".\",\".\",\".\",\"6\",\".\"],[\"8\",\".\",\".\",\".\",\"6\",\".\",\".\",\".\",\"3\"],[\"4\",\".\",\".\",\"8\",\".\",\"3\",\".\",\".\",\"1\"],[\"7\",\".\",\".\",\".\",\"2\",\".\",\".\",\".\",\"6\"],[\".\",\"6\",\".\",\".\",\".\",\".\",\"2\",\"8\",\".\"],[\".\",\".\",\".\",\"4\",\"1\",\"9\",\".\",\".\",\"5\"],[\".\",\".\",\".\",\".\",\"8\",\".\",\".\",\"7\",\"9\"]]",
                            "[[\"5\",\"3\",\"4\",\"6\",\"7\",\"8\",\"9\",\"1\",\"2\"],[\"6\",\"7\",\"2\",\"1\",\"9\",\"5\",\"3\",\"4\",\"8\"],[\"1\",\"9\",\"8\",\"3\",\"4\",\"2\",\"5\",\"6\",\"7\"],[\"8\",\"5\",\"9\",\"7\",\"6\",\"1\",\"4\",\"2\",\"3\"],[\"4\",\"2\",\"6\",\"8\",\"5\",\"3\",\"7\",\"9\",\"1\"],[\"7\",\"1\",\"3\",\"9\",\"2\",\"4\",\"8\",\"5\",\"6\"],[\"9\",\"6\",\"1\",\"5\",\"3\",\"7\",\"2\",\"8\",\"4\"],[\"2\",\"8\",\"7\",\"4\",\"1\",\"9\",\"6\",\"3\",\"5\"],[\"3\",\"4\",\"5\",\"2\",\"8\",\"6\",\"1\",\"7\",\"9\"]]")
            .gen(g -> new Object[]{ puzzle(g) })
            .patterns("Array", "Hash Table", "Backtracking", "Matrix").limitMs(4000).maxN(1);
    }

    /** A random valid solved grid (relabelled, row/column-shuffled base pattern) with cells removed. */
    static char[][] puzzle(Gen g) {
        int[] digit = g.permutation(9), band = g.permutation(3), stack = g.permutation(3);
        int[][] rowIn = new int[3][], colIn = new int[3][];
        for (int i = 0; i < 3; i++) { rowIn[i] = g.permutation(3); colIn[i] = g.permutation(3); }
        char[][] b = new char[9][9];
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            int R = band[r / 3] * 3 + rowIn[r / 3][r % 3], C = stack[c / 3] * 3 + colIn[c / 3][c % 3];
            b[r][c] = (char) ('1' + digit[(R * 3 + R / 3 + C) % 9]);
        }
        int holes = g.nextInt(0, 55);
        for (int k = 0; k < holes; k++) b[g.nextInt(0, 8)][g.nextInt(0, 8)] = '.';
        return b;
    }

    public void solveSudoku(char[][] board) {
        Pre.check(board.length == 9, "9x9");
        for (char[] r : board) Pre.check(r.length == 9, "9x9");
        int[] row = new int[9], col = new int[9], box = new int[9];
        for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) if (board[i][j] != '.') {
            int bit = 1 << (board[i][j] - '1');
            Pre.check(((row[i] | col[j] | box[i / 3 * 3 + j / 3]) & bit) == 0, "the givens are consistent");
            row[i] |= bit; col[j] |= bit; box[i / 3 * 3 + j / 3] |= bit;
        }
        Pre.check(solve(board, row, col, box), "the puzzle is solvable");
    }

    /** Backtracking on the most-constrained empty cell. */
    private boolean solve(char[][] b, int[] row, int[] col, int[] box) {
        int bi = -1, bj = -1, bestFree = 0, bestCount = 10;
        for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) if (b[i][j] == '.') {
            int free = ~(row[i] | col[j] | box[i / 3 * 3 + j / 3]) & 0x1FF, n = Integer.bitCount(free);
            if (n < bestCount) { bestCount = n; bi = i; bj = j; bestFree = free; }
        }
        if (bi < 0) return true;
        for (int f = bestFree; f != 0; f &= f - 1) {
            int bit = f & -f, k = bi / 3 * 3 + bj / 3;
            b[bi][bj] = (char) ('1' + Integer.numberOfTrailingZeros(bit));
            row[bi] |= bit; col[bj] |= bit; box[k] |= bit;
            if (solve(b, row, col, box)) return true;
            row[bi] ^= bit; col[bj] ^= bit; box[k] ^= bit;
        }
        b[bi][bj] = '.';
        return false;
    }
}
