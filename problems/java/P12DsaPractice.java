/*
 * P12 — DSA practice: the patterns that actually come up in a bank screen.
 *
 * RUN:  java problems/java/P12DsaPractice.java
 *
 * HOW TO USE THIS
 *   Every method below is a TODO with a test harness. Implement them, run, get PASS/FAIL.
 *   Solutions and complexity analysis in SOLUTIONS.md -- but write your own first.
 *
 *   CRITICAL for a HireVue-style recorded round: after each one, SAY OUT LOUD
 *     "This is O(n) time and O(k) space, where k is the number of distinct characters,
 *      because I make a single pass and the map holds at most one entry per distinct char."
 *   Practise that sentence for every problem. The recorded format gives you no feedback
 *   and no chance to be prompted, so the narration has to be a reflex. See the notes at
 *   the bottom of this file.
 *
 * THE PROBLEMS (the commonly-reported bank screen set)
 *   1.  reverseString            -- warm-up, two pointers
 *   2.  reverseWords             -- string manipulation, edge cases
 *   3.  wordFrequency            -- hashing, and ordering the output
 *   4.  firstNonRepeating        -- two passes vs LinkedHashMap
 *   5.  hasDuplicates            -- Set, and the early-exit
 *   6.  findDuplicates          -- return all of them
 *   7.  longestUniqueSubstring   -- SLIDING WINDOW. The one they most like.
 *   8.  isBalanced               -- stack
 *   9.  twoSum                   -- the canonical hash-map trade
 *   10. isAnagram                -- counting
 *   11. fizzBuzz                 -- yes, really; don't fumble it
 *   12. mergeIntervals          -- sorting + sweep (common follow-up)
 */

import java.util.*;
import java.util.stream.*;

public class P12DsaPractice {

    // ============================================================
    // 1. Reverse a string. O(n) time, O(n) space (Java strings are immutable).
    //    Do NOT just call new StringBuilder(s).reverse() in an interview without
    //    saying you know how to do it by hand.
    // ============================================================
    static String reverseString(String s) {
        throw new UnsupportedOperationException("TODO 1");
    }

    // ============================================================
    // 2. Reverse the ORDER OF WORDS. "the sky  is blue" -> "blue is sky the"
    //    Watch: leading/trailing/multiple spaces.
    // ============================================================
    static String reverseWords(String s) {
        throw new UnsupportedOperationException("TODO 2");
    }

    // ============================================================
    // 3. Count word frequency. Return counts keyed by lowercase word.
    //    Follow-up they always ask: "now return the top K by count."
    // ============================================================
    static Map<String, Integer> wordFrequency(String text) {
        throw new UnsupportedOperationException("TODO 3");
    }

    /** Top k words by count, descending; tie-break alphabetically ascending. */
    static List<String> topKWords(String text, int k) {
        throw new UnsupportedOperationException("TODO 3b");
    }

    // ============================================================
    // 4. First non-repeating character. Return it, or '\0' if none.
    //    "swiss" -> 'w'.   Think about how to do it in ONE pass.
    // ============================================================
    static char firstNonRepeating(String s) {
        throw new UnsupportedOperationException("TODO 4");
    }

    // ============================================================
    // 5. Does the array contain any duplicate?
    // ============================================================
    static boolean hasDuplicates(int[] a) {
        throw new UnsupportedOperationException("TODO 5");
    }

    // ============================================================
    // 6. Return every value that appears more than once, ascending.
    // ============================================================
    static List<Integer> findDuplicates(int[] a) {
        throw new UnsupportedOperationException("TODO 6");
    }

    // ============================================================
    // 7. Longest substring WITHOUT repeating characters -- return its LENGTH.
    //    "abcabcbb" -> 3 ("abc").   "bbbbb" -> 1.   "pwwkew" -> 3 ("wke").
    //    THE classic sliding-window problem. Aim for O(n), not O(n^2).
    // ============================================================
    static int longestUniqueSubstring(String s) {
        throw new UnsupportedOperationException("TODO 7");
    }

