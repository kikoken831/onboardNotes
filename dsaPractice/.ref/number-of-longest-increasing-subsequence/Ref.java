class Ref implements Problem {
    public Spec spec() {
        return Spec.method("findNumberOfLIS", T.INT_ARRAY).returns(T.INT)
            .example("[1,3,5,4,7]", "2").example("[2,2,2,2,2]", "5").example("[1,2,4,3,5,4,7,2]", "3").example("[1]", "1")
            .gen(g -> new Object[]{ g.ints(1, 30, 0, 6) })
            .patterns("Array", "Dynamic Programming", "Binary Indexed Tree", "Segment Tree").limitMs(2000).maxN(2000);
    }
    public int findNumberOfLIS(int[] a) {
        int n = a.length, best = 0, total = 0;
        int[] len = new int[n], cnt = new int[n];
        for (int i = 0; i < n; i++) {
            len[i] = 1; cnt[i] = 1;
            for (int j = 0; j < i; j++) if (a[j] < a[i]) {
                if (len[j] + 1 > len[i]) { len[i] = len[j] + 1; cnt[i] = cnt[j]; }
                else if (len[j] + 1 == len[i]) cnt[i] += cnt[j];
            }
            if (len[i] > best) { best = len[i]; total = cnt[i]; } else if (len[i] == best) total += cnt[i];
        }
        return total;
    }
}
