class Ref implements Problem {
    public Spec spec() {
        return Spec.method("numDecodings", T.STRING).returns(T.INT)
            .example("\"12\"", "2").example("\"226\"", "3").example("\"06\"", "0").example("\"10\"", "1")
            .example("\"2101\"", "1").example("\"11106\"", "2")
            .gen(g -> new Object[]{ g.string(1, 30, "0112226789") })
            .patterns("String", "Dynamic Programming").limitMs(2000).maxN(200000);
    }
    public int numDecodings(String s) {
        int a = 1, b = s.charAt(0) == '0' ? 0 : 1;          // ways for s[0..i-2], s[0..i-1]
        for (int i = 2; i <= s.length(); i++) {
            int c = 0, one = s.charAt(i - 1) - '0', two = Integer.parseInt(s.substring(i - 2, i));
            if (one != 0) c += b;
            if (two >= 10 && two <= 26) c += a;
            a = b; b = c;
        }
        return b;
    }
}
