/*
 * P11 — Collections: measure the claims, then break the contract.
 *
 * RUN:  java problems/java/P11Collections.java
 *
 * TASK
 *   1. PART 1: predict the ranking of HashMap / LinkedHashMap / TreeMap for put and get.
 *      By roughly what factor? Does the result support the crib-note claim that
 *      "TreeMap is good for frequent add/remove"? (quiz 09 Q2)
 *   2. PART 2: what does TreeMap give you that HashMap cannot, at any price?
 *   3. PART 3: predict the output of each of the four broken-contract cases.
 *      Case C is the one that costs people whole afternoons.
 *   4. PART 4: predict the ranking, and by what factor. Then explain why the
 *      "textbook" answer about LinkedList is wrong.
 *   5. PART 5: why is List.contains inside a loop the most common real
 *      performance bug in ordinary code?
 */

import java.util.*;
import java.util.function.Supplier;

public class P11Collections {

    static final int N = 200_000;

    public static void main(String[] args) {
        part1_mapPerformance();
        part2_whatTreeMapBuys();
        part3_hashCodeEqualsContract();
        part4_listPerformance();
        part5_containsIsOn();
    }

    // ==================================================================
    static void part1_mapPerformance() {
        System.out.println("=== PART 1: HashMap vs LinkedHashMap vs TreeMap (" + N + " entries) ===");
        System.out.printf("%-16s %12s %12s %12s%n", "", "put (ms)", "get (ms)", "iterate (ms)");

        benchMap("HashMap", HashMap::new);
        benchMap("LinkedHashMap", LinkedHashMap::new);
        benchMap("TreeMap", TreeMap::new);

        System.out.println("""

                  HashMap and LinkedHashMap are O(1); TreeMap is O(log n) -- with n = 200k
                  that is ~17 comparisons per operation, each a virtual compareTo() call.

                  So the common crib-note claim that "TreeMap is good for frequent
                  add/remove" is BACKWARDS as a speed claim. The correct version:
                  if you need the data KEPT IN SORTED ORDER while it is frequently
                  modified, TreeMap maintains that order incrementally at O(log n),
                  which beats re-sorting a list on every change. The advantage is the
                  ORDERING, not the mutation speed. (quiz 09 Q2)""");
    }

    static void benchMap(String name, Supplier<Map<Integer, String>> factory) {
        // Shuffled keys, so we are not measuring a best case for the tree.
        Integer[] keys = new Integer[N];
        for (int i = 0; i < N; i++) keys[i] = i;
        Collections.shuffle(Arrays.asList(keys), new Random(42));

        Map<Integer, String> map = factory.get();

        long t0 = System.nanoTime();
        for (Integer k : keys) map.put(k, "v");
        long put = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        long hits = 0;
        for (Integer k : keys) if (map.get(k) != null) hits++;
        long get = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        long sum = 0;
        for (Integer k : map.keySet()) sum += k;
        long iterate = (System.nanoTime() - t2) / 1_000_000;

        System.out.printf("%-16s %12d %12d %12d   (hits=%d, checksum=%d)%n",
                name, put, get, iterate, hits, sum);
    }

    // ==================================================================
    static void part2_whatTreeMapBuys() {
        System.out.println("\n=== PART 2: what TreeMap gives you that HashMap cannot ===");

        var hash = new HashMap<Integer, String>();
        var tree = new TreeMap<Integer, String>();
        for (int i : new int[]{50, 10, 40, 20, 30}) { hash.put(i, "v" + i); tree.put(i, "v" + i); }

        System.out.println("  HashMap iteration order: " + hash.keySet()
                + "   <-- unspecified, and it CHANGES on resize");
        System.out.println("  TreeMap iteration order: " + tree.keySet()
                + "   <-- always sorted");

        System.out.println("\n  NavigableMap operations -- the actual reason to choose TreeMap:");
        System.out.println("    firstKey()      = " + tree.firstKey());
        System.out.println("    lastKey()       = " + tree.lastKey());
        System.out.println("    floorKey(35)    = " + tree.floorKey(35)    + "   (greatest key <= 35)");
        System.out.println("    ceilingKey(35)  = " + tree.ceilingKey(35)  + "   (least key >= 35)");
        System.out.println("    higherKey(30)   = " + tree.higherKey(30));
        System.out.println("    headMap(30)     = " + tree.headMap(30));
        System.out.println("    subMap(20, 45)  = " + tree.subMap(20, 45)  + "   <-- RANGE QUERY");
        System.out.println("    descendingMap() = " + tree.descendingMap());

        System.out.println("\n  Nulls -- and the crib note is only half right (quiz 09 Q2):");
        hash.put(null, "hashmap allows ONE null key");
        System.out.println("    HashMap null key : OK -> " + hash.get(null));
        tree.put(99, null);
        System.out.println("    TreeMap null VALUE: OK -> tree.get(99) = " + tree.get(99));
        try {
            tree.put(null, "x");
            System.out.println("    TreeMap null KEY : unexpectedly allowed");
        } catch (NullPointerException e) {
            System.out.println("    TreeMap null KEY : NullPointerException"
                    + "   <-- keys only. Values are fine.");
        }
    }

