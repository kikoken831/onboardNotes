import java.util.ArrayList;
import java.util.List;

class Ref implements Problem {

    public Spec spec() {
        return Spec.method("kidsWithCandies", T.INT_ARRAY, T.INT).returns(T.LIST_BOOL)
            .example("[2,3,5,1,3], 3", "[true,true,true,false,true]")
            .example("[4,2,1,1,2], 1", "[true,false,false,false,false]")
            .example("[12,1,12], 10",  "[true,false,true]")
            .example("[1,4], 3",       "[true,true]")
            .example("[1,4], 2",       "[false,true]")
            .gen(g -> new Object[]{ g.ints(2, 40, 1, 20), g.nextInt(1, 20) })
            .patterns("Array")
            .limitMs(2000)
            .maxN(100000);
    }

    public List<Boolean> kidsWithCandies(int[] candies, int extraCandies) {
        Pre.length(candies, 2, 1000_000, "candies");
        int max = 0;
        for (int c : candies) max = Math.max(max, c);
        List<Boolean> out = new ArrayList<>(candies.length);
        for (int c : candies) out.add(c + extraCandies >= max);
        return out;
    }
}
