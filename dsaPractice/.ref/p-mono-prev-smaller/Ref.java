import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("prevSmallerLeft", T.INT_ARRAY).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[2,1,5,6,2,3]", "[-1,-1,1,2,1,4]")
            .example("[1,2,3]",       "[-1,0,1]")
            .example("[3,2,1]",       "[-1,-1,-1]")
            .example("[]",            "[]")
            .example("[4,4,4]",       "[-1,-1,-1]")
            .gen(g -> new Object[]{ g.ints(0, 60, -8, 8) })
            .patterns("Array", "Stack", "Monotonic Stack")
            .limitMs(2000)
            .maxN(200000);
    }

    public int[] prevSmallerLeft(int[] nums) {
        int[] out = new int[nums.length];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < nums.length; i++) {
            while (!st.isEmpty() && nums[st.peek()] >= nums[i]) st.pop();
            out[i] = st.isEmpty() ? -1 : st.peek();
            st.push(i);
        }
        return out;
    }
}
