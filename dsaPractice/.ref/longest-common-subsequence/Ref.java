class Ref implements Problem {
    public Spec spec() {
        return Spec.method("longestCommonSubsequence", T.STRING, T.STRING).returns(T.INT)
            .example("\"abcde\", \"ace\"", "3").example("\"abc\", \"abc\"", "3").example("\"abc\", \"def\"", "0").example("\"bl\", \"yby\"", "1")
            .gen(g -> new Object[]{ g.lower3(1, 25), g.lower3(1, 25) })
            .patterns("String", "Dynamic Programming").limitMs(2000).maxN(1000);
    }
    public int longestCommonSubsequence(String a, String b) {
        int[] d = new int[b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            int diag = 0;                                   // d[i-1][j-1]
            for (int j = 1; j <= b.length(); j++) {
                int up = d[j];
                d[j] = a.charAt(i - 1) == b.charAt(j - 1) ? diag + 1 : Math.max(d[j], d[j - 1]);
                diag = up;
            }
        }
        return d[b.length()];
    }
}
