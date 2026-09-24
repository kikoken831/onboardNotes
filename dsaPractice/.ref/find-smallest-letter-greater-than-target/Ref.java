class Ref implements Problem {
    public Spec spec() {
        return Spec.method("nextGreatestLetter", T.CHAR_ARRAY, T.CHAR).returns(T.CHAR)
            .example("[\"c\",\"f\",\"j\"], \"a\"", "\"c\"").example("[\"c\",\"f\",\"j\"], \"c\"", "\"f\"")
            .example("[\"c\",\"f\",\"j\"], \"d\"", "\"f\"").example("[\"x\",\"x\",\"y\",\"y\"], \"z\"", "\"x\"")   // wraps around
            .example("[\"c\",\"f\",\"j\"], \"j\"", "\"c\"")
            .gen(g -> {
                char[] c = g.string(2, 20, "abcdefg").toCharArray();
                java.util.Arrays.sort(c);
                if (c[0] == c[c.length - 1]) c[c.length - 1] = 'h';
                return new Object[]{ c, g.string(1, 1, "abcdefgh").charAt(0) };
            })
            .patterns("Array", "Binary Search").limitMs(2000).maxN(200000);
    }
    public char nextGreatestLetter(char[] letters, char target) {
        Pre.check(letters.length >= 2, "2 <= letters.length");
        for (int i = 1; i < letters.length; i++) Pre.check(letters[i] >= letters[i - 1], "sorted");
        Pre.check(letters[0] != letters[letters.length - 1], "at least two different characters");
        int lo = 0, hi = letters.length;
        while (lo < hi) { int m = (lo + hi) / 2; if (letters[m] <= target) lo = m + 1; else hi = m; }
        return letters[lo % letters.length];
    }
}
