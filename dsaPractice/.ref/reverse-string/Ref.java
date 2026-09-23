class Ref implements Problem {

    public Spec spec() {
        return Spec.method("reverseString", T.CHAR_ARRAY).returns(T.VOID)
            .inPlace(0)
            .exampleInPlace("[\"h\",\"e\",\"l\",\"l\",\"o\"]",     "[\"o\",\"l\",\"l\",\"e\",\"h\"]")
            .exampleInPlace("[\"H\",\"a\",\"n\",\"n\",\"a\",\"h\"]", "[\"h\",\"a\",\"n\",\"n\",\"a\",\"H\"]")
            .exampleInPlace("[\"a\"]",         "[\"a\"]")
            .exampleInPlace("[\"a\",\"b\"]",   "[\"b\",\"a\"]")
            .gen(g -> new Object[]{ g.string(1, 60, "abcdeXYZ!").toCharArray() })
            .patterns("Two Pointers", "String")
            .limitMs(2000)
            .maxN(200000);
    }

    public void reverseString(char[] s) {
        Pre.length(s, 1, 1000_000, "s");
        for (int i = 0, j = s.length - 1; i < j; i++, j--) {
            char t = s[i]; s[i] = s[j]; s[j] = t;
        }
    }
}
