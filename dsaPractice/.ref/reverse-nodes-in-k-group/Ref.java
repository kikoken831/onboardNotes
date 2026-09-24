class Ref implements Problem {
    public Spec spec() {
        return Spec.method("reverseKGroup", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .example("[1,2,3,4,5], 2", "[2,1,4,3,5]").example("[1,2,3,4,5], 3", "[3,2,1,4,5]")
            .example("[1,2,3,4,5], 1", "[1,2,3,4,5]").example("[1,2], 2", "[2,1]").example("[1,2,3], 3", "[3,2,1]")
            .gen(g -> { int n = g.size(1, 30); return new Object[]{ LcCodec.buildList(g.ints(n, 0, 20)), g.nextInt(1, n) }; })
            .patterns("Linked List", "Recursion").limitMs(2000).maxN(100000);
    }
    public ListNode reverseKGroup(ListNode head, int k) {
        int n = 0;
        for (ListNode p = head; p != null; p = p.next) n++;
        Pre.check(k >= 1 && k <= n, "1 <= k <= n");
        ListNode dummy = new ListNode(0, head), prev = dummy;
        while (true) {
            ListNode end = prev;
            for (int i = 0; i < k && end != null; i++) end = end.next;
            if (end == null) break;                        // fewer than k left: leave as is
            ListNode first = prev.next, next = end.next, p = first, q = next;
            while (p != next) { ListNode t = p.next; p.next = q; q = p; p = t; }
            prev.next = end;
            prev = first;
        }
        return dummy.next;
    }
}
