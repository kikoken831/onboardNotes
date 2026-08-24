class Ref implements Problem {

    public Spec spec() {
        return Spec.method("reverseList", T.LIST_NODE).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,2,3,4,5]", "[5,4,3,2,1]")
            .example("[1,2]",       "[2,1]")
            .example("[]",          "[]")
            .example("[7]",         "[7]")
            .example("[1,1,2]",     "[2,1,1]")
            .gen(g -> new Object[]{ g.list(0, 60, -20, 20) })
            .patterns("Linked List", "Recursion")
            .limitMs(2000)
            .maxN(200000);
    }

    public ListNode reverseList(ListNode head) {
        ListNode prev = null, cur = head;
        while (cur != null) {
            ListNode next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        return prev;
    }
}
