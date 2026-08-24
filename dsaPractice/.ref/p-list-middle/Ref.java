class Ref implements Problem {

    public Spec spec() {
        return Spec.method("middleNode", T.LIST_NODE).returns(T.LIST_NODE)
            .compare(Cmp.EXACT)
            .example("[1,2,3,4,5]",   "[3,4,5]")
            .example("[1,2,3,4,5,6]", "[4,5,6]")
            .example("[]",            "[]")
            .example("[1]",           "[1]")
            .example("[1,2]",         "[2]")
            .gen(g -> new Object[]{ g.list(0, 60, -20, 20) })
            .patterns("Linked List", "Two Pointers")
            .limitMs(2000)
            .maxN(200000);
    }

    public ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
}
