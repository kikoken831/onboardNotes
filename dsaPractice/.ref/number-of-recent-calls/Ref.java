import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.design("RecentCounter")
            .op("ping", T.INT, T.INT)
            .exampleCalls("[\"RecentCounter\",\"ping\",\"ping\",\"ping\",\"ping\"]",
                          "[[],[1],[100],[3001],[3002]]",
                          "[null,1,2,3,3]")
            .exampleCalls("[\"RecentCounter\",\"ping\",\"ping\"]",
                          "[[],[1],[3002]]",
                          "[null,1,1]")                  // 1 is 3001 ms before 3002: just outside
            .exampleCalls("[\"RecentCounter\",\"ping\",\"ping\"]",
                          "[[],[1],[3001]]",
                          "[null,1,2]")                  // exactly 3000 ms: inclusive
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("RecentCounter");
                int n = g.size(1, 60), t = 0;
                for (int i = 0; i < n; i++) {
                    t += g.chance(0.2) ? 3000 : g.nextInt(1, 1500);
                    b.call("ping", t);
                }
                return new Object[]{ b.build() };
            })
            .patterns("Design", "Queue", "Data Stream")
            .limitMs(2000)
            .maxN(100000);
    }
}

class RecentCounter {
    private final Deque<Integer> q = new ArrayDeque<>();
    private int last = 0;

    public RecentCounter() {}

    public int ping(int t) {
        Pre.check(t > last, "ping times strictly increase");
        last = t;
        q.add(t);
        while (q.peek() < t - 3000) q.poll();
        return q.size();
    }
}
