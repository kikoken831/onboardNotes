class Ref implements Problem {
    public Spec spec() {
        return Spec.method("oddEvenList", T.LIST_NODE).returns(T.LIST_NODE)
            .example("[1,2,3,4,5]", "[1,3,5,2,4]").example("[2,1,3,5,6,4,7]", "[2,3,6,7,1,5,4]")
            .example("[]", "[]").example("[1]", "[1]").example("[1,2]", "[1,2]")
            .gen(g -> new Object[]{ g.list(0, 30, 0, 20) })
            .patterns("Linked List").limitMs(2000).maxN(100000);
    }
    public ListNode oddEvenList(ListNode head) {
        if (head == null) return null;
        ListNode odd = head, even = head.next, evenHead = even;
        while (even != null && even.next != null) {
            odd.next = even.next; odd = odd.next;
            even.next = odd.next; even = even.next;
        }
        odd.next = evenHead;
        return head;
    }
}
