/**
 * How the judge decides whether two answers agree.
 *
 * "Correct" is not one thing. A subsets answer is a set of sets; a permutations answer
 * is a set of sequences; find-peak has many right answers and needs a predicate.
 */
public enum Cmp {
    /** Deep structural equality. Order significant everywhere. */
    EXACT,

    /** Flat collection, order irrelevant. e.g. top-k-frequent, minimum-height-trees. */
    MULTISET,

    /** Outer collection unordered, inner order significant. e.g. permutations, n-queens. */
    SET_OF_SEQS,

    /** Outer and inner both unordered. e.g. subsets, 3sum, combination-sum. */
    SET_OF_SETS,

    /** Floating point within Spec.eps. */
    EPSILON,

    /**
     * The "return k, first k slots matter" convention (LC 26, 27, 80, and the drills
     * that precede them). The method returns a length and mutates an array; anything
     * at or beyond index k is explicitly undefined and must NOT be compared.
     */
    LENGTH_AND_PREFIX,

    /** Spec-supplied predicate. Used when many answers are valid. */
    VALIDATE
}
