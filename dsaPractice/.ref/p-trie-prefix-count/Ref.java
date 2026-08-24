class Ref implements Problem {

    public Spec spec() {
        return Spec.method("prefixCounts", T.STRING_ARRAY, T.STRING_ARRAY).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[\"apple\",\"app\",\"ape\"], [\"app\",\"ap\",\"b\",\"\"]", "[2,3,0,3]")
            .example("[\"a\",\"a\"], [\"a\"]", "[2]")
            .example("[], [\"x\"]",            "[0]")
            .example("[\"abc\"], [\"abcd\"]",  "[0]")
            .gen(g -> new Object[]{
                g.words(0, 20, 0, 5, "abc"),
                g.words(1, 20, 0, 5, "abc") })
            .patterns("Trie", "String")
            .limitMs(3000)
            .maxN(20000);
    }

    private static final class Node {
        final Node[] next = new Node[26];
        int passing;
    }

    public int[] prefixCounts(String[] words, String[] queries) {
        Node root = new Node();
        for (String w : words) {
            Node cur = root;
            cur.passing++;
            for (int i = 0; i < w.length(); i++) {
                int c = w.charAt(i) - 'a';
                if (cur.next[c] == null) cur.next[c] = new Node();
                cur = cur.next[c];
                cur.passing++;
            }
        }

        int[] out = new int[queries.length];
        for (int qi = 0; qi < queries.length; qi++) {
            String q = queries[qi];
            Node cur = root;
            boolean fell = false;
            for (int i = 0; i < q.length(); i++) {
                cur = cur.next[q.charAt(i) - 'a'];
                if (cur == null) { fell = true; break; }
            }
            out[qi] = fell ? 0 : cur.passing;
        }
        return out;
    }
}