    // ============================================================
    // 8. Balanced parentheses: ()[]{} properly nested and matched.
    // ============================================================
    static boolean isBalanced(String s) {
        throw new UnsupportedOperationException("TODO 8");
    }

    // ============================================================
    // 9. Two sum: return the INDICES of two numbers adding to target, or null.
    //    Say the trade-off: O(n^2) brute force vs O(n) time / O(n) space with a map.
    // ============================================================
    static int[] twoSum(int[] nums, int target) {
        throw new UnsupportedOperationException("TODO 9");
    }

    // ============================================================
    // 10. Are the two strings anagrams? Case-insensitive, ignore spaces.
    // ============================================================
    static boolean isAnagram(String a, String b) {
        throw new UnsupportedOperationException("TODO 10");
    }

    // ============================================================
    // 11. FizzBuzz, 1..n inclusive. Don't fumble this one.
    // ============================================================
    static List<String> fizzBuzz(int n) {
        throw new UnsupportedOperationException("TODO 11");
    }

    // ============================================================
    // 12. Merge overlapping intervals. [[1,3],[2,6],[8,10]] -> [[1,6],[8,10]]
    //     Sort by start, then sweep. O(n log n).
    // ============================================================
    static int[][] mergeIntervals(int[][] intervals) {
        throw new UnsupportedOperationException("TODO 12");
    }

    // ############################################################
    // TEST HARNESS
    // ############################################################
    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        System.out.println("=== P12: DSA practice ===\n");

        // 1
        check("reverseString(\"hello\")",        () -> reverseString("hello"), "olleh");
        check("reverseString(\"\")",             () -> reverseString(""), "");
        check("reverseString(\"a\")",            () -> reverseString("a"), "a");

        // 2
        check("reverseWords(\"the sky is blue\")", () -> reverseWords("the sky is blue"), "blue is sky the");
        check("reverseWords(\"  hello   world  \")", () -> reverseWords("  hello   world  "), "world hello");
        check("reverseWords(\"single\")",        () -> reverseWords("single"), "single");

        // 3
        check("wordFrequency", () -> wordFrequency("the cat the dog THE cat"),
                Map.of("the", 3, "cat", 2, "dog", 1));
        check("topKWords k=2", () -> topKWords("the cat the dog THE cat bird", 2),
                List.of("the", "cat"));

        // 4
        check("firstNonRepeating(\"swiss\")",    () -> firstNonRepeating("swiss"), 'w');
        check("firstNonRepeating(\"aabb\")",     () -> firstNonRepeating("aabb"), '\0');
        check("firstNonRepeating(\"abcab\")",    () -> firstNonRepeating("abcab"), 'c');

        // 5
        check("hasDuplicates([1,2,3])",          () -> hasDuplicates(new int[]{1,2,3}), false);
        check("hasDuplicates([1,2,2])",          () -> hasDuplicates(new int[]{1,2,2}), true);
        check("hasDuplicates([])",               () -> hasDuplicates(new int[]{}), false);

        // 6
        check("findDuplicates([4,3,2,7,8,2,3,1])",
                () -> findDuplicates(new int[]{4,3,2,7,8,2,3,1}), List.of(2, 3));
        check("findDuplicates([1,2,3])",         () -> findDuplicates(new int[]{1,2,3}), List.of());

        // 7
        check("longestUniqueSubstring(\"abcabcbb\")", () -> longestUniqueSubstring("abcabcbb"), 3);
        check("longestUniqueSubstring(\"bbbbb\")",    () -> longestUniqueSubstring("bbbbb"), 1);
        check("longestUniqueSubstring(\"pwwkew\")",   () -> longestUniqueSubstring("pwwkew"), 3);
        check("longestUniqueSubstring(\"\")",         () -> longestUniqueSubstring(""), 0);
        check("longestUniqueSubstring(\"abcdef\")",   () -> longestUniqueSubstring("abcdef"), 6);

