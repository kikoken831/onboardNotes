class Ref implements Problem {

    public Spec spec() {
        return Spec.method("partitionList", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,4,3,2,5,2], 3", "[1,2,2,4,3,5]")
            .example("[2,1], 2",         "[1,2]")
            .example("[], 1",            "[]")
            .example("[1,2,3], 0",       "[1,2,3]")
            .example("[1,2,3], 9",       "[1,2,3]")
            .example("[3,1], 3",         "[1,3]")
            .gen(g -> new Object[]{ g.list(0, 50, -8, 8), g.value(-8, 8) })
            .patterns("Linked List", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public ListNode partitionList(ListNode head, int x) {
        ListNode lo = new ListNode(), loTail = lo;
        ListNode hi = new ListNode(), hiTail = hi;
        for (ListNode p = head; p != null; p = p.next) {
            if (p.val < x) { loTail.next = p; loTail = p; }
            else           { hiTail.next = p; hiTail = p; }
        }
        hiTail.next = null;
        loTail.next = hi.next;
        return lo.next;
    }
}
