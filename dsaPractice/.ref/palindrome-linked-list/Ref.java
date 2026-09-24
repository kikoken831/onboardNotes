class Ref implements Problem {
    public Spec spec() {
        return Spec.method("isPalindrome", T.LIST_NODE).returns(T.BOOL)
            .example("[1,2,2,1]", "true").example("[1,2]", "false").example("[1]", "true").example("[1,2,1]", "true")
            .example("[1,1,2,1]", "false")
            .gen(g -> {
                int[] half = g.ints(0, 12, 0, 3);
                int n = half.length, mid = g.chance(0.5) ? 1 : 0;
                int[] a = new int[2 * n + mid];
                for (int i = 0; i < n; i++) { a[i] = half[i]; a[a.length - 1 - i] = half[i]; }
                if (mid == 1) a[n] = g.nextInt(0, 3);
                if (a.length == 0) a = new int[]{ 1 };
                if (g.chance(0.4)) a[g.nextInt(0, a.length - 1)] = g.nextInt(0, 3);
                return new Object[]{ LcCodec.buildList(a) };
            })
            .patterns("Linked List", "Two Pointers", "Stack", "Recursion").limitMs(2000).maxN(100000);
    }
    public boolean isPalindrome(ListNode head) {
        Pre.check(head != null, "at least one node");
        java.util.List<Integer> v = new java.util.ArrayList<>();
        for (ListNode p = head; p != null; p = p.next) v.add(p.val);
        for (int i = 0, j = v.size() - 1; i < j; i++, j--) if (!v.get(i).equals(v.get(j))) return false;
        return true;
    }
}
