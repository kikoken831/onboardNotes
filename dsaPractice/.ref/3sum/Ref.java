import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("threeSum", T.INT_ARRAY).returns(T.LIST_LIST_INT)
            .compare(Cmp.SET_OF_SETS)
            .example("[-1,0,1,2,-1,-4]", "[[-1,-1,2],[-1,0,1]]")
            .example("[0,1,1]",          "[]")
            .example("[0,0,0]",          "[[0,0,0]]")
            .example("[0,0,0,0]",        "[[0,0,0]]")        // no duplicate triplets
            .example("[-2,0,1,1,2]",     "[[-2,0,2],[-2,1,1]]")
            .gen(g -> new Object[]{ g.ints(3, 40, -6, 6) })
            .patterns("Array", "Two Pointers", "Sorting").limitMs(2000).maxN(3000);
    }
    public List<List<Integer>> threeSum(int[] nums) {
        Pre.length(nums, 3, 1_000_000, "nums");
        int[] a = nums.clone();
        Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < a.length - 2; i++) {
            if (i > 0 && a[i] == a[i - 1]) continue;
            int l = i + 1, r = a.length - 1;
            while (l < r) {
                int s = a[i] + a[l] + a[r];
                if (s < 0) l++;
                else if (s > 0) r--;
                else {
                    out.add(Arrays.asList(a[i], a[l], a[r]));
                    while (l < r && a[l] == a[l + 1]) l++;
                    while (l < r && a[r] == a[r - 1]) r--;
                    l++; r--;
                }
            }
        }
        return out;
    }
}
