class Ref implements Problem {
    public Spec spec() {
        return Spec.method("uniqueLetterString", T.STRING).returns(T.INT)
            .example("\"ABC\"", "10").example("\"ABA\"", "8").example("\"LEETCODE\"", "92")
            .example("\"A\"", "1").example("\"AA\"", "2")
            .gen(g -> new Object[]{ g.string(1, 40, "ABCD") })
            .patterns("Hash Table", "String", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int uniqueLetterString(String s) {
        int n = s.length();
        int[] prev = new int[26], prev2 = new int[26];            // last two positions per letter
        java.util.Arrays.fill(prev, -1);
        java.util.Arrays.fill(prev2, -1);
        long total = 0;
        for (int i = 0; i < n; i++) {
            int c = s.charAt(i) - 'A';
            Pre.check(c >= 0 && c < 26, "uppercase letters");
            if (prev[c] >= 0) total += (long) (prev[c] - prev2[c]) * (i - prev[c]);
            prev2[c] = prev[c];
            prev[c] = i;
        }
        for (int c = 0; c < 26; c++) if (prev[c] >= 0) total += (long) (prev[c] - prev2[c]) * (n - prev[c]);
        return (int) total;
    }
}
