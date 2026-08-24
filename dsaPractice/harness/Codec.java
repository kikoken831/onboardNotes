import java.util.ArrayList;
import java.util.List;

/**
 * Parses and prints LeetCode literal notation.
 *
 *   [2,7,11,15]          -> int[]
 *   [[1,2],[3]]          -> int[][] or List&lt;List&lt;Integer&gt;&gt;
 *   ["ab","cd"]          -> String[]
 *   [1,2,3,null,4]       -> TreeNode (level order, LeetCode's own encoding)
 *   "hello"              -> String
 *   'x'                  -> char
 *
 * Parsing goes through a neutral intermediate (Long / Double / String / Boolean /
 * null / List&lt;Object&gt;) and is then coerced to the declared type. That keeps the
 * grammar in one place and the type-specific logic in another.
 */
public final class Codec {

    private Codec() {}

    // ---------------------------------------------------------------- parsing

    /** Parse one LC literal and coerce it to the declared type. */
    public static Object parse(String src, T type) {
        Object raw = new P(src).value(true);
        return coerce(raw, type);
    }

    /**
     * Split a comma-separated argument list at top level, respecting nesting and
     * quotes, then parse each piece as its declared type.
     */
    public static Object[] parseArgs(String src, T[] types) {
        List<String> parts = splitTopLevel(src);
        if (parts.size() != types.length) {
            throw new IllegalArgumentException(
                "expected " + types.length + " args, got " + parts.size() + " in: " + src);
        }
        Object[] out = new Object[types.length];
        for (int i = 0; i < types.length; i++) out[i] = parse(parts.get(i), types[i]);
        return out;
    }

