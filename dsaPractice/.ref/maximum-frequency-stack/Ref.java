import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.design("FreqStack")
            .op("push", T.VOID, T.INT).op("pop", T.INT)
            .exampleCalls("[\"FreqStack\",\"push\",\"push\",\"push\",\"push\",\"push\",\"push\",\"pop\",\"pop\",\"pop\",\"pop\"]",
                          "[[],[5],[7],[5],[7],[4],[5],[],[],[],[]]", "[null,null,null,null,null,null,null,5,7,5,4]")
            .exampleCalls("[\"FreqStack\",\"push\",\"push\",\"pop\",\"pop\"]", "[[],[1],[2],[],[]]", "[null,null,null,2,1]")   // tie -> most recent
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("FreqStack");
                int n = g.size(1, 60), live = 0;
                for (int i = 0; i < n; i++) {
                    if (live == 0 || g.chance(0.6)) { b.call("push", g.nextInt(0, 4)); live++; }
                    else { b.call("pop"); live--; }
                }
                return new Object[]{ b.build() };
            })
            .patterns("Hash Table", "Stack", "Design", "Ordered Set").limitMs(2000).maxN(50000);
    }
}

class FreqStack {
    private final Map<Integer, Integer> freq = new HashMap<>();
    private final Map<Integer, Deque<Integer>> group = new HashMap<>();    // frequency -> values pushed at it
    private int maxf = 0;
    public FreqStack() {}
    public void push(int x) {
        int f = freq.merge(x, 1, Integer::sum);
        maxf = Math.max(maxf, f);
        group.computeIfAbsent(f, k -> new ArrayDeque<>()).push(x);
    }
    public int pop() {
        Pre.check(maxf > 0, "pop on a non-empty stack");
        int x = group.get(maxf).pop();
        freq.merge(x, -1, Integer::sum);
        if (group.get(maxf).isEmpty()) maxf--;
        return x;
    }
}
