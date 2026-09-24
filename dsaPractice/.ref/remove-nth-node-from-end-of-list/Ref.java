class Ref implements Problem {
    public Spec spec() {
        return Spec.method("removeNthFromEnd", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .example("[1,2,3,4,5], 2", "[1,2,3,5]").example("[1], 1", "[]").example("[1,2], 1", "[1]")
            .example("[1,2], 2", "[2]")                    // removing the head
            .gen(g -> { int n = g.size(1, 30); return new Object[]{ LcCodec.buildList(g.ints(n, 0, 20)), g.nextInt(1, n) }; })
            .patterns("Linked List", "Two Pointers").limitMs(2000).maxN(100000);
    }
    public ListNode removeNthFromEnd(ListNode head, int n) {
        int len = 0;
        for (ListNode p = head; p != null; p = p.next) len++;
        Pre.check(n >= 1 && n <= len, "1 <= n <= length");
        ListNode dummy = new ListNode(0, head), fast = dummy, slow = dummy;
        for (int i = 0; i <= n; i++) fast = fast.next;
        while (fast != null) { fast = fast.next; slow = slow.next; }
        slow.next = slow.next.next;
        return dummy.next;
    }
}
