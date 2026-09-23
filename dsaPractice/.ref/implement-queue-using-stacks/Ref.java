import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.design("MyQueue")
            .op("push", T.VOID, T.INT)
            .op("pop", T.INT)
            .op("peek", T.INT)
            .op("empty", T.BOOL)
            .exampleCalls("[\"MyQueue\",\"push\",\"push\",\"peek\",\"pop\",\"empty\"]",
                          "[[],[1],[2],[],[],[]]",
                          "[null,null,null,1,1,false]")
            .exampleCalls("[\"MyQueue\",\"empty\",\"push\",\"pop\",\"empty\"]",
                          "[[],[],[7],[],[]]",
                          "[null,true,null,7,true]")
            .exampleCalls("[\"MyQueue\",\"push\",\"push\",\"pop\",\"push\",\"pop\",\"pop\"]",
                          "[[],[1],[2],[],[3],[],[]]",
                          "[null,null,null,1,null,2,3]")   // push after a transfer
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("MyQueue");
                int n = g.size(1, 60), live = 0;
                for (int i = 0; i < n; i++) {
                    int r = g.nextInt(0, 9);
                    if (live == 0 || r < 4) { b.call("push", g.nextInt(1, 9)); live++; }
                    else if (r < 7)         { b.call("pop"); live--; }
                    else if (r < 9)         b.call("peek");
                    else                    b.call("empty");
                }
                return new Object[]{ b.build() };
            })
            .patterns("Stack", "Design", "Queue")
            .limitMs(2000)
            .maxN(100000);
    }
}

class MyQueue {
    private final Deque<Integer> in = new ArrayDeque<>(), out = new ArrayDeque<>();

    public MyQueue() {}

    public void push(int x) { in.push(x); }

    public int pop()  { shift(); return out.pop(); }

    public int peek() { shift(); return out.peek(); }

    public boolean empty() { return in.isEmpty() && out.isEmpty(); }

    private void shift() {
        Pre.check(!empty(), "pop/peek only on a non-empty queue");
        if (out.isEmpty()) while (!in.isEmpty()) out.push(in.pop());
    }
}
