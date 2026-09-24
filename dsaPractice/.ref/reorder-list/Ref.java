class Ref implements Problem {
    public Spec spec() {
        return Spec.method("reorderList", T.LIST_NODE).returns(T.VOID).inPlace(0)
            .exampleInPlace("[1,2,3,4]", "[1,4,2,3]").exampleInPlace("[1,2,3,4,5]", "[1,5,2,4,3]")
            .exampleInPlace("[1]", "[1]").exampleInPlace("[1,2]", "[1,2]")
            .gen(g -> new Object[]{ g.list(1, 30, 0, 20) })
            .patterns("Linked List", "Two Pointers", "Stack", "Recursion").limitMs(2000).maxN(100000);
    }
    public void reorderList(ListNode head) {
        Pre.check(head != null, "at least one node");
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) { slow = slow.next; fast = fast.next.next; }
        ListNode second = slow.next, prev = null;
        slow.next = null;                                    // cut, or the result has a cycle
        while (second != null) { ListNode t = second.next; second.next = prev; prev = second; second = t; }
        for (ListNode a = head, b = prev; b != null; ) {
            ListNode an = a.next, bn = b.next;
            a.next = b; b.next = an;
            a = an; b = bn;
        }
    }
}
