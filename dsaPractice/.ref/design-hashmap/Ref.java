class Ref implements Problem {

    public Spec spec() {
        return Spec.design("MyHashMap")
            .op("put", T.VOID, T.INT, T.INT)
            .op("get", T.INT, T.INT)
            .op("remove", T.VOID, T.INT)
            .exampleCalls("[\"MyHashMap\",\"put\",\"put\",\"get\",\"get\",\"put\",\"get\",\"remove\",\"get\"]",
                          "[[],[1,1],[2,2],[1],[3],[2,1],[2],[2],[2]]",
                          "[null,null,null,1,-1,null,1,null,-1]")
            .exampleCalls("[\"MyHashMap\",\"put\",\"get\",\"put\",\"get\"]",
                          "[[],[5,0],[5],[5,7],[5]]",
                          "[null,null,0,null,7]")        // value 0 is a real value, not "absent"
            .exampleCalls("[\"MyHashMap\",\"get\",\"remove\",\"put\",\"get\"]",
                          "[[],[1000000],[1000000],[1000000,3],[1000000]]",
                          "[null,-1,null,null,3]")
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("MyHashMap");
                int n = g.size(1, 80);
                boolean wide = g.chance(0.3);
                for (int i = 0; i < n; i++) {
                    int key = wide ? g.value(0, 1_000_000) : g.nextInt(0, 12);
                    int r = g.nextInt(0, 2);
                    if (r == 0) b.call("put", key, g.chance(0.3) ? 0 : g.value(0, 1_000_000));
                    else b.call(r == 1 ? "get" : "remove", key);
                }
                return new Object[]{ b.build() };
            })
            .patterns("Array", "Hash Table", "Design")
            .limitMs(2000)
            .maxN(100000);
    }
}

/** Separate chaining over a fixed bucket count, as the problem intends (no java.util maps). */
class MyHashMap {
    private static final int B = 1024;
    private final int[][] keys = new int[B][], vals = new int[B][];
    private final int[] sizes = new int[B];

    public MyHashMap() {}

    public void put(int key, int value) {
        Pre.check(key >= 0 && key <= 1_000_000 && value >= 0 && value <= 1_000_000, "0 <= key, value <= 10^6");
        int h = key % B, i = find(h, key);
        if (i >= 0) { vals[h][i] = value; return; }
        if (keys[h] == null) { keys[h] = new int[4]; vals[h] = new int[4]; }
        if (sizes[h] == keys[h].length) {
            keys[h] = java.util.Arrays.copyOf(keys[h], sizes[h] * 2);
            vals[h] = java.util.Arrays.copyOf(vals[h], sizes[h] * 2);
        }
        keys[h][sizes[h]] = key;
        vals[h][sizes[h]++] = value;
    }

    public int get(int key) {
        Pre.check(key >= 0 && key <= 1_000_000, "0 <= key <= 10^6");
        int h = key % B, i = find(h, key);
        return i >= 0 ? vals[h][i] : -1;
    }

    public void remove(int key) {
        Pre.check(key >= 0 && key <= 1_000_000, "0 <= key <= 10^6");
        int h = key % B, i = find(h, key);
        if (i < 0) return;
        int last = --sizes[h];
        keys[h][i] = keys[h][last];
        vals[h][i] = vals[h][last];
    }

    private int find(int h, int key) {
        for (int i = 0; i < sizes[h]; i++) if (keys[h][i] == key) return i;
        return -1;
    }
}
