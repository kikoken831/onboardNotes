import java.util.HashMap;
import java.util.Map;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("twoSum", T.INT_ARRAY, T.INT).returns(T.INT_ARRAY)
            .compare(Cmp.MULTISET)                 // [0,1] and [1,0] are both accepted
            .example("[2,7,11,15], 9", "[0,1]")
            .example("[3,2,4], 6",     "[1,2]")
            .example("[3,3], 6",       "[0,1]")
            .example("[-1,-2,-3,-4,-5], -8", "[2,4]")
            .gen(g -> g.twoSumInput(2, 120, -50, 50))
            .patterns("Array", "Hash Table")
            .limitMs(2000);
    }

    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[]{ j, i };
            seen.put(nums[i], i);
        }
        return new int[0];
    }
}
