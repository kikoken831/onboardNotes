import java.util.PriorityQueue;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("mergeKSorted", T.INT_ARRAY_2D).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[[1,4,5],[1,3,4],[2,6]]", "[1,1,2,3,4,4,5,6]")
            .example("[]",                      "[]")
            .example("[[]]",                    "[]")
            .example("[[1]]",                   "[1]")
            .example("[[],[1],[]]",             "[1]")
            .gen(g -> {
                int k = g.size(0, 8);
                int[][] arrays = new int[k][];
                for (int i = 0; i < k; i++) arrays[i] = g.sortedInts(0, 12, -15, 15);
                return new Object[]{ arrays };
            })
            .patterns("Array", "Heap (Priority Queue)", "Merge Sort")
            .limitMs(3000)
            .maxN(100000);
    }

    public int[] mergeKSorted(int[][] arrays) {
        int total = 0;
        for (int[] a : arrays) total += a.length;
        int[] out = new int[total];

        PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        for (int i = 0; i < arrays.length; i++) {
            if (arrays[i].length > 0) heap.offer(new int[]{ arrays[i][0], i, 0 });
        }

        int w = 0;
        while (!heap.isEmpty()) {
            int[] top = heap.poll();
            out[w++] = top[0];
            int next = top[2] + 1;
            if (next < arrays[top[1]].length) {
                heap.offer(new int[]{ arrays[top[1]][next], top[1], next });
            }
        }
        return out;
    }
}
