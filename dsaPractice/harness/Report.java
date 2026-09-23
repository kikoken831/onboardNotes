/**
 * Terminal output for the judge.
 *
 * Machine-readable lines are prefixed with "::" so the Python CLI can parse verdicts
 * out of the stream without guessing; everything else is for you to read.
 */
public final class Report {

    private Report() {}

    private static final boolean COLOUR = System.console() != null
            && !"dumb".equals(System.getenv("TERM"))
            && System.getenv("NO_COLOR") == null;

    private static String c(String code, String s) { return COLOUR ? "[" + code + "m" + s + "[0m" : s; }
    private static String green(String s) { return c("32", s); }
    private static String red(String s)   { return c("31", s); }
    private static String dim(String s)   { return c("2",  s); }
    private static String bold(String s)  { return c("1",  s); }

    public static void ok(String phase, String detail) {
        System.out.println("  " + pad(phase) + green("✓") + "  " + dim(detail));
    }

    public static void section(String phase, String detail) {
        System.out.println("  " + pad(phase) + red("✗") + "  " + detail);
    }

    public static void warn(String message) {
        System.out.println("  " + pad("note") + dim(message));
    }

    public static void example(boolean passed, String args, String actual,
                               String expected, String why) {
        System.out.println("  " + pad("fixed") + red("✗") + "  " + args);
        if (actual != null) System.out.println("      yours     " + actual);
        System.out.println("      expected  " + expected);
        if (why != null)    System.out.println("      " + dim(why));
    }

    public static void timing(long solNanos, long refNanos) {
        String ratio = refNanos > 0
            ? String.format("%.1f× reference", (double) solNanos / refNanos)
            : "";
        System.out.println("  " + pad("time")
            + dim(ms(solNanos) + " worst case   " + ratio));
    }

    public static void curve(int size, int prevSize, long nanos, long prevNanos) {
        String growth = "";
        if (prevNanos > 0 && prevSize > 0 && size > prevSize) {
            double sizeRatio = (double) size / prevSize;
            double timeRatio = (double) nanos / prevNanos;
            growth = String.format("   n ×%.1f → t ×%.1f   %s",
                    sizeRatio, timeRatio, classify(sizeRatio, timeRatio));
        }
        System.out.println("  " + pad("") + String.format("n=%-10d %-11s%s", size, ms(nanos), dim(growth)));
    }

    /** Left-hand column label, matching ok()/section() alignment. */
    public static String label(String phase) { return pad(phase); }

    /**
     * A rough label, not a proof. Doubling n and seeing time roughly double is
     * consistent with O(n); seeing it quadruple is consistent with O(n^2). Constant
     * factors and JIT noise make anything finer than this dishonest.
     */
    private static String classify(double sizeRatio, double timeRatio) {
        if (sizeRatio <= 1.0) return "";
        double exponent = Math.log(timeRatio) / Math.log(sizeRatio);
        if (exponent < 0.35) return "looks ~O(1)";
        if (exponent < 0.75) return "looks ~O(log n)";
        if (exponent < 1.35) return "looks ~O(n)";
        if (exponent < 1.75) return "looks ~O(n log n)";
        if (exponent < 2.40) return "looks ~O(n²)";
        return "looks worse than O(n²)";
    }

    public static void verdict(String v, String detail) {
        String tag = Judge.AC.equals(v) ? green(bold(v)) : red(bold(v));
        System.out.println();
        System.out.println("  " + tag + (detail != null ? dim("  " + detail) : ""));
        System.out.println("::verdict " + v);
    }

    public static void fail(String v, String detail) {
        System.out.println("      " + detail);
        System.out.println();
        System.out.println("  " + red(bold(v)));
        System.out.println("::verdict " + v);
    }

    /** Render an argument list for display, truncating anything unreadably long. */
    public static String args(Object[] a) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) b.append(", ");
            String s = Codec.print(a[i]);
            if (s.length() > 160) s = s.substring(0, 157) + "...";
            b.append(s);
        }
        return b.toString();
    }

    /** Best-effort "size" of an input, for the growth curve. */
    public static int sizeOf(Object[] a) {
        int total = 0;
        for (Object v : a) {
            if (v == null) continue;
            if (v.getClass().isArray()) {
                int n = java.lang.reflect.Array.getLength(v);
                total += n;
                for (int i = 0; i < n; i++) {
                    Object e = java.lang.reflect.Array.get(v, i);
                    if (e != null && e.getClass().isArray()) {
                        total += java.lang.reflect.Array.getLength(e);
                    }
                }
            } else if (v instanceof java.util.List) {
                total += ((java.util.List<?>) v).size();
            } else if (v instanceof Calls) {
                total += ((Calls) v).size();
            } else if (v instanceof String) {
                total += ((String) v).length();
            } else if (v instanceof ListNode) {
                int n = 0;
                for (ListNode p = (ListNode) v; p != null && n < 5_000_000; p = p.next) n++;
                total += n;
            }
        }
        return Math.max(1, total);
    }

    private static String ms(long nanos) {
        if (nanos < 1_000_000) return String.format("%.2fms", nanos / 1e6);
        return String.format("%dms", nanos / 1_000_000);
    }

    private static String pad(String s) {
        StringBuilder b = new StringBuilder(s);
        while (b.length() < 9) b.append(' ');
        return dim(b.toString());
    }
}
