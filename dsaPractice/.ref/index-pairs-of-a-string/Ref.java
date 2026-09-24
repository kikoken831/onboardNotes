import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("indexPairs", T.STRING, T.STRING_ARRAY).returns(T.INT_ARRAY_2D)
            .example("\"thestoryofleetcodeandme\", [\"story\",\"fleet\",\"leetcode\"]", "[[3,7],[9,13],[10,17]]")
            .example("\"ababa\", [\"aba\",\"ab\"]", "[[0,1],[0,2],[2,3],[2,4]]")          // overlapping, sorted
            .example("\"a\", [\"b\"]", "[]")
            .gen(g -> new Object[]{ g.string(1, 25, "ab"), new LinkedHashSet<>(Arrays.asList(g.words(1, 5, 1, 4, "ab"))).toArray(new String[0]) })
            .patterns("Array", "String", "Trie", "Sorting").limitMs(2000).maxN(1000);
    }
    public int[][] indexPairs(String text, String[] words) {
        List<int[]> out = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) for (String w : words) if (text.startsWith(w, i)) out.add(new int[]{ i, i + w.length() - 1 });
        out.sort((a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);
        return out.toArray(new int[0][]);
    }
}
