import java.util.PriorityQueue;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("mergeKLists", T.LIST_NODE_ARRAY).returns(T.LIST_NODE)
            .example("[[1,4,5],[1,3,4],[2,6]]", "[1,1,2,3,4,4,5,6]").example("[]", "[]").example("[[]]", "[]")
            .example("[[],[1]]", "[1]")
            .gen(g -> {
                int k = g.size(0, 8);
                ListNode[] a = new ListNode[k];
                for (int i = 0; i < k; i++) a[i] = g.sortedList(0, 12, -20, 20);
                return new Object[]{ a };
            })
            .patterns("Linked List", "Divide and Conquer", "Heap (Priority Queue)", "Merge Sort").limitMs(2000).maxN(20000);
    }
    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> pq = new PriorityQueue<>((x, y) -> Integer.compare(x.val, y.val));
        for (ListNode l : lists) if (l != null) pq.add(l);
        ListNode dummy = new ListNode(0), t = dummy;
        while (!pq.isEmpty()) {
            ListNode n = pq.poll();
            t = t.next = n;
            if (n.next != null) pq.add(n.next);
        }
        t.next = null;
        return dummy.next;
    }
}
