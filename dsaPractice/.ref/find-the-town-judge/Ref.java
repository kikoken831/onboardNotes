import java.util.HashSet;
import java.util.Set;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("findJudge", T.INT, T.INT_ARRAY_2D).returns(T.INT)
            .example("2, [[1,2]]",                               "2")
            .example("3, [[1,3],[2,3]]",                         "3")
            .example("3, [[1,3],[2,3],[3,1]]",                   "-1")
            .example("1, []",                                    "1")
            .example("4, [[1,3],[1,4],[2,3],[2,4],[4,3]]",       "3")
            .example("3, [[1,2],[2,3]]",                         "-1")
            .example("2, []",                                    "-1")
            .gen(g -> {
                int n = g.size(1, 10);
                Set<Long> seen = new HashSet<>();
                java.util.List<int[]> t = new java.util.ArrayList<>();
                int judge = g.chance(0.6) ? g.nextInt(1, n) : -1;
                if (judge > 0) for (int p = 1; p <= n; p++) if (p != judge && g.chance(0.9)) {
                    t.add(new int[]{ p, judge }); seen.add(p * 100L + judge);
                }
                int extra = g.nextInt(0, n * 2);
                for (int k = 0; k < extra * 3 && extra > 0; k++) {
                    int a = g.nextInt(1, n), b = g.nextInt(1, n);
                    if (a == b || (a == judge && g.chance(0.8))) continue;
                    if (seen.add(a * 100L + b)) { t.add(new int[]{ a, b }); extra--; }
                }
                return new Object[]{ n, t.toArray(new int[0][]) };
            })
            .patterns("Array", "Hash Table", "Graph Theory")
            .limitMs(2000)
            .maxN(20000);
    }

    public int findJudge(int n, int[][] trust) {
        Pre.check(n >= 1, "1 <= n");
        Set<Long> seen = new HashSet<>();
        int[] score = new int[n + 1];                 // in-degree minus out-degree
        for (int[] t : trust) {
            Pre.check(t[0] != t[1] && t[0] >= 1 && t[0] <= n && t[1] >= 1 && t[1] <= n, "1 <= a, b <= n, a != b");
            Pre.check(seen.add(t[0] * 1_000_000L + t[1]), "trust pairs are unique");
            score[t[0]]--;
            score[t[1]]++;
        }
        for (int p = 1; p <= n; p++) if (score[p] == n - 1) return p;
        return -1;
    }
}