    // ==================================================================
    static void part3_hashCodeEqualsContract() {
        System.out.println("\n=== PART 3: breaking the hashCode/equals contract ===");

        // --- Case A: equals overridden, hashCode NOT ---
        System.out.println("\n  CASE A: equals() overridden, hashCode() not");
        var mapA = new HashMap<BadKey, String>();
        mapA.put(new BadKey("k1"), "value");
        BadKey lookupA = new BadKey("k1");
        System.out.println("    new BadKey(\"k1\").equals(existing) = " + lookupA.equals(new BadKey("k1")));
        System.out.println("    map.get(equal key)               = " + mapA.get(lookupA)
                + "   <-- NULL! different buckets");
        var setA = new HashSet<BadKey>();
        setA.add(new BadKey("k1"));
        setA.add(new BadKey("k1"));
        System.out.println("    set.size() after adding 2 equals = " + setA.size()
                + "   <-- a Set containing duplicates");

        // --- Case B: done correctly ---
        System.out.println("\n  CASE B: both overridden (or use a record)");
        var mapB = new HashMap<GoodKey, String>();
        mapB.put(new GoodKey("k1"), "value");
        System.out.println("    map.get(equal key)               = " + mapB.get(new GoodKey("k1")));
        var setB = new HashSet<GoodKey>();
        setB.add(new GoodKey("k1"));
        setB.add(new GoodKey("k1"));
        System.out.println("    set.size() after adding 2 equals = " + setB.size() + "   <-- correct");

        // --- Case C: MUTATING a key after insertion. The expensive one. ---
        System.out.println("\n  CASE C: mutating a key after it is in the map");
        var mapC = new HashMap<MutableKey, String>();
        var key = new MutableKey("original");
        mapC.put(key, "value");
        System.out.println("    before mutation: get(key) = " + mapC.get(key));
        key.name = "mutated";                       // hashCode now differs
        System.out.println("    after  mutation: get(key) = " + mapC.get(key)
                + "   <-- NULL, using the SAME object reference!");
        System.out.println("    containsKey(key)          = " + mapC.containsKey(key));
        System.out.println("    but map.size()            = " + mapC.size() + "   <-- still there!");
        System.out.println("    remove(key) returns       = " + mapC.remove(key));
        System.out.println("    size after remove         = " + mapC.size()
                + "   <-- UNREACHABLE AND UNREMOVABLE");
        System.out.print("    the entry, via iteration   : ");
        mapC.forEach((k, v) -> System.out.println("key=" + k.name + " value=" + v));
        System.out.println("    --> THIS is why map keys must be immutable. (quiz 09 Q5)");

        // --- Case D: a legal but terrible hashCode ---
        System.out.println("\n  CASE D: legal but constant hashCode -- correctness OK, performance not");
        int n = 30_000;
        var good = new HashMap<GoodKey, Integer>();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) good.put(new GoodKey("k" + i), i);
        for (int i = 0; i < n; i++) good.get(new GoodKey("k" + i));
        long goodMs = (System.nanoTime() - t0) / 1_000_000;

