class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isPalindrome", T.STRING).returns(T.BOOL)
            .example("\"A man, a plan, a canal: Panama\"", "true")
            .example("\"race a car\"",                     "false")
            .example("\" \"",                              "true")
            .example("\"0P\"",                             "false")    // digits count, and 0 != p
            .example("\"a.\"",                             "true")
            .gen(g -> {
                String s = g.string(1, 30, "aAbB1 ,.:");
                if (g.chance(0.5)) {                      // plant a palindrome with noise
                    StringBuilder b = new StringBuilder(s);
                    s = s + b.reverse();
                }
                return new Object[]{ s };
            })
            .patterns("Two Pointers", "String").limitMs(2000).maxN(200000);
    }
    public boolean isPalindrome(String s) {
        int i = 0, j = s.length() - 1;
        while (i < j) {
            char a = s.charAt(i), b = s.charAt(j);
            if (!Character.isLetterOrDigit(a)) { i++; continue; }
            if (!Character.isLetterOrDigit(b)) { j--; continue; }
            if (Character.toLowerCase(a) != Character.toLowerCase(b)) return false;
            i++; j--;
        }
        return true;
    }
}
