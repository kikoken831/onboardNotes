import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("groupAnagrams", T.STRING_ARRAY).returns(T.LIST_LIST_STRING)
            .compare(Cmp.SET_OF_SETS)
            .example("[\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]", "[[\"bat\"],[\"nat\",\"tan\"],[\"ate\",\"eat\",\"tea\"]]")
            .example("[\"\"]",   "[[\"\"]]")
            .example("[\"a\"]",  "[[\"a\"]]")
            .example("[\"ab\",\"ba\",\"ab\"]", "[[\"ab\",\"ba\",\"ab\"]]")      // duplicates stay
            .example("[\"aab\",\"abb\"]", "[[\"aab\"],[\"abb\"]]")
            .gen(g -> new Object[]{ g.words(1, 30, 0, 4, "abc") })
            .patterns("Array", "Hash Table", "String", "Sorting").limitMs(2000).maxN(50000);
    }
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String s : strs) {
            int[] c = new int[26];
            for (char ch : s.toCharArray()) c[ch - 'a']++;
            groups.computeIfAbsent(Arrays.toString(c), k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(groups.values());
    }
}
