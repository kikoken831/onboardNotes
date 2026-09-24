class Ref implements Problem {
    public Spec spec() {
        return Spec.method("removeElements", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .example("[1,2,6,3,4,5,6], 6", "[1,2,3,4,5]").example("[], 1", "[]").example("[7,7,7,7], 7", "[]")
            .example("[1,7,7,2], 7", "[1,2]")
            .gen(g -> new Object[]{ g.list(0, 30, 1, 4), g.nextInt(0, 4) })   // LeetCode: 1 <= Node.val, 0 <= val
            .patterns("Linked List", "Recursion").limitMs(2000).maxN(100000);
    }
    public ListNode removeElements(ListNode head, int val) {
        ListNode dummy = new ListNode(0, head), p = dummy;
        while (p.next != null) if (p.next.val == val) p.next = p.next.next; else p = p.next;
        return dummy.next;
    }
}
