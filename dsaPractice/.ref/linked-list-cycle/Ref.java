class Ref implements Problem {
    public Spec spec() {
        return Spec.method("hasCycle", T.LIST_NODE).returns(T.BOOL)
            .hidden(Ref::wire, T.INT)                           // pos: where the tail links back
            .example("[3,2,0,-4], 1", "true").example("[1,2], 0", "true").example("[1], -1", "false")
            .example("[], -1", "false").example("[1], 0", "true")
            .gen(g -> {
                int n = g.size(0, 30);
                int pos = n == 0 || g.chance(0.4) ? -1 : g.nextInt(0, n - 1);
                return new Object[]{ LcCodec.buildList(g.ints(n, -9, 9)), pos };
            })
            .patterns("Hash Table", "Linked List", "Two Pointers").limitMs(2000).maxN(100000);
    }
    /**
     * LeetCode describes the cycle by `pos`, which is never passed to you. Wire it into
     * this call's private copy: tail.next = node at index pos. Keep a snapshot of the
     * intended shape in the hidden slot so the result can be checked afterwards.
     */
    static void wire(Object[] a) {
        ListNode head = (ListNode) a[0];
        int pos = (Integer) a[1], n = 0;
        java.util.List<Integer> vals = new java.util.ArrayList<>();
        for (ListNode p = head; p != null; p = p.next) { n++; vals.add(p.val); }
        Pre.check(pos >= -1 && pos < n, "-1 <= pos < length");
        if (pos >= 0) {
            ListNode tail = head, at = head;
            while (tail.next != null) tail = tail.next;
            for (int i = 0; i < pos; i++) at = at.next;
            tail.next = at;
        }
        a[1] = new Object[]{ pos, vals };                     // snapshot for the judge
    }

    /** Index of each node in a possibly-cyclic list, walking until a node repeats. */
    static java.util.IdentityHashMap<ListNode, Integer> indices(ListNode head) {
        java.util.IdentityHashMap<ListNode, Integer> ix = new java.util.IdentityHashMap<>();
        for (ListNode p = head; p != null && !ix.containsKey(p); p = p.next) ix.put(p, ix.size());
        return ix;
    }

    /** Null if the list still has the shape it was given, else what changed. */
    @SuppressWarnings("unchecked")
    static String modified(Object[] a) {
        Object[] snap = (Object[]) a[1];
        int pos = (Integer) snap[0];
        java.util.List<Integer> vals = (java.util.List<Integer>) snap[1];
        java.util.IdentityHashMap<ListNode, Integer> ix = indices((ListNode) a[0]);
        java.util.List<Integer> now = new java.util.ArrayList<>();
        ListNode p = (ListNode) a[0], last = null;
        for (int i = 0; i < ix.size(); i++) { now.add(p.val); last = p; p = p.next; }
        int nowPos = last == null || last.next == null ? -1 : ix.getOrDefault(last.next, -2);
        if (!now.equals(vals) || nowPos != pos) return "you modified the list (LeetCode forbids it): it was "
            + vals + " with pos " + pos + ", now " + now + " with pos " + nowPos;
        return null;
    }

    public boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }
}
