import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("reorganizeString", T.STRING).returns(T.STRING)
            // Any arrangement with no two equal neighbours is accepted; "" exactly when none exists.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    String s = (String) input[0], y = (String) yours, r = (String) reference;
                    if (y == null) return "returned null";
                    if (r.isEmpty()) return y.isEmpty() ? null : "no valid arrangement exists, so the answer is \"\"";
                    char[] a = s.toCharArray(), b = y.toCharArray(); Arrays.sort(a); Arrays.sort(b);
                    if (!Arrays.equals(a, b)) return y.isEmpty() ? "an arrangement exists (e.g. \"" + r + "\")" : "not a rearrangement of the input";
                    for (int i = 1; i < y.length(); i++) if (y.charAt(i) == y.charAt(i - 1)) return "'" + y.charAt(i) + "' is repeated at index " + i;
                    return null;
                }
            })
            .example("\"aab\"", "\"aba\"").example("\"aaab\"", "\"\"").example("\"a\"", "\"a\"").example("\"aabb\"", "\"abab\"")
            .gen(g -> new Object[]{ g.string(1, 20, g.chance(0.5) ? "aab" : "abc") })
            .patterns("Hash Table", "String", "Greedy", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(200000);
    }
    public String reorganizeString(String s) {
        int n = s.length();
        int[] c = new int[26];
        for (char x : s.toCharArray()) c[x - 'a']++;
        int top = 0;
        for (int i = 0; i < 26; i++) if (c[i] > c[top]) top = i;
        if (c[top] > (n + 1) / 2) return "";
        char[] out = new char[n];
        int i = 0;
        for (int k = 0; k < 26; k++) {                           // most frequent first, even slots then odd
            int ch = k == 0 ? top : (k <= top ? k - 1 : k);
            while (c[ch]-- > 0) { if (i >= n) i = 1; out[i] = (char) ('a' + ch); i += 2; }
        }
        return new String(out);
    }
}
