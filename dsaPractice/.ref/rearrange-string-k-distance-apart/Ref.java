import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rearrangeString", T.STRING, T.INT).returns(T.STRING)
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    String s = (String) input[0], y = (String) yours, r = (String) reference;
                    int k = (Integer) input[1];
                    if (y == null) return "returned null";
                    if (r.isEmpty()) return y.isEmpty() ? null : "no valid arrangement exists, so the answer is \"\"";
                    if (y.isEmpty()) return "an arrangement exists (e.g. \"" + r + "\")";
                    char[] a = s.toCharArray(), b = y.toCharArray(); Arrays.sort(a); Arrays.sort(b);
                    if (!Arrays.equals(a, b)) return "not a rearrangement of the input";
                    Map<Character, Integer> last = new HashMap<>();
                    for (int i = 0; i < y.length(); i++) {
                        Integer p = last.put(y.charAt(i), i);
                        if (p != null && i - p < k) return "'" + y.charAt(i) + "' at " + p + " and " + i + " are closer than " + k;
                    }
                    return null;
                }
            })
            .example("\"aabbcc\", 3", "\"abcabc\"").example("\"aaabc\", 3", "\"\"").example("\"aaadbbcc\", 2", "\"abacabcd\"")
            .example("\"aa\", 0", "\"aa\"").example("\"a\", 5", "\"a\"")
            .gen(g -> new Object[]{ g.string(1, 12, g.chance(0.5) ? "aabc" : "abcd"), g.nextInt(0, 4) })
            .patterns("Hash Table", "String", "Greedy", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(1000);
    }
    public String rearrangeString(String s, int k) {
        if (k <= 1) return s;
        int[] c = new int[26];
        for (char x : s.toCharArray()) c[x - 'a']++;
        PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> x[1] != y[1] ? y[1] - x[1] : x[0] - y[0]);
        for (int i = 0; i < 26; i++) if (c[i] > 0) pq.add(new int[]{ i, c[i] });
        Deque<int[]> cooling = new ArrayDeque<>();
        StringBuilder b = new StringBuilder();
        while (!pq.isEmpty()) {
            int[] t = pq.poll();
            b.append((char) ('a' + t[0]));
            t[1]--;
            cooling.add(t);
            if (cooling.size() >= k) { int[] back = cooling.poll(); if (back[1] > 0) pq.add(back); }   // free again after k steps
        }
        return b.length() == s.length() ? b.toString() : "";
    }
}
