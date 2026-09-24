import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("alienOrder", T.STRING_ARRAY).returns(T.STRING)
            // Any order consistent with the words is accepted; "" exactly when none exists.
            .validate(new Spec.Validator() {
                public String check(Object[] input, Object yours, Object reference) {
                    String[] w = (String[]) input[0];
                    String y = (String) yours, r = (String) reference;
                    if (y == null) return "returned null";
                    if (r.isEmpty()) return y.isEmpty() ? null : "no valid order exists, so the answer is \"\"";
                    Set<Character> letters = new TreeSet<>();
                    for (String s : w) for (char c : s.toCharArray()) letters.add(c);
                    Set<Character> got = new TreeSet<>();
                    for (char c : y.toCharArray()) if (!got.add(c)) return "letter '" + c + "' appears twice";
                    if (!got.equals(letters)) return "must contain exactly the letters " + letters;
                    for (int i = 0; i + 1 < w.length; i++) {
                        String a = w[i], b = w[i + 1];
                        for (int k = 0; k < Math.min(a.length(), b.length()); k++) if (a.charAt(k) != b.charAt(k)) {
                            if (y.indexOf(a.charAt(k)) > y.indexOf(b.charAt(k)))
                                return "\"" + a + "\" before \"" + b + "\" needs '" + a.charAt(k) + "' before '" + b.charAt(k) + "'";
                            break;
                        }
                    }
                    return null;
                }
            })
            .example("[\"wrt\",\"wrf\",\"er\",\"ett\",\"rftt\"]", "\"wertf\"").example("[\"z\",\"x\"]", "\"zx\"")
            .example("[\"z\",\"x\",\"z\"]", "\"\"").example("[\"abc\",\"ab\"]", "\"\"")          // a longer word before its prefix
            .example("[\"z\",\"z\"]", "\"z\"")
            .gen(g -> {
                String[] w = g.words(1, 8, 1, 4, "abcd");
                if (g.chance(0.6)) { char[] perm = "abcd".toCharArray(); List<Character> l = new ArrayList<>(); for (char c : perm) l.add(c); Collections.shuffle(l, new Random(g.nextInt(0, 1 << 20)));
                    String ord = ""; for (char c : l) ord += c; final String o = ord;
                    Arrays.sort(w, (x, y) -> { for (int k = 0; k < Math.min(x.length(), y.length()); k++) if (x.charAt(k) != y.charAt(k)) return o.indexOf(x.charAt(k)) - o.indexOf(y.charAt(k)); return x.length() - y.length(); }); }
                return new Object[]{ w };
            })
            .patterns("Array", "String", "Depth-First Search", "Breadth-First Search", "Graph Theory", "Topological Sort").limitMs(2000).maxN(20000);
    }
    public String alienOrder(String[] words) {
        Map<Character, Set<Character>> adj = new TreeMap<>();
        Map<Character, Integer> in = new TreeMap<>();
        for (String s : words) for (char c : s.toCharArray()) { adj.putIfAbsent(c, new TreeSet<>()); in.putIfAbsent(c, 0); }
        for (int i = 0; i + 1 < words.length; i++) {
            String a = words[i], b = words[i + 1];
            int k = 0;
            while (k < Math.min(a.length(), b.length()) && a.charAt(k) == b.charAt(k)) k++;
            if (k == Math.min(a.length(), b.length())) { if (a.length() > b.length()) return ""; continue; }
            if (adj.get(a.charAt(k)).add(b.charAt(k))) in.merge(b.charAt(k), 1, Integer::sum);
        }
        Deque<Character> q = new ArrayDeque<>();
        for (Map.Entry<Character, Integer> e : in.entrySet()) if (e.getValue() == 0) q.add(e.getKey());
        StringBuilder out = new StringBuilder();
        while (!q.isEmpty()) { char c = q.poll(); out.append(c); for (char d : adj.get(c)) if (in.merge(d, -1, Integer::sum) == 0) q.add(d); }
        return out.length() == in.size() ? out.toString() : "";
    }
}
