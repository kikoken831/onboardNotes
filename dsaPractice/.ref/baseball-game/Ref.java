import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("calPoints", T.STRING_ARRAY).returns(T.INT)
            .example("[\"5\",\"2\",\"C\",\"D\",\"+\"]",                   "30")
            .example("[\"5\",\"-2\",\"4\",\"C\",\"D\",\"9\",\"+\",\"+\"]", "27")
            .example("[\"1\",\"C\"]",                                   "0")
            .example("[\"-3\"]",                                        "-3")
            .gen(g -> {
                int n = g.size(1, 40);
                String[] ops = new String[n];
                int depth = 0;
                for (int i = 0; i < n; i++) {
                    int roll = g.nextInt(0, 9);
                    if (roll <= 3 || depth == 0)       { ops[i] = String.valueOf(g.nextInt(-30, 30)); depth++; }
                    else if (roll <= 5)                { ops[i] = "C"; depth--; }
                    else if (roll <= 7)                { ops[i] = "D"; depth++; }
                    else if (depth >= 2)               { ops[i] = "+"; depth++; }
                    else                               { ops[i] = "D"; depth++; }
                }
                return new Object[]{ ops };
            })
            .patterns("Array", "Stack", "Simulation")
            .limitMs(2000)
            .maxN(20000);
    }

    public int calPoints(String[] operations) {
        Deque<Integer> st = new ArrayDeque<>();
        for (String op : operations) {
            switch (op) {
                case "C": Pre.check(!st.isEmpty(), "C has a previous score"); st.pop(); break;
                case "D": Pre.check(!st.isEmpty(), "D has a previous score"); st.push(2 * st.peek()); break;
                case "+": {
                    Pre.check(st.size() >= 2, "+ has two previous scores");
                    int a = st.pop(), b = st.peek();
                    st.push(a);
                    st.push(a + b);
                    break;
                }
                default: st.push(Integer.parseInt(op));
            }
        }
        int sum = 0;
        for (int x : st) sum += x;
        return sum;
    }
}
