class Ref implements Problem {

    public Spec spec() {
        return Spec.design("MyHashSet")
            .op("add", T.VOID, T.INT)
            .op("remove", T.VOID, T.INT)
            .op("contains", T.BOOL, T.INT)
            .exampleCalls("[\"MyHashSet\",\"add\",\"add\",\"contains\",\"contains\",\"add\",\"contains\",\"remove\",\"contains\"]",
                          "[[],[1],[2],[1],[3],[2],[2],[2],[2]]",
                          "[null,null,null,true,false,null,true,null,false]")
            .exampleCalls("[\"MyHashSet\",\"add\",\"contains\",\"remove\",\"remove\",\"contains\"]",
                          "[[],[1000000],[1000000],[1000000],[1000000],[1000000]]",
                          "[null,null,true,null,null,false]")   // largest key; remove twice
            .exampleCalls("[\"MyHashSet\",\"contains\",\"remove\",\"add\",\"contains\"]",
                          "[[],[0],[0],[0],[0]]",
                          "[null,false,null,null,true]")
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("MyHashSet");
                int n = g.size(1, 80);
                boolean wide = g.chance(0.3);
                for (int i = 0; i < n; i++) {
                    int key = wide ? g.value(0, 1_000_000) : g.nextInt(0, 12);
                    int r = g.nextInt(0, 2);
                    b.call(r == 0 ? "add" : r == 1 ? "remove" : "contains", key);
                }
                return new Object[]{ b.build() };
            })
            .patterns("Array", "Hash Table", "Design")
            .limitMs(2000)
            .maxN(100000);
    }
}

/** Separate chaining over a fixed bucket count, as the problem intends (no java.util sets). */
class MyHashSet {
    private static final int B = 1024;
    private final int[][] buckets = new int[B][];
    private final int[] sizes = new int[B];

    public MyHashSet() {}

    public void add(int key) {
        check(key);
        if (contains(key)) return;
        int h = key % B;
        if (buckets[h] == null) buckets[h] = new int[4];
        if (sizes[h] == buckets[h].length) buckets[h] = java.util.Arrays.copyOf(buckets[h], sizes[h] * 2);
        buckets[h][sizes[h]++] = key;
    }

    public void remove(int key) {
        check(key);
        int h = key % B;
        for (int i = 0; i < sizes[h]; i++) {
            if (buckets[h][i] == key) { buckets[h][i] = buckets[h][--sizes[h]]; return; }
        }
    }

    public boolean contains(int key) {
        check(key);
        int h = key % B;
        for (int i = 0; i < sizes[h]; i++) if (buckets[h][i] == key) return true;
        return false;
    }

    private static void check(int key) { Pre.check(key >= 0 && key <= 1_000_000, "0 <= key <= 10^6"); }
}
