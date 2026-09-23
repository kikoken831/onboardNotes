class Ref implements Problem {

    public Spec spec() {
        return Spec.method("interpret", T.STRING).returns(T.STRING)
            .example("\"G()(al)\"",        "\"Goal\"")
            .example("\"G()()()()(al)\"",  "\"Gooooal\"")
            .example("\"(al)G(al)()()G\"", "\"alGalooG\"")
            .example("\"G\"",              "\"G\"")
            .example("\"()\"",             "\"o\"")
            .gen(g -> {
                String[] tok = { "G", "()", "(al)" };
                int n = g.size(1, 25);
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < n; i++) b.append(tok[g.nextInt(0, 2)]);
                return new Object[]{ b.toString() };
            })
            .patterns("String", "Simulation")
            .limitMs(2000)
            .maxN(100000);
    }

    public String interpret(String command) {
        StringBuilder b = new StringBuilder();
        int i = 0;
        while (i < command.length()) {
            if (command.charAt(i) == 'G') { b.append('G'); i += 1; }
            else if (command.startsWith("()", i)) { b.append('o'); i += 2; }
            else { Pre.check(command.startsWith("(al)", i), "only G, () and (al)"); b.append("al"); i += 4; }
        }
        return b.toString();
    }
}
