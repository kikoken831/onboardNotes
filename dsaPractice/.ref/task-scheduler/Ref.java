class Ref implements Problem {
    public Spec spec() {
        return Spec.method("leastInterval", T.CHAR_ARRAY, T.INT).returns(T.INT)
            .example("[\"A\",\"A\",\"A\",\"B\",\"B\",\"B\"], 2", "8").example("[\"A\",\"A\",\"A\",\"B\",\"B\",\"B\"], 0", "6")
            .example("[\"A\",\"A\",\"A\",\"A\",\"A\",\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\"], 2", "16")
            .example("[\"A\",\"C\",\"A\",\"B\",\"D\",\"B\"], 1", "6")                // no idle needed
            .gen(g -> new Object[]{ g.string(1, 30, "AAABBC").toCharArray(), g.nextInt(0, 5) })
            .patterns("Array", "Hash Table", "Greedy", "Sorting", "Heap (Priority Queue)").limitMs(2000).maxN(200000);
    }
    public int leastInterval(char[] tasks, int n) {
        int[] c = new int[26];
        int max = 0, tied = 0;
        for (char t : tasks) max = Math.max(max, ++c[t - 'A']);
        for (int x : c) if (x == max) tied++;
        return Math.max(tasks.length, (max - 1) * (n + 1) + tied);   // frames of the most frequent task
    }
}
