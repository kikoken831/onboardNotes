class Ref implements Problem {
    public Spec spec() {
        return Spec.method("swapPairs", T.LIST_NODE).returns(T.LIST_NODE)
            .example("[1,2,3,4]", "[2,1,4,3]").example("[]", "[]").example("[1]", "[1]").example("[1,2,3]", "[2,1,3]")
            .gen(g -> new Object[]{ g.list(0, 30, 0, 20) })
            .patterns("Linked List", "Recursion").limitMs(2000).maxN(100000);
    }
    public ListNode swapPairs(ListNode head) {
        ListNode dummy = new ListNode(0, head), p = dummy;
        while (p.next != null && p.next.next != null) {
            ListNode a = p.next, b = a.next;
            a.next = b.next; b.next = a; p.next = b;
            p = a;
        }
        return dummy.next;
    }
}
