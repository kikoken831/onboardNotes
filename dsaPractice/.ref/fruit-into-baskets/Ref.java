class Ref implements Problem {
    public Spec spec() {
        return Spec.method("totalFruit", T.INT_ARRAY).returns(T.INT)
            .example("[1,2,1]", "3").example("[0,1,2,2]", "3").example("[1,2,3,2,2]", "4")
            .example("[3,3,3,1,2,1,1,2,3,3,4]", "5").example("[5]", "1")
            .gen(g -> new Object[]{ g.ints(1, 50, 0, 3) })
            .patterns("Array", "Hash Table", "Sliding Window").limitMs(2000).maxN(200000);
    }
    public int totalFruit(int[] fruits) {
        java.util.Map<Integer, Integer> c = new java.util.HashMap<>();
        int l = 0, best = 0;
        for (int r = 0; r < fruits.length; r++) {
            c.merge(fruits[r], 1, Integer::sum);
            while (c.size() > 2) { if (c.merge(fruits[l], -1, Integer::sum) == 0) c.remove(fruits[l]); l++; }
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
}
