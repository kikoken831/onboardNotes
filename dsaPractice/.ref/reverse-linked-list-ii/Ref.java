class Ref implements Problem {
    public Spec spec() {
        return Spec.method("reverseBetween", T.LIST_NODE, T.INT, T.INT).returns(T.LIST_NODE)
            .example("[1,2,3,4,5], 2, 4", "[1,4,3,2,5]").example("[5], 1, 1", "[5]")
            .example("[3,5], 1, 2", "[5,3]").example("[1,2,3], 1, 3", "[3,2,1]")
            .gen(g -> { int n = g.size(1, 30), a = g.nextInt(1, n), b = g.nextInt(a, n); return new Object[]{ LcCodec.buildList(g.ints(n, 0, 20)), a, b }; })
            .patterns("Linked List").limitMs(2000).maxN(100000);
    }
    public ListNode reverseBetween(ListNode head, int left, int right) {
        int n = 0;
        for (ListNode p = head; p != null; p = p.next) n++;
        Pre.check(1 <= left && left <= right && right <= n, "1 <= left <= right <= n");
        ListNode dummy = new ListNode(0, head), prev = dummy;
        for (int i = 1; i < left; i++) prev = prev.next;
        ListNode cur = prev.next;
        for (int i = 0; i < right - left; i++) {        // move cur.next to the front of the segment
            ListNode t = cur.next;
            cur.next = t.next;
            t.next = prev.next;
            prev.next = t;
        }
        return dummy.next;
    }
}
