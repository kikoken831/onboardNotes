import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("employeeFreeTime", T.INT_ARRAY_3D).returns(T.INT_ARRAY_2D)
            // LeetCode's input is a List<List<Interval>>: build it from the literal for each call.
            .prepare(a -> {
                List<List<Interval>> sch = new ArrayList<>();
                for (int[][] emp : (int[][][]) a[0]) {
                    List<Interval> l = new ArrayList<>();
                    for (int[] iv : emp) l.add(new Interval(iv[0], iv[1]));
                    sch.add(l);
                }
                a[0] = sch;
            })
            .judgeAs(T.INT_ARRAY_2D, (a, ret) -> {
                List<?> out = (List<?>) ret;
                int[][] r = new int[out.size()][];
                for (int i = 0; i < r.length; i++) { Interval iv = (Interval) out.get(i); r[i] = new int[]{ iv.start, iv.end }; }
                return r;
            })
            .example("[[[1,2],[5,6]],[[1,3]],[[4,10]]]", "[[3,4]]").example("[[[1,3],[6,7]],[[2,4]],[[2,5],[9,12]]]", "[[5,6],[7,9]]")
            .example("[[[1,5]],[[5,8]]]", "[]")                           // touching is not free time
            .gen(g -> {
                int e = g.size(1, 5);
                int[][][] s = new int[e][][];
                for (int i = 0; i < e; i++) {
                    int n = g.size(1, 4), x = g.nextInt(0, 5);
                    s[i] = new int[n][];
                    for (int j = 0; j < n; j++) { int st = x + g.nextInt(0, 3); int en = st + g.nextInt(1, 4); s[i][j] = new int[]{ st, en }; x = en + 1; }
                }
                return new Object[]{ s };
            })
            .patterns("Array", "Sweep Line", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(20000);
    }
    public List<Interval> employeeFreeTime(List<List<Interval>> schedule) {
        List<Interval> all = new ArrayList<>();
        for (List<Interval> e : schedule) all.addAll(e);
        all.sort((x, y) -> Integer.compare(x.start, y.start));
        List<Interval> out = new ArrayList<>();
        int end = all.get(0).end;
        for (Interval iv : all) {
            if (iv.start > end) out.add(new Interval(end, iv.start));
            end = Math.max(end, iv.end);
        }
        return out;
    }
}
