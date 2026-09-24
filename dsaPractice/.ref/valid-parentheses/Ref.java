import java.util.ArrayDeque;
import java.util.Deque;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isValid", T.STRING).returns(T.BOOL)
            .example("\"()\"", "true").example("\"()[]{}\"", "true").example("\"(]\"", "false")
            .example("\"([)]\"", "false").example("\"{[]}\"", "true").example("\"(\"", "false").example("\"]\"", "false")
            .gen(g -> {
                if (g.chance(0.5)) {                       // plant a balanced string, maybe corrupt it
                    StringBuilder b = new StringBuilder();
                    Deque<Character> st = new ArrayDeque<>();
                    int n = g.size(1, 20);
                    for (int i = 0; i < n; i++) {
                        if (st.isEmpty() || g.chance(0.5)) { char o = "([{".charAt(g.nextInt(0, 2)); b.append(o); st.push(o); }
                        else { char o = st.pop(); b.append(o == '(' ? ')' : o == '[' ? ']' : '}'); }
                    }
                    while (!st.isEmpty()) { char o = st.pop(); b.append(o == '(' ? ')' : o == '[' ? ']' : '}'); }
                    if (g.chance(0.3)) b.setCharAt(g.nextInt(0, b.length() - 1), "()[]{}".charAt(g.nextInt(0, 5)));
                    return new Object[]{ b.toString() };
                }
                return new Object[]{ g.string(1, 20, "()[]{}") };
            })
            .patterns("String", "Stack").limitMs(2000).maxN(200000);
    }
    public boolean isValid(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') st.push(c);
            else {
                if (st.isEmpty()) return false;
                char o = st.pop();
                if ((c == ')' && o != '(') || (c == ']' && o != '[') || (c == '}' && o != '{')) return false;
            }
        }
        return st.isEmpty();
    }
}
