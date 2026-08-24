import java.util.*;
import java.util.stream.*;

public class G {
  static void p(String k, Object v){ System.out.println(k+" => "+v); }
  public static void main(String[] a) throws Exception {
    // 1. Integer cache / == vs equals
    Integer x1=127, y1=127, x2=128, y2=128;
    p("1a Integer 127==127", (x1==y1));
    p("1b Integer 128==128", (x2==y2));
    p("1c Integer 128.equals", x2.equals(y2));

    // 2. Arrays.asList on primitive array
    int[] prim = {1,2,3};
    List<int[]> wrong = Arrays.asList(prim);
    p("2a Arrays.asList(int[]).size()", wrong.size());
    p("2b correct boxing", Arrays.stream(prim).boxed().collect(Collectors.toList()));
    List<Integer> back = List.of(1,2,3);
    p("2c unbox to int[]", Arrays.toString(back.stream().mapToInt(Integer::intValue).toArray()));

    // 3. ArrayDeque: add + pop is NOT a stack
    Deque<Integer> d1 = new ArrayDeque<>();
    d1.add(1); d1.add(2); d1.add(3);
    p("3a add,add,add then pop()", d1.pop());          // expect 1 -> FIFO, a bug
    Deque<Integer> d2 = new ArrayDeque<>();
    d2.push(1); d2.push(2); d2.push(3);
    p("3b push,push,push then pop()", d2.pop());       // expect 3 -> LIFO
    p("3c ArrayDeque push order iteration", d2.toString());
    Stack<Integer> st = new Stack<>(); st.push(1); st.push(2); st.push(3);
    p("3d java.util.Stack iteration order", st.toString());
    try { d2.push(null); } catch (Exception e){ p("3e ArrayDeque.push(null)", e.getClass().getSimpleName()); }

    // 4. PriorityQueue iteration/toString is not sorted
    PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5,1,4,2,3));
    p("4a pq.toString()", pq.toString());
    StringBuilder sb=new StringBuilder(); PriorityQueue<Integer> c=new PriorityQueue<>(pq);
    while(!c.isEmpty()) sb.append(c.poll()).append(' ');
    p("4b drain order", sb.toString().trim());

    // 5. comparator subtraction overflow
    Comparator<Integer> bad = (p1,q)->p1-q;
    p("5a bad.compare(Integer.MIN,1) sign", bad.compare(Integer.MIN_VALUE,1));
    p("5b Integer.compare(MIN,1) sign", Integer.compare(Integer.MIN_VALUE,1));

    // 6. char arithmetic types
    char ch='a';
    var t = ch + 1;
    p("6a type of ('a'+1)", ((Object)t).getClass().getSimpleName()+" value="+t);
    p("6b 'b'-'a'", ('b'-'a'));
    p("6c \"\"+'a'+'b' vs 'a'+'b'", (""+'a'+'b')+" | "+(int)('a'+'b'));

    // 7. modulo sign
    p("7a -7 % 3 (Java)", (-7 % 3));
    p("7b Math.floorMod(-7,3)", Math.floorMod(-7,3));
    p("7c -7/3 vs floorDiv", (-7/3)+" | "+Math.floorDiv(-7,3));

    // 8. Arrays.binarySearch miss return value
    int[] s={1,3,5,7};
    p("8a binarySearch(4)", Arrays.binarySearch(s,4));
    p("8b insertion point", -Arrays.binarySearch(s,4)-1);

    // 9. int[] in a HashSet does not dedupe
    Set<int[]> bad2=new HashSet<>();
    bad2.add(new int[]{1,2}); bad2.add(new int[]{1,2});
    p("9a Set<int[]> size after 2 equal pairs", bad2.size());
    Set<List<Integer>> ok=new HashSet<>();
    ok.add(List.of(1,2)); ok.add(List.of(1,2));
    p("9b Set<List<Integer>> size", ok.size());
    p("9c int[].equals", new int[]{1,2}.equals(new int[]{1,2})+" | Arrays.equals="+Arrays.equals(new int[]{1,2}, new int[]{1,2}));

    // 10. 2D array pitfalls
    int[][] g = new int[3][3];
    Arrays.fill(g[0], 7);
    p("10a Arrays.fill on one row", Arrays.deepToString(g));
    int[][] shallow = g.clone(); shallow[0][0]=99;
    p("10b clone() is shallow, orig[0][0]", g[0][0]);
    int[][] jag = new int[3][];
    p("10c new int[3][] rows are null", jag[0]==null);

    // 11. TimSort contract violation
    Integer[] arr = new Integer[100];
    Random r = new Random(1);
    for(int i=0;i<arr.length;i++) arr[i]=r.nextInt(1000);
    try {
      Arrays.sort(arr, (u,v) -> 1);   // inconsistent comparator
      p("11a inconsistent comparator", "no exception (n too small / merge not triggered)");
    } catch(Exception e){ p("11a inconsistent comparator", e.getClass().getSimpleName()+": "+e.getMessage()); }

    // 12. Arrays.asList fixed-size
    List<Integer> fixed = Arrays.asList(1,2,3);
    try { fixed.add(4); } catch(Exception e){ p("12a Arrays.asList().add", e.getClass().getSimpleName()); }
    try { List.of(1,2).set(0,5); } catch(Exception e){ p("12b List.of().set", e.getClass().getSimpleName()); }

    // 13. unboxing NPE from map.get
    Map<String,Integer> m=new HashMap<>();
    try { int v = m.get("missing"); p("13a", v); } catch(Exception e){ p("13a int v = map.get(missing)", e.getClass().getSimpleName()); }
    p("13b getOrDefault", m.getOrDefault("missing",0));

    // 14. remove(int) vs remove(Object) on List<Integer>
    List<Integer> L=new ArrayList<>(List.of(10,20,30));
    L.remove(1);
    p("14a List<Integer>.remove(1) removed index", L);
    List<Integer> L2=new ArrayList<>(List.of(10,20,30));
    L2.remove(Integer.valueOf(20));
    p("14b remove(Integer.valueOf(20))", L2);

    // 15. sort stability
    p("15a Arrays.sort(Object[]) stable (TimSort)?", "documented stable");
    // 16. String.split regex surprise
    p("16a \"a.b.c\".split(\".\").length", "a.b.c".split("\\.").length + " (escaped) vs " + "a.b.c".split(".").length + " (unescaped)");
    p("16b \"a,,b,,\".split(\",\") length", "a,,b,,".split(",").length);
    p("16c split with limit -1", "a,,b,,".split(",",-1).length);
  }
}
