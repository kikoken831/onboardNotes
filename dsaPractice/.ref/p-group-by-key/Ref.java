import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("groupByKey", T.STRING_ARRAY).returns(T.LIST_LIST_STRING)
            // Group order is free; order within a group is not.
            .compare(Cmp.SET_OF_SEQS)
            .example("[\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]",
                     "[[\"eat\",\"tea\",\"ate\"],[\"tan\",\"nat\"],[\"bat\"]]")
            .example("[]",      "[]")
            .example("[\"\"]",  "[[\"\"]]")
            .example("[\"a\"]", "[[\"a\"]]")
            .example("[\"ab\",\"ba\",\"abc\"]", "[[\"ab\",\"ba\"],[\"abc\"]]")
            .gen(g -> new Object[]{ g.words(0, 40, 0, 4, "abc") })
            .patterns("Array", "Hash Table", "String", "Sorting")
            .limitMs(3000)
            .maxN(50000);
    }

    public List<List<String>> groupByKey(String[] words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String w : words) {
            char[] c = w.toCharArray();
            Arrays.sort(c);
            groups.computeIfAbsent(new String(c), k -> new ArrayList<>()).add(w);
        }
        return new ArrayList<>(groups.values());
    }
}
