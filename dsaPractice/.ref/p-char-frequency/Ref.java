class Ref implements Problem {

    public Spec spec() {
        return Spec.method("charFrequency", T.STRING).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("\"aab\"",
                     "[2,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]")
            .example("\"\"",
                     "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]")
            .example("\"zzz\"",
                     "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3]")
            .gen(g -> new Object[]{ g.lower3(0, 60) })
            .patterns("Hash Table", "String", "Counting")
            .limitMs(2000)
            .maxN(500000);
    }

    public int[] charFrequency(String s) {
        int[] freq = new int[26];
        for (int i = 0; i < s.length(); i++) freq[s.charAt(i) - 'a']++;
        return freq;
    }
}
