class Ref implements Problem {

    public Spec spec() {
        return Spec.method("lemonadeChange", T.INT_ARRAY).returns(T.BOOL)
            .example("[5,5,5,10,20]",       "true")
            .example("[5,5,10,10,20]",      "false")
            .example("[10]",                "false")
            .example("[5,5,10,20]",         "true")
            .example("[5,5,5,5,20,20]",     "false")
            .example("[5,5,5,5,10,20,10]",  "true")    // paying 5+5+5 for the $20 strands the last $10
            .gen(g -> {
                int n = g.size(1, 40);
                int[] b = new int[n];
                for (int i = 0; i < n; i++) {
                    int r = g.nextInt(0, 9);
                    b[i] = r < 5 ? 5 : r < 8 ? 10 : 20;
                }
                return new Object[]{ b };
            })
            .patterns("Array", "Greedy")
            .limitMs(2000)
            .maxN(200000);
    }

    public boolean lemonadeChange(int[] bills) {
        int fives = 0, tens = 0;
        for (int b : bills) {
            Pre.check(b == 5 || b == 10 || b == 20, "bills are 5, 10 or 20");
            if (b == 5) fives++;
            else if (b == 10) {
                if (fives == 0) return false;
                fives--; tens++;
            } else {
                if (tens > 0 && fives > 0) { tens--; fives--; }   // spend the 10 first
                else if (fives >= 3) fives -= 3;
                else return false;
            }
        }
        return true;
    }
}
