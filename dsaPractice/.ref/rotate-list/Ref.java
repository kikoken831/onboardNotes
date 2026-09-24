class Ref implements Problem {
    public Spec spec() {
        return Spec.method("rotateRight", T.LIST_NODE, T.INT).returns(T.LIST_NODE)
            .example("[1,2,3,4,5], 2", "[4,5,1,2,3]").example("[0,1,2], 4", "[2,0,1]").example("[], 0", "[]")
            .example("[1], 2000000000", "[1]").example("[1,2], 2", "[1,2]")
            .gen(g -> new Object[]{ g.list(0, 30, 0, 20), g.chance(0.2) ? g.value(0, 2_000_000_000) : g.nextInt(0, 40) })
            .patterns("Linked List", "Two Pointers").limitMs(2000).maxN(100000);
    }
    public ListNode rotateRight(ListNode head, int k) {
        Pre.check(k >= 0, "0 <= k");
        if (head == null) return null;
        int n = 1;
        ListNode tail = head;
        while (tail.next != null) { tail = tail.next; n++; }
        k %= n;
        if (k == 0) return head;
        ListNode newTail = head;
        for (int i = 0; i < n - k - 1; i++) newTail = newTail.next;
        ListNode newHead = newTail.next;
        newTail.next = null;
        tail.next = head;
        return newHead;
    }
}
