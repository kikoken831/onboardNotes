class Ref implements Problem {

    public Spec spec() {
        return Spec.method("numJewelsInStones", T.STRING, T.STRING).returns(T.INT)
            .example("\"aA\", \"aAAbbbb\"", "3")
            .example("\"z\", \"ZZ\"",       "0")
            .example("\"a\", \"aaaa\"",     "4")
            .example("\"abc\", \"d\"",      "0")
            .gen(g -> {
                String alpha = "aAbBcCdD";
                StringBuilder j = new StringBuilder();
                for (char c : alpha.toCharArray()) if (g.chance(0.35)) j.append(c);
                if (j.length() == 0) j.append('a');
                return new Object[]{ j.toString(), g.string(1, 50, alpha) };
            })
            .patterns("Hash Table", "String")
            .limitMs(2000)
            .maxN(100000);
    }

    public int numJewelsInStones(String jewels, String stones) {
        Pre.check(!jewels.isEmpty() && !stones.isEmpty(), "1 <= lengths");
        boolean[] isJewel = new boolean[128];
        for (char c : jewels.toCharArray()) {
            Pre.check(!isJewel[c], "jewels are distinct");
            isJewel[c] = true;
        }
        int n = 0;
        for (char c : stones.toCharArray()) if (isJewel[c]) n++;
        return n;
    }
}
