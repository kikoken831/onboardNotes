class Ref implements Problem {
    public Spec spec() {
        return Spec.method("deleteDuplicates", T.LIST_NODE).returns(T.LIST_NODE)
            .example("[1,1,2]", "[1,2]").example("[1,1,2,3,3]", "[1,2,3]").example("[]", "[]").example("[1,1,1]", "[1]")
            .gen(g -> new Object[]{ g.sortedList(0, 30, -5, 5) })
            .patterns("Linked List").limitMs(2000).maxN(100000);
    }
    public ListNode deleteDuplicates(ListNode head) {
        for (ListNode p = head; p != null && p.next != null; ) {
            Pre.check(p.val <= p.next.val, "sorted");
            if (p.next.val == p.val) p.next = p.next.next; else p = p.next;
        }
        return head;
    }
}
