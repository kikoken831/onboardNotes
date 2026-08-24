import java.util.*;
public class H {
  static void p(String k,Object v){System.out.println(k+" => "+v);}
  static int depth=0;
  static void rec(){ depth++; rec(); }
  public static void main(String[] a){
    // A. default JVM recursion depth
    try{ rec(); }catch(StackOverflowError e){ p("A default stack recursion depth (trivial frame)", depth); }

    // B. TimSort contract violation at larger n
    for(int n : new int[]{40,100,1000,100000}){
      Integer[] arr=new Integer[n]; Random r=new Random(7);
      for(int i=0;i<n;i++)arr[i]=r.nextInt(1_000_000);
      try{ Arrays.sort(arr,(u,v)->1); p("B n="+n+" inconsistent comparator","no exception"); }
      catch(Exception e){ p("B n="+n+" inconsistent comparator", e.getClass().getSimpleName()+": "+e.getMessage()); }
    }

    // C. String concat in loop vs StringBuilder
    int N=40000;
    long t0=System.nanoTime(); String s=""; for(int i=0;i<N;i++) s+="x"; long t1=System.nanoTime();
    StringBuilder sb=new StringBuilder(); for(int i=0;i<N;i++) sb.append('x'); long t2=System.nanoTime();
    p("C string += x"+N+" ms", (t1-t0)/1_000_000);
    p("C StringBuilder ms", (t2-t1)/1_000_000);

    // D. substring cost: is it O(n) copy? (post Java 7u6)
    String big="a".repeat(2_000_000);
    long u0=System.nanoTime(); String cur=big; int cnt=0;
    while(cur.length()>1_000_000){ cur=cur.substring(1); cnt++; }
    long u1=System.nanoTime();
    p("D "+cnt+" substring(1) calls on ~2M string ms", (u1-u0)/1_000_000);

    // E. Long boxing == in map keys
    Long l1=200L, l2=200L; p("E Long 200==200", l1==l2);
    Map<Long,String> mm=new HashMap<>(); mm.put(1L,"x");
    p("E map.get(1) with int literal key", mm.get(1));      // null: Integer 1 != Long 1L
    p("E map.get(1L)", mm.get(1L));

    // F. ConcurrentModificationException
    List<Integer> L=new ArrayList<>(List.of(1,2,3,4));
    try{ for(Integer v:L) if(v==2) L.remove(v); p("F foreach remove","no exception"); }
    catch(Exception e){ p("F foreach remove", e.getClass().getSimpleName()); }
    Map<String,List<String>> g=new HashMap<>();
    try{ g.computeIfAbsent("a",k->{ g.put("b",new ArrayList<>()); return new ArrayList<>();}); p("F nested computeIfAbsent","no exception"); }
    catch(Exception e){ p("F nested computeIfAbsent", e.getClass().getSimpleName()); }

    // G. TreeMap navigation (Java advantage Python lacks)
    TreeMap<Integer,String> tm=new TreeMap<>(); tm.put(10,"a"); tm.put(20,"b"); tm.put(30,"c");
    p("G floorKey(25)/ceilingKey(25)/higherKey(20)", tm.floorKey(25)+"/"+tm.ceilingKey(25)+"/"+tm.higherKey(20));
    TreeSet<Integer> ts=new TreeSet<>(List.of(1,5,9));
    p("G TreeSet subSet(2,9) / floor(8)", ts.subSet(2,9)+" / "+ts.floor(8));

    // H. Arrays.sort(int[]) has NO comparator overload -> must box. Show max-heap of int must box.
    int[] arr={3,1,2};
    // Arrays.sort(arr, Comparator.reverseOrder());  // does not compile
    Integer[] box={3,1,2}; Arrays.sort(box, Comparator.reverseOrder());
    p("H descending requires boxing", Arrays.toString(box));

    // I. mid overflow (Bloch)
    int lo=Integer.MAX_VALUE-3, hi=Integer.MAX_VALUE-1;
    p("I (lo+hi)/2 overflow", (lo+hi)/2);
    p("I lo+(hi-lo)/2", lo+(hi-lo)/2);
    p("I >>> form", (lo+hi)>>>1);
  }
}
