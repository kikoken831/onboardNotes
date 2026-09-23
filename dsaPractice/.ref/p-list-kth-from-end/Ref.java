class Ref implements Problem {

    public Spec spec() {
        return Spec.method("deleteKthFromEnd", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,2,3,4,5], 2", "[1,2,3,5]")
            .example("[1], 1",         "[]")
            .example("[1,2], 1",       "[1]")
            .example("[1,2], 2",       "[2]")
            .example("[1,2,3], 3",     "[2,3]")
            .gen(g -> {
                int n = g.size(1, 40);
                int[] vals = g.ints(n, -20, 20);
                return new Object[]{ LcCodec.buildList(vals), g.nextInt(1, n) };
            })
            .patterns("Linked List", "Two Pointers")
            .limitMs(2000)
            .maxN(100000);
    }

    public ListNode deleteKthFromEnd(ListNode head, int k) {
        ListNode dummy = new ListNode(0, head);
        ListNode lead = dummy, trail = dummy;
        for (int i = 0; i < k; i++) lead = lead.next;
        while (lead.next != null) { lead = lead.next; trail = trail.next; }
        trail.next = trail.next.next;
        return dummy.next;
    }
}
