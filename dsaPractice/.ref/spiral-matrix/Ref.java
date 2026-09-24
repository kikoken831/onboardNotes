import java.util.*;

class Ref implements Problem {
    public Spec spec() {
        return Spec.method("spiralOrder", T.INT_ARRAY_2D).returns(T.LIST_INT)
            .example("[[1,2,3],[4,5,6],[7,8,9]]", "[1,2,3,6,9,8,7,4,5]")
            .example("[[1,2,3,4],[5,6,7,8],[9,10,11,12]]", "[1,2,3,4,8,12,11,10,9,5,6,7]")
            .example("[[1],[2],[3]]", "[1,2,3]").example("[[1,2,3]]", "[1,2,3]")
            .gen(g -> new Object[]{ g.grid(1, 8, 1, 8, -9, 9) })
            .patterns("Array", "Matrix", "Simulation").limitMs(2000).maxN(20000);
    }
    public List<Integer> spiralOrder(int[][] m) {
        List<Integer> out = new ArrayList<>();
        int top = 0, bot = m.length - 1, left = 0, right = m[0].length - 1;
        while (top <= bot && left <= right) {
            for (int j = left; j <= right; j++) out.add(m[top][j]);
            for (int i = top + 1; i <= bot; i++) out.add(m[i][right]);
            if (top < bot) for (int j = right - 1; j >= left; j--) out.add(m[bot][j]);
            if (left < right) for (int i = bot - 1; i > top; i--) out.add(m[i][left]);
            top++; bot--; left++; right--;
        }
        return out;
    }
}
