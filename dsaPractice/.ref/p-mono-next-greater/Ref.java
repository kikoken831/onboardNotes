import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("nextGreaterRight", T.INT_ARRAY).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[73,74,75,71,69,72,76,73]", "[1,1,4,2,1,1,0,0]")
            .example("[30,40,50]",                "[1,1,0]")
            .example("[50,40,30]",                "[0,0,0]")
            .example("[]",                        "[]")
            .example("[5]",                       "[0]")
            .example("[2,2,2]",                   "[0,0,0]")
            .gen(g -> new Object[]{ g.ints(0, 60, -8, 8) })   // narrow range -> ties
            .patterns("Array", "Stack", "Monotonic Stack")
            .limitMs(2000)
            .maxN(200000);
    }

    public int[] nextGreaterRight(int[] nums) {
        int[] out = new int[nums.length];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < nums.length; i++) {
            while (!st.isEmpty() && nums[st.peek()] < nums[i]) {
                int j = st.pop();
                out[j] = i - j;
            }
            st.push(i);
        }
        return out;
    }
}
