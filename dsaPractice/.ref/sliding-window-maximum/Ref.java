import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("maxSlidingWindow", T.INT_ARRAY, T.INT).returns(T.INT_ARRAY)
            .example("[1,3,-1,-3,5,3,6,7], 3", "[3,3,5,5,6,7]").example("[1], 1", "[1]")
            .example("[9,8,7,6], 2", "[9,8,7]").example("[1,-1], 1", "[1,-1]").example("[4,4,4], 2", "[4,4]")
            .gen(g -> { int[] a = g.ints(1, 50, -9, 9); return new Object[]{ a, g.nextInt(1, a.length) }; })
            .patterns("Array", "Queue", "Sliding Window", "Monotonic Queue").limitMs(2000).maxN(200000);
    }
    public int[] maxSlidingWindow(int[] nums, int k) {
        Pre.check(k >= 1 && k <= nums.length, "1 <= k <= n");
        int[] out = new int[nums.length - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();            // indices, values decreasing
        for (int i = 0; i < nums.length; i++) {
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();
            while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i]) dq.pollLast();
            dq.addLast(i);
            if (i >= k - 1) out[i - k + 1] = nums[dq.peekFirst()];
        }
        return out;
    }
}
