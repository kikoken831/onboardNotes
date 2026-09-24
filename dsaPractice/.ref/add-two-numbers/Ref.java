class Ref implements Problem {
    public Spec spec() {
        return Spec.method("addTwoNumbers", T.LIST_NODE, T.LIST_NODE).returns(T.LIST_NODE)
            .example("[2,4,3], [5,6,4]", "[7,0,8]").example("[0], [0]", "[0]")
            .example("[9,9,9,9,9,9,9], [9,9,9,9]", "[8,9,9,9,0,0,0,1]").example("[5], [5]", "[0,1]")
            .gen(g -> new Object[]{ number(g), number(g) })
            .patterns("Linked List", "Math", "Recursion").limitMs(2000).maxN(100000);
    }
    /** Digits least-significant first, no leading zeros (the number 0 is [0]). */
    static ListNode number(Gen g) {
        int n = g.size(1, 20);
        int[] d = new int[n];
        for (int i = 0; i < n; i++) d[i] = g.chance(0.4) ? 9 : g.nextInt(0, 9);
        if (n > 1 && d[n - 1] == 0) d[n - 1] = g.nextInt(1, 9);
        return LcCodec.buildList(d);
    }
    public ListNode addTwoNumbers(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), t = dummy;
        int carry = 0;
        while (a != null || b != null || carry != 0) {
            int s = carry + (a != null ? a.val : 0) + (b != null ? b.val : 0);
            t = t.next = new ListNode(s % 10);
            carry = s / 10;
            if (a != null) a = a.next;
            if (b != null) b = b.next;
        }
        return dummy.next;
    }
}
