import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("largestRectangle", T.INT_ARRAY).returns(T.INT)
            .compare(Cmp.EXACT)
            .example("[2,1,5,6,2,3]", "10")
            .example("[2,4]",         "4")
            .example("[]",            "0")
            .example("[5]",           "5")
            .example("[3,3,3]",       "9")
            .example("[0]",           "0")
            .gen(g -> {
                int n = g.size(0, 50);
                int[] h = new int[n];
                for (int i = 0; i < n; i++) h[i] = g.nextInt(0, 9);
                return new Object[]{ h };
            })
            .patterns("Array", "Stack", "Monotonic Stack")
            .limitMs(2000)
            .maxN(100000);
    }

    public int largestRectangle(int[] heights) {
        Deque<Integer> st = new ArrayDeque<>();
        int best = 0;
        for (int i = 0; i <= heights.length; i++) {
            int h = (i == heights.length) ? -1 : heights[i];
            while (!st.isEmpty() && heights[st.peek()] >= h) {
                int height = heights[st.pop()];
                int left = st.isEmpty() ? -1 : st.peek();
                best = Math.max(best, height * (i - left - 1));
            }
            st.push(i);
        }
        return best;
    }
}
