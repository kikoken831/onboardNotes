"""
Mutants for tier 3. See tools/mutants.py.

Where a reference is non-trivial, one "accept" mutant is deliberately a brute force:
agreeing with it over the whole stress run is an independent check on the reference.
"""

from mutants_t2 import LIST, S, D  # noqa: F401

MUTANTS = {}

# ------------------------------------------------ batch 1: arrays / hashing

MUTANTS["move-zeroes"] = [
    ("no-zero-fill", "reject", S("""
    public void moveZeroes(int[] a) { int w = 0; for (int x : a) if (x != 0) a[w++] = x; }""")),
    ("compact-then-fill", "accept", S("""
    public void moveZeroes(int[] a) { int w = 0; for (int x : a) if (x != 0) a[w++] = x; while (w < a.length) a[w++] = 0; }""")),
]
MUTANTS["squares-of-a-sorted-array"] = [
    ("forgets-negatives", "reject", S("""
    public int[] sortedSquares(int[] a) { int[] o = new int[a.length]; for (int i = 0; i < a.length; i++) o[i] = a[i] * a[i]; return o; }""")),
    ("square-then-sort", "accept", S("""
    public int[] sortedSquares(int[] a) { int[] o = new int[a.length]; for (int i = 0; i < a.length; i++) o[i] = a[i] * a[i]; Arrays.sort(o); return o; }""", LIST)),
]
MUTANTS["rotate-array"] = [
    ("no-mod-k", "reject", S("""
    public void rotate(int[] a, int k) { int n = a.length; int[] b = new int[n]; for (int i = 0; i < n; i++) b[(i + k) % n] = a[i]; if (k >= n) return; System.arraycopy(b, 0, a, 0, n); }""")),
    ("rotates-left", "reject", S("""
    public void rotate(int[] a, int k) { int n = a.length; k %= n; int[] b = new int[n]; for (int i = 0; i < n; i++) b[i] = a[(i + k) % n]; System.arraycopy(b, 0, a, 0, n); }""")),
    ("extra-array", "accept", S("""
    public void rotate(int[] a, int k) { int n = a.length; int[] b = new int[n]; for (int i = 0; i < n; i++) b[(int) ((i + (long) k) % n)] = a[i]; System.arraycopy(b, 0, a, 0, n); }""")),
]
MUTANTS["sort-colors"] = [
    ("advances-mid-after-2", "reject", S("""
    public void sortColors(int[] a) {
        int lo = 0, m = 0, hi = a.length - 1;
        while (m <= hi) { if (a[m] == 0) { int t = a[lo]; a[lo++] = a[m]; a[m++] = t; } else if (a[m] == 2) { int t = a[hi]; a[hi--] = a[m]; a[m++] = t; } else m++; }
    }""")),
    ("counting", "accept", S("""
    public void sortColors(int[] a) { int[] c = new int[3]; for (int x : a) c[x]++; int w = 0; for (int v = 0; v < 3; v++) while (c[v]-- > 0) a[w++] = v; }""")),
]
MUTANTS["container-with-most-water"] = [
    ("moves-taller-line", "reject", S("""
    public int maxArea(int[] h) { int i = 0, j = h.length - 1, b = 0; while (i < j) { b = Math.max(b, Math.min(h[i], h[j]) * (j - i)); if (h[i] > h[j]) i++; else j--; } return b; }""")),
    ("brute-force", "accept", S("""
    public int maxArea(int[] h) { int b = 0; for (int i = 0; i < h.length; i++) for (int j = i + 1; j < h.length; j++) b = Math.max(b, Math.min(h[i], h[j]) * (j - i)); return b; }""")),
]
MUTANTS["3sum"] = [
    ("no-dedupe", "reject", S("""
    public List<List<Integer>> threeSum(int[] n) {
        Arrays.sort(n); List<List<Integer>> o = new ArrayList<>();
        for (int i = 0; i < n.length; i++) { int l = i + 1, r = n.length - 1;
            while (l < r) { int s = n[i] + n[l] + n[r]; if (s < 0) l++; else if (s > 0) r--; else { o.add(Arrays.asList(n[i], n[l], n[r])); l++; r--; } } }
        return o;
    }""", LIST)),
    ("brute-force-set", "accept", S("""
    public List<List<Integer>> threeSum(int[] n) {
        Set<List<Integer>> s = new HashSet<>();
        for (int i = 0; i < n.length; i++) for (int j = i + 1; j < n.length; j++) for (int k = j + 1; k < n.length; k++)
            if (n[i] + n[j] + n[k] == 0) { List<Integer> t = Arrays.asList(n[i], n[j], n[k]); Collections.sort(t); s.add(t); }
        return new ArrayList<>(s);
    }""", LIST)),
]
MUTANTS["3sum-closest"] = [
    ("max-value-init-overflows", "reject", S("""
    public int threeSumClosest(int[] a, int t) {
        Arrays.sort(a); int best = Integer.MAX_VALUE;
        for (int i = 0; i < a.length; i++) { int l = i + 1, r = a.length - 1;
            while (l < r) { int s = a[i] + a[l] + a[r]; if (Math.abs(s - t) < Math.abs(best - t)) best = s; if (s < t) l++; else r--; } }
        return best;
    }""", LIST)),
    ("brute-force", "accept", S("""
    public int threeSumClosest(int[] a, int t) {
        int best = a[0] + a[1] + a[2];
        for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) for (int k = j + 1; k < a.length; k++) {
            int s = a[i] + a[j] + a[k]; if (Math.abs(s - t) < Math.abs(best - t)) best = s; }
        return best;
    }""")),
]
MUTANTS["valid-palindrome"] = [
    ("drops-digits", "reject", S("""
    public boolean isPalindrome(String s) { String f = s.toLowerCase().replaceAll("[^a-z]", ""); return new StringBuilder(f).reverse().toString().equals(f); }""")),
    ("filter-and-reverse", "accept", S("""
    public boolean isPalindrome(String s) { String f = s.toLowerCase().replaceAll("[^a-z0-9]", ""); return new StringBuilder(f).reverse().toString().equals(f); }""")),
]
MUTANTS["valid-anagram"] = [
    ("no-length-check-set", "reject", S("""
    public boolean isAnagram(String s, String t) { Set<Character> a = new HashSet<>(), b = new HashSet<>(); for (char c : s.toCharArray()) a.add(c); for (char c : t.toCharArray()) b.add(c); return a.equals(b); }""", LIST)),
    ("sort-both", "accept", S("""
    public boolean isAnagram(String s, String t) { char[] a = s.toCharArray(), b = t.toCharArray(); Arrays.sort(a); Arrays.sort(b); return Arrays.equals(a, b); }""", LIST)),
]
MUTANTS["contains-duplicate"] = [
    ("adjacent-only", "reject", S("""
    public boolean containsDuplicate(int[] a) { for (int i = 1; i < a.length; i++) if (a[i] == a[i - 1]) return true; return false; }""")),
    ("sort", "accept", S("""
    public boolean containsDuplicate(int[] a) { int[] b = a.clone(); Arrays.sort(b); for (int i = 1; i < b.length; i++) if (b[i] == b[i - 1]) return true; return false; }""", LIST)),
]
MUTANTS["single-number"] = [
    ("sum-trick", "reject", S("""
    public int singleNumber(int[] a) { int s = 0; for (int x : a) s += x; return s % 2 == 0 ? s / 2 : s; }""")),
    ("map-count", "accept", S("""
    public int singleNumber(int[] a) { Map<Integer, Integer> c = new HashMap<>(); for (int x : a) c.merge(x, 1, Integer::sum); for (int k : c.keySet()) if (c.get(k) == 1) return k; return 0; }""", LIST)),
]
MUTANTS["majority-element"] = [
    ("most-common-of-first-three", "reject", S("""
    public int majorityElement(int[] a) { return a[a.length / 3]; }""")),
    ("sort-middle", "accept", S("""
    public int majorityElement(int[] a) { int[] b = a.clone(); Arrays.sort(b); return b[b.length / 2]; }""", LIST)),
]
MUTANTS["missing-number"] = [
    ("assumes-range-1-to-n", "reject", S("""
    public int missingNumber(int[] a) { int n = a.length; long s = 0; for (int x : a) s += x; return (int) ((long) n * (n - 1) / 2 - s); }""")),
    ("xor", "accept", S("""
    public int missingNumber(int[] a) { int x = a.length; for (int i = 0; i < a.length; i++) x ^= i ^ a[i]; return x; }""")),
]
MUTANTS["find-all-numbers-disappeared-in-an-array"] = [
    ("no-abs-when-flipping", "reject", S("""
    public List<Integer> findDisappearedNumbers(int[] a) {
        for (int v : a) { int i = v - 1; if (i >= 0 && i < a.length && a[i] > 0) a[i] = -a[i]; }
        List<Integer> o = new ArrayList<>(); for (int i = 0; i < a.length; i++) if (a[i] > 0) o.add(i + 1); return o;
    }""", LIST)),
    ("sign-flip", "accept", S("""
    public List<Integer> findDisappearedNumbers(int[] a) {
        for (int v : a) { int i = Math.abs(v) - 1; if (a[i] > 0) a[i] = -a[i]; }
        List<Integer> o = new ArrayList<>(); for (int i = 0; i < a.length; i++) if (a[i] > 0) o.add(i + 1); return o;
    }""", LIST)),
]
MUTANTS["find-all-duplicates-in-an-array"] = [
    ("reports-every-sighting", "reject", S("""
    public List<Integer> findDuplicates(int[] a) { List<Integer> o = new ArrayList<>(); Set<Integer> s = new HashSet<>(); for (int v : a) if (!s.add(v) || s.size() == 1) o.add(v); return o; }""", LIST)),
    ("sign-flip", "accept", S("""
    public List<Integer> findDuplicates(int[] a) {
        List<Integer> o = new ArrayList<>();
        for (int v : a) { int i = Math.abs(v) - 1; if (a[i] < 0) o.add(i + 1); else a[i] = -a[i]; } return o;
    }""", LIST)),
]
MUTANTS["first-missing-positive"] = [
    ("if-not-while-placement", "reject", S("""
    public int firstMissingPositive(int[] a) {
        int n = a.length;
        for (int i = 0; i < n; i++) if (a[i] >= 1 && a[i] <= n && a[a[i] - 1] != a[i]) { int j = a[i] - 1, t = a[i]; a[i] = a[j]; a[j] = t; }
        for (int i = 0; i < n; i++) if (a[i] != i + 1) return i + 1; return n + 1;
    }""")),
    ("set-scan", "accept", S("""
    public int firstMissingPositive(int[] a) { Set<Integer> s = new HashSet<>(); for (int x : a) s.add(x); int k = 1; while (s.contains(k)) k++; return k; }""", LIST)),
]
MUTANTS["longest-consecutive-sequence"] = [
    ("duplicates-break-run", "reject", S("""
    public int longestConsecutive(int[] a) {
        if (a.length == 0) return 0; int[] b = a.clone(); Arrays.sort(b); int best = 1, run = 1;
        for (int i = 1; i < b.length; i++) { if (b[i] == b[i - 1] + 1) run++; else run = 1; best = Math.max(best, run); } return best;
    }""", LIST)),
    ("sort-skip-dupes", "accept", S("""
    public int longestConsecutive(int[] a) {
        if (a.length == 0) return 0; int[] b = a.clone(); Arrays.sort(b); int best = 1, run = 1;
        for (int i = 1; i < b.length; i++) { if (b[i] == b[i - 1]) continue; if ((long) b[i] == (long) b[i - 1] + 1) run++; else run = 1; best = Math.max(best, run); }
        return best;
    }""", LIST)),
]
MUTANTS["product-of-array-except-self"] = [
    ("division-breaks-on-zero", "reject", S("""
    public int[] productExceptSelf(int[] a) { int p = 1; for (int x : a) p *= x; int[] o = new int[a.length]; for (int i = 0; i < a.length; i++) o[i] = a[i] == 0 ? 0 : p / a[i]; return o; }""")),
    ("brute-force", "accept", S("""
    public int[] productExceptSelf(int[] a) { int[] o = new int[a.length]; for (int i = 0; i < a.length; i++) { int p = 1; for (int j = 0; j < a.length; j++) if (j != i) p *= a[j]; o[i] = p; } return o; }""")),
]
MUTANTS["group-anagrams"] = [
    ("key-is-char-sum", "reject", S("""
    public List<List<String>> groupAnagrams(String[] ss) { Map<Integer, List<String>> m = new HashMap<>(); for (String s : ss) { int k = 0; for (char c : s.toCharArray()) k += c; m.computeIfAbsent(k, x -> new ArrayList<>()).add(s); } return new ArrayList<>(m.values()); }""", LIST)),
    ("sorted-key", "accept", S("""
    public List<List<String>> groupAnagrams(String[] ss) { Map<String, List<String>> m = new HashMap<>(); for (String s : ss) { char[] c = s.toCharArray(); Arrays.sort(c); m.computeIfAbsent(new String(c), x -> new ArrayList<>()).add(s); } return new ArrayList<>(m.values()); }""", LIST)),
]
MUTANTS["top-k-frequent-elements"] = [
    ("k-largest-values-not-frequencies", "reject", S("""
    public int[] topKFrequent(int[] a, int k) { int[] b = Arrays.stream(a).distinct().sorted().toArray(); return Arrays.copyOfRange(b, b.length - k, b.length); }""", LIST)),
    ("sort-entries", "accept", S("""
    public int[] topKFrequent(int[] a, int k) {
        Map<Integer, Integer> c = new HashMap<>(); for (int x : a) c.merge(x, 1, Integer::sum);
        return c.entrySet().stream().sorted((x, y) -> y.getValue() - x.getValue()).limit(k).mapToInt(Map.Entry::getKey).toArray();
    }""", LIST)),
]
MUTANTS["find-the-duplicate-number"] = [
    ("floyd-restart-from-zero-index-value", "reject", S("""
    public int findDuplicate(int[] a) { int s = a[0], f = a[0]; do { s = a[s]; f = a[a[f]]; } while (s != f); return s; }""")),
    ("binary-search-on-value", "accept", S("""
    public int findDuplicate(int[] a) {
        int lo = 1, hi = a.length - 1;
        while (lo < hi) { int m = (lo + hi) / 2, c = 0; for (int x : a) if (x <= m) c++; if (c > m) hi = m; else lo = m + 1; } return lo;
    }""")),
]
