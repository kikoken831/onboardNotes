import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("frequencySort", T.STRING).returns(T.STRING)
            // Characters with equal counts may come in either order.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    String s = (String) input[0], y = (String) yours;
                    if (y == null || y.length() != s.length()) return "wrong length";
                    char[] a = s.toCharArray(), b = y.toCharArray(); Arrays.sort(a); Arrays.sort(b);
                    if (!Arrays.equals(a, b)) return "not a rearrangement of the input";
                    Map<Character, Integer> c = new HashMap<>(); for (char x : s.toCharArray()) c.merge(x, 1, Integer::sum);
                    int prev = Integer.MAX_VALUE;
                    for (int i = 0; i < y.length(); ) {
                        char x = y.charAt(i); int j = i; while (j < y.length() && y.charAt(j) == x) j++;
                        if (j - i != c.get(x)) return "all copies of '" + x + "' must be together";
                        if (c.get(x) > prev) return "'" + x + "' (count " + c.get(x) + ") comes after a less frequent character";
                        prev = c.get(x); i = j;
                    }
                    return null;
                }
            })
            .example("\"tree\"", "\"eert\"").example("\"cccaaa\"", "\"aaaccc\"").example("\"Aabb\"", "\"bbAa\"")   // case-sensitive
            .gen(g -> new Object[]{ g.string(1, 30, "aAbc1") })
            .patterns("Hash Table", "String", "Sorting", "Heap (Priority Queue)", "Bucket Sort").limitMs(2000).maxN(200000);
    }
    public String frequencySort(String s) {
        int[] c = new int[128];
        for (char x : s.toCharArray()) c[x]++;
        List<Character> order = new ArrayList<>();
        for (char x = 0; x < 128; x++) if (c[x] > 0) order.add(x);
        order.sort((x, y) -> c[y] - c[x]);
        StringBuilder b = new StringBuilder();
        for (char x : order) for (int i = 0; i < c[x]; i++) b.append(x);
        return b.toString();
    }
}
