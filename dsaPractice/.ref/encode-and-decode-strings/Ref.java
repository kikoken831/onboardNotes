import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.driven("Codec", T.LIST_STRING).returns(T.LIST_STRING)
            // The encoded string is "sent to another machine": decode on a SEPARATE instance,
            // so the codec can't pass the list along in a field.
            .driver((fresh, a) -> {
                Object s = Reflect.call(fresh.get(), "encode", a[0]);
                if (!(s instanceof String)) return "encode returned " + s + ", not a String";
                return Reflect.call(fresh.get(), "decode", s);
            })
            .judgeAs(T.LIST_STRING, (a, ret) -> ret == a[0] ? "decode returned the original List object, not a decoded copy" : ret)
            .example("[\"lint\",\"code\",\"love\",\"you\"]", "[\"lint\",\"code\",\"love\",\"you\"]")
            .example("[\"we\",\"say\",\":\",\"yes\"]", "[\"we\",\"say\",\":\",\"yes\"]")
            .example("[\"\"]", "[\"\"]").example("[]", "[]").example("[\"#\",\"3#ab\",\"\",\"\"]", "[\"#\",\"3#ab\",\"\",\"\"]")
            .gen(g -> new Object[]{ new ArrayList<>(Arrays.asList(g.words(0, 8, 0, 5, "a#1:/, "))) })
            .patterns("Array", "String", "Design").limitMs(2000).maxN(2000);
    }
}

class Codec {
    public String encode(List<String> strs) {
        StringBuilder b = new StringBuilder();
        for (String s : strs) b.append(s.length()).append('#').append(s);    // length-prefixed
        return b.toString();
    }
    public List<String> decode(String s) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int j = s.indexOf('#', i), len = Integer.parseInt(s.substring(i, j));
            out.add(s.substring(j + 1, j + 1 + len));
            i = j + 1 + len;
        }
        return out;
    }
}