    static List<String> splitTopLevel(String s) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        boolean inStr = false, esc = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc)                       { cur.append(c); esc = false; continue; }
            if (inStr) {
                cur.append(c);
                if (c == '\\')      esc = true;
                else if (c == '"')  inStr = false;
                continue;
            }
            switch (c) {
                case '"':            inStr = true;  cur.append(c); break;
                case '[': case '{':  depth++;       cur.append(c); break;
                case ']': case '}':  depth--;       cur.append(c); break;
                case ',':
                    if (depth == 0) { out.add(cur.toString()); cur.setLength(0); }
                    else              cur.append(c);
                    break;
                default: cur.append(c);
            }
        }
        if (cur.toString().trim().length() > 0 || !out.isEmpty()) out.add(cur.toString());
        return out;
    }

    /** Recursive-descent reader for the literal grammar. */
    private static final class P {
        private final String s;
        private int i;
        P(String s) { this.s = s; }

        Object value(boolean requireEof) {
            ws();
            Object v = value0();
            if (requireEof) { ws(); if (i < s.length()) throw err("trailing input"); }
            return v;
        }

        private Object value0() {
            ws();
            if (i >= s.length()) throw err("unexpected end");
            char c = s.charAt(i);
            if (c == '[')  return array();
            if (c == '"')  return string('"');
            if (c == '\'') return string('\'');
            if (starts("null"))  { i += 4; return null; }
            if (starts("true"))  { i += 4; return Boolean.TRUE; }
            if (starts("false")) { i += 5; return Boolean.FALSE; }
            return number();
        }

        private List<Object> array() {
            expect('[');
            List<Object> out = new ArrayList<>();
            ws();
            if (peek() == ']') { i++; return out; }
            while (true) {
                out.add(value0());
                ws();
                char c = peek();
                if (c == ',')      { i++; continue; }
                if (c == ']')      { i++; return out; }
                throw err("expected , or ] but saw '" + c + "'");
            }
        }

        private String string(char quote) {
            expect(quote);
            StringBuilder b = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw err("unterminated string");
                char c = s.charAt(i++);
                if (c == quote) return b.toString();
                if (c == '\\') {
                    if (i >= s.length()) throw err("dangling escape");
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n':  b.append('\n'); break;
                        case 't':  b.append('\t'); break;
                        case 'r':  b.append('\r'); break;
                        case '\\': b.append('\\'); break;
                        case '"':  b.append('"');  break;
                        case '\'': b.append('\''); break;
                        default:   b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
        }

        private Object number() {
            int start = i;
            if (peek() == '-' || peek() == '+') i++;
            boolean real = false;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') { i++; }
                else if (c == '.' || c == 'e' || c == 'E') { real = true; i++; }
                else if ((c == '-' || c == '+') && (s.charAt(i - 1) == 'e' || s.charAt(i - 1) == 'E')) { i++; }
                else break;
            }
            if (start == i) throw err("expected a value");
            String tok = s.substring(start, i);
            return real ? (Object) Double.valueOf(tok) : (Object) Long.valueOf(tok);
        }

        private boolean starts(String w) { return s.startsWith(w, i); }
        private char peek()   { return i < s.length() ? s.charAt(i) : '\0'; }
        private void expect(char c) { if (peek() != c) throw err("expected '" + c + "'"); i++; }
        private void ws()     { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        private IllegalArgumentException err(String m) {
            return new IllegalArgumentException("parse error at " + i + ": " + m + " in <" + s + ">");
        }
    }

    // --------------------------------------------------------------- coercion

    @SuppressWarnings("unchecked")
    private static Object coerce(Object raw, T type) {
        switch (type) {
            case INT:     return (int) num(raw);
            case LONG:    return num(raw);
            case DOUBLE:  return dbl(raw);
            case BOOL:    return (Boolean) raw;
            case CHAR:    return ((String) raw).charAt(0);
            case STRING:  return (String) raw;

            case INT_ARRAY: {
                List<Object> l = (List<Object>) raw;
                int[] a = new int[l.size()];
                for (int k = 0; k < a.length; k++) a[k] = (int) num(l.get(k));
                return a;
            }
            case LONG_ARRAY: {
                List<Object> l = (List<Object>) raw;
                long[] a = new long[l.size()];
                for (int k = 0; k < a.length; k++) a[k] = num(l.get(k));
                return a;
            }
            case DOUBLE_ARRAY: {
                List<Object> l = (List<Object>) raw;
                double[] a = new double[l.size()];
                for (int k = 0; k < a.length; k++) a[k] = dbl(l.get(k));
                return a;
            }
            case BOOL_ARRAY: {
                List<Object> l = (List<Object>) raw;
                boolean[] a = new boolean[l.size()];
                for (int k = 0; k < a.length; k++) a[k] = (Boolean) l.get(k);
                return a;
            }
            case CHAR_ARRAY: {
                if (raw instanceof String) return ((String) raw).toCharArray();
                List<Object> l = (List<Object>) raw;
                char[] a = new char[l.size()];
                for (int k = 0; k < a.length; k++) a[k] = ((String) l.get(k)).charAt(0);
                return a;
            }
            case STRING_ARRAY: {
                List<Object> l = (List<Object>) raw;
                String[] a = new String[l.size()];
                for (int k = 0; k < a.length; k++) a[k] = (String) l.get(k);
                return a;
            }
            case INT_ARRAY_2D: {
                List<Object> l = (List<Object>) raw;
                int[][] a = new int[l.size()][];
                for (int k = 0; k < a.length; k++) a[k] = (int[]) coerce(l.get(k), T.INT_ARRAY);
                return a;
            }
            case CHAR_ARRAY_2D: {
                List<Object> l = (List<Object>) raw;
                char[][] a = new char[l.size()][];
                for (int k = 0; k < a.length; k++) a[k] = (char[]) coerce(l.get(k), T.CHAR_ARRAY);
                return a;
            }
            case STRING_ARRAY_2D: {
                List<Object> l = (List<Object>) raw;
                String[][] a = new String[l.size()][];
                for (int k = 0; k < a.length; k++) a[k] = (String[]) coerce(l.get(k), T.STRING_ARRAY);
                return a;
            }

            case LIST_INT: {
                List<Object> l = (List<Object>) raw;
                List<Integer> o = new ArrayList<>(l.size());
                for (Object x : l) o.add((int) num(x));
                return o;
            }
            case LIST_DOUBLE: {
                List<Object> l = (List<Object>) raw;
                List<Double> o = new ArrayList<>(l.size());
                for (Object x : l) o.add(dbl(x));
                return o;
            }
            case LIST_STRING: {
                List<Object> l = (List<Object>) raw;
                List<String> o = new ArrayList<>(l.size());
                for (Object x : l) o.add((String) x);
                return o;
            }
            case LIST_LIST_INT: {
                List<Object> l = (List<Object>) raw;
                List<List<Integer>> o = new ArrayList<>(l.size());
                for (Object x : l) o.add((List<Integer>) coerce(x, T.LIST_INT));
                return o;
            }
            case LIST_LIST_STRING: {
                List<Object> l = (List<Object>) raw;
                List<List<String>> o = new ArrayList<>(l.size());
                for (Object x : l) o.add((List<String>) coerce(x, T.LIST_STRING));
                return o;
            }

            case LIST_NODE: return buildList((List<Object>) raw);
            case TREE_NODE: return buildTree((List<Object>) raw);

            case VOID: return null;
            default: throw new IllegalStateException("no coercion for " + type);
        }
    }

    private static long num(Object o) {
        if (o instanceof Long)   return (Long) o;
        if (o instanceof Double) return (long) (double) (Double) o;
        throw new IllegalArgumentException("not a number: " + o);
    }

    private static double dbl(Object o) {
        if (o instanceof Double) return (Double) o;
        if (o instanceof Long)   return (Long) o;
        throw new IllegalArgumentException("not a number: " + o);
    }

    // ------------------------------------------------------ structure builders

    public static ListNode buildList(List<Object> vals) {
        ListNode dummy = new ListNode(), tail = dummy;
        for (Object v : vals) { tail.next = new ListNode((int) num(v)); tail = tail.next; }
        return dummy.next;
    }

    public static ListNode buildList(int[] vals) {
        ListNode dummy = new ListNode(), tail = dummy;
        for (int v : vals) { tail.next = new ListNode(v); tail = tail.next; }
        return dummy.next;
    }

    /**
     * LeetCode's level-order tree encoding: nulls are placeholders for absent children
     * and are NOT emitted for the children of an absent node. Hence the queue walk
     * rather than 2i+1 index arithmetic.
     */
    public static TreeNode buildTree(List<Object> vals) {
        if (vals == null || vals.isEmpty() || vals.get(0) == null) return null;
        TreeNode root = new TreeNode((int) num(vals.get(0)));
        java.util.ArrayDeque<TreeNode> q = new java.util.ArrayDeque<>();
        q.add(root);
        int i = 1;
        while (!q.isEmpty() && i < vals.size()) {
            TreeNode n = q.poll();
            if (i < vals.size()) {
                Object v = vals.get(i++);
                if (v != null) { n.left = new TreeNode((int) num(v)); q.add(n.left); }
            }
            if (i < vals.size()) {
                Object v = vals.get(i++);
                if (v != null) { n.right = new TreeNode((int) num(v)); q.add(n.right); }
            }
        }
        return root;
    }

    // ------------------------------------------------------------- printing

    /** Render any judged value back into LC literal notation, for display. */
    public static String print(Object v) {
        if (v == null) return "null";
        if (v instanceof String)   return "\"" + escape((String) v) + "\"";
        if (v instanceof Character) return "\"" + v + "\"";
        if (v instanceof Double)   return trimDouble((Double) v);
        if (v instanceof ListNode) return printList((ListNode) v);
        if (v instanceof TreeNode) return printTree((TreeNode) v);
        if (v instanceof boolean[]) {
            boolean[] a = (boolean[]) v; StringBuilder b = new StringBuilder("[");
            for (int i = 0; i < a.length; i++) { if (i > 0) b.append(','); b.append(a[i]); }
            return b.append(']').toString();
        }
        if (v.getClass().isArray()) {
            int n = java.lang.reflect.Array.getLength(v);
            StringBuilder b = new StringBuilder("[");
            for (int i = 0; i < n; i++) {
                if (i > 0) b.append(',');
                b.append(print(java.lang.reflect.Array.get(v, i)));
            }
            return b.append(']').toString();
        }
        if (v instanceof List) {
            StringBuilder b = new StringBuilder("[");
            List<?> l = (List<?>) v;
            for (int i = 0; i < l.size(); i++) { if (i > 0) b.append(','); b.append(print(l.get(i))); }
            return b.append(']').toString();
        }
        return String.valueOf(v);
    }

    /** Cycle-safe: a cyclic list would otherwise hang the judge on 141/142. */
    public static String printList(ListNode head) {
        StringBuilder b = new StringBuilder("[");
        java.util.IdentityHashMap<ListNode, Integer> seen = new java.util.IdentityHashMap<>();
        int i = 0;
        for (ListNode p = head; p != null; p = p.next, i++) {
            Integer at = seen.get(p);
            if (at != null) { b.append(",...cycle back to index ").append(at); break; }
            seen.put(p, i);
            if (i > 0) b.append(',');
            b.append(p.val);
        }
        return b.append(']').toString();
    }

    public static String printTree(TreeNode root) {
        List<String> out = new ArrayList<>();
        java.util.ArrayDeque<TreeNode> q = new java.util.ArrayDeque<>();
        if (root != null) q.add(root);
        while (!q.isEmpty()) {
            TreeNode n = q.poll();
            if (n == null) { out.add("null"); continue; }
            out.add(String.valueOf(n.val));
            q.add(n.left);
            q.add(n.right);
        }
        while (!out.isEmpty() && out.get(out.size() - 1).equals("null")) out.remove(out.size() - 1);
        return "[" + String.join(",", out) + "]";
    }

    private static String trimDouble(double d) {
        if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d) + ".0";
        }
        return String.valueOf(d);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t");
    }
}
