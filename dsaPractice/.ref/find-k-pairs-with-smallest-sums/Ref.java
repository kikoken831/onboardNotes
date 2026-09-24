import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("kSmallestPairs", T.INT_ARRAY, T.INT_ARRAY, T.INT).returns(T.LIST_LIST_INT)
            // Ties make many answers correct: check the sums and that every pair is available.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    int[] a = (int[]) input[0], b = (int[]) input[1];
                    List<List<Integer>> y = (List<List<Integer>>) yours, r = (List<List<Integer>>) reference;
                    if (y == null) return "returned null";
                    if (y.size() != r.size()) return "returned " + y.size() + " pairs, expected " + r.size();
                    List<Integer> ys = new ArrayList<>(), rs = new ArrayList<>();
                    Map<Integer, Integer> ca = new HashMap<>(), cb = new HashMap<>();
                    for (int x : a) ca.merge(x, 1, Integer::sum);
                    for (int x : b) cb.merge(x, 1, Integer::sum);
                    Map<List<Integer>, Integer> used = new HashMap<>();
                    for (List<Integer> p : y) {
                        if (p.size() != 2) return "pair " + p + " is not a pair";
                        int limit = ca.getOrDefault(p.get(0), 0) * cb.getOrDefault(p.get(1), 0);
                        if (used.merge(p, 1, Integer::sum) > limit) return "pair " + p + " used more often than nums1/nums2 allow";
                        ys.add(p.get(0) + p.get(1));
                    }
                    for (List<Integer> p : r) rs.add(p.get(0) + p.get(1));
                    Collections.sort(ys); Collections.sort(rs);
                    return ys.equals(rs) ? null : "pair sums " + ys + " are not the " + r.size() + " smallest " + rs;
                }
            })
            .example("[1,7,11], [2,4,6], 3", "[[1,2],[1,4],[1,6]]").example("[1,1,2], [1,2,3], 2", "[[1,1],[1,1]]")
            .example("[1,2], [3], 2", "[[1,3],[2,3]]")                         // k == m*n: every pair
            .gen(g -> {
                int[] a = g.sortedInts(1, 10, -10, 10), b = g.sortedInts(1, 10, -10, 10);
                return new Object[]{ a, b, g.nextInt(1, Math.min(30, a.length * b.length)) };   // LeetCode: k <= m*n
            })
            .patterns("Array", "Heap (Priority Queue)").limitMs(2000).maxN(20000);
    }
    public List<List<Integer>> kSmallestPairs(int[] a, int[] b, int k) {
        Pre.sorted(a, false, "nums1"); Pre.sorted(b, false, "nums2");
        Pre.check(a.length >= 1 && b.length >= 1, "1 <= nums1.length, nums2.length");
        Pre.check(k >= 1 && (long) k <= (long) a.length * b.length, "1 <= k <= nums1.length * nums2.length");
        PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> Long.compare((long) a[x[0]] + b[x[1]], (long) a[y[0]] + b[y[1]]));
        for (int i = 0; i < Math.min(a.length, k); i++) pq.add(new int[]{ i, 0 });
        List<List<Integer>> out = new ArrayList<>();
        while (!pq.isEmpty() && out.size() < k) {
            int[] c = pq.poll();
            out.add(Arrays.asList(a[c[0]], b[c[1]]));
            if (c[1] + 1 < b.length) pq.add(new int[]{ c[0], c[1] + 1 });
        }
        return out;
    }
}
