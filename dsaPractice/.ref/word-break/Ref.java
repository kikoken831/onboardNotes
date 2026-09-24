import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("wordBreak", T.STRING, T.LIST_STRING).returns(T.BOOL)
            .example("\"leetcode\", [\"leet\",\"code\"]", "true").example("\"applepenapple\", [\"apple\",\"pen\"]", "true")
            .example("\"catsandog\", [\"cats\",\"dog\",\"sand\",\"and\",\"cat\"]", "false")
            .example("\"aaaaaaa\", [\"aaaa\",\"aaa\"]", "true")                  // greedy longest-first fails
            .example("\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab\", [\"a\",\"aa\",\"aaa\",\"aaaa\"]", "false")   // exponential without memo
            .gen(g -> {
                List<String> d = new ArrayList<>(new LinkedHashSet<>(Arrays.asList(g.words(1, 5, 1, 3, "ab"))));   // LeetCode: words are unique
                return new Object[]{ g.string(1, 20, "ab"), d };
            })
            .patterns("Array", "Hash Table", "String", "Dynamic Programming", "Trie").limitMs(2000).maxN(300);
    }
    public boolean wordBreak(String s, List<String> dict) {
        Set<String> d = new HashSet<>(dict);
        Pre.check(!dict.isEmpty() && d.size() == dict.size(), "the dictionary words are unique");
        boolean[] ok = new boolean[s.length() + 1];
        ok[0] = true;
        for (int i = 1; i <= s.length(); i++)
            for (int j = 0; j < i && !ok[i]; j++) ok[i] = ok[j] && d.contains(s.substring(j, i));
        return ok[s.length()];
    }
}
