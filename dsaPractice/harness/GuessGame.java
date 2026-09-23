/**
 * The pre-defined API for LC 374, Guess Number Higher or Lower.
 *
 * On LeetCode your Solution `extends GuessGame` and calls `guess(num)`; the pick is
 * hidden from you. Here the judge installs the pick before every call via
 * Spec.hidden(), so the reference and your solution each face the same pick without
 * either being able to read it.
 *
 *   guess(num) == -1   your guess is higher than the pick   (num > pick)
 *   guess(num) ==  1   your guess is lower than the pick    (num < pick)
 *   guess(num) ==  0   correct
 */
public class GuessGame {

    private static int pick;
    private static long calls;

    /** Judge-only. Not part of the LeetCode API. */
    public static void install(int p) { pick = p; calls = 0; }

    public static long callsMade() { return calls; }

    public int guess(int num) {
        calls++;
        return Integer.compare(pick, num);
    }
}
