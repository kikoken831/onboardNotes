import java.util.Arrays;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("canAttendMeetings", T.INT_ARRAY_2D).returns(T.BOOL)
            .example("[[0,30],[5,10],[15,20]]", "false").example("[[7,10],[2,4]]", "true")
            .example("[[1,2],[2,3]]", "true").example("[]", "true").example("[[5,8],[6,8]]", "false")
            .gen(g -> {
                int n = g.size(0, 12);
                int[][] a = new int[n][];
                for (int i = 0; i < n; i++) { int s = g.nextInt(0, 60); a[i] = new int[]{ s, s + g.nextInt(1, 6) }; }
                return new Object[]{ a };
            })
            .patterns("Array", "Sorting").limitMs(2000).maxN(100000);
    }
    public boolean canAttendMeetings(int[][] intervals) {
        for (int[] m : intervals) Pre.check(m.length == 2 && 0 <= m[0] && m[0] < m[1], "0 <= start < end");
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        for (int i = 1; i < a.length; i++) if (a[i][0] < a[i - 1][1]) return false;
        return true;
    }
}
