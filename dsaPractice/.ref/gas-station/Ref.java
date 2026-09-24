class Ref implements Problem {
    public Spec spec() {
        return Spec.method("canCompleteCircuit", T.INT_ARRAY, T.INT_ARRAY).returns(T.INT)
            .example("[1,2,3,4,5], [3,4,5,1,2]", "3").example("[2,3,4], [3,4,3]", "-1").example("[5,1,2,3,4], [4,4,1,5,1]", "4")
            .example("[3], [3]", "0")
            .gen(g -> {
                for (;;) {
                    int n = g.size(1, 30);
                    int[] gas = g.ints(n, 0, 8), cost = g.ints(n, 0, 8);
                    if (uniqueOrNone(gas, cost)) return new Object[]{ gas, cost };
                }
            })
            .patterns("Array", "Greedy").limitMs(2000).maxN(200000);
    }
    /** LeetCode guarantees that if a solution exists it is unique. */
    static boolean uniqueOrNone(int[] gas, int[] cost) {
        int n = gas.length, found = 0;
        for (int s = 0; s < n; s++) {
            long t = 0; boolean ok = true;
            for (int k = 0; k < n && ok; k++) { int i = (s + k) % n; t += gas[i] - cost[i]; if (t < 0) ok = false; }
            if (ok) found++;
        }
        return found <= 1;
    }
    public int canCompleteCircuit(int[] gas, int[] cost) {
        Pre.check(gas.length == cost.length && gas.length >= 1, "equal non-zero lengths");
        Pre.check(gas.length > 200 || uniqueOrNone(gas, cost), "the answer is unique");
        long total = 0, tank = 0;
        int start = 0;
        for (int i = 0; i < gas.length; i++) {
            total += gas[i] - cost[i];
            tank += gas[i] - cost[i];
            if (tank < 0) { start = i + 1; tank = 0; }     // no station up to i can be the start
        }
        return total < 0 ? -1 : start;
    }
}
