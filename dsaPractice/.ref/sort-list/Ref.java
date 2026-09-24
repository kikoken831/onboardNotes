class Ref implements Problem {
    public Spec spec() {
        return Spec.method("sortList", T.LIST_NODE).returns(T.LIST_NODE)
            .example("[4,2,1,3]", "[1,2,3,4]").example("[-1,5,3,4,0]", "[-1,0,3,4,5]").example("[]", "[]")
            .example("[2,2,1]", "[1,2,2]")
            .gen(g -> new Object[]{ g.list(0, 40, -20, 20) })
            .patterns("Linked List", "Two Pointers", "Divide and Conquer", "Sorting", "Merge Sort").limitMs(2000).maxN(100000);
    }
    public ListNode sortList(ListNode head) {
        if (head == null || head.next == null) return head;
        ListNode slow = head, fast = head.next;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        ListNode right = slow.next;
        slow.next = null;
        return merge(sortList(head), sortList(right));
    }
    private static ListNode merge(ListNode a, ListNode b) {
        ListNode d = new ListNode(0), t = d;
        while (a != null && b != null) { if (a.val <= b.val) { t.next = a; a = a.next; } else { t.next = b; b = b.next; } t = t.next; }
        t.next = a != null ? a : b;
        return d.next;
    }
}
