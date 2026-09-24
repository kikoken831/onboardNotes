class Ref implements Problem {
    public Spec spec() {
        return Spec.method("mergeTwoLists", T.LIST_NODE, T.LIST_NODE).returns(T.LIST_NODE)
            .example("[1,2,4], [1,3,4]", "[1,1,2,3,4,4]").example("[], []", "[]").example("[], [0]", "[0]")
            .example("[5], [1,2,3]", "[1,2,3,5]")
            .gen(g -> new Object[]{ g.sortedList(0, 30, -20, 20), g.sortedList(0, 30, -20, 20) })
            .patterns("Linked List", "Recursion").limitMs(2000).maxN(100000);
    }
    public ListNode mergeTwoLists(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), t = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { t.next = a; a = a.next; } else { t.next = b; b = b.next; }
            t = t.next;
        }
        t.next = a != null ? a : b;
        return dummy.next;
    }
}