        // 8
        check("isBalanced(\"()[]{}\")",          () -> isBalanced("()[]{}"), true);
        check("isBalanced(\"([{}])\")",          () -> isBalanced("([{}])"), true);
        check("isBalanced(\"(]\")",              () -> isBalanced("(]"), false);
        check("isBalanced(\"(\")",               () -> isBalanced("("), false);
        check("isBalanced(\")\")",               () -> isBalanced(")"), false);
        check("isBalanced(\"\")",                () -> isBalanced(""), true);

        // 9
        checkArr("twoSum([2,7,11,15], 9)",       () -> twoSum(new int[]{2,7,11,15}, 9), new int[]{0,1});
        checkArr("twoSum([3,2,4], 6)",           () -> twoSum(new int[]{3,2,4}, 6), new int[]{1,2});
        check("twoSum([1,2], 99) == null",       () -> twoSum(new int[]{1,2}, 99) == null, true);

        // 10
        check("isAnagram(\"listen\",\"silent\")", () -> isAnagram("listen", "silent"), true);
        check("isAnagram(\"Dormitory\",\"Dirty Room\")", () -> isAnagram("Dormitory", "Dirty Room"), true);
        check("isAnagram(\"abc\",\"abd\")",       () -> isAnagram("abc", "abd"), false);

        // 11
        check("fizzBuzz(15) last 3", () -> {
            var r = fizzBuzz(15);
            return r.subList(r.size() - 3, r.size());
        }, List.of("13", "14", "FizzBuzz"));

        // 12
        checkArr2("mergeIntervals([[1,3],[2,6],[8,10]])",
                () -> mergeIntervals(new int[][]{{1,3},{2,6},{8,10}}), new int[][]{{1,6},{8,10}});
        checkArr2("mergeIntervals([[1,4],[4,5]])",
                () -> mergeIntervals(new int[][]{{1,4},{4,5}}), new int[][]{{1,5}});

