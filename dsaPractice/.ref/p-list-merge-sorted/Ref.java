class Ref implements Problem {

    public Spec spec() {
        return Spec.method("mergeTwoSorted", T.LIST_NODE, T.LIST_NODE).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,2,4], [1,3,4]", "[1,1,2,3,4,4]")
            .example("[], []",           "[]")
            .example("[], [0]",          "[0]")
            .example("[1,2,3], []",      "[1,2,3]")
            .example("[5], [1]",         "[1,5]")
            .example("[1,1], [1,1]",     "[1,1,1,1]")
            .gen(g -> new Object[]{
                g.sortedList(0, 40, -15, 15),
                g.sortedList(0, 40, -15, 15) })
            .patterns("Linked List", "Recursion")
            .limitMs(2000)
            .maxN(200000);
    }

    public ListNode mergeTwoSorted(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(), tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { tail.next = a; a = a.next; }
            else                { tail.next = b; b = b.next; }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;
        return dummy.next;
    }
}
