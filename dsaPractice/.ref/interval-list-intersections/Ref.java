import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("intervalIntersection", T.INT_ARRAY_2D, T.INT_ARRAY_2D).returns(T.INT_ARRAY_2D)
            .example("[[0,2],[5,10],[13,23],[24,25]], [[1,5],[8,12],[15,24],[25,26]]", "[[1,2],[5,5],[8,10],[15,23],[24,24],[25,25]]")
            .example("[[1,3],[5,9]], []", "[]").example("[[1,7]], [[3,10]]", "[[3,7]]").example("[[3,5]], [[5,9]]", "[[5,5]]")
            .gen(g -> new Object[]{ disjoint(g), disjoint(g) })
            .patterns("Array", "Two Pointers", "Sweep Line").limitMs(2000).maxN(100000);
    }
    static int[][] disjoint(Gen g) {
        int n = g.size(0, 12), x = -1;
        int[][] a = new int[n][];
        for (int i = 0; i < n; i++) { x += g.nextInt(1, 4); int e = x + g.nextInt(1, 4); a[i] = new int[]{ x, e }; x = e; }
        return a;
    }
    public int[][] intervalIntersection(int[][] A, int[][] B) {
        for (int[][] L : new int[][][]{ A, B }) for (int[] iv : L) Pre.check(iv.length == 2 && iv[0] < iv[1], "start_i < end_i");
        for (int[][] L : new int[][][]{ A, B }) for (int i = 1; i < L.length; i++) Pre.check(L[i][0] > L[i - 1][1], "sorted, disjoint");
        List<int[]> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < A.length && j < B.length) {
            int lo = Math.max(A[i][0], B[j][0]), hi = Math.min(A[i][1], B[j][1]);
            if (lo <= hi) out.add(new int[]{ lo, hi });
            if (A[i][1] < B[j][1]) i++; else j++;
        }
        return out.toArray(new int[0][]);
    }
}