        System.out.printf("%n===== %d passed, %d failed, %d not implemented =====%n",
                passed, failed, skipped);
        printNarrationNotes();
    }

    // --- harness plumbing ---
    interface Thunk<T> { T get() throws Exception; }

    static <T> void check(String name, Thunk<T> f, T expected) {
        try {
            T actual = f.get();
            if (Objects.equals(actual, expected)) { System.out.printf("  [PASS] %s%n", name); passed++; }
            else { System.out.printf("  [FAIL] %-45s expected %s but got %s%n", name, expected, actual); failed++; }
        } catch (UnsupportedOperationException e) {
            System.out.printf("  [TODO] %-45s %s%n", name, e.getMessage()); skipped++;
        } catch (Exception e) {
            System.out.printf("  [ERR ] %-45s threw %s%n", name, e); failed++;
        }
    }

    static void checkArr(String name, Thunk<int[]> f, int[] expected) {
        try {
            int[] actual = f.get();
            if (Arrays.equals(actual, expected)) { System.out.printf("  [PASS] %s%n", name); passed++; }
            else { System.out.printf("  [FAIL] %-45s expected %s but got %s%n",
                    name, Arrays.toString(expected), Arrays.toString(actual)); failed++; }
        } catch (UnsupportedOperationException e) {
            System.out.printf("  [TODO] %-45s %s%n", name, e.getMessage()); skipped++;
        } catch (Exception e) {
            System.out.printf("  [ERR ] %-45s threw %s%n", name, e); failed++;
        }
    }

    static void checkArr2(String name, Thunk<int[][]> f, int[][] expected) {
        try {
            int[][] actual = f.get();
            if (Arrays.deepEquals(actual, expected)) { System.out.printf("  [PASS] %s%n", name); passed++; }
            else { System.out.printf("  [FAIL] %-45s expected %s but got %s%n",
                    name, Arrays.deepToString(expected), Arrays.deepToString(actual)); failed++; }
        } catch (UnsupportedOperationException e) {
            System.out.printf("  [TODO] %-45s %s%n", name, e.getMessage()); skipped++;
        } catch (Exception e) {
            System.out.printf("  [ERR ] %-45s threw %s%n", name, e); failed++;
        }
    }

    static void printNarrationNotes() {
        System.out.println("""

                ############################################################
                NARRATING COMPLEXITY ON CAMERA (the HireVue skill)
                ############################################################

                A recorded round gives you NO feedback and NO prompting, so the
                narration must be automatic. Use this four-beat script every time:

                  1. RESTATE + CLARIFY (even to a camera).
                     "I'm reading this as: find the length of the longest substring
                      with no repeated characters. I'll assume ASCII input and that
                      an empty string returns zero."
                     -> On camera, STATE your assumptions out loud instead of asking.
                        That is what the assumption-checking mark is for.

                  2. NAME THE APPROACH BEFORE CODING.
                     "The brute force is to check every substring, which is O(n^2)
                      or O(n^3). I'll use a sliding window with a map from character
                      to its last index, which gets it to a single pass."

                  3. CODE IT, talking through the invariant, not every keystroke.
                     "left is the start of the current window; I move it past any
                      previous occurrence of the current character."

                  4. STATE COMPLEXITY WITH THE REASON, then edge cases.
                     "O(n) time -- each character is visited at most twice, once by
                      right and once when left advances past it. O(min(n, k)) space,
                      where k is the alphabet size, because the map holds at most one
                      entry per distinct character.
                      Edge cases: empty string returns 0; all-identical returns 1;
                      all-distinct returns n. I've covered those in the tests."

                THE FORMULA:  "O(x) time because <reason>. O(y) space because <what
                               the extra memory actually holds>."
                Naming WHAT THE SPACE HOLDS is what separates a real analysis from a
                recited one. "O(n) space" is a guess; "O(k) because the map holds one
                entry per distinct character" is an analysis.

                THINGS THAT LOSE MARKS ON CAMERA
                  * Silence while thinking. Narrate the dead ends: "my first instinct
                    was to sort, but that loses the original indices, so instead..."
                  * Saying "O(n)" with no justification.
                  * Ignoring edge cases: empty, single element, all-same, all-distinct,
                    negative numbers, overflow, null input, Unicode/surrogate pairs.
                  * Forgetting that Java strings are IMMUTABLE, so any "in-place"
                    string answer is really O(n) space. Say so.
                  * Not testing your own code. Walk one example through by hand.
                  * Running out of time silently. If you do, say what remains:
                    "with more time I'd handle Unicode surrogate pairs and add tests
                     for the empty case."

                COMPLEXITIES TO HAVE MEMORISED
                  HashMap get/put            O(1) avg,  O(log n) worst (treeified bins)
                  TreeMap get/put            O(log n)
                  ArrayList get              O(1);  add O(1) amortised;  insert O(n)
                  LinkedList get             O(n)      <- and 178x slower measured, see P11
                  ArrayDeque push/pop        O(1)
                  Arrays.sort (primitives)   O(n log n)  dual-pivot quicksort
                  Collections.sort (objects) O(n log n)  TimSort, and it is STABLE
                  binary search              O(log n)
                  PriorityQueue offer/poll   O(log n);  peek O(1)
                  String concat in a loop    O(n^2)      <- use StringBuilder
                  List.contains in a loop    O(n*m)      <- 114x measured, see P11 Part 5

                SCALA NOTE (this JD is Scala)
                  If you get to choose the language, idiomatic Scala reads very well:
                    s.reverse
                    text.toLowerCase.split("\\\\s+").groupMapReduce(identity)(_ => 1)(_ + _)
                    s.sliding(2).forall { case Seq(a, b) => a <= b }
                  BUT know the cost: `List` apply is O(n) (quiz 09 Q3), and
                  groupBy/tuples allocate heavily (S03AllocationPressure.scala).
                  If asked for an in-place O(1)-space answer, use Array and a while
                  loop -- and SAY why you dropped out of the idiomatic style.""");
    }
}
