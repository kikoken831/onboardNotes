import java.util.ArrayDeque;
import java.util.Queue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.design("MyStack")
            .op("push", T.VOID, T.INT)
            .op("pop", T.INT)
            .op("top", T.INT)
            .op("empty", T.BOOL)
            .exampleCalls("[\"MyStack\",\"push\",\"push\",\"top\",\"pop\",\"empty\"]",
                          "[[],[1],[2],[],[],[]]",
                          "[null,null,null,2,2,false]")
            .exampleCalls("[\"MyStack\",\"push\",\"push\",\"push\",\"pop\",\"pop\",\"top\"]",
                          "[[],[1],[2],[3],[],[],[]]",
                          "[null,null,null,null,3,2,1]")
            .exampleCalls("[\"MyStack\",\"empty\",\"push\",\"empty\"]",
                          "[[],[],[5],[]]",
                          "[null,true,null,false]")
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("MyStack");
                int n = g.size(1, 60), live = 0;
                for (int i = 0; i < n; i++) {
                    int r = g.nextInt(0, 9);
                    if (live == 0 || r < 4) { b.call("push", g.nextInt(1, 9)); live++; }
                    else if (r < 7)         { b.call("pop"); live--; }
                    else if (r < 9)         b.call("top");
                    else                    b.call("empty");
                }
                return new Object[]{ b.build() };
            })
            .patterns("Stack", "Design", "Queue")
            .limitMs(2000)
            .maxN(20000);
    }
}

class MyStack {
    private final Queue<Integer> q = new ArrayDeque<>();

    public MyStack() {}

    /** Rotate so the newest element is at the front. O(n) push, O(1) pop and top. */
    public void push(int x) {
        q.add(x);
        for (int i = 1; i < q.size(); i++) q.add(q.poll());
    }

    public int pop() { Pre.check(!q.isEmpty(), "pop on non-empty"); return q.poll(); }

    public int top() { Pre.check(!q.isEmpty(), "top on non-empty"); return q.peek(); }

    public boolean empty() { return q.isEmpty(); }
}
