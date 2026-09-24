class Ref implements Problem {
    public Spec spec() {
        return Spec.method("characterReplacement", T.STRING, T.INT).returns(T.INT)
            .example("\"ABAB\", 2", "4").example("\"AABABBA\", 1", "4")
            .example("\"A\", 0", "1").example("\"ABCD\", 0", "1").example("\"AAAB\", 0", "3")
            .gen(g -> new Object[]{ g.string(1, 40, "ABC"), g.nextInt(0, 5) })
            .patterns("Hash Table", "String", "Sliding Window").limitMs(2000).maxN(200000);
    }
    public int characterReplacement(String s, int k) {
        Pre.check(!s.isEmpty() && k >= 0, "1 <= s.length, 0 <= k");
        int[] c = new int[26];
        int maxf = 0, l = 0, best = 0;
        for (int r = 0; r < s.length(); r++) {
            maxf = Math.max(maxf, ++c[s.charAt(r) - 'A']);
            while (r - l + 1 - maxf > k) c[s.charAt(l++) - 'A']--;
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
}