        var terrible = new HashMap<ConstantHashKey, Integer>();
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) terrible.put(new ConstantHashKey("k" + i), i);
        for (int i = 0; i < n; i++) terrible.get(new ConstantHashKey("k" + i));
        long badMs = (System.nanoTime() - t1) / 1_000_000;

        System.out.printf("    good hashCode:     %5d ms%n", goodMs);
        System.out.printf("    constant hashCode: %5d ms   (%.0fx slower)%n",
                badMs, (double) badMs / Math.max(goodMs, 1));
        System.out.println("    Every entry lands in ONE bucket. Java 8+ treeifies it, so it is");
        System.out.println("    O(log n) rather than O(n) -- still far worse than O(1), and");
        System.out.println("    pre-Java-8 this was a DoS vector via crafted collisions.");
    }

    /** equals but no hashCode -- the classic bug. */
    static final class BadKey {
        final String name;
        BadKey(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof BadKey b && Objects.equals(name, b.name);
        }
        // NO hashCode() -- inherits Object's identity hash
    }

    /** Correct. (A `record GoodKey(String name)` would generate exactly this.) */
    static final class GoodKey {
        final String name;
        GoodKey(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof GoodKey g && Objects.equals(name, g.name);
        }
        @Override public int hashCode() { return Objects.hash(name); }
    }

    /** Correct equals/hashCode, but MUTABLE -- so unusable as a key. */
    static final class MutableKey {
        String name;                                 // not final!
        MutableKey(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof MutableKey m && Objects.equals(name, m.name);
        }
        @Override public int hashCode() { return Objects.hash(name); }
    }

    /** Contract-compliant and catastrophic. */
    static final class ConstantHashKey {
        final String name;
        ConstantHashKey(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof ConstantHashKey c && Objects.equals(name, c.name);
        }
        @Override public int hashCode() { return 1; }      // legal! and awful
    }

    // ==================================================================
    static void part4_listPerformance() {
        System.out.println("\n=== PART 4: ArrayList vs LinkedList ===");
        int n = 100_000;

        // Indexed access -- the headline difference.
        var arrayList = new ArrayList<Integer>();
        var linkedList = new LinkedList<Integer>();
        for (int i = 0; i < n; i++) { arrayList.add(i); linkedList.add(i); }

        long t0 = System.nanoTime();
        long s1 = 0;
        for (int i = 0; i < n; i++) s1 += arrayList.get(i);
        long alGet = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        long s2 = 0;
        int cap = 20_000;                             // fewer: O(n) each would take forever
        for (int i = 0; i < cap; i++) s2 += linkedList.get(i);
        long llGet = (System.nanoTime() - t1) / 1_000_000;

        System.out.printf("  get(i) x %,d   ArrayList : %5d ms%n", n, alGet);
        System.out.printf("  get(i) x %,d    LinkedList: %5d ms   (only %,d, and still slower!)%n",
                cap, llGet, cap);

        // Insertion in the MIDDLE -- where the textbook says LinkedList wins.
        int inserts = 20_000;
        var al2 = new ArrayList<Integer>();
        long t2 = System.nanoTime();
        for (int i = 0; i < inserts; i++) al2.add(al2.size() / 2, i);
        long alMid = (System.nanoTime() - t2) / 1_000_000;

        var ll2 = new LinkedList<Integer>();
        long t3 = System.nanoTime();
        for (int i = 0; i < inserts; i++) ll2.add(ll2.size() / 2, i);
        long llMid = (System.nanoTime() - t3) / 1_000_000;

        System.out.printf("%n  insert at MIDDLE x %,d%n", inserts);
        System.out.printf("    ArrayList : %5d ms   (O(n) arraycopy -- but vectorised)%n", alMid);
        System.out.printf("    LinkedList: %5d ms   (O(1) unlink, but O(n) to WALK there)%n", llMid);

        // Iteration -- cache locality.
        long t4 = System.nanoTime();
        long s3 = 0;
        for (int v : arrayList) s3 += v;
        long alIter = (System.nanoTime() - t4) / 1_000_000;
        long t5 = System.nanoTime();
        long s4 = 0;
        for (int v : linkedList) s4 += v;
        long llIter = (System.nanoTime() - t5) / 1_000_000;
        System.out.printf("%n  full iteration  ArrayList: %d ms   LinkedList: %d ms%n", alIter, llIter);

        System.out.println("""

                  The textbook says "LinkedList for frequent insertion". In practice that
                  is usually WRONG, because:
                    * you must TRAVERSE to the position first -- O(n) -- so the O(1)
                      unlink is irrelevant (the List API never hands you the node)
                    * ArrayList's O(n) removal is System.arraycopy: one vectorised memory
                      move, often faster than walking scattered nodes
                    * cache misses dominate. Constant factors beat asymptotics here.
                    * LinkedList allocates a node per element -> more GC pressure

                  Use ArrayList for lists and ArrayDeque for queues/stacks.
                  LinkedList is effectively never the right answer. (quiz 09 Q6)""");
        System.out.println("  (checksums: " + s1 + " " + s2 + " " + s3 + " " + s4 + ")");
    }

    // ==================================================================
    static void part5_containsIsOn() {
        System.out.println("\n=== PART 5: List.contains inside a loop -- the O(n*m) trap ===");
        int n = 20_000;

        var list = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) list.add(i);
        var set = new HashSet<>(list);

        long t0 = System.nanoTime();
        int found1 = 0;
        for (int i = 0; i < n; i++) if (list.contains(i)) found1++;      // O(n) each -> O(n^2)
        long listNs = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        int found2 = 0;
        for (int i = 0; i < n; i++) if (set.contains(i)) found2++;       // O(1) each -> O(n)
        long setNs = System.nanoTime() - t1;

        // Microseconds: the Set version is often under a millisecond, so ms rounds to 0
        // and the ratio becomes meaningless.
        System.out.printf("  %,d lookups against a List: %9.1f ms  (found %d)%n",
                n, listNs / 1e6, found1);
        System.out.printf("  %,d lookups against a Set : %9.1f ms  (found %d)%n",
                n, setNs / 1e6, found2);
        System.out.printf("  --> %.0fx difference, and the gap grows LINEARLY with n%n",
                (double) listNs / Math.max(setNs, 1));
        System.out.println("""

                  This is the most common real performance bug in ordinary business code,
                  and it hides well: it is fast with 100 elements in a unit test and
                  quadratic with 50,000 in production.

                  Whenever you see `.contains()` inside a loop, or a nested loop over two
                  collections, reach for a Set or a Map. Same shape as the N+1 query
                  problem (quiz 11 Q9): correct, and accidentally quadratic.""");
    }
}
