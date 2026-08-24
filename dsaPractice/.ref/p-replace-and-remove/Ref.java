class Ref implements Problem {

    public Spec spec() {
        return Spec.method("replaceAndRemove", T.CHAR_ARRAY, T.INT).returns(T.INT)
            .lengthAndPrefix(0)
            .example("[\"a\",\"c\",\"d\",\"b\",\"b\",\"c\",\"a\",\"_\",\"_\",\"_\"], 7",
                     "[\"d\",\"d\",\"c\",\"d\",\"c\",\"d\",\"d\"]")
            .example("[\"b\",\"b\",\"b\"], 3", "[]")
            .example("[\"a\",\"_\"], 1",       "[\"d\",\"d\"]")
            .example("[\"c\"], 1",             "[\"c\"]")
            .example("[], 0",                  "[]")
            .example("[\"b\",\"a\",\"_\"], 2", "[\"d\",\"d\"]")
            .gen(g -> {
                // Capacity must fit the worst case: every live char is an 'a'.
                int size = g.size(0, 40);
                char[] s = new char[size * 2 + 1];
                String alphabet = "abcd";
                for (int i = 0; i < size; i++) {
                    s[i] = alphabet.charAt(g.nextInt(0, alphabet.length() - 1));
                }
                for (int i = size; i < s.length; i++) s[i] = '_';
                return new Object[]{ s, size };
            })
            .patterns("Array", "Two Pointers")
            .limitMs(2000)
            .maxN(100000);
    }

    public int replaceAndRemove(char[] s, int size) {
        int write = 0, countA = 0;
        for (int r = 0; r < size; r++) {
            if (s[r] != 'b') s[write++] = s[r];
            if (s[r] == 'a') countA++;
        }
        int end = write + countA;
        int w = end - 1;
        for (int r = write - 1; r >= 0; r--) {
            if (s[r] == 'a') { s[w--] = 'd'; s[w--] = 'd'; }
            else             { s[w--] = s[r]; }
        }
        return end;
    }
}
