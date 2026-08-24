class Ref implements Problem {

    public Spec spec() {
        return Spec.method("deleteByValue", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,2,6,3,4,5,6], 6", "[1,2,3,4,5]")
            .example("[], 1",              "[]")
            .example("[7,7,7], 7",         "[]")
            .example("[7,7,7], 3",         "[7,7,7]")
            .example("[1], 1",             "[]")
            .example("[6,1,6], 6",         "[1]")
            .gen(g -> new Object[]{ g.list(0, 60, 0, 5), g.value(0, 5) })
            .patterns("Linked List", "Recursion")
            .limitMs(2000)
            .maxN(200000);
    }

    public ListNode deleteByValue(ListNode head, int val) {
        ListNode dummy = new ListNode(0, head);
        ListNode prev = dummy;
        while (prev.next != null) {
            if (prev.next.val == val) prev.next = prev.next.next;
            else prev = prev.next;
        }
        return dummy.next;
    }
}
