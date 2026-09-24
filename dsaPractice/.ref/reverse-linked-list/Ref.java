class Ref implements Problem {
    public Spec spec() {
        return Spec.method("reverseList", T.LIST_NODE).returns(T.LIST_NODE)
            .example("[1,2,3,4,5]", "[5,4,3,2,1]").example("[1,2]", "[2,1]").example("[]", "[]")
            .gen(g -> new Object[]{ g.list(0, 40, -20, 20) })
            .patterns("Linked List", "Recursion").limitMs(2000).maxN(100000);
    }
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        while (head != null) { ListNode t = head.next; head.next = prev; prev = head; head = t; }
        return prev;
    }
}
