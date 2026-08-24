class Ref implements Problem {

    public Spec spec() {
        return Spec.method("reverseSublist", T.LIST_NODE, T.INT, T.INT).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,2,3,4,5], 2, 4", "[1,4,3,2,5]")
            .example("[1,2,3,4,5], 1, 5", "[5,4,3,2,1]")
            .example("[5], 1, 1",         "[5]")
            .example("[1,2], 1, 2",       "[2,1]")
            .example("[1,2,3], 2, 2",     "[1,2,3]")
            .example("[1,2,3,4], 3, 4",   "[1,2,4,3]")
            .gen(g -> {
                int len = g.size(1, 40);
                int[] vals = g.ints(len, -20, 20);
                int m = g.nextInt(1, len);
                int n = g.nextInt(m, len);
                return new Object[]{ Codec.buildList(vals), m, n };
            })
            .patterns("Linked List")
            .limitMs(2000)
            .maxN(50000);
    }

    public ListNode reverseSublist(ListNode head, int m, int n) {
        ListNode dummy = new ListNode(0, head);
        ListNode prev = dummy;
        for (int i = 1; i < m; i++) prev = prev.next;

        ListNode cur = prev.next;
        for (int i = 0; i < n - m; i++) {
            ListNode moved = cur.next;
            cur.next = moved.next;
            moved.next = prev.next;
            prev.next = moved;
        }
        return dummy.next;
    }
}
