import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("merge", T.INT_ARRAY_2D).returns(T.INT_ARRAY_2D)
            .compare(Cmp.SET_OF_SEQS)
            .example("[[1,3],[2,6],[8,10],[15,18]]", "[[1,6],[8,10],[15,18]]").example("[[1,4],[4,5]]", "[[1,5]]")   // touching merges
            .example("[[1,4],[2,3]]", "[[1,4]]").example("[[4,7],[1,4]]", "[[1,7]]").example("[[1,4]]", "[[1,4]]")
            .gen(g -> {
                int n = g.size(1, 25);
                int[][] a = new int[n][];
                for (int i = 0; i < n; i++) { int s = g.nextInt(0, 30); a[i] = new int[]{ s, s + g.nextInt(0, 8) }; }
                return new Object[]{ a };
            })
            .patterns("Array", "Sorting").limitMs(2000).maxN(100000);
    }
    public int[][] merge(int[][] intervals) {
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        List<int[]> out = new ArrayList<>();
        for (int[] iv : a) {
            Pre.check(iv[0] <= iv[1], "start <= end");
            if (!out.isEmpty() && iv[0] <= out.get(out.size() - 1)[1]) out.get(out.size() - 1)[1] = Math.max(out.get(out.size() - 1)[1], iv[1]);
            else out.add(new int[]{ iv[0], iv[1] });
        }
        return out.toArray(new int[0][]);
    }
}
