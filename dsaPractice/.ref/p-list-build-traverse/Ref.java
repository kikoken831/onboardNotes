class Ref implements Problem {

    public Spec spec() {
        return Spec.method("listToArray", T.LIST_NODE).returns(T.INT_ARRAY)
            .compare(Cmp.EXACT)
            .example("[1,2,3]", "[1,2,3]")
            .example("[]",      "[]")
            .example("[7]",     "[7]")
            .example("[1,1,1]", "[1,1,1]")
            .gen(g -> new Object[]{ g.list(0, 60, -20, 20) })
            .patterns("Linked List")
            .limitMs(2000)
            .maxN(200000);
    }

    public int[] listToArray(ListNode head) {
        int n = 0;
        for (ListNode p = head; p != null; p = p.next) n++;
        int[] out = new int[n];
        int i = 0;
        for (ListNode p = head; p != null; p = p.next) out[i++] = p.val;
        return out;
    }
}
