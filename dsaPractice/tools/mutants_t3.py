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

# ------------------------------------------------ batch 2: windows / strings / stack

MUTANTS["longest-substring-without-repeating-characters"] = [
    ("left-moves-backwards", "reject", S("""
    public int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> last = new HashMap<>(); int best = 0, l = 0;
        for (int r = 0; r < s.length(); r++) { Integer p = last.get(s.charAt(r)); if (p != null) l = p + 1; last.put(s.charAt(r), r); best = Math.max(best, r - l + 1); }
        return best;
    }""", LIST)),
    ("brute-force", "accept", S("""
    public int lengthOfLongestSubstring(String s) {
        int best = 0;
        for (int i = 0; i < s.length(); i++) { Set<Character> seen = new HashSet<>(); for (int j = i; j < s.length() && seen.add(s.charAt(j)); j++) best = Math.max(best, j - i + 1); }
        return best;
    }""", LIST)),
]
MUTANTS["minimum-window-substring"] = [
    ("membership-not-counts", "reject", S("""
    public String minWindow(String s, String t) {
        Set<Character> need = new HashSet<>(); for (char c : t.toCharArray()) need.add(c); String best = "";
        for (int i = 0; i < s.length(); i++) { Set<Character> have = new HashSet<>();
            for (int j = i; j < s.length(); j++) { have.add(s.charAt(j)); if (have.containsAll(need)) { if (best.isEmpty() || j - i + 1 < best.length()) best = s.substring(i, j + 1); break; } } }
        return best;
    }""", LIST)),
    ("brute-force", "accept", S("""
    public String minWindow(String s, String t) {
        String best = "";
        for (int i = 0; i < s.length(); i++) { int[] c = new int[128]; for (char ch : t.toCharArray()) c[ch]++; int miss = t.length();
            for (int j = i; j < s.length(); j++) { if (c[s.charAt(j)]-- > 0) miss--; if (miss == 0) { if (best.isEmpty() || j - i + 1 < best.length()) best = s.substring(i, j + 1); break; } } }
        return best;
    }""")),
]
MUTANTS["substring-with-concatenation-of-all-words"] = [
    ("ignores-duplicate-words", "reject", S("""
    public List<Integer> findSubstring(String s, String[] w) {
        Set<String> need = new HashSet<>(Arrays.asList(w)); int wl = w[0].length(), L = wl * w.length; List<Integer> o = new ArrayList<>();
        for (int i = 0; i + L <= s.length(); i++) { boolean ok = true; for (int k = 0; k < w.length; k++) if (!need.contains(s.substring(i + k * wl, i + (k + 1) * wl))) { ok = false; break; } if (ok) o.add(i); }
        return o;
    }""", LIST)),
    ("brute-force-map", "accept", S("""
    public List<Integer> findSubstring(String s, String[] w) {
        Map<String, Integer> need = new HashMap<>(); for (String x : w) need.merge(x, 1, Integer::sum);
        int wl = w[0].length(), L = wl * w.length; List<Integer> o = new ArrayList<>();
        for (int i = 0; i + L <= s.length(); i++) { Map<String, Integer> have = new HashMap<>();
            for (int k = 0; k < w.length; k++) have.merge(s.substring(i + k * wl, i + (k + 1) * wl), 1, Integer::sum);
            if (have.equals(need)) o.add(i); }
        return o;
    }""", LIST)),
]
MUTANTS["longest-repeating-character-replacement"] = [
    ("if-not-while-and-no-k", "reject", S("""
    public int characterReplacement(String s, int k) {
        int best = 1, run = 1; for (int i = 1; i < s.length(); i++) { run = s.charAt(i) == s.charAt(i - 1) ? run + 1 : 1; best = Math.max(best, run); } return Math.min(s.length(), best + k);
    }""")),
    ("brute-force", "accept", S("""
    public int characterReplacement(String s, int k) {
        int best = 0;
        for (int i = 0; i < s.length(); i++) { int[] c = new int[26]; int mx = 0; for (int j = i; j < s.length(); j++) { mx = Math.max(mx, ++c[s.charAt(j) - 'A']); if (j - i + 1 - mx <= k) best = Math.max(best, j - i + 1); } }
        return best;
    }""")),
]
MUTANTS["permutation-in-string"] = [
    ("subsequence-not-substring", "reject", S("""
    public boolean checkInclusion(String a, String b) { int[] c = new int[26]; for (char x : a.toCharArray()) c[x - 'a']++; for (char x : b.toCharArray()) c[x - 'a']--; for (int v : c) if (v > 0) return false; return true; }""")),
    ("compare-26-each-step", "accept", S("""
    public boolean checkInclusion(String a, String b) {
        int n = a.length(); if (n > b.length()) return false; int[] ca = new int[26], cb = new int[26];
        for (int i = 0; i < n; i++) { ca[a.charAt(i) - 'a']++; cb[b.charAt(i) - 'a']++; }
        if (Arrays.equals(ca, cb)) return true;
        for (int r = n; r < b.length(); r++) { cb[b.charAt(r) - 'a']++; cb[b.charAt(r - n) - 'a']--; if (Arrays.equals(ca, cb)) return true; }
        return false;
    }""", LIST)),
]
MUTANTS["fruit-into-baskets"] = [
    ("never-removes-empty-key", "reject", S("""
    public int totalFruit(int[] f) {
        Map<Integer, Integer> c = new HashMap<>(); int l = 0, b = 0;
        for (int r = 0; r < f.length; r++) { c.merge(f[r], 1, Integer::sum); while (c.size() > 2) { c.merge(f[l], -1, Integer::sum); l++; } b = Math.max(b, r - l + 1); }
        return b;
    }""", LIST)),
    ("brute-force", "accept", S("""
    public int totalFruit(int[] f) { int b = 0; for (int i = 0; i < f.length; i++) { Set<Integer> s = new HashSet<>(); for (int j = i; j < f.length; j++) { s.add(f[j]); if (s.size() > 2) break; b = Math.max(b, j - i + 1); } } return b; }""", LIST)),
]
MUTANTS["minimum-size-subarray-sum"] = [
    ("if-not-while", "reject", S("""
    public int minSubArrayLen(int t, int[] a) { int l = 0, s = 0, b = Integer.MAX_VALUE; for (int r = 0; r < a.length; r++) { s += a[r]; if (s >= t) { b = Math.min(b, r - l + 1); s -= a[l++]; } } return b == Integer.MAX_VALUE ? 0 : b; }""")),
    ("brute-force", "accept", S("""
    public int minSubArrayLen(int t, int[] a) { int b = 0; for (int i = 0; i < a.length; i++) { int s = 0; for (int j = i; j < a.length; j++) { s += a[j]; if (s >= t) { if (b == 0 || j - i + 1 < b) b = j - i + 1; break; } } } return b; }""")),
]
MUTANTS["subarray-product-less-than-k"] = [
    # NB: adding `&& l <= r` to the while makes this CORRECT -- it handles k <= 1 by
    # emptying the window. The bug is the unguarded loop, which walks l past r.
    ("no-k-le-1-guard", "reject", S("""
    public int numSubarrayProductLessThanK(int[] a, int k) { long p = 1; int l = 0, c = 0; for (int r = 0; r < a.length; r++) { p *= a[r]; while (p >= k) p /= a[l++]; c += r - l + 1; } return c; }""")),
    ("guarded-loop-is-fine", "accept", S("""
    public int numSubarrayProductLessThanK(int[] a, int k) { long p = 1; int l = 0, c = 0; for (int r = 0; r < a.length; r++) { p *= a[r]; while (p >= k && l <= r) p /= a[l++]; c += r - l + 1; } return c; }""")),
    ("brute-force", "accept", S("""
    public int numSubarrayProductLessThanK(int[] a, int k) { int c = 0; for (int i = 0; i < a.length; i++) { long p = 1; for (int j = i; j < a.length; j++) { p *= a[j]; if (p >= k) break; c++; } } return c; }""")),
]
MUTANTS["maximum-average-subarray-i"] = [
    ("best-starts-at-zero", "reject", S("""
    public double findMaxAverage(int[] a, int k) { long s = 0, b = 0; for (int i = 0; i < a.length; i++) { s += a[i]; if (i >= k) s -= a[i - k]; if (i >= k - 1) b = Math.max(b, s); } return (double) b / k; }""")),
    ("prefix-sums", "accept", S("""
    public double findMaxAverage(int[] a, int k) { long[] p = new long[a.length + 1]; for (int i = 0; i < a.length; i++) p[i + 1] = p[i] + a[i]; long b = Long.MIN_VALUE; for (int i = k; i <= a.length; i++) b = Math.max(b, p[i] - p[i - k]); return b / (double) k; }""")),
]
MUTANTS["sliding-window-maximum"] = [
    ("never-evicts-front", "reject", S("""
    public int[] maxSlidingWindow(int[] a, int k) {
        int[] o = new int[a.length - k + 1]; Deque<Integer> d = new ArrayDeque<>();
        for (int i = 0; i < a.length; i++) { while (!d.isEmpty() && a[d.peekLast()] <= a[i]) d.pollLast(); d.addLast(i); if (i >= k - 1) o[i - k + 1] = a[d.peekFirst()]; }
        return o;
    }""", LIST)),
    ("brute-force", "accept", S("""
    public int[] maxSlidingWindow(int[] a, int k) { int[] o = new int[a.length - k + 1]; for (int i = 0; i < o.length; i++) { int m = a[i]; for (int j = i; j < i + k; j++) m = Math.max(m, a[j]); o[i] = m; } return o; }""")),
]
MUTANTS["sliding-window-median"] = [
    ("int-overflow-mean", "reject", S("""
    public double[] medianSlidingWindow(int[] a, int k) { double[] o = new double[a.length - k + 1]; for (int i = 0; i < o.length; i++) { int[] w = Arrays.copyOfRange(a, i, i + k); Arrays.sort(w); o[i] = (w[(k - 1) / 2] + w[k / 2]) / 2.0; } return o; }""", LIST)),
    ("sort-each-window", "accept", S("""
    public double[] medianSlidingWindow(int[] a, int k) { double[] o = new double[a.length - k + 1]; for (int i = 0; i < o.length; i++) { int[] w = Arrays.copyOfRange(a, i, i + k); Arrays.sort(w); o[i] = ((long) w[(k - 1) / 2] + w[k / 2]) / 2.0; } return o; }""", LIST)),
]
MUTANTS["valid-parentheses"] = [
    ("counts-only", "reject", S("""
    public boolean isValid(String s) { int a = 0, b = 0, c = 0; for (char x : s.toCharArray()) { if (x == '(') a++; if (x == ')') a--; if (x == '[') b++; if (x == ']') b--; if (x == '{') c++; if (x == '}') c--; if (a < 0 || b < 0 || c < 0) return false; } return a == 0 && b == 0 && c == 0; }""")),
    ("replace-pairs", "accept", S("""
    public boolean isValid(String s) { String p; do { p = s; s = s.replace("()", "").replace("[]", "").replace("{}", ""); } while (!s.equals(p)); return s.isEmpty(); }""")),
]
MUTANTS["backspace-string-compare"] = [
    ("backspace-on-empty-crashes-or-counts", "reject", S("""
    public boolean backspaceCompare(String s, String t) { return f(s).equals(f(t)); }
    String f(String s) { StringBuilder b = new StringBuilder(); for (char c : s.toCharArray()) { if (c == '#') { if (b.length() > 0) b.setLength(b.length() - 1); else b.append('#'); } else b.append(c); } return b.toString(); }""")),
    ("stack-build", "accept", S("""
    public boolean backspaceCompare(String s, String t) { return f(s).equals(f(t)); }
    String f(String s) { StringBuilder b = new StringBuilder(); for (char c : s.toCharArray()) { if (c == '#') { if (b.length() > 0) b.setLength(b.length() - 1); } else b.append(c); } return b.toString(); }""")),
]
MUTANTS["is-subsequence"] = [
    ("contains-each-char", "reject", S("""
    public boolean isSubsequence(String s, String t) { for (char c : s.toCharArray()) if (t.indexOf(c) < 0) return false; return true; }""")),
    ("indexOf-from", "accept", S("""
    public boolean isSubsequence(String s, String t) { int p = 0; for (char c : s.toCharArray()) { p = t.indexOf(c, p); if (p < 0) return false; p++; } return true; }""")),
]
MUTANTS["find-k-closest-elements"] = [
    ("tie-prefers-larger", "reject", S("""
    public List<Integer> findClosestElements(int[] a, int k, int x) { int l = 0, r = a.length - 1; while (r - l + 1 > k) { if (Math.abs(a[l] - x) >= Math.abs(a[r] - x)) l++; else r--; } List<Integer> o = new ArrayList<>(); for (int i = l; i <= r; i++) o.add(a[i]); return o; }""", LIST)),
    ("shrink-both-ends", "accept", S("""
    public List<Integer> findClosestElements(int[] a, int k, int x) { int l = 0, r = a.length - 1; while (r - l + 1 > k) { if (Math.abs(a[l] - x) > Math.abs(a[r] - x)) l++; else r--; } List<Integer> o = new ArrayList<>(); for (int i = l; i <= r; i++) o.add(a[i]); return o; }""", LIST)),
]
MUTANTS["trapping-rain-water"] = [
    ("uses-global-max", "reject", S("""
    public int trap(int[] h) { int m = 0; for (int x : h) m = Math.max(m, x); int w = 0; for (int i = 1; i < h.length - 1; i++) w += Math.max(0, m - h[i]); return w; }""")),
    ("prefix-max-arrays", "accept", S("""
    public int trap(int[] h) { int n = h.length; int[] L = new int[n], R = new int[n]; for (int i = 0; i < n; i++) L[i] = Math.max(i > 0 ? L[i - 1] : 0, h[i]); for (int i = n - 1; i >= 0; i--) R[i] = Math.max(i < n - 1 ? R[i + 1] : 0, h[i]); int w = 0; for (int i = 0; i < n; i++) w += Math.min(L[i], R[i]) - h[i]; return w; }""")),
]
MUTANTS["longest-palindromic-substring"] = [
    ("odd-centres-only", "reject", S("""
    public String longestPalindrome(String s) { int bs = 0, bl = 1; for (int c = 0; c < s.length(); c++) { int l = c, r = c; while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; } if (r - l - 1 > bl) { bl = r - l - 1; bs = l + 1; } } return s.substring(bs, bs + bl); }""")),
    ("dp-table-picks-other-tie", "accept", S("""
    public String longestPalindrome(String s) {
        int n = s.length(); boolean[][] d = new boolean[n][n]; String best = s.substring(n - 1);
        for (int i = n - 1; i >= 0; i--) for (int j = i; j < n; j++) { d[i][j] = s.charAt(i) == s.charAt(j) && (j - i < 2 || d[i + 1][j - 1]); if (d[i][j] && j - i + 1 >= best.length()) best = s.substring(i, j + 1); }
        return best;
    }""")),
]
MUTANTS["palindromic-substrings"] = [
    ("counts-distinct", "reject", S("""
    public int countSubstrings(String s) { Set<String> seen = new HashSet<>(); for (int i = 0; i < s.length(); i++) for (int j = i + 1; j <= s.length(); j++) { String t = s.substring(i, j); if (new StringBuilder(t).reverse().toString().equals(t)) seen.add(t); } return seen.size(); }""", LIST)),
    ("brute-force", "accept", S("""
    public int countSubstrings(String s) { int c = 0; for (int i = 0; i < s.length(); i++) for (int j = i + 1; j <= s.length(); j++) { String t = s.substring(i, j); if (new StringBuilder(t).reverse().toString().equals(t)) c++; } return c; }""")),
]
MUTANTS["count-unique-characters-of-all-substrings-of-a-given-string"] = [
    ("counts-distinct-not-unique", "reject", S("""
    public int uniqueLetterString(String s) { int t = 0; for (int i = 0; i < s.length(); i++) { Set<Character> seen = new HashSet<>(); for (int j = i; j < s.length(); j++) { seen.add(s.charAt(j)); t += seen.size(); } } return t; }""", LIST)),
    ("brute-force", "accept", S("""
    public int uniqueLetterString(String s) { int t = 0; for (int i = 0; i < s.length(); i++) { int[] c = new int[26]; int u = 0; for (int j = i; j < s.length(); j++) { int x = s.charAt(j) - 'A'; if (++c[x] == 1) u++; else if (c[x] == 2) u--; t += u; } } return t; }""")),
]

# ------------------------------------------------------------ batch 3: linked lists

MUTANTS["add-two-numbers"] = [
    ("drops-final-carry", "reject", S("""
    public ListNode addTwoNumbers(ListNode a, ListNode b) { ListNode d = new ListNode(0), t = d; int c = 0;
        while (a != null || b != null) { int s = c + (a != null ? a.val : 0) + (b != null ? b.val : 0); t = t.next = new ListNode(s % 10); c = s / 10; if (a != null) a = a.next; if (b != null) b = b.next; }
        return d.next; }""")),
    ("recursive", "accept", S("""
    public ListNode addTwoNumbers(ListNode a, ListNode b) { return go(a, b, 0); }
    ListNode go(ListNode a, ListNode b, int c) { if (a == null && b == null && c == 0) return null; int s = c + (a != null ? a.val : 0) + (b != null ? b.val : 0); ListNode n = new ListNode(s % 10); n.next = go(a != null ? a.next : null, b != null ? b.next : null, s / 10); return n; }""")),
]
MUTANTS["remove-nth-node-from-end-of-list"] = [
    ("no-dummy-head-removal-fails", "reject", S("""
    public ListNode removeNthFromEnd(ListNode h, int n) { ListNode f = h, s = h; for (int i = 0; i < n; i++) f = f.next; if (f == null) return h; while (f.next != null) { f = f.next; s = s.next; } s.next = s.next.next; return h; }""")),
    ("two-pass", "accept", S("""
    public ListNode removeNthFromEnd(ListNode h, int n) { int len = 0; for (ListNode p = h; p != null; p = p.next) len++; ListNode d = new ListNode(0, h), p = d; for (int i = 0; i < len - n; i++) p = p.next; p.next = p.next.next; return d.next; }""")),
]
MUTANTS["merge-two-sorted-lists"] = [
    ("drops-remainder", "reject", S("""
    public ListNode mergeTwoLists(ListNode a, ListNode b) { ListNode d = new ListNode(0), t = d; while (a != null && b != null) { if (a.val <= b.val) { t.next = a; a = a.next; } else { t.next = b; b = b.next; } t = t.next; } t.next = null; return d.next; }""")),
    ("recursive", "accept", S("""
    public ListNode mergeTwoLists(ListNode a, ListNode b) { if (a == null) return b; if (b == null) return a; if (a.val <= b.val) { a.next = mergeTwoLists(a.next, b); return a; } b.next = mergeTwoLists(a, b.next); return b; }""")),
]
MUTANTS["merge-k-sorted-lists"] = [
    ("null-heads-into-heap", "reject", S("""
    public ListNode mergeKLists(ListNode[] ls) { PriorityQueue<ListNode> q = new PriorityQueue<>((x, y) -> x.val - y.val); for (ListNode l : ls) q.add(l); ListNode d = new ListNode(0), t = d; while (!q.isEmpty()) { ListNode n = q.poll(); t = t.next = n; if (n.next != null) q.add(n.next); } return d.next; }""", LIST)),
    ("collect-sort-rebuild", "accept", S("""
    public ListNode mergeKLists(ListNode[] ls) { List<Integer> v = new ArrayList<>(); for (ListNode l : ls) for (ListNode p = l; p != null; p = p.next) v.add(p.val); Collections.sort(v); ListNode d = new ListNode(0), t = d; for (int x : v) t = t.next = new ListNode(x); return d.next; }""", LIST)),
]
MUTANTS["swap-nodes-in-pairs"] = [
    ("loses-rest-of-list", "reject", S("""
    public ListNode swapPairs(ListNode h) { ListNode d = new ListNode(0, h), p = d; while (p.next != null && p.next.next != null) { ListNode a = p.next, b = a.next; b.next = a; p.next = b; a.next = null; p = a; } return d.next; }""")),
    ("swap-values", "accept", S("""
    public ListNode swapPairs(ListNode h) { for (ListNode p = h; p != null && p.next != null; p = p.next.next) { int t = p.val; p.val = p.next.val; p.next.val = t; } return h; }""")),
]
MUTANTS["reverse-nodes-in-k-group"] = [
    ("reverses-short-tail-too", "reject", S("""
    public ListNode reverseKGroup(ListNode h, int k) { List<Integer> v = new ArrayList<>(); for (ListNode p = h; p != null; p = p.next) v.add(p.val); for (int i = 0; i < v.size(); i += k) Collections.reverse(v.subList(i, Math.min(i + k, v.size()))); ListNode d = new ListNode(0), t = d; for (int x : v) t = t.next = new ListNode(x); return d.next; }""", LIST)),
    ("array-rebuild-correct", "accept", S("""
    public ListNode reverseKGroup(ListNode h, int k) { List<Integer> v = new ArrayList<>(); for (ListNode p = h; p != null; p = p.next) v.add(p.val); for (int i = 0; i + k <= v.size(); i += k) Collections.reverse(v.subList(i, i + k)); ListNode d = new ListNode(0), t = d; for (int x : v) t = t.next = new ListNode(x); return d.next; }""", LIST)),
]
MUTANTS["rotate-list"] = [
    ("step-by-step-rotation", "reject", S("""
    public ListNode rotateRight(ListNode h, int k) { if (h == null || h.next == null) return h; for (int r = 0; r < k; r++) { ListNode p = h; while (p.next.next != null) p = p.next; ListNode last = p.next; p.next = null; last.next = h; h = last; } return h; }""")),
    ("ring-and-cut", "accept", S("""
    public ListNode rotateRight(ListNode h, int k) { if (h == null) return h; int n = 1; ListNode t = h; while (t.next != null) { t = t.next; n++; } t.next = h; for (int i = 0; i < n - k % n; i++) t = t.next; ListNode nh = t.next; t.next = null; return nh; }""")),
]
MUTANTS["remove-duplicates-from-sorted-list"] = [
    ("always-advances", "reject", S("""
    public ListNode deleteDuplicates(ListNode h) { for (ListNode p = h; p != null && p.next != null; p = p.next) if (p.next.val == p.val) p.next = p.next.next; return h; }""")),
    ("recursive", "accept", S("""
    public ListNode deleteDuplicates(ListNode h) { if (h == null || h.next == null) return h; h.next = deleteDuplicates(h.next); return h.val == h.next.val ? h.next : h; }""")),
]
MUTANTS["reverse-linked-list-ii"] = [
    ("off-by-one-right", "reject", S("""
    public ListNode reverseBetween(ListNode h, int l, int r) { ListNode d = new ListNode(0, h), p = d; for (int i = 1; i < l; i++) p = p.next; ListNode c = p.next; for (int i = 0; i < r - l - 1; i++) { ListNode t = c.next; c.next = t.next; t.next = p.next; p.next = t; } return d.next; }""")),
    ("values-in-list", "accept", S("""
    public ListNode reverseBetween(ListNode h, int l, int r) { List<ListNode> ns = new ArrayList<>(); for (ListNode p = h; p != null; p = p.next) ns.add(p); for (int i = l - 1, j = r - 1; i < j; i++, j--) { int t = ns.get(i).val; ns.get(i).val = ns.get(j).val; ns.get(j).val = t; } return h; }""", LIST)),
]
MUTANTS["linked-list-cycle"] = [
    ("counts-to-a-limit", "reject", S("""
    public boolean hasCycle(ListNode h) { int n = 0; for (ListNode p = h; p != null; p = p.next) if (++n > 5) return true; return false; }""")),
    ("visited-set", "accept", S("""
    public boolean hasCycle(ListNode h) { Set<ListNode> s = new HashSet<>(); for (ListNode p = h; p != null; p = p.next) if (!s.add(p)) return true; return false; }""", LIST)),
]
MUTANTS["linked-list-cycle-ii"] = [
    ("returns-meeting-point", "reject", S("""
    public ListNode detectCycle(ListNode h) { ListNode s = h, f = h; while (f != null && f.next != null) { s = s.next; f = f.next.next; if (s == f) return s; } return null; }""")),
    ("marks-nodes-by-modifying", "reject", S("""
    public ListNode detectCycle(ListNode h) { for (ListNode p = h; p != null; p = p.next) { if (p.val == Integer.MIN_VALUE) return p; p.val = Integer.MIN_VALUE; } return null; }""")),
    ("visited-set", "accept", S("""
    public ListNode detectCycle(ListNode h) { Set<ListNode> s = new HashSet<>(); for (ListNode p = h; p != null; p = p.next) if (!s.add(p)) return p; return null; }""", LIST)),
]
MUTANTS["reorder-list"] = [
    ("forgets-to-cut-middle", "reject", S("""
    public void reorderList(ListNode h) {
        ListNode s = h, f = h; while (f.next != null && f.next.next != null) { s = s.next; f = f.next.next; }
        ListNode sec = s.next, prev = null; while (sec != null) { ListNode t = sec.next; sec.next = prev; prev = sec; sec = t; }
        for (ListNode a = h, b = prev; b != null; ) { ListNode an = a.next, bn = b.next; a.next = b; b.next = an; a = an; b = bn; }
    }""")),
    ("list-of-nodes", "accept", S("""
    public void reorderList(ListNode h) { List<ListNode> ns = new ArrayList<>(); for (ListNode p = h; p != null; p = p.next) ns.add(p); int i = 0, j = ns.size() - 1;
        while (i < j) { ns.get(i).next = ns.get(j); i++; if (i == j) break; ns.get(j).next = ns.get(i); j--; } ns.get(i).next = null; }""", LIST)),
]
MUTANTS["sort-list"] = [
    ("split-from-head-infinite", "reject", S("""
    public ListNode sortList(ListNode h) { if (h == null || h.next == null) return h; ListNode s = h, f = h; while (f != null && f.next != null) { s = s.next; f = f.next.next; } ListNode r = s.next; s.next = null; return m(sortList(h), sortList(r)); }
    ListNode m(ListNode a, ListNode b) { ListNode d = new ListNode(0), t = d; while (a != null && b != null) { if (a.val <= b.val) { t.next = a; a = a.next; } else { t.next = b; b = b.next; } t = t.next; } t.next = a != null ? a : b; return d.next; }""")),
    ("insertion-sort", "accept", S("""
    public ListNode sortList(ListNode h) { ListNode d = new ListNode(Integer.MIN_VALUE); while (h != null) { ListNode nx = h.next, p = d; while (p.next != null && p.next.val <= h.val) p = p.next; h.next = p.next; p.next = h; h = nx; } return d.next; }""")),
]
MUTANTS["remove-linked-list-elements"] = [
    ("misses-leading-run", "reject", S("""
    public ListNode removeElements(ListNode h, int v) { if (h != null && h.val == v) h = h.next; for (ListNode p = h; p != null && p.next != null; ) if (p.next.val == v) p.next = p.next.next; else p = p.next; return h; }""")),
    ("recursive", "accept", S("""
    public ListNode removeElements(ListNode h, int v) { if (h == null) return null; h.next = removeElements(h.next, v); return h.val == v ? h.next : h; }""")),
]
MUTANTS["reverse-linked-list"] = [
    ("recursive-no-null-out", "reject", S("""
    public ListNode reverseList(ListNode h) { if (h == null || h.next == null) return h; ListNode r = reverseList(h.next); h.next.next = h; return r; }""")),
    ("recursive", "accept", S("""
    public ListNode reverseList(ListNode h) { if (h == null || h.next == null) return h; ListNode r = reverseList(h.next); h.next.next = h; h.next = null; return r; }""")),
]
MUTANTS["palindrome-linked-list"] = [
    ("first-equals-last-only", "reject", S("""
    public boolean isPalindrome(ListNode h) { ListNode t = h; while (t.next != null) t = t.next; return h.val == t.val; }""")),
    ("reverse-second-half", "accept", S("""
    public boolean isPalindrome(ListNode h) { ListNode s = h, f = h; while (f != null && f.next != null) { s = s.next; f = f.next.next; } ListNode p = null; while (s != null) { ListNode t = s.next; s.next = p; p = s; s = t; }
        for (ListNode a = h, b = p; b != null; a = a.next, b = b.next) if (a.val != b.val) return false; return true; }""")),
]
MUTANTS["odd-even-linked-list"] = [
    ("partitions-by-value", "reject", S("""
    public ListNode oddEvenList(ListNode h) { ListNode o = new ListNode(0), e = new ListNode(0), a = o, b = e; for (ListNode p = h; p != null; p = p.next) { if (p.val % 2 == 1) a = a.next = p; else b = b.next = p; } b.next = null; a.next = e.next; return o.next; }""")),
    ("index-based", "accept", S("""
    public ListNode oddEvenList(ListNode h) { ListNode o = new ListNode(0), e = new ListNode(0), a = o, b = e; int i = 1; for (ListNode p = h; p != null; p = p.next, i++) { if (i % 2 == 1) a = a.next = p; else b = b.next = p; } b.next = null; a.next = e.next; return o.next; }""")),
]
MUTANTS["middle-of-the-linked-list"] = [
    ("first-middle", "reject", S("""
    public ListNode middleNode(ListNode h) { ListNode s = h, f = h; while (f.next != null && f.next.next != null) { s = s.next; f = f.next.next; } return s; }""")),
    ("count-then-walk", "accept", S("""
    public ListNode middleNode(ListNode h) { int n = 0; for (ListNode p = h; p != null; p = p.next) n++; ListNode p = h; for (int i = 0; i < n / 2; i++) p = p.next; return p; }""")),
]

# --------------------------------------------------------------------- batch 4: trees

MUTANTS["validate-binary-search-tree"] = [
    ("checks-children-only", "reject", S("""
    public boolean isValidBST(TreeNode n) { if (n == null) return true; if (n.left != null && n.left.val >= n.val) return false; if (n.right != null && n.right.val <= n.val) return false; return isValidBST(n.left) && isValidBST(n.right); }""")),
    ("int-bounds", "reject", S("""
    public boolean isValidBST(TreeNode n) { return ok(n, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    boolean ok(TreeNode n, int lo, int hi) { if (n == null) return true; return n.val > lo && n.val < hi && ok(n.left, lo, n.val) && ok(n.right, n.val, hi); }""")),
    ("inorder-strictly-increasing", "accept", S("""
    Long prev = null;
    public boolean isValidBST(TreeNode n) { prev = null; return in(n); }
    boolean in(TreeNode n) { if (n == null) return true; if (!in(n.left)) return false; if (prev != null && n.val <= prev) return false; prev = (long) n.val; return in(n.right); }""")),
]
MUTANTS["same-tree"] = [
    ("same-preorder-values", "reject", S("""
    public boolean isSameTree(TreeNode p, TreeNode q) { List<Integer> a = new ArrayList<>(), b = new ArrayList<>(); w(p, a); w(q, b); return a.equals(b); }
    void w(TreeNode n, List<Integer> o) { if (n == null) return; o.add(n.val); w(n.left, o); w(n.right, o); }""", LIST)),
    ("bfs-pairs", "accept", S("""
    public boolean isSameTree(TreeNode p, TreeNode q) { Deque<TreeNode[]> d = new ArrayDeque<>(); d.add(new TreeNode[]{p, q});
        while (!d.isEmpty()) { TreeNode[] x = d.poll(); if (x[0] == null && x[1] == null) continue; if (x[0] == null || x[1] == null || x[0].val != x[1].val) return false;
            d.add(new TreeNode[]{x[0].left, x[1].left}); d.add(new TreeNode[]{x[0].right, x[1].right}); } return true; }""", LIST)),
]
MUTANTS["binary-tree-level-order-traversal"] = [
    ("size-read-inside-loop", "reject", S("""
    public List<List<Integer>> levelOrder(TreeNode r) { List<List<Integer>> o = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r);
        while (!q.isEmpty()) { List<Integer> l = new ArrayList<>(); for (int i = 0; i < q.size(); i++) { TreeNode x = q.poll(); l.add(x.val); if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } o.add(l); } return o; }""", LIST)),
    ("dfs-with-depth", "accept", S("""
    public List<List<Integer>> levelOrder(TreeNode r) { List<List<Integer>> o = new ArrayList<>(); go(r, 0, o); return o; }
    void go(TreeNode n, int d, List<List<Integer>> o) { if (n == null) return; if (o.size() == d) o.add(new ArrayList<>()); o.get(d).add(n.val); go(n.left, d + 1, o); go(n.right, d + 1, o); }""", LIST)),
]
MUTANTS["binary-tree-zigzag-level-order-traversal"] = [
    ("swaps-child-enqueue-order", "reject", S("""
    public List<List<Integer>> zigzagLevelOrder(TreeNode r) { List<List<Integer>> o = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r); boolean rev = false;
        while (!q.isEmpty()) { List<Integer> l = new ArrayList<>(); for (int n = q.size(); n > 0; n--) { TreeNode x = q.poll(); l.add(x.val);
            if (rev) { if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } else { if (x.right != null) q.add(x.right); if (x.left != null) q.add(x.left); } }
            o.add(l); rev = !rev; } return o; }""", LIST)),
    ("reverse-odd-levels", "accept", S("""
    public List<List<Integer>> zigzagLevelOrder(TreeNode r) { List<List<Integer>> o = new ArrayList<>(); go(r, 0, o); for (int i = 1; i < o.size(); i += 2) Collections.reverse(o.get(i)); return o; }
    void go(TreeNode n, int d, List<List<Integer>> o) { if (n == null) return; if (o.size() == d) o.add(new ArrayList<>()); o.get(d).add(n.val); go(n.left, d + 1, o); go(n.right, d + 1, o); }""", LIST)),
]
MUTANTS["maximum-depth-of-binary-tree"] = [
    ("counts-edges", "reject", S("""
    public int maxDepth(TreeNode r) { if (r == null) return 0; if (r.left == null && r.right == null) return 0; return 1 + Math.max(maxDepth(r.left), maxDepth(r.right)); }""")),
    ("bfs-levels", "accept", S("""
    public int maxDepth(TreeNode r) { int d = 0; Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r); while (!q.isEmpty()) { d++; for (int n = q.size(); n > 0; n--) { TreeNode x = q.poll(); if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } } return d; }""", LIST)),
]
MUTANTS["construct-binary-tree-from-preorder-and-inorder-traversal"] = [
    ("everything-goes-left", "reject", S("""
    public TreeNode buildTree(int[] p, int[] in) { TreeNode r = new TreeNode(p[0]), c = r; for (int i = 1; i < p.length; i++) { c.left = new TreeNode(p[i]); c = c.left; } return r; }""")),
    ("linear-search-recursion", "accept", S("""
    public TreeNode buildTree(int[] p, int[] in) { return b(p, 0, p.length - 1, in, 0, in.length - 1); }
    TreeNode b(int[] p, int ps, int pe, int[] in, int is, int ie) { if (ps > pe) return null; TreeNode n = new TreeNode(p[ps]); int m = is; while (in[m] != p[ps]) m++; int L = m - is;
        n.left = b(p, ps + 1, ps + L, in, is, m - 1); n.right = b(p, ps + L + 1, pe, in, m + 1, ie); return n; }""")),
]
MUTANTS["binary-tree-level-order-traversal-ii"] = [
    ("reverses-within-levels", "reject", S("""
    public List<List<Integer>> levelOrderBottom(TreeNode r) { List<List<Integer>> o = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r);
        while (!q.isEmpty()) { List<Integer> l = new ArrayList<>(); for (int n = q.size(); n > 0; n--) { TreeNode x = q.poll(); l.add(x.val); if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } Collections.reverse(l); o.add(0, l); }
        Collections.reverse(o); return o; }""", LIST)),
    ("bfs-then-reverse", "accept", S("""
    public List<List<Integer>> levelOrderBottom(TreeNode r) { List<List<Integer>> o = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r);
        while (!q.isEmpty()) { List<Integer> l = new ArrayList<>(); for (int n = q.size(); n > 0; n--) { TreeNode x = q.poll(); l.add(x.val); if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } o.add(l); }
        Collections.reverse(o); return o; }""", LIST)),
]
MUTANTS["minimum-depth-of-binary-tree"] = [
    ("min-of-both-sides", "reject", S("""
    public int minDepth(TreeNode r) { return r == null ? 0 : 1 + Math.min(minDepth(r.left), minDepth(r.right)); }""")),
    ("bfs-first-leaf", "accept", S("""
    public int minDepth(TreeNode r) { if (r == null) return 0; Deque<TreeNode> q = new ArrayDeque<>(); q.add(r); int d = 0;
        while (!q.isEmpty()) { d++; for (int n = q.size(); n > 0; n--) { TreeNode x = q.poll(); if (x.left == null && x.right == null) return d; if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } } return d; }""", LIST)),
]
MUTANTS["path-sum"] = [
    ("any-node-not-leaf", "reject", S("""
    public boolean hasPathSum(TreeNode n, int t) { if (n == null) return false; if (n.val == t) return true; return hasPathSum(n.left, t - n.val) || hasPathSum(n.right, t - n.val); }""")),
    ("empty-tree-target-zero-true", "reject", S("""
    public boolean hasPathSum(TreeNode n, int t) { if (n == null) return t == 0; return hasPathSum(n.left, t - n.val) || hasPathSum(n.right, t - n.val); }""")),
    ("iterative-stack", "accept", S("""
    public boolean hasPathSum(TreeNode r, int t) { if (r == null) return false; Deque<Object[]> s = new ArrayDeque<>(); s.push(new Object[]{r, r.val});
        while (!s.isEmpty()) { Object[] e = s.pop(); TreeNode n = (TreeNode) e[0]; int v = (Integer) e[1]; if (n.left == null && n.right == null && v == t) return true;
            if (n.left != null) s.push(new Object[]{n.left, v + n.left.val}); if (n.right != null) s.push(new Object[]{n.right, v + n.right.val}); } return false; }""", LIST)),
]
MUTANTS["path-sum-ii"] = [
    ("stores-shared-path", "reject", S("""
    public List<List<Integer>> pathSum(TreeNode r, int t) { List<List<Integer>> o = new ArrayList<>(); go(r, t, new ArrayList<>(), o); return o; }
    void go(TreeNode n, int t, List<Integer> p, List<List<Integer>> o) { if (n == null) return; p.add(n.val); if (n.left == null && n.right == null && t == n.val) o.add(p); go(n.left, t - n.val, p, o); go(n.right, t - n.val, p, o); p.remove(p.size() - 1); }""", LIST)),
    ("copy-on-descent", "accept", S("""
    public List<List<Integer>> pathSum(TreeNode r, int t) { List<List<Integer>> o = new ArrayList<>(); go(r, t, new ArrayList<>(), o); return o; }
    void go(TreeNode n, int t, List<Integer> p, List<List<Integer>> o) { if (n == null) return; List<Integer> q = new ArrayList<>(p); q.add(n.val); if (n.left == null && n.right == null && t == n.val) o.add(q); go(n.left, t - n.val, q, o); go(n.right, t - n.val, q, o); }""", LIST)),
]
MUTANTS["binary-tree-maximum-path-sum"] = [
    ("best-starts-at-zero", "reject", S("""
    int b = 0;
    public int maxPathSum(TreeNode r) { b = 0; g(r); return b; }
    int g(TreeNode n) { if (n == null) return 0; int l = Math.max(0, g(n.left)), rr = Math.max(0, g(n.right)); b = Math.max(b, n.val + l + rr); return n.val + Math.max(l, rr); }""")),
    ("no-clamp-negative-arms", "reject", S("""
    int b = Integer.MIN_VALUE;
    public int maxPathSum(TreeNode r) { b = Integer.MIN_VALUE; g(r); return b; }
    int g(TreeNode n) { if (n == null) return 0; int l = g(n.left), rr = g(n.right); b = Math.max(b, n.val + l + rr); return n.val + Math.max(l, rr); }""")),
    ("brute-every-pair-via-parents", "accept", S("""
    public int maxPathSum(TreeNode r) {
        List<TreeNode> all = new ArrayList<>(); Map<TreeNode, TreeNode> par = new IdentityHashMap<>(); Deque<TreeNode> q = new ArrayDeque<>(); q.add(r);
        while (!q.isEmpty()) { TreeNode n = q.poll(); all.add(n); for (TreeNode c : new TreeNode[]{n.left, n.right}) if (c != null) { par.put(c, n); q.add(c); } }
        int best = Integer.MIN_VALUE;
        for (TreeNode a : all) { Map<TreeNode, Integer> up = new IdentityHashMap<>(); int s = 0; for (TreeNode x = a; x != null; x = par.get(x)) { s += x.val; up.put(x, s); }
            for (TreeNode b : all) { int t = 0; TreeNode x = b; while (!up.containsKey(x)) { t += x.val; x = par.get(x); } best = Math.max(best, t + up.get(x)); } }
        return best;
    }""", LIST)),
]
MUTANTS["binary-tree-right-side-view"] = [
    ("follows-right-children-only", "reject", S("""
    public List<Integer> rightSideView(TreeNode r) { List<Integer> o = new ArrayList<>(); for (TreeNode n = r; n != null; n = n.right != null ? n.right : null) o.add(n.val); return o; }""", LIST)),
    ("dfs-right-first", "accept", S("""
    public List<Integer> rightSideView(TreeNode r) { List<Integer> o = new ArrayList<>(); go(r, 0, o); return o; }
    void go(TreeNode n, int d, List<Integer> o) { if (n == null) return; if (d == o.size()) o.add(n.val); go(n.right, d + 1, o); go(n.left, d + 1, o); }""", LIST)),
]
MUTANTS["invert-binary-tree"] = [
    ("reads-overwritten-child", "reject", S("""
    public TreeNode invertTree(TreeNode r) { if (r == null) return null; r.left = invertTree(r.right); r.right = invertTree(r.left); return r; }""")),
    ("bfs-swap", "accept", S("""
    public TreeNode invertTree(TreeNode r) { Deque<TreeNode> q = new ArrayDeque<>(); if (r != null) q.add(r); while (!q.isEmpty()) { TreeNode n = q.poll(); TreeNode t = n.left; n.left = n.right; n.right = t; if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); } return r; }""", LIST)),
]
MUTANTS["kth-smallest-element-in-a-bst"] = [
    ("preorder-not-inorder", "reject", S("""
    int k2, ans;
    public int kthSmallest(TreeNode r, int k) { k2 = k; go(r); return ans; }
    void go(TreeNode n) { if (n == null || k2 <= 0) return; if (--k2 == 0) { ans = n.val; return; } go(n.left); go(n.right); }""")),
    ("collect-inorder", "accept", S("""
    public int kthSmallest(TreeNode r, int k) { List<Integer> o = new ArrayList<>(); go(r, o); return o.get(k - 1); }
    void go(TreeNode n, List<Integer> o) { if (n == null) return; go(n.left, o); o.add(n.val); go(n.right, o); }""", LIST)),
]
MUTANTS["lowest-common-ancestor-of-a-binary-search-tree"] = [
    ("returns-a-copy", "reject", S("""
    public TreeNode lowestCommonAncestor(TreeNode r, TreeNode p, TreeNode q) { TreeNode n = r; while (true) { if (p.val < n.val && q.val < n.val) n = n.left; else if (p.val > n.val && q.val > n.val) n = n.right; else return new TreeNode(n.val); } }""")),
    ("general-tree-algorithm", "accept", S("""
    public TreeNode lowestCommonAncestor(TreeNode r, TreeNode p, TreeNode q) { if (r == null || r == p || r == q) return r; TreeNode l = lowestCommonAncestor(r.left, p, q), rr = lowestCommonAncestor(r.right, p, q); return l != null && rr != null ? r : l != null ? l : rr; }""")),
]
MUTANTS["lowest-common-ancestor-of-a-binary-tree"] = [
    ("returns-p-always-if-found-left", "reject", S("""
    public TreeNode lowestCommonAncestor(TreeNode r, TreeNode p, TreeNode q) { if (r == null || r == p || r == q) return r; TreeNode l = lowestCommonAncestor(r.left, p, q); return l != null ? l : lowestCommonAncestor(r.right, p, q); }""")),
    ("parent-map", "accept", S("""
    public TreeNode lowestCommonAncestor(TreeNode r, TreeNode p, TreeNode q) { Map<TreeNode, TreeNode> par = new IdentityHashMap<>(); Deque<TreeNode> d = new ArrayDeque<>(); d.add(r); par.put(r, null);
        while (!d.isEmpty()) { TreeNode n = d.poll(); for (TreeNode c : new TreeNode[]{n.left, n.right}) if (c != null) { par.put(c, n); d.add(c); } }
        Set<TreeNode> anc = Collections.newSetFromMap(new IdentityHashMap<>()); for (TreeNode x = p; x != null; x = par.get(x)) anc.add(x); TreeNode x = q; while (!anc.contains(x)) x = par.get(x); return x; }""", LIST)),
]
MUTANTS["binary-tree-paths"] = [
    ("every-node-not-leaves", "reject", S("""
    public List<String> binaryTreePaths(TreeNode r) { List<String> o = new ArrayList<>(); go(r, "", o); return o; }
    void go(TreeNode n, String p, List<String> o) { if (n == null) return; String s = p.isEmpty() ? "" + n.val : p + "->" + n.val; o.add(s); go(n.left, s, o); go(n.right, s, o); }""", LIST)),
    ("stringbuilder-backtrack", "accept", S("""
    public List<String> binaryTreePaths(TreeNode r) { List<String> o = new ArrayList<>(); go(r, new StringBuilder(), o); return o; }
    void go(TreeNode n, StringBuilder b, List<String> o) { if (n == null) return; int len = b.length(); if (len > 0) b.append("->"); b.append(n.val);
        if (n.left == null && n.right == null) o.add(b.toString()); else { go(n.left, b, o); go(n.right, b, o); } b.setLength(len); }""", LIST)),
]
MUTANTS["serialize-and-deserialize-binary-tree"] = [
    ("static-field-cheat", "reject", D("""
public class Codec {
    static TreeNode last;
    public String serialize(TreeNode root) { last = root; return "x"; }
    public TreeNode deserialize(String data) { return last; }
}""")),
    ("preorder-without-null-markers", "reject", D("""
public class Codec {
    public String serialize(TreeNode r) { StringBuilder b = new StringBuilder(); s(r, b); return b.toString(); }
    void s(TreeNode n, StringBuilder b) { if (n == null) return; b.append(n.val).append(','); s(n.left, b); s(n.right, b); }
    public TreeNode deserialize(String d) { TreeNode root = null; for (String t : d.split(",")) if (!t.isEmpty()) root = ins(root, Integer.parseInt(t)); return root; }
    TreeNode ins(TreeNode n, int v) { if (n == null) return new TreeNode(v); if (v < n.val) n.left = ins(n.left, v); else n.right = ins(n.right, v); return n; }
}""")),
    ("level-order-with-nulls", "accept", D("""
public class Codec {
    public String serialize(TreeNode r) { StringBuilder b = new StringBuilder(); Deque<TreeNode> q = new LinkedList<>(); q.add(r);
        while (!q.isEmpty()) { TreeNode n = q.poll(); if (n == null) { b.append("n "); continue; } b.append(n.val).append(' '); q.add(n.left); q.add(n.right); } return b.toString(); }
    public TreeNode deserialize(String d) { String[] t = d.trim().split(" "); if (t[0].equals("n")) return null; TreeNode r = new TreeNode(Integer.parseInt(t[0])); Deque<TreeNode> q = new ArrayDeque<>(); q.add(r); int i = 1;
        while (!q.isEmpty()) { TreeNode n = q.poll(); if (!t[i].equals("n")) { n.left = new TreeNode(Integer.parseInt(t[i])); q.add(n.left); } i++; if (!t[i].equals("n")) { n.right = new TreeNode(Integer.parseInt(t[i])); q.add(n.right); } i++; } return r; }
}""")),
]
MUTANTS["path-sum-iii"] = [
    ("int-prefix-overflows", "reject", S("""
    public int pathSum(TreeNode r, int t) { Map<Integer, Integer> m = new HashMap<>(); m.put(0, 1); return go(r, 0, t, m); }
    int go(TreeNode n, int s, int t, Map<Integer, Integer> m) { if (n == null) return 0; s += n.val; int c = m.getOrDefault(s - t, 0); m.merge(s, 1, Integer::sum); c += go(n.left, s, t, m) + go(n.right, s, t, m); m.merge(s, -1, Integer::sum); return c; }""", LIST)),
    ("no-backtrack-of-map", "reject", S("""
    public int pathSum(TreeNode r, int t) { Map<Long, Integer> m = new HashMap<>(); m.put(0L, 1); return go(r, 0, t, m); }
    int go(TreeNode n, long s, int t, Map<Long, Integer> m) { if (n == null) return 0; s += n.val; int c = m.getOrDefault(s - t, 0); m.merge(s, 1, Integer::sum); return c + go(n.left, s, t, m) + go(n.right, s, t, m); }""", LIST)),
    ("double-recursion-long", "accept", S("""
    public int pathSum(TreeNode r, int t) { if (r == null) return 0; return from(r, t) + pathSum(r.left, t) + pathSum(r.right, t); }
    int from(TreeNode n, long t) { if (n == null) return 0; return (n.val == t ? 1 : 0) + from(n.left, t - n.val) + from(n.right, t - n.val); }""")),
]
MUTANTS["subtree-of-another-tree"] = [
    ("string-contains-no-delimiter", "reject", S("""
    public boolean isSubtree(TreeNode r, TreeNode s) { return ser(r).contains(ser(s)); }
    String ser(TreeNode n) { return n == null ? "#" : n.val + ser(n.left) + ser(n.right); }""")),
    ("serialise-with-delimiters", "accept", S("""
    public boolean isSubtree(TreeNode r, TreeNode s) { return ser(r).contains(ser(s)); }
    String ser(TreeNode n) { return n == null ? ",#" : "," + n.val + ser(n.left) + ser(n.right); }""")),
]
MUTANTS["merge-two-binary-trees"] = [
    ("drops-nodes-only-in-second", "reject", S("""
    public TreeNode mergeTrees(TreeNode a, TreeNode b) { if (a == null) return null; if (b == null) return a; a.val += b.val; a.left = mergeTrees(a.left, b.left); a.right = mergeTrees(a.right, b.right); return a; }""")),
    ("in-place-into-first", "accept", S("""
    public TreeNode mergeTrees(TreeNode a, TreeNode b) { if (a == null) return b; if (b == null) return a; a.val += b.val; a.left = mergeTrees(a.left, b.left); a.right = mergeTrees(a.right, b.right); return a; }""")),
]
MUTANTS["average-of-levels-in-binary-tree"] = [
    ("int-sum-overflows", "reject", S("""
    public List<Double> averageOfLevels(TreeNode r) { List<Double> o = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); q.add(r);
        while (!q.isEmpty()) { int n = q.size(), s = 0; for (int i = 0; i < n; i++) { TreeNode x = q.poll(); s += x.val; if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } o.add((double) s / n); } return o; }""", LIST)),
    ("double-sum", "accept", S("""
    public List<Double> averageOfLevels(TreeNode r) { List<Double> o = new ArrayList<>(); Deque<TreeNode> q = new ArrayDeque<>(); q.add(r);
        while (!q.isEmpty()) { int n = q.size(); double s = 0; for (int i = 0; i < n; i++) { TreeNode x = q.poll(); s += x.val; if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } o.add(s / n); } return o; }""", LIST)),
]
MUTANTS["maximum-binary-tree"] = [
    ("max-root-sorted-children", "reject", S("""
    public TreeNode constructMaximumBinaryTree(int[] a) { int[] b = a.clone(); Arrays.sort(b); TreeNode r = new TreeNode(b[b.length - 1]), c = r; for (int i = b.length - 2; i >= 0; i--) { c.right = new TreeNode(b[i]); c = c.right; } return r; }""", LIST)),
    ("recursive", "accept", S("""
    public TreeNode constructMaximumBinaryTree(int[] a) { return b(a, 0, a.length - 1); }
    TreeNode b(int[] a, int lo, int hi) { if (lo > hi) return null; int m = lo; for (int i = lo; i <= hi; i++) if (a[i] > a[m]) m = i; TreeNode n = new TreeNode(a[m]); n.left = b(a, lo, m - 1); n.right = b(a, m + 1, hi); return n; }""")),
]
MUTANTS["maximum-width-of-binary-tree"] = [
    ("counts-nodes-per-level", "reject", S("""
    public int widthOfBinaryTree(TreeNode r) { int b = 0; Deque<TreeNode> q = new ArrayDeque<>(); q.add(r); while (!q.isEmpty()) { int n = q.size(); b = Math.max(b, n); for (int i = 0; i < n; i++) { TreeNode x = q.poll(); if (x.left != null) q.add(x.left); if (x.right != null) q.add(x.right); } } return b; }""", LIST)),
    ("dfs-first-position-per-depth", "accept", S("""
    List<Long> first = new ArrayList<>(); long best = 0;
    public int widthOfBinaryTree(TreeNode r) { first.clear(); best = 0; go(r, 0, 0); return (int) best; }
    void go(TreeNode n, int d, long pos) { if (n == null) return; if (first.size() == d) first.add(pos); pos -= first.get(d); first.set(d, first.get(d)); best = Math.max(best, pos + 1); go(n.left, d + 1, 2 * pos); go(n.right, d + 1, 2 * pos + 1); }""", LIST)),
]
MUTANTS["all-nodes-distance-k-in-binary-tree"] = [
    ("only-downwards", "reject", S("""
    public List<Integer> distanceK(TreeNode r, TreeNode t, int k) { List<Integer> o = new ArrayList<>(); go(t, k, o); return o; }
    void go(TreeNode n, int k, List<Integer> o) { if (n == null) return; if (k == 0) { o.add(n.val); return; } go(n.left, k - 1, o); go(n.right, k - 1, o); }""", LIST)),
    ("graph-bfs", "accept", S("""
    public List<Integer> distanceK(TreeNode r, TreeNode t, int k) {
        Map<TreeNode, List<TreeNode>> g = new IdentityHashMap<>(); Deque<TreeNode> q = new ArrayDeque<>(); q.add(r); g.put(r, new ArrayList<>());
        while (!q.isEmpty()) { TreeNode n = q.poll(); for (TreeNode c : new TreeNode[]{n.left, n.right}) if (c != null) { g.computeIfAbsent(c, x -> new ArrayList<>()).add(n); g.get(n).add(c); q.add(c); } }
        Map<TreeNode, Integer> dist = new IdentityHashMap<>(); dist.put(t, 0); q.add(t); List<Integer> o = new ArrayList<>();
        while (!q.isEmpty()) { TreeNode n = q.poll(); if (dist.get(n) == k) o.add(n.val); for (TreeNode m : g.get(n)) if (!dist.containsKey(m)) { dist.put(m, dist.get(n) + 1); q.add(m); } }
        return o; }""", LIST)),
]

# --------------------------------------------- batch 5: binary search / intervals / matrix

MUTANTS["binary-search"] = [
    ("lo-lt-hi-misses-last", "reject", S("""
    public int search(int[] a, int t) { int lo = 0, hi = a.length - 1; while (lo < hi) { int m = (lo + hi) / 2; if (a[m] == t) return m; if (a[m] < t) lo = m + 1; else hi = m - 1; } return -1; }""")),
    ("half-open", "accept", S("""
    public int search(int[] a, int t) { int lo = 0, hi = a.length; while (lo < hi) { int m = (lo + hi) >>> 1; if (a[m] < t) lo = m + 1; else hi = m; } return lo < a.length && a[lo] == t ? lo : -1; }""")),
]
MUTANTS["find-smallest-letter-greater-than-target"] = [
    ("greater-or-equal", "reject", S("""
    public char nextGreatestLetter(char[] l, char t) { for (char c : l) if (c >= t) return c; return l[0]; }""")),
    ("linear-scan", "accept", S("""
    public char nextGreatestLetter(char[] l, char t) { for (char c : l) if (c > t) return c; return l[0]; }""")),
]
MUTANTS["peak-index-in-a-mountain-array"] = [
    ("compares-with-previous-off-by-one", "reject", S("""
    public int peakIndexInMountainArray(int[] a) { int lo = 1, hi = a.length - 1; while (lo < hi) { int m = (lo + hi) / 2; if (a[m] > a[m - 1]) lo = m + 1; else hi = m; } return lo; }""")),
    ("linear", "accept", S("""
    public int peakIndexInMountainArray(int[] a) { int i = 0; while (a[i + 1] > a[i]) i++; return i; }""")),
]
MUTANTS["search-in-rotated-sorted-array"] = [
    ("strict-lt-sorted-half-test", "reject", S("""
    public int search(int[] a, int t) { int lo = 0, hi = a.length - 1; while (lo <= hi) { int m = (lo + hi) / 2; if (a[m] == t) return m;
        if (a[lo] < a[m]) { if (a[lo] <= t && t < a[m]) hi = m - 1; else lo = m + 1; } else { if (a[m] < t && t <= a[hi]) lo = m + 1; else hi = m - 1; } } return -1; }""")),
    ("find-pivot-then-search", "accept", S("""
    public int search(int[] a, int t) { int lo = 0, hi = a.length - 1; while (lo < hi) { int m = (lo + hi) / 2; if (a[m] > a[hi]) lo = m + 1; else hi = m; } int p = lo, n = a.length; lo = 0; hi = n - 1;
        while (lo <= hi) { int m = (lo + hi) / 2, r = (m + p) % n; if (a[r] == t) return r; if (a[r] < t) lo = m + 1; else hi = m - 1; } return -1; }""")),
]
MUTANTS["search-in-rotated-sorted-array-ii"] = [
    ("lc33-without-duplicate-case", "reject", S("""
    public boolean search(int[] a, int t) { int lo = 0, hi = a.length - 1; while (lo <= hi) { int m = (lo + hi) / 2; if (a[m] == t) return true;
        if (a[lo] <= a[m]) { if (a[lo] <= t && t < a[m]) hi = m - 1; else lo = m + 1; } else { if (a[m] < t && t <= a[hi]) lo = m + 1; else hi = m - 1; } } return false; }""")),
    ("linear", "accept", S("""
    public boolean search(int[] a, int t) { for (int x : a) if (x == t) return true; return false; }""")),
]
MUTANTS["find-minimum-in-rotated-sorted-array"] = [
    ("compares-with-left-end", "reject", S("""
    public int findMin(int[] a) { int lo = 0, hi = a.length - 1; while (lo < hi) { int m = (lo + hi) / 2; if (a[m] >= a[lo]) lo = m + 1; else hi = m; } return a[lo]; }""")),
    ("linear", "accept", S("""
    public int findMin(int[] a) { int m = a[0]; for (int x : a) m = Math.min(m, x); return m; }""")),
]
MUTANTS["find-peak-element"] = [
    ("returns-global-max-index-off-by-one", "reject", S("""
    public int findPeakElement(int[] a) { int b = 0; for (int i = 1; i < a.length; i++) if (a[i] > a[b]) b = i; return b == 0 ? 0 : b - 1; }""")),
    ("linear-first-descent", "accept", S("""
    public int findPeakElement(int[] a) { for (int i = 0; i + 1 < a.length; i++) if (a[i] > a[i + 1]) return i; return a.length - 1; }""")),
]
MUTANTS["search-a-2d-matrix"] = [
    ("divides-by-rows", "reject", S("""
    public boolean searchMatrix(int[][] a, int t) { int m = a.length, n = a[0].length, lo = 0, hi = m * n - 1; while (lo <= hi) { int x = (lo + hi) / 2, v = a[x / m % m][x % n]; if (v == t) return true; if (v < t) lo = x + 1; else hi = x - 1; } return false; }""")),
    ("row-then-column", "accept", S("""
    public boolean searchMatrix(int[][] a, int t) { for (int[] r : a) if (r[0] <= t && t <= r[r.length - 1]) return Arrays.binarySearch(r, t) >= 0; return false; }""", LIST)),
]
MUTANTS["search-a-2d-matrix-ii"] = [
    ("top-left-greedy", "reject", S("""
    public boolean searchMatrix(int[][] a, int t) { int r = 0, c = 0; while (r < a.length && c < a[0].length) { if (a[r][c] == t) return true; if (c + 1 < a[0].length && a[r][c + 1] <= t) c++; else r++; } return false; }""")),
    ("binary-search-each-row", "accept", S("""
    public boolean searchMatrix(int[][] a, int t) { for (int[] r : a) { int lo = 0, hi = r.length - 1; while (lo <= hi) { int m = (lo + hi) / 2; if (r[m] == t) return true; if (r[m] < t) lo = m + 1; else hi = m - 1; } } return false; }""")),
]
MUTANTS["kth-smallest-element-in-a-sorted-matrix"] = [
    ("reads-row-major-index", "reject", S("""
    public int kthSmallest(int[][] a, int k) { int n = a.length; return a[(k - 1) / n][(k - 1) % n]; }""")),
    ("heap", "accept", S("""
    public int kthSmallest(int[][] a, int k) { int n = a.length; PriorityQueue<int[]> q = new PriorityQueue<>((x, y) -> a[x[0]][x[1]] - a[y[0]][y[1]]);
        for (int i = 0; i < n; i++) q.add(new int[]{i, 0}); int[] c = null; for (int i = 0; i < k; i++) { c = q.poll(); if (c[1] + 1 < n) q.add(new int[]{c[0], c[1] + 1}); } return a[c[0]][c[1]]; }""", LIST)),
]
MUTANTS["median-of-two-sorted-arrays"] = [
    ("average-of-medians", "reject", S("""
    public double findMedianSortedArrays(int[] a, int[] b) { if (a.length == 0) return m(b); if (b.length == 0) return m(a); return (m(a) + m(b)) / 2; }
    double m(int[] x) { int n = x.length; return n % 2 == 1 ? x[n / 2] : (x[n / 2 - 1] + x[n / 2]) / 2.0; }""")),
    ("merge", "accept", S("""
    public double findMedianSortedArrays(int[] a, int[] b) { int[] c = new int[a.length + b.length]; int i = 0, j = 0, k = 0;
        while (i < a.length || j < b.length) c[k++] = j >= b.length || (i < a.length && a[i] <= b[j]) ? a[i++] : b[j++];
        int n = c.length; return n % 2 == 1 ? c[n / 2] : (c[n / 2 - 1] + (double) c[n / 2]) / 2; }""")),
]
MUTANTS["merge-intervals"] = [
    ("takes-next-end-not-max", "reject", S("""
    public int[][] merge(int[][] a) { Arrays.sort(a, (x, y) -> x[0] - y[0]); List<int[]> o = new ArrayList<>(); for (int[] v : a) { if (!o.isEmpty() && v[0] <= o.get(o.size() - 1)[1]) o.get(o.size() - 1)[1] = v[1]; else o.add(v.clone()); } return o.toArray(new int[0][]); }""", LIST)),
    ("touching-not-merged", "reject", S("""
    public int[][] merge(int[][] a) { Arrays.sort(a, (x, y) -> x[0] - y[0]); List<int[]> o = new ArrayList<>(); for (int[] v : a) { if (!o.isEmpty() && v[0] < o.get(o.size() - 1)[1]) o.get(o.size() - 1)[1] = Math.max(o.get(o.size() - 1)[1], v[1]); else o.add(v.clone()); } return o.toArray(new int[0][]); }""", LIST)),
    ("coverage-array", "accept", S("""
    public int[][] merge(int[][] a) { int max = 0; for (int[] v : a) max = Math.max(max, v[1]); int[] start = new int[max + 2]; int[] reach = new int[max + 2]; Arrays.fill(reach, -1);
        for (int[] v : a) reach[v[0]] = Math.max(reach[v[0]], v[1]); List<int[]> o = new ArrayList<>(); int s = -1, e = -1;
        for (int x = 0; x <= max; x++) { if (reach[x] < 0) continue; if (s >= 0 && x <= e) e = Math.max(e, reach[x]); else { if (s >= 0) o.add(new int[]{s, e}); s = x; e = reach[x]; } }
        o.add(new int[]{s, e}); return o.toArray(new int[0][]); }""", LIST)),
]
MUTANTS["insert-interval"] = [
    ("appends-then-no-sort", "reject", S("""
    public int[][] insert(int[][] a, int[] nw) { List<int[]> o = new ArrayList<>(); int s = nw[0], e = nw[1]; for (int[] v : a) { if (v[1] < s || v[0] > e) o.add(v); else { s = Math.min(s, v[0]); e = Math.max(e, v[1]); } } o.add(new int[]{s, e}); return o.toArray(new int[0][]); }""", LIST)),
    ("append-sort-merge", "accept", S("""
    public int[][] insert(int[][] a, int[] nw) { List<int[]> l = new ArrayList<>(Arrays.asList(a)); l.add(nw); l.sort((x, y) -> x[0] - y[0]); List<int[]> o = new ArrayList<>();
        for (int[] v : l) { if (!o.isEmpty() && v[0] <= o.get(o.size() - 1)[1]) o.get(o.size() - 1)[1] = Math.max(o.get(o.size() - 1)[1], v[1]); else o.add(v.clone()); } return o.toArray(new int[0][]); }""", LIST)),
]
MUTANTS["non-overlapping-intervals"] = [
    ("sort-by-start-keep-first", "reject", S("""
    public int eraseOverlapIntervals(int[][] a) { Arrays.sort(a, (x, y) -> x[0] - y[0]); int kept = 0; long end = Long.MIN_VALUE; for (int[] v : a) if (v[0] >= end) { kept++; end = v[1]; } return a.length - kept; }""", LIST)),
    ("sort-by-start-keep-earlier-end", "accept", S("""
    public int eraseOverlapIntervals(int[][] a) { Arrays.sort(a, (x, y) -> x[0] - y[0]); int rm = 0, end = a[0][1]; for (int i = 1; i < a.length; i++) { if (a[i][0] < end) { rm++; end = Math.min(end, a[i][1]); } else end = a[i][1]; } return rm; }""", LIST)),
]
MUTANTS["minimum-number-of-arrows-to-burst-balloons"] = [
    ("subtracting-comparator", "reject", S("""
    public int findMinArrowShots(int[][] p) { Arrays.sort(p, (x, y) -> x[1] - y[1]); int n = 0; long at = Long.MIN_VALUE; for (int[] b : p) if (b[0] > at) { n++; at = b[1]; } return n; }""", LIST)),
    ("touching-needs-new-arrow", "reject", S("""
    public int findMinArrowShots(int[][] p) { Arrays.sort(p, (x, y) -> Integer.compare(x[1], y[1])); int n = 0; long at = Long.MIN_VALUE; for (int[] b : p) if (b[0] >= at) { n++; at = b[1]; } return n; }""", LIST)),
    ("sort-by-start-shrink", "accept", S("""
    public int findMinArrowShots(int[][] p) { Arrays.sort(p, (x, y) -> Integer.compare(x[0], y[0])); int n = 1; long end = p[0][1]; for (int i = 1; i < p.length; i++) { if (p[i][0] > end) { n++; end = p[i][1]; } else end = Math.min(end, p[i][1]); } return n; }""", LIST)),
]
MUTANTS["interval-list-intersections"] = [
    ("advances-by-start", "reject", S("""
    public int[][] intervalIntersection(int[][] A, int[][] B) { List<int[]> o = new ArrayList<>(); int i = 0, j = 0; while (i < A.length && j < B.length) { int lo = Math.max(A[i][0], B[j][0]), hi = Math.min(A[i][1], B[j][1]); if (lo <= hi) o.add(new int[]{lo, hi}); if (A[i][0] < B[j][0]) i++; else j++; } return o.toArray(new int[0][]); }""", LIST)),
    ("all-pairs-then-sort", "accept", S("""
    public int[][] intervalIntersection(int[][] A, int[][] B) { List<int[]> o = new ArrayList<>(); for (int[] a : A) for (int[] b : B) { int lo = Math.max(a[0], b[0]), hi = Math.min(a[1], b[1]); if (lo <= hi) o.add(new int[]{lo, hi}); } o.sort((x, y) -> x[0] - y[0]); return o.toArray(new int[0][]); }""", LIST)),
]
MUTANTS["meeting-rooms"] = [
    ("back-to-back-conflicts", "reject", S("""
    public boolean canAttendMeetings(int[][] a) { Arrays.sort(a, (x, y) -> x[0] - y[0]); for (int i = 1; i < a.length; i++) if (a[i][0] <= a[i - 1][1]) return false; return true; }""", LIST)),
    ("all-pairs", "accept", S("""
    public boolean canAttendMeetings(int[][] a) { for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) if (a[i][0] < a[j][1] && a[j][0] < a[i][1]) return false; return true; }""")),
]
MUTANTS["meeting-rooms-ii"] = [
    ("overlap-with-previous-only", "reject", S("""
    public int minMeetingRooms(int[][] a) { Arrays.sort(a, (x, y) -> x[0] - y[0]); int best = 1, cur = 1; for (int i = 1; i < a.length; i++) { cur = a[i][0] < a[i - 1][1] ? cur + 1 : 1; best = Math.max(best, cur); } return best; }""", LIST)),
    ("sweep-line", "accept", S("""
    public int minMeetingRooms(int[][] a) { int n = a.length; int[] s = new int[n], e = new int[n]; for (int i = 0; i < n; i++) { s[i] = a[i][0]; e[i] = a[i][1]; } Arrays.sort(s); Arrays.sort(e);
        int rooms = 0, j = 0; for (int i = 0; i < n; i++) { if (s[i] < e[j]) rooms++; else j++; } return rooms; }""", LIST)),
]
MUTANTS["rotate-image"] = [
    ("counter-clockwise", "reject", S("""
    public void rotate(int[][] m) { int n = m.length; for (int[] r : m) for (int i = 0, j = n - 1; i < j; i++, j--) { int t = r[i]; r[i] = r[j]; r[j] = t; } for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) { int t = m[i][j]; m[i][j] = m[j][i]; m[j][i] = t; } }""")),
    ("four-way-rings", "accept", S("""
    public void rotate(int[][] m) { int n = m.length; for (int i = 0; i < n / 2; i++) for (int j = i; j < n - 1 - i; j++) { int t = m[i][j]; m[i][j] = m[n - 1 - j][i]; m[n - 1 - j][i] = m[n - 1 - i][n - 1 - j]; m[n - 1 - i][n - 1 - j] = m[j][n - 1 - i]; m[j][n - 1 - i] = t; } }""")),
]
MUTANTS["spiral-matrix"] = [
    ("no-single-row-guard", "reject", S("""
    public List<Integer> spiralOrder(int[][] m) { List<Integer> o = new ArrayList<>(); int t = 0, b = m.length - 1, l = 0, r = m[0].length - 1;
        while (t <= b && l <= r) { for (int j = l; j <= r; j++) o.add(m[t][j]); for (int i = t + 1; i <= b; i++) o.add(m[i][r]); for (int j = r - 1; j >= l; j--) o.add(m[b][j]); for (int i = b - 1; i > t; i--) o.add(m[i][l]); t++; b--; l++; r--; }
        return o; }""", LIST)),
    ("direction-vector", "accept", S("""
    public List<Integer> spiralOrder(int[][] m) { int R = m.length, C = m[0].length; boolean[][] s = new boolean[R][C]; int[] dr = {0, 1, 0, -1}, dc = {1, 0, -1, 0}; List<Integer> o = new ArrayList<>(); int r = 0, c = 0, d = 0;
        for (int k = 0; k < R * C; k++) { o.add(m[r][c]); s[r][c] = true; int nr = r + dr[d], nc = c + dc[d]; if (nr < 0 || nc < 0 || nr >= R || nc >= C || s[nr][nc]) { d = (d + 1) % 4; nr = r + dr[d]; nc = c + dc[d]; } r = nr; c = nc; } return o; }""", LIST)),
]
MUTANTS["set-matrix-zeroes"] = [
    ("zeroes-while-scanning", "reject", S("""
    public void setZeroes(int[][] m) { for (int i = 0; i < m.length; i++) for (int j = 0; j < m[0].length; j++) if (m[i][j] == 0) { for (int k = 0; k < m.length; k++) m[k][j] = 0; for (int k = 0; k < m[0].length; k++) m[i][k] = 0; } }""")),
    ("first-row-col-flags", "accept", S("""
    public void setZeroes(int[][] m) { int R = m.length, C = m[0].length; boolean col0 = false;
        for (int i = 0; i < R; i++) { if (m[i][0] == 0) col0 = true; for (int j = 1; j < C; j++) if (m[i][j] == 0) { m[i][0] = 0; m[0][j] = 0; } }
        for (int i = R - 1; i >= 0; i--) { for (int j = C - 1; j >= 1; j--) if (m[i][0] == 0 || m[0][j] == 0) m[i][j] = 0; if (col0) m[i][0] = 0; } }""")),
]
MUTANTS["convert-1d-array-into-2d-array"] = [
    ("returns-null-when-impossible", "reject", S("""
    public int[][] construct2DArray(int[] a, int m, int n) { if (m * n != a.length) return null; int[][] o = new int[m][n]; for (int i = 0; i < a.length; i++) o[i / n][i % n] = a[i]; return o; }""")),
    ("row-copy", "accept", S("""
    public int[][] construct2DArray(int[] a, int m, int n) { if ((long) m * n != a.length) return new int[0][]; int[][] o = new int[m][]; for (int i = 0; i < m; i++) o[i] = Arrays.copyOfRange(a, i * n, i * n + n); return o; }""", LIST)),
]

# ----------------------------------------------------------- batch 6: backtracking

MUTANTS["letter-combinations-of-a-phone-number"] = [
    ("empty-input-gives-empty-string", "reject", S("""
    String[] K = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
    public List<String> letterCombinations(String d) { List<String> o = new ArrayList<>(); o.add(""); for (char c : d.toCharArray()) { List<String> n = new ArrayList<>(); for (String p : o) for (char x : K[c - '0'].toCharArray()) n.add(p + x); o = n; } return o; }""", LIST)),
    ("iterative-bfs", "accept", S("""
    String[] K = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
    public List<String> letterCombinations(String d) { if (d.isEmpty()) return new ArrayList<>(); List<String> o = new ArrayList<>(); o.add(""); for (char c : d.toCharArray()) { List<String> n = new ArrayList<>(); for (String p : o) for (char x : K[c - '0'].toCharArray()) n.add(p + x); o = n; } return o; }""", LIST)),
]
MUTANTS["generate-parentheses"] = [
    ("close-when-close-positive", "reject", S("""
    public List<String> generateParenthesis(int n) { List<String> o = new ArrayList<>(); go(n, n, "", o); return o; }
    void go(int op, int cl, String s, List<String> o) { if (op == 0 && cl == 0) { o.add(s); return; } if (op > 0) go(op - 1, cl, s + "(", o); if (cl > 0) go(op, cl - 1, s + ")", o); }""", LIST)),
    ("filter-all-strings", "accept", S("""
    public List<String> generateParenthesis(int n) { List<String> o = new ArrayList<>(); for (int m = 0; m < 1 << 2 * n; m++) { int bal = 0; StringBuilder b = new StringBuilder(); boolean ok = true;
        for (int i = 0; i < 2 * n && ok; i++) { boolean open = (m >> i & 1) == 1; b.append(open ? '(' : ')'); bal += open ? 1 : -1; if (bal < 0) ok = false; } if (ok && bal == 0) o.add(b.toString()); } return o; }""", LIST)),
]
MUTANTS["sudoku-solver"] = [
    ("fills-without-checking-boxes", "reject", S("""
    public void solveSudoku(char[][] b) { go(b); }
    boolean go(char[][] b) { for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) if (b[i][j] == '.') { for (char d = '1'; d <= '9'; d++) { boolean ok = true; for (int k = 0; k < 9; k++) if (b[i][k] == d || b[k][j] == d) ok = false; if (ok) { b[i][j] = d; if (go(b)) return true; b[i][j] = '.'; } } return false; } return true; }""")),
    ("plain-backtracking", "accept", S("""
    public void solveSudoku(char[][] b) { go(b); }
    boolean go(char[][] b) { for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) if (b[i][j] == '.') { for (char d = '1'; d <= '9'; d++) if (ok(b, i, j, d)) { b[i][j] = d; if (go(b)) return true; b[i][j] = '.'; } return false; } return true; }
    boolean ok(char[][] b, int r, int c, char d) { for (int k = 0; k < 9; k++) if (b[r][k] == d || b[k][c] == d || b[r / 3 * 3 + k / 3][c / 3 * 3 + k % 3] == d) return false; return true; }""")),
]
MUTANTS["combination-sum"] = [
    ("restarts-at-zero-gives-permutations", "reject", S("""
    public List<List<Integer>> combinationSum(int[] c, int t) { List<List<Integer>> o = new ArrayList<>(); go(c, t, new ArrayList<>(), o); return o; }
    void go(int[] c, int r, List<Integer> cur, List<List<Integer>> o) { if (r == 0) { o.add(new ArrayList<>(cur)); return; } for (int x : c) if (x <= r) { cur.add(x); go(c, r - x, cur, o); cur.remove(cur.size() - 1); } }""", LIST)),
    ("dp-over-targets", "accept", S("""
    public List<List<Integer>> combinationSum(int[] c, int t) { List<List<List<Integer>>> d = new ArrayList<>(); for (int i = 0; i <= t; i++) d.add(new ArrayList<>()); d.get(0).add(new ArrayList<>());
        for (int x : c) for (int s = x; s <= t; s++) for (List<Integer> p : d.get(s - x)) { List<Integer> q = new ArrayList<>(p); q.add(x); d.get(s).add(q); } return d.get(t); }""", LIST)),
]
MUTANTS["combination-sum-ii"] = [
    ("skip-dup-with-j-gt-0", "reject", S("""
    public List<List<Integer>> combinationSum2(int[] c, int t) { Arrays.sort(c); List<List<Integer>> o = new ArrayList<>(); go(c, 0, t, new ArrayList<>(), o); return o; }
    void go(int[] a, int i, int r, List<Integer> cur, List<List<Integer>> o) { if (r == 0) { o.add(new ArrayList<>(cur)); return; } for (int j = i; j < a.length && a[j] <= r; j++) { if (j > 0 && a[j] == a[j - 1]) continue; cur.add(a[j]); go(a, j + 1, r - a[j], cur, o); cur.remove(cur.size() - 1); } }""", LIST)),
    ("include-exclude-with-set", "accept", S("""
    public List<List<Integer>> combinationSum2(int[] c, int t) { Arrays.sort(c); Set<List<Integer>> s = new HashSet<>(); go(c, 0, t, new ArrayList<>(), s); return new ArrayList<>(s); }
    void go(int[] a, int i, int r, List<Integer> cur, Set<List<Integer>> s) { if (r == 0) { s.add(new ArrayList<>(cur)); return; } if (i == a.length || r < 0) return; cur.add(a[i]); go(a, i + 1, r - a[i], cur, s); cur.remove(cur.size() - 1); go(a, i + 1, r, cur, s); }""", LIST)),
]
MUTANTS["permutations"] = [
    ("stores-live-list", "reject", S("""
    public List<List<Integer>> permute(int[] a) { List<List<Integer>> o = new ArrayList<>(); go(a, new boolean[a.length], new ArrayList<>(), o); return o; }
    void go(int[] a, boolean[] u, List<Integer> c, List<List<Integer>> o) { if (c.size() == a.length) { o.add(c); return; } for (int i = 0; i < a.length; i++) if (!u[i]) { u[i] = true; c.add(a[i]); go(a, u, c, o); c.remove(c.size() - 1); u[i] = false; } }""", LIST)),
    ("swap-in-place", "accept", S("""
    public List<List<Integer>> permute(int[] a) { List<List<Integer>> o = new ArrayList<>(); go(a, 0, o); return o; }
    void go(int[] a, int k, List<List<Integer>> o) { if (k == a.length) { List<Integer> l = new ArrayList<>(); for (int x : a) l.add(x); o.add(l); return; } for (int i = k; i < a.length; i++) { int t = a[k]; a[k] = a[i]; a[i] = t; go(a, k + 1, o); t = a[k]; a[k] = a[i]; a[i] = t; } }""", LIST)),
]
MUTANTS["permutations-ii"] = [
    ("lc46-produces-duplicates", "reject", S("""
    public List<List<Integer>> permuteUnique(int[] a) { List<List<Integer>> o = new ArrayList<>(); go(a, new boolean[a.length], new ArrayList<>(), o); return o; }
    void go(int[] a, boolean[] u, List<Integer> c, List<List<Integer>> o) { if (c.size() == a.length) { o.add(new ArrayList<>(c)); return; } for (int i = 0; i < a.length; i++) if (!u[i]) { u[i] = true; c.add(a[i]); go(a, u, c, o); c.remove(c.size() - 1); u[i] = false; } }""", LIST)),
    ("set-of-lists", "accept", S("""
    public List<List<Integer>> permuteUnique(int[] a) { Set<List<Integer>> s = new HashSet<>(); go(a, new boolean[a.length], new ArrayList<>(), s); return new ArrayList<>(s); }
    void go(int[] a, boolean[] u, List<Integer> c, Set<List<Integer>> o) { if (c.size() == a.length) { o.add(new ArrayList<>(c)); return; } for (int i = 0; i < a.length; i++) if (!u[i]) { u[i] = true; c.add(a[i]); go(a, u, c, o); c.remove(c.size() - 1); u[i] = false; } }""", LIST)),
]
MUTANTS["n-queens"] = [
    ("checks-one-diagonal", "reject", S("""
    public List<List<String>> solveNQueens(int n) { List<List<String>> o = new ArrayList<>(); go(0, n, new int[n], o); return o; }
    void go(int r, int n, int[] q, List<List<String>> o) { if (r == n) { List<String> b = new ArrayList<>(); for (int c : q) { char[] x = new char[n]; Arrays.fill(x, '.'); x[c] = 'Q'; b.add(new String(x)); } o.add(b); return; }
        for (int c = 0; c < n; c++) { boolean ok = true; for (int i = 0; i < r; i++) if (q[i] == c || q[i] - i == c - r) ok = false; if (ok) { q[r] = c; go(r + 1, n, q, o); } } }""", LIST)),
    ("scan-previous-rows", "accept", S("""
    public List<List<String>> solveNQueens(int n) { List<List<String>> o = new ArrayList<>(); go(0, n, new int[n], o); return o; }
    void go(int r, int n, int[] q, List<List<String>> o) { if (r == n) { List<String> b = new ArrayList<>(); for (int c : q) { char[] x = new char[n]; Arrays.fill(x, '.'); x[c] = 'Q'; b.add(new String(x)); } o.add(b); return; }
        for (int c = 0; c < n; c++) { boolean ok = true; for (int i = 0; i < r; i++) if (q[i] == c || Math.abs(q[i] - c) == r - i) ok = false; if (ok) { q[r] = c; go(r + 1, n, q, o); } } }""", LIST)),
]
MUTANTS["combinations"] = [
    ("allows-repeats", "reject", S("""
    public List<List<Integer>> combine(int n, int k) { List<List<Integer>> o = new ArrayList<>(); go(1, n, k, new ArrayList<>(), o); return o; }
    void go(int s, int n, int k, List<Integer> c, List<List<Integer>> o) { if (c.size() == k) { o.add(new ArrayList<>(c)); return; } for (int v = s; v <= n; v++) { c.add(v); go(v, n, k, c, o); c.remove(c.size() - 1); } }""", LIST)),
    ("bitmask-popcount", "accept", S("""
    public List<List<Integer>> combine(int n, int k) { List<List<Integer>> o = new ArrayList<>(); for (int m = 0; m < 1 << n; m++) if (Integer.bitCount(m) == k) { List<Integer> l = new ArrayList<>(); for (int i = 0; i < n; i++) if ((m >> i & 1) == 1) l.add(i + 1); o.add(l); } return o; }""", LIST)),
]
MUTANTS["subsets"] = [
    ("misses-empty-set", "reject", S("""
    public List<List<Integer>> subsets(int[] a) { List<List<Integer>> o = new ArrayList<>(); for (int m = 1; m < 1 << a.length; m++) { List<Integer> s = new ArrayList<>(); for (int i = 0; i < a.length; i++) if ((m >> i & 1) == 1) s.add(a[i]); o.add(s); } return o; }""", LIST)),
    ("cascading", "accept", S("""
    public List<List<Integer>> subsets(int[] a) { List<List<Integer>> o = new ArrayList<>(); o.add(new ArrayList<>()); for (int x : a) { int n = o.size(); for (int i = 0; i < n; i++) { List<Integer> s = new ArrayList<>(o.get(i)); s.add(x); o.add(s); } } return o; }""", LIST)),
]
MUTANTS["word-search"] = [
    ("never-restores-cell", "reject", S("""
    public boolean exist(char[][] b, String w) { for (int i = 0; i < b.length; i++) for (int j = 0; j < b[0].length; j++) if (d(b, w, i, j, 0)) return true; return false; }
    boolean d(char[][] b, String w, int i, int j, int k) { if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || b[i][j] != w.charAt(k)) return false; if (k == w.length() - 1) return true; b[i][j] = '#';
        return d(b, w, i + 1, j, k + 1) || d(b, w, i - 1, j, k + 1) || d(b, w, i, j + 1, k + 1) || d(b, w, i, j - 1, k + 1); }""")),
    ("allows-reuse", "reject", S("""
    public boolean exist(char[][] b, String w) { for (int i = 0; i < b.length; i++) for (int j = 0; j < b[0].length; j++) if (d(b, w, i, j, 0)) return true; return false; }
    boolean d(char[][] b, String w, int i, int j, int k) { if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || b[i][j] != w.charAt(k)) return false; if (k == w.length() - 1) return true;
        return d(b, w, i + 1, j, k + 1) || d(b, w, i - 1, j, k + 1) || d(b, w, i, j + 1, k + 1) || d(b, w, i, j - 1, k + 1); }""")),
    ("visited-array-restored", "accept", S("""
    boolean[][] v;
    public boolean exist(char[][] b, String w) { v = new boolean[b.length][b[0].length]; for (int i = 0; i < b.length; i++) for (int j = 0; j < b[0].length; j++) if (d(b, w, i, j, 0)) return true; return false; }
    boolean d(char[][] b, String w, int i, int j, int k) { if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || v[i][j] || b[i][j] != w.charAt(k)) return false; if (k == w.length() - 1) return true; v[i][j] = true;
        boolean r = d(b, w, i + 1, j, k + 1) || d(b, w, i - 1, j, k + 1) || d(b, w, i, j + 1, k + 1) || d(b, w, i, j - 1, k + 1); v[i][j] = false; return r; }""")),
]
MUTANTS["subsets-ii"] = [
    ("no-dedupe", "reject", S("""
    public List<List<Integer>> subsetsWithDup(int[] a) { List<List<Integer>> o = new ArrayList<>(); o.add(new ArrayList<>()); for (int x : a) { int n = o.size(); for (int i = 0; i < n; i++) { List<Integer> s = new ArrayList<>(o.get(i)); s.add(x); o.add(s); } } return o; }""", LIST)),
    ("sorted-set", "accept", S("""
    public List<List<Integer>> subsetsWithDup(int[] a) { Arrays.sort(a); Set<List<Integer>> s = new HashSet<>(); for (int m = 0; m < 1 << a.length; m++) { List<Integer> l = new ArrayList<>(); for (int i = 0; i < a.length; i++) if ((m >> i & 1) == 1) l.add(a[i]); s.add(l); } return new ArrayList<>(s); }""", LIST)),
]
MUTANTS["palindrome-partitioning"] = [
    ("only-single-chars", "reject", S("""
    public List<List<String>> partition(String s) { List<String> p = new ArrayList<>(); for (char c : s.toCharArray()) p.add("" + c); List<List<String>> o = new ArrayList<>(); o.add(p); return o; }""", LIST)),
    ("dp-table-backtrack", "accept", S("""
    boolean[][] pal;
    public List<List<String>> partition(String s) { int n = s.length(); pal = new boolean[n][n]; for (int i = n - 1; i >= 0; i--) for (int j = i; j < n; j++) pal[i][j] = s.charAt(i) == s.charAt(j) && (j - i < 2 || pal[i + 1][j - 1]); List<List<String>> o = new ArrayList<>(); go(s, 0, new ArrayList<>(), o); return o; }
    void go(String s, int i, List<String> c, List<List<String>> o) { if (i == s.length()) { o.add(new ArrayList<>(c)); return; } for (int j = i; j < s.length(); j++) if (pal[i][j]) { c.add(s.substring(i, j + 1)); go(s, j + 1, c, o); c.remove(c.size() - 1); } }""", LIST)),
]
MUTANTS["combination-sum-iii"] = [
    ("any-size", "reject", S("""
    public List<List<Integer>> combinationSum3(int k, int n) { List<List<Integer>> o = new ArrayList<>(); go(1, n, new ArrayList<>(), o); return o; }
    void go(int s, int r, List<Integer> c, List<List<Integer>> o) { if (r == 0) { o.add(new ArrayList<>(c)); return; } for (int v = s; v <= 9 && v <= r; v++) { c.add(v); go(v + 1, r - v, c, o); c.remove(c.size() - 1); } }""", LIST)),
    ("bitmask", "accept", S("""
    public List<List<Integer>> combinationSum3(int k, int n) { List<List<Integer>> o = new ArrayList<>(); for (int m = 0; m < 512; m++) { if (Integer.bitCount(m) != k) continue; int s = 0; List<Integer> l = new ArrayList<>(); for (int i = 0; i < 9; i++) if ((m >> i & 1) == 1) { s += i + 1; l.add(i + 1); } if (s == n) o.add(l); } return o; }""", LIST)),
]
MUTANTS["factor-combinations"] = [
    ("unordered-duplicates", "reject", S("""
    public List<List<Integer>> getFactors(int n) { List<List<Integer>> o = new ArrayList<>(); go(n, new ArrayList<>(), o); return o; }
    void go(int n, List<Integer> c, List<List<Integer>> o) { for (int f = 2; f < n; f++) if (n % f == 0) { c.add(f); List<Integer> x = new ArrayList<>(c); x.add(n / f); o.add(x); go(n / f, c, o); c.remove(c.size() - 1); } }""", LIST)),
    ("brute-to-n-ordered", "accept", S("""
    public List<List<Integer>> getFactors(int n) { List<List<Integer>> o = new ArrayList<>(); go(n, 2, new ArrayList<>(), o); return o; }
    void go(int n, int lo, List<Integer> c, List<List<Integer>> o) { for (int f = lo; f < n; f++) if (n % f == 0 && n / f >= f) { c.add(f); List<Integer> x = new ArrayList<>(c); x.add(n / f); o.add(x); go(n / f, f, c, o); c.remove(c.size() - 1); } }""", LIST)),
]
MUTANTS["generalized-abbreviation"] = [
    ("unmerged-counts", "reject", S("""
    public List<String> generateAbbreviations(String w) { List<String> o = new ArrayList<>(); for (int m = 0; m < 1 << w.length(); m++) { StringBuilder b = new StringBuilder(); for (int i = 0; i < w.length(); i++) b.append((m >> i & 1) == 1 ? "1" : "" + w.charAt(i)); o.add(b.toString()); } return o; }""", LIST)),
    ("bitmask-merged", "accept", S("""
    public List<String> generateAbbreviations(String w) { List<String> o = new ArrayList<>(); for (int m = 0; m < 1 << w.length(); m++) { StringBuilder b = new StringBuilder(); int k = 0;
        for (int i = 0; i < w.length(); i++) { if ((m >> i & 1) == 1) k++; else { if (k > 0) b.append(k); k = 0; b.append(w.charAt(i)); } } if (k > 0) b.append(k); o.add(b.toString()); } return o; }""", LIST)),
]
MUTANTS["letter-case-permutation"] = [
    ("branches-on-digits", "reject", S("""
    public List<String> letterCasePermutation(String s) { List<String> o = new ArrayList<>(); go(s.toCharArray(), 0, o); return o; }
    void go(char[] c, int i, List<String> o) { if (i == c.length) { o.add(new String(c)); return; } c[i] = Character.toLowerCase(c[i]); go(c, i + 1, o); c[i] = Character.toUpperCase(c[i]); go(c, i + 1, o); }""", LIST)),
    ("iterative", "accept", S("""
    public List<String> letterCasePermutation(String s) { List<String> o = new ArrayList<>(); o.add(""); for (char c : s.toCharArray()) { List<String> n = new ArrayList<>(); for (String p : o) { if (Character.isLetter(c)) { n.add(p + Character.toLowerCase(c)); n.add(p + Character.toUpperCase(c)); } else n.add(p + c); } o = n; } return o; }""", LIST)),
]
MUTANTS["word-squares"] = [
    ("no-word-reuse", "reject", S("""
    public List<List<String>> wordSquares(String[] w) { List<List<String>> o = new ArrayList<>(); go(w, new ArrayList<>(), new boolean[w.length], o); return o; }
    void go(String[] w, List<String> sq, boolean[] u, List<List<String>> o) { int L = w[0].length(), r = sq.size(); if (r == L) { o.add(new ArrayList<>(sq)); return; }
        for (int i = 0; i < w.length; i++) { if (u[i]) continue; boolean ok = true; for (int k = 0; k < r; k++) if (sq.get(k).charAt(r) != w[i].charAt(k)) ok = false; if (ok) { u[i] = true; sq.add(w[i]); go(w, sq, u, o); sq.remove(sq.size() - 1); u[i] = false; } } }""", LIST)),
    ("brute-check-each-row", "accept", S("""
    public List<List<String>> wordSquares(String[] w) { List<List<String>> o = new ArrayList<>(); go(w, new ArrayList<>(), o); return o; }
    void go(String[] w, List<String> sq, List<List<String>> o) { int L = w[0].length(), r = sq.size(); if (r == L) { o.add(new ArrayList<>(sq)); return; }
        for (String x : w) { boolean ok = true; for (int k = 0; k < r; k++) if (sq.get(k).charAt(r) != x.charAt(k)) ok = false; if (ok) { sq.add(x); go(w, sq, o); sq.remove(sq.size() - 1); } } }""", LIST)),
]

# ----------------------------------------------------------------- batch 7: DP

MUTANTS["maximum-subarray"] = [
    ("init-zero", "reject", S("""
    public int maxSubArray(int[] a) { int b = 0, c = 0; for (int x : a) { c = Math.max(0, c + x); b = Math.max(b, c); } return b; }""")),
    ("prefix-min", "accept", S("""
    public int maxSubArray(int[] a) { long pre = 0, mn = 0, b = Long.MIN_VALUE; for (int x : a) { pre += x; b = Math.max(b, pre - mn); mn = Math.min(mn, pre); } return (int) b; }""")),
]
MUTANTS["jump-game"] = [
    ("only-checks-zeros-at-start", "reject", S("""
    public boolean canJump(int[] a) { return a.length == 1 || a[0] > 0; }""")),
    ("backwards-goal", "accept", S("""
    public boolean canJump(int[] a) { int goal = a.length - 1; for (int i = a.length - 1; i >= 0; i--) if (i + a[i] >= goal) goal = i; return goal == 0; }""")),
]
MUTANTS["unique-paths"] = [
    ("counts-cells", "reject", S("""
    public int uniquePaths(int m, int n) { return m + n - 1; }""")),
    ("combinatorics", "accept", S("""
    public int uniquePaths(int m, int n) { long r = 1; for (int i = 1; i <= m - 1; i++) r = r * (n - 1 + i) / i; return (int) r; }""")),
]
MUTANTS["climbing-stairs"] = [
    ("naive-recursion-tle", "reject", S("""
    public int climbStairs(int n) { return n <= 2 ? n : climbStairs(n - 1) + climbStairs(n - 2); }""")),
    ("memo", "accept", S("""
    int[] m = new int[50];
    public int climbStairs(int n) { if (n <= 2) return n; if (m[n] != 0) return m[n]; return m[n] = climbStairs(n - 1) + climbStairs(n - 2); }""")),
]
MUTANTS["decode-ways"] = [
    ("ignores-zero", "reject", S("""
    public int numDecodings(String s) { int a = 1, b = 1; for (int i = 2; i <= s.length(); i++) { int t = Integer.parseInt(s.substring(i - 2, i)); int c = b + (t <= 26 && t >= 10 ? a : 0); a = b; b = c; } return b; }""")),
    ("memo-recursion", "accept", S("""
    Integer[] m;
    public int numDecodings(String s) { m = new Integer[s.length() + 1]; return g(s, 0); }
    int g(String s, int i) { if (i == s.length()) return 1; if (s.charAt(i) == '0') return 0; if (m[i] != null) return m[i]; int r = g(s, i + 1); if (i + 1 < s.length() && Integer.parseInt(s.substring(i, i + 2)) <= 26) r += g(s, i + 2); return m[i] = r; }""")),
]
MUTANTS["best-time-to-buy-and-sell-stock"] = [
    ("max-minus-min", "reject", S("""
    public int maxProfit(int[] p) { int mx = 0, mn = Integer.MAX_VALUE; for (int x : p) { mx = Math.max(mx, x); mn = Math.min(mn, x); } return Math.max(0, mx - mn); }""")),
    ("brute-force", "accept", S("""
    public int maxProfit(int[] p) { int b = 0; for (int i = 0; i < p.length; i++) for (int j = i + 1; j < p.length; j++) b = Math.max(b, p[j] - p[i]); return b; }""")),
]
MUTANTS["word-break"] = [
    ("greedy-longest-first", "reject", S("""
    public boolean wordBreak(String s, List<String> d) { int i = 0; while (i < s.length()) { int best = -1; for (String w : d) if (s.startsWith(w, i) && w.length() > best) best = w.length(); if (best < 0) return false; i += best; } return true; }""", LIST)),
    ("unmemoised-recursion-tle", "reject", S("""
    public boolean wordBreak(String s, List<String> d) { return g(s, 0, d); }
    boolean g(String s, int i, List<String> d) { if (i == s.length()) return true; for (String w : d) if (s.startsWith(w, i) && g(s, i + w.length(), d)) return true; return false; }""", LIST)),
    ("memo-recursion", "accept", S("""
    Boolean[] m;
    public boolean wordBreak(String s, List<String> d) { m = new Boolean[s.length() + 1]; return g(s, 0, d); }
    boolean g(String s, int i, List<String> d) { if (i == s.length()) return true; if (m[i] != null) return m[i]; for (String w : d) if (s.startsWith(w, i) && g(s, i + w.length(), d)) return m[i] = true; return m[i] = false; }""", LIST)),
]
MUTANTS["maximum-product-subarray"] = [
    ("kadane-on-product", "reject", S("""
    public int maxProduct(int[] a) { int b = a[0], c = a[0]; for (int i = 1; i < a.length; i++) { c = Math.max(a[i], c * a[i]); b = Math.max(b, c); } return b; }""")),
    ("brute-force", "accept", S("""
    public int maxProduct(int[] a) { int b = a[0]; for (int i = 0; i < a.length; i++) { int p = 1; for (int j = i; j < a.length; j++) { p *= a[j]; b = Math.max(b, p); } } return b; }""")),
]
MUTANTS["house-robber"] = [
    ("odds-vs-evens", "reject", S("""
    public int rob(int[] a) { int o = 0, e = 0; for (int i = 0; i < a.length; i++) if (i % 2 == 0) e += a[i]; else o += a[i]; return Math.max(o, e); }""")),
    ("dp-array", "accept", S("""
    public int rob(int[] a) { int n = a.length; int[] d = new int[n + 1]; d[1] = a[0]; for (int i = 2; i <= n; i++) d[i] = Math.max(d[i - 1], d[i - 2] + a[i - 1]); return d[n]; }""")),
]
MUTANTS["house-robber-ii"] = [
    ("ignores-circle", "reject", S("""
    public int rob(int[] a) { int p = 0, c = 0; for (int x : a) { int t = Math.max(c, p + x); p = c; c = t; } return c; }""")),
    ("two-dp-arrays", "accept", S("""
    public int rob(int[] a) { int n = a.length; if (n == 1) return a[0]; int[] x = new int[n], y = new int[n]; x[0] = a[0]; x[1] = Math.max(a[0], a[1]); for (int i = 2; i < n - 1; i++) x[i] = Math.max(x[i - 1], x[i - 2] + a[i]);
        y[1] = a[1]; for (int i = 2; i < n; i++) y[i] = Math.max(y[i - 1], y[i - 2] + a[i]); return Math.max(n > 2 ? x[n - 2] : x[0], y[n - 1]); }""")),
]
MUTANTS["longest-increasing-subsequence"] = [
    ("upper-bound-allows-equal", "reject", S("""
    public int lengthOfLIS(int[] a) { int[] t = new int[a.length]; int len = 0; for (int x : a) { int lo = 0, hi = len; while (lo < hi) { int m = (lo + hi) / 2; if (t[m] <= x) lo = m + 1; else hi = m; } t[lo] = x; if (lo == len) len++; } return len; }""")),
    ("quadratic", "accept", S("""
    public int lengthOfLIS(int[] a) { int[] d = new int[a.length]; int b = 0; for (int i = 0; i < a.length; i++) { d[i] = 1; for (int j = 0; j < i; j++) if (a[j] < a[i]) d[i] = Math.max(d[i], d[j] + 1); b = Math.max(b, d[i]); } return b; }""")),
]
MUTANTS["best-time-to-buy-and-sell-stock-with-cooldown"] = [
    ("no-cooldown", "reject", S("""
    public int maxProfit(int[] p) { int b = 0; for (int i = 1; i < p.length; i++) b += Math.max(0, p[i] - p[i - 1]); return b; }""")),
    ("memo-states", "accept", S("""
    Integer[][] m;
    public int maxProfit(int[] p) { m = new Integer[p.length + 2][2]; return g(p, 0, 0); }
    int g(int[] p, int i, int h) { if (i >= p.length) return 0; if (m[i][h] != null) return m[i][h]; int r = g(p, i + 1, h); r = h == 1 ? Math.max(r, p[i] + g(p, i + 2, 0)) : Math.max(r, -p[i] + g(p, i + 1, 1)); return m[i][h] = r; }""")),
]
MUTANTS["coin-change"] = [
    ("greedy-largest-first", "reject", S("""
    public int coinChange(int[] c, int a) { Arrays.sort(c); int n = 0; for (int i = c.length - 1; i >= 0; i--) { n += a / c[i]; a %= c[i]; } return a == 0 ? n : -1; }""", LIST)),
    ("sentinel-overflow", "reject", S("""
    public int coinChange(int[] c, int a) { int[] d = new int[a + 1]; Arrays.fill(d, Integer.MAX_VALUE); d[0] = 0; for (int x = 1; x <= a; x++) for (int k : c) if (k <= x) d[x] = Math.min(d[x], d[x - k] + 1); return d[a] == Integer.MAX_VALUE ? -1 : d[a]; }""", LIST)),
    ("bfs", "accept", S("""
    public int coinChange(int[] c, int a) { if (a == 0) return 0; boolean[] seen = new boolean[a + 1]; Deque<Integer> q = new ArrayDeque<>(); q.add(0); seen[0] = true; int steps = 0;
        while (!q.isEmpty()) { steps++; for (int n = q.size(); n > 0; n--) { int v = q.poll(); for (int k : c) { int w = v + k; if (w == a) return steps; if (w < a && !seen[w]) { seen[w] = true; q.add(w); } } } } return -1; }""", LIST)),
]
MUTANTS["counting-bits"] = [
    ("off-by-one-length", "reject", S("""
    public int[] countBits(int n) { int[] b = new int[n]; for (int i = 1; i < n; i++) b[i] = b[i >> 1] + (i & 1); return b; }""")),
    ("lowest-bit-clear", "accept", S("""
    public int[] countBits(int n) { int[] b = new int[n + 1]; for (int i = 1; i <= n; i++) b[i] = b[i & (i - 1)] + 1; return b; }""")),
]
MUTANTS["combination-sum-iv"] = [
    ("counts-combinations-not-orders", "reject", S("""
    public int combinationSum4(int[] a, int t) { long[] d = new long[t + 1]; d[0] = 1; for (int x : a) for (int s = x; s <= t; s++) d[s] += d[s - x]; return (int) d[t]; }""")),
    ("memo-recursion", "accept", S("""
    Integer[] m;
    public int combinationSum4(int[] a, int t) { m = new Integer[t + 1]; return g(a, t); }
    int g(int[] a, int t) { if (t == 0) return 1; if (m[t] != null) return m[t]; int r = 0; for (int x : a) if (x <= t) r += g(a, t - x); return m[t] = r; }""")),
]
MUTANTS["partition-equal-subset-sum"] = [
    ("forward-loop-reuses-items", "reject", S("""
    public boolean canPartition(int[] a) { int s = 0; for (int x : a) s += x; if (s % 2 == 1) return false; boolean[] c = new boolean[s / 2 + 1]; c[0] = true; for (int x : a) for (int t = x; t <= s / 2; t++) c[t] |= c[t - x]; return c[s / 2]; }""")),
    ("set-of-sums", "accept", S("""
    public boolean canPartition(int[] a) { int s = 0; for (int x : a) s += x; if (s % 2 == 1) return false; Set<Integer> r = new HashSet<>(); r.add(0); for (int x : a) { Set<Integer> n = new HashSet<>(r); for (int v : r) n.add(v + x); r = n; } return r.contains(s / 2); }""", LIST)),
]
MUTANTS["target-sum"] = [
    ("no-bounds-guard", "reject", S("""
    public int findTargetSumWays(int[] a, int t) { int s = 0; for (int x : a) s += x; if ((s + t) % 2 != 0) return 0; int w = (s + t) / 2; if (w < 0) return 0; int[] d = new int[Math.max(w, 0) + 1]; d[0] = 1; for (int x : a) for (int v = w; v >= x; v--) d[v] += d[v - x]; return w > s ? 1 : d[w]; }""")),
    ("brute-force", "accept", S("""
    public int findTargetSumWays(int[] a, int t) { return g(a, 0, t); }
    int g(int[] a, int i, int r) { if (i == a.length) return r == 0 ? 1 : 0; return g(a, i + 1, r - a[i]) + g(a, i + 1, r + a[i]); }""")),
]
MUTANTS["number-of-longest-increasing-subsequence"] = [
    ("count-at-last-index", "reject", S("""
    public int findNumberOfLIS(int[] a) { int n = a.length; int[] l = new int[n], c = new int[n]; for (int i = 0; i < n; i++) { l[i] = 1; c[i] = 1; for (int j = 0; j < i; j++) if (a[j] < a[i]) { if (l[j] + 1 > l[i]) { l[i] = l[j] + 1; c[i] = c[j]; } else if (l[j] + 1 == l[i]) c[i] += c[j]; } } return c[n - 1]; }""")),
    ("memo-length-and-count", "accept", S("""
    int[] a; int[][] m;
    public int findNumberOfLIS(int[] a) { this.a = a; int n = a.length; m = new int[n][]; int best = 0, cnt = 0; for (int i = 0; i < n; i++) { int[] r = g(i); if (r[0] > best) { best = r[0]; cnt = r[1]; } else if (r[0] == best) cnt += r[1]; } return cnt; }
    int[] g(int i) { if (m[i] != null) return m[i]; int len = 1, cnt = 1; for (int j = i + 1; j < a.length; j++) if (a[j] > a[i]) { int[] r = g(j); if (r[0] + 1 > len) { len = r[0] + 1; cnt = r[1]; } else if (r[0] + 1 == len) cnt += r[1]; } return m[i] = new int[]{len, cnt}; }""")),
]
MUTANTS["partition-to-k-equal-sum-subsets"] = [
    ("greedy-fill-largest-first", "reject", S("""
    public boolean canPartitionKSubsets(int[] a, int k) { int s = 0; for (int x : a) s += x; if (s % k != 0) return false; int t = s / k; Integer[] b = Arrays.stream(a).boxed().sorted(Collections.reverseOrder()).toArray(Integer[]::new); int[] fill = new int[k];
        for (int x : b) { int j = 0; while (j < k && fill[j] + x > t) j++; if (j == k) return false; fill[j] += x; } return true; }""", LIST)),
    ("bucket-backtracking", "accept", S("""
    public boolean canPartitionKSubsets(int[] a, int k) { int s = 0; for (int x : a) s += x; if (s % k != 0) return false; Arrays.sort(a); return go(a, a.length - 1, new int[k], s / k); }
    boolean go(int[] a, int i, int[] b, int t) { if (i < 0) return true; for (int j = 0; j < b.length; j++) { if (b[j] + a[i] <= t) { b[j] += a[i]; if (go(a, i - 1, b, t)) return true; b[j] -= a[i]; } if (b[j] == 0) break; } return false; }""", LIST)),
]
MUTANTS["longest-common-subsequence"] = [
    ("longest-common-substring", "reject", S("""
    public int longestCommonSubsequence(String a, String b) { int[][] d = new int[a.length() + 1][b.length() + 1]; int best = 0; for (int i = 1; i <= a.length(); i++) for (int j = 1; j <= b.length(); j++) if (a.charAt(i - 1) == b.charAt(j - 1)) { d[i][j] = d[i - 1][j - 1] + 1; best = Math.max(best, d[i][j]); } return best; }""")),
    ("full-table", "accept", S("""
    public int longestCommonSubsequence(String a, String b) { int[][] d = new int[a.length() + 1][b.length() + 1]; for (int i = 1; i <= a.length(); i++) for (int j = 1; j <= b.length(); j++) d[i][j] = a.charAt(i - 1) == b.charAt(j - 1) ? d[i - 1][j - 1] + 1 : Math.max(d[i - 1][j], d[i][j - 1]); return d[a.length()][b.length()]; }""")),
]
MUTANTS["concatenated-words"] = [
    ("word-builds-itself", "reject", S("""
    public List<String> findAllConcatenatedWordsInADict(String[] w) { Set<String> d = new HashSet<>(Arrays.asList(w)); List<String> o = new ArrayList<>(); for (String x : w) { boolean[] ok = new boolean[x.length() + 1]; ok[0] = true; for (int i = 1; i <= x.length(); i++) for (int j = 0; j < i; j++) if (ok[j] && d.contains(x.substring(j, i))) ok[i] = true; if (ok[x.length()]) o.add(x); } return o; }""", LIST)),
    ("count-pieces", "accept", S("""
    public List<String> findAllConcatenatedWordsInADict(String[] w) { Set<String> d = new HashSet<>(Arrays.asList(w)); List<String> o = new ArrayList<>();
        for (String x : w) { int[] best = new int[x.length() + 1]; Arrays.fill(best, -1); best[0] = 0; for (int i = 1; i <= x.length(); i++) for (int j = 0; j < i; j++) if (best[j] >= 0 && d.contains(x.substring(j, i))) best[i] = Math.max(best[i], best[j] + 1); if (best[x.length()] >= 2) o.add(x); } return o; }""", LIST)),
]
MUTANTS["gas-station"] = [
    ("start-at-max-surplus", "reject", S("""
    public int canCompleteCircuit(int[] g, int[] c) { long t = 0; int b = 0; for (int i = 0; i < g.length; i++) { t += g[i] - c[i]; if (g[i] - c[i] > g[b] - c[b]) b = i; } return t < 0 ? -1 : b; }""")),
    ("try-every-start", "accept", S("""
    public int canCompleteCircuit(int[] g, int[] c) { int n = g.length; for (int s = 0; s < n; s++) { long t = 0; boolean ok = true; for (int k = 0; k < n && ok; k++) { int i = (s + k) % n; t += g[i] - c[i]; if (t < 0) ok = false; } if (ok) return s; } return -1; }""")),
]

# ---------------------------------------------------------------- batch 8: graphs

MUTANTS["number-of-islands"] = [
    ("counts-land-cells", "reject", S("""
    public int numIslands(char[][] g) { int n = 0; for (char[] r : g) for (char c : r) if (c == '1') n++; return n; }""")),
    ("diagonals-connect", "reject", S("""
    public int numIslands(char[][] g) { int n = 0; for (int i = 0; i < g.length; i++) for (int j = 0; j < g[0].length; j++) if (g[i][j] == '1') { n++; s(g, i, j); } return n; }
    void s(char[][] g, int i, int j) { if (i < 0 || j < 0 || i >= g.length || j >= g[0].length || g[i][j] != '1') return; g[i][j] = '0'; for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) s(g, i + a, j + b); }""")),
    ("union-find", "accept", S("""
    int[] p;
    int f(int x) { while (p[x] != x) x = p[x] = p[p[x]]; return x; }
    public int numIslands(char[][] g) { int R = g.length, C = g[0].length, n = 0; p = new int[R * C]; for (int i = 0; i < R * C; i++) p[i] = i;
        for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) if (g[i][j] == '1') { n++; if (i > 0 && g[i - 1][j] == '1' && f(i * C + j) != f((i - 1) * C + j)) { p[f(i * C + j)] = f((i - 1) * C + j); n--; } if (j > 0 && g[i][j - 1] == '1' && f(i * C + j) != f(i * C + j - 1)) { p[f(i * C + j)] = f(i * C + j - 1); n--; } }
        return n; }""")),
]
MUTANTS["course-schedule"] = [
    ("visited-set-not-colours", "reject", S("""
    public boolean canFinish(int n, int[][] pre) { List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); for (int[] p : pre) g.get(p[1]).add(p[0]); boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) if (!seen[i] && !d(g, i, seen)) return false; return true; }
    boolean d(List<List<Integer>> g, int u, boolean[] seen) { if (seen[u]) return false; seen[u] = true; for (int v : g.get(u)) if (!d(g, v, seen)) return false; return true; }""", LIST)),
    ("three-colour-dfs", "accept", S("""
    public boolean canFinish(int n, int[][] pre) { List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); for (int[] p : pre) g.get(p[1]).add(p[0]); int[] c = new int[n];
        for (int i = 0; i < n; i++) if (!d(g, i, c)) return false; return true; }
    boolean d(List<List<Integer>> g, int u, int[] c) { if (c[u] == 1) return false; if (c[u] == 2) return true; c[u] = 1; for (int v : g.get(u)) if (!d(g, v, c)) return false; c[u] = 2; return true; }""", LIST)),
]
MUTANTS["course-schedule-ii"] = [
    ("returns-partial-order-on-cycle", "reject", S("""
    public int[] findOrder(int n, int[][] pre) { List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); int[] in = new int[n]; for (int[] p : pre) { g.get(p[1]).add(p[0]); in[p[0]]++; }
        Deque<Integer> q = new ArrayDeque<>(); for (int i = 0; i < n; i++) if (in[i] == 0) q.add(i); List<Integer> o = new ArrayList<>(); while (!q.isEmpty()) { int u = q.poll(); o.add(u); for (int v : g.get(u)) if (--in[v] == 0) q.add(v); }
        return o.stream().mapToInt(Integer::intValue).toArray(); }""", LIST)),
    ("reversed-edge-direction", "reject", S("""
    public int[] findOrder(int n, int[][] pre) { List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); int[] in = new int[n]; for (int[] p : pre) { g.get(p[0]).add(p[1]); in[p[1]]++; }
        Deque<Integer> q = new ArrayDeque<>(); for (int i = 0; i < n; i++) if (in[i] == 0) q.add(i); int[] o = new int[n]; int k = 0; while (!q.isEmpty()) { int u = q.poll(); o[k++] = u; for (int v : g.get(u)) if (--in[v] == 0) q.add(v); } return k == n ? o : new int[0]; }""", LIST)),
    ("dfs-postorder-reversed", "accept", S("""
    List<List<Integer>> g; int[] c; List<Integer> post;
    public int[] findOrder(int n, int[][] pre) { g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); for (int[] p : pre) g.get(p[1]).add(p[0]); c = new int[n]; post = new ArrayList<>();
        for (int i = 0; i < n; i++) if (!d(i)) return new int[0]; Collections.reverse(post); return post.stream().mapToInt(Integer::intValue).toArray(); }
    boolean d(int u) { if (c[u] == 1) return false; if (c[u] == 2) return true; c[u] = 1; for (int v : g.get(u)) if (!d(v)) return false; c[u] = 2; post.add(u); return true; }""", LIST)),
]
MUTANTS["graph-valid-tree"] = [
    ("edge-count-only", "reject", S("""
    public boolean validTree(int n, int[][] e) { return e.length == n - 1; }""")),
    ("acyclic-only", "reject", S("""
    int[] p; int f(int x) { while (p[x] != x) x = p[x]; return x; }
    public boolean validTree(int n, int[][] e) { p = new int[n]; for (int i = 0; i < n; i++) p[i] = i; for (int[] x : e) { int a = f(x[0]), b = f(x[1]); if (a == b) return false; p[a] = b; } return true; }""")),
    ("bfs-connected-and-count", "accept", S("""
    public boolean validTree(int n, int[][] e) { if (e.length != n - 1) return false; List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); for (int[] x : e) { g.get(x[0]).add(x[1]); g.get(x[1]).add(x[0]); }
        boolean[] s = new boolean[n]; Deque<Integer> q = new ArrayDeque<>(); q.add(0); s[0] = true; int c = 1; while (!q.isEmpty()) for (int v : g.get(q.poll())) if (!s[v]) { s[v] = true; c++; q.add(v); } return c == n; }""", LIST)),
]
MUTANTS["number-of-connected-components-in-an-undirected-graph"] = [
    ("n-minus-edges", "reject", S("""
    public int countComponents(int n, int[][] e) { return n - e.length; }""")),
    ("dfs", "accept", S("""
    public int countComponents(int n, int[][] e) { List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); for (int[] x : e) { g.get(x[0]).add(x[1]); g.get(x[1]).add(x[0]); }
        boolean[] s = new boolean[n]; int c = 0; for (int i = 0; i < n; i++) if (!s[i]) { c++; Deque<Integer> st = new ArrayDeque<>(); st.push(i); s[i] = true; while (!st.isEmpty()) for (int v : g.get(st.pop())) if (!s[v]) { s[v] = true; st.push(v); } } return c; }""", LIST)),
]
MUTANTS["alien-dictionary"] = [
    ("ignores-prefix-rule", "reject", S("""
    public String alienOrder(String[] w) { Map<Character, Set<Character>> g = new TreeMap<>(); Map<Character, Integer> in = new TreeMap<>(); for (String s : w) for (char c : s.toCharArray()) { g.putIfAbsent(c, new TreeSet<>()); in.putIfAbsent(c, 0); }
        for (int i = 0; i + 1 < w.length; i++) { String a = w[i], b = w[i + 1]; for (int k = 0; k < Math.min(a.length(), b.length()); k++) if (a.charAt(k) != b.charAt(k)) { if (g.get(a.charAt(k)).add(b.charAt(k))) in.merge(b.charAt(k), 1, Integer::sum); break; } }
        Deque<Character> q = new ArrayDeque<>(); for (char c : in.keySet()) if (in.get(c) == 0) q.add(c); StringBuilder o = new StringBuilder(); while (!q.isEmpty()) { char c = q.poll(); o.append(c); for (char d : g.get(c)) if (in.merge(d, -1, Integer::sum) == 0) q.add(d); } return o.length() == in.size() ? o.toString() : ""; }""", LIST)),
    ("dfs-topo", "accept", S("""
    Map<Character, Set<Character>> g = new HashMap<>(); Map<Character, Integer> c = new HashMap<>(); StringBuilder o = new StringBuilder();
    public String alienOrder(String[] w) { for (String s : w) for (char x : s.toCharArray()) g.putIfAbsent(x, new HashSet<>());
        for (int i = 0; i + 1 < w.length; i++) { String a = w[i], b = w[i + 1]; int k = 0; while (k < Math.min(a.length(), b.length()) && a.charAt(k) == b.charAt(k)) k++; if (k == Math.min(a.length(), b.length())) { if (a.length() > b.length()) return ""; } else g.get(a.charAt(k)).add(b.charAt(k)); }
        for (char x : g.keySet()) if (!d(x)) return ""; return o.reverse().toString(); }
    boolean d(char x) { int s = c.getOrDefault(x, 0); if (s == 1) return false; if (s == 2) return true; c.put(x, 1); for (char y : g.get(x)) if (!d(y)) return false; c.put(x, 2); o.append(x); return true; }""", LIST)),
]
MUTANTS["minimum-height-trees"] = [
    ("max-degree-node", "reject", S("""
    public List<Integer> findMinHeightTrees(int n, int[][] e) { int[] d = new int[n]; for (int[] x : e) { d[x[0]]++; d[x[1]]++; } int b = 0; for (int i = 0; i < n; i++) if (d[i] > d[b]) b = i; return new ArrayList<>(List.of(b)); }""", LIST)),
    ("bfs-from-every-root", "accept", S("""
    public List<Integer> findMinHeightTrees(int n, int[][] e) { List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>()); for (int[] x : e) { g.get(x[0]).add(x[1]); g.get(x[1]).add(x[0]); }
        int[] h = new int[n]; int mn = Integer.MAX_VALUE; for (int r = 0; r < n; r++) { int[] d = new int[n]; Arrays.fill(d, -1); d[r] = 0; Deque<Integer> q = new ArrayDeque<>(); q.add(r); int m = 0; while (!q.isEmpty()) { int u = q.poll(); m = Math.max(m, d[u]); for (int v : g.get(u)) if (d[v] < 0) { d[v] = d[u] + 1; q.add(v); } } h[r] = m; mn = Math.min(mn, m); }
        List<Integer> o = new ArrayList<>(); for (int r = 0; r < n; r++) if (h[r] == mn) o.add(r); return o; }""", LIST)),
]
MUTANTS["pacific-atlantic-water-flow"] = [
    ("strictly-downhill", "reject", S("""
    public List<List<Integer>> pacificAtlantic(int[][] h) { int R = h.length, C = h[0].length; boolean[][] p = new boolean[R][C], a = new boolean[R][C];
        for (int i = 0; i < R; i++) { d(h, p, i, 0, Integer.MIN_VALUE); d(h, a, i, C - 1, Integer.MIN_VALUE); } for (int j = 0; j < C; j++) { d(h, p, 0, j, Integer.MIN_VALUE); d(h, a, R - 1, j, Integer.MIN_VALUE); }
        List<List<Integer>> o = new ArrayList<>(); for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) if (p[i][j] && a[i][j]) o.add(Arrays.asList(i, j)); return o; }
    void d(int[][] h, boolean[][] s, int i, int j, int prev) { if (i < 0 || j < 0 || i >= h.length || j >= h[0].length || s[i][j] || h[i][j] <= prev && prev != Integer.MIN_VALUE) return; s[i][j] = true; d(h, s, i + 1, j, h[i][j]); d(h, s, i - 1, j, h[i][j]); d(h, s, i, j + 1, h[i][j]); d(h, s, i, j - 1, h[i][j]); }""", LIST)),
    ("per-cell-search", "accept", S("""
    public List<List<Integer>> pacificAtlantic(int[][] h) { int R = h.length, C = h[0].length; List<List<Integer>> o = new ArrayList<>();
        for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) { boolean[][] s = new boolean[R][C]; boolean[] hit = new boolean[2]; Deque<int[]> q = new ArrayDeque<>(); q.add(new int[]{i, j}); s[i][j] = true;
            while (!q.isEmpty()) { int[] c = q.poll(); if (c[0] == 0 || c[1] == 0) hit[0] = true; if (c[0] == R - 1 || c[1] == C - 1) hit[1] = true;
                for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) { int x = c[0] + d[0], y = c[1] + d[1]; if (x >= 0 && y >= 0 && x < R && y < C && !s[x][y] && h[x][y] <= h[c[0]][c[1]]) { s[x][y] = true; q.add(new int[]{x, y}); } } }
            if (hit[0] && hit[1]) o.add(Arrays.asList(i, j)); } return o; }""", LIST)),
]
MUTANTS["clone-graph"] = [
    ("returns-original", "reject", S("""
    public Node cloneGraph(Node node) { return node; }""")),
    ("shallow-copies-neighbours", "reject", S("""
    public Node cloneGraph(Node node) { if (node == null) return null; Node c = new Node(node.val); c.neighbors.addAll(node.neighbors); return c; }""")),
    ("dfs-with-map", "accept", S("""
    Map<Node, Node> m = new IdentityHashMap<>();
    public Node cloneGraph(Node node) { if (node == null) return null; if (m.containsKey(node)) return m.get(node); Node c = new Node(node.val); m.put(node, c); for (Node v : node.neighbors) c.neighbors.add(cloneGraph(v)); return c; }""", LIST)),
]

# ------------------------------------------------------- batch 9: tries and design

MUTANTS["implement-trie-prefix-tree"] = [
    ("no-word-flag", "reject", D("""
class Trie {
    Trie[] n = new Trie[26];
    public void insert(String w) { Trie t = this; for (char c : w.toCharArray()) { if (t.n[c - 'a'] == null) t.n[c - 'a'] = new Trie(); t = t.n[c - 'a']; } }
    public boolean search(String w) { Trie t = this; for (char c : w.toCharArray()) { t = t.n[c - 'a']; if (t == null) return false; } return true; }
    public boolean startsWith(String p) { return search(p); }
}""")),
    ("hashset-of-prefixes", "accept", D("""
class Trie {
    Set<String> words = new HashSet<>(), pre = new HashSet<>();
    public void insert(String w) { words.add(w); for (int i = 0; i <= w.length(); i++) pre.add(w.substring(0, i)); }
    public boolean search(String w) { return words.contains(w); }
    public boolean startsWith(String p) { return pre.contains(p); }
}""")),
]
MUTANTS["design-add-and-search-words-data-structure"] = [
    ("dot-matches-any-length", "reject", D("""
class WordDictionary {
    List<String> w = new ArrayList<>();
    public void addWord(String s) { w.add(s); }
    public boolean search(String s) { String re = s.replace(".", ".*"); for (String x : w) if (x.matches(re)) return true; return false; }
}""")),
    ("list-with-regex", "accept", D("""
class WordDictionary {
    List<String> w = new ArrayList<>();
    public void addWord(String s) { w.add(s); }
    public boolean search(String s) { for (String x : w) if (x.matches(s)) return true; return false; }
}""")),
]
MUTANTS["word-search-ii"] = [
    ("reports-duplicates", "reject", S("""
    public List<String> findWords(char[][] b, String[] ws) { List<String> o = new ArrayList<>(); for (String w : ws) for (int i = 0; i < b.length; i++) for (int j = 0; j < b[0].length; j++) if (d(b, w, i, j, 0)) o.add(w); return o; }
    boolean d(char[][] b, String w, int i, int j, int k) { if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || b[i][j] != w.charAt(k)) return false; if (k == w.length() - 1) return true; char c = b[i][j]; b[i][j] = '#';
        boolean r = d(b, w, i + 1, j, k + 1) || d(b, w, i - 1, j, k + 1) || d(b, w, i, j + 1, k + 1) || d(b, w, i, j - 1, k + 1); b[i][j] = c; return r; }""", LIST)),
    ("word-search-per-word", "accept", S("""
    public List<String> findWords(char[][] b, String[] ws) { List<String> o = new ArrayList<>(); outer: for (String w : ws) for (int i = 0; i < b.length; i++) for (int j = 0; j < b[0].length; j++) if (d(b, w, i, j, 0)) { o.add(w); continue outer; } return o; }
    boolean d(char[][] b, String w, int i, int j, int k) { if (i < 0 || j < 0 || i >= b.length || j >= b[0].length || b[i][j] != w.charAt(k)) return false; if (k == w.length() - 1) return true; char c = b[i][j]; b[i][j] = '#';
        boolean r = d(b, w, i + 1, j, k + 1) || d(b, w, i - 1, j, k + 1) || d(b, w, i, j + 1, k + 1) || d(b, w, i, j - 1, k + 1); b[i][j] = c; return r; }""", LIST)),
]
MUTANTS["longest-word-in-dictionary"] = [
    ("ignores-tie-break", "reject", S("""
    public String longestWord(String[] w) { Set<String> s = new HashSet<>(Arrays.asList(w)); String b = ""; for (String x : w) { boolean ok = true; for (int i = 1; i < x.length(); i++) if (!s.contains(x.substring(0, i))) ok = false; if (ok && x.length() >= b.length()) b = x; } return b; }""", LIST)),
    ("sort-then-build", "accept", S("""
    public String longestWord(String[] w) { String[] a = w.clone(); Arrays.sort(a); Set<String> built = new HashSet<>(); built.add(""); String b = ""; for (String x : a) if (built.contains(x.substring(0, x.length() - 1))) { built.add(x); if (x.length() > b.length()) b = x; } return b; }""", LIST)),
]
MUTANTS["prefix-and-suffix-search"] = [
    ("smallest-index", "reject", D("""
class WordFilter {
    String[] w;
    public WordFilter(String[] words) { w = words; }
    public int f(String p, String s) { for (int i = 0; i < w.length; i++) if (w[i].startsWith(p) && w[i].endsWith(s)) return i; return -1; }
}""")),
    ("scan-from-back", "accept", D("""
class WordFilter {
    String[] w;
    public WordFilter(String[] words) { w = words; }
    public int f(String p, String s) { for (int i = w.length - 1; i >= 0; i--) if (w[i].startsWith(p) && w[i].endsWith(s)) return i; return -1; }
}""")),
]
MUTANTS["design-search-autocomplete-system"] = [
    ("never-records-typed-sentences", "reject", D("""
class AutocompleteSystem {
    Map<String, Integer> h = new HashMap<>(); StringBuilder cur = new StringBuilder();
    public AutocompleteSystem(String[] s, int[] t) { for (int i = 0; i < s.length; i++) h.put(s[i], t[i]); }
    public List<String> input(char c) { if (c == '#') { cur.setLength(0); return new ArrayList<>(); } cur.append(c); String p = cur.toString(); List<String> m = new ArrayList<>();
        for (String s : h.keySet()) if (s.startsWith(p)) m.add(s); m.sort((x, y) -> h.get(x).equals(h.get(y)) ? x.compareTo(y) : h.get(y) - h.get(x)); return new ArrayList<>(m.subList(0, Math.min(3, m.size()))); }
}""")),
    ("tie-break-reversed", "reject", D("""
class AutocompleteSystem {
    Map<String, Integer> h = new HashMap<>(); StringBuilder cur = new StringBuilder();
    public AutocompleteSystem(String[] s, int[] t) { for (int i = 0; i < s.length; i++) h.put(s[i], t[i]); }
    public List<String> input(char c) { if (c == '#') { h.merge(cur.toString(), 1, Integer::sum); cur.setLength(0); return new ArrayList<>(); } cur.append(c); String p = cur.toString(); List<String> m = new ArrayList<>();
        for (String s : h.keySet()) if (s.startsWith(p)) m.add(s); m.sort((x, y) -> h.get(x).equals(h.get(y)) ? y.compareTo(x) : h.get(y) - h.get(x)); return new ArrayList<>(m.subList(0, Math.min(3, m.size()))); }
}""")),
    ("trie-with-sentence-sets", "accept", D("""
class AutocompleteSystem {
    class N { Map<Character, N> k = new HashMap<>(); Set<String> s = new HashSet<>(); }
    N root = new N(), at; Map<String, Integer> h = new HashMap<>(); StringBuilder cur = new StringBuilder();
    public AutocompleteSystem(String[] s, int[] t) { for (int i = 0; i < s.length; i++) add(s[i], t[i]); at = root; }
    void add(String s, int t) { h.merge(s, t, Integer::sum); N n = root; for (char c : s.toCharArray()) { n = n.k.computeIfAbsent(c, x -> new N()); n.s.add(s); } }
    public List<String> input(char c) { if (c == '#') { add(cur.toString(), 1); cur.setLength(0); at = root; return new ArrayList<>(); } cur.append(c);
        if (at != null) at = at.k.get(c); if (at == null) return new ArrayList<>(); List<String> m = new ArrayList<>(at.s);
        m.sort((x, y) -> h.get(x).equals(h.get(y)) ? x.compareTo(y) : h.get(y) - h.get(x)); return new ArrayList<>(m.subList(0, Math.min(3, m.size()))); }
}""")),
]
MUTANTS["index-pairs-of-a-string"] = [
    ("unsorted-by-word-order", "reject", S("""
    public int[][] indexPairs(String t, String[] ws) { List<int[]> o = new ArrayList<>(); for (String w : ws) for (int i = 0; i + w.length() <= t.length(); i++) if (t.startsWith(w, i)) o.add(new int[]{i, i + w.length() - 1}); return o.toArray(new int[0][]); }""", LIST)),
    ("trie-walk", "accept", S("""
    class N { N[] n = new N[26]; boolean w; }
    public int[][] indexPairs(String t, String[] ws) { N r = new N(); for (String w : ws) { N x = r; for (char c : w.toCharArray()) { if (x.n[c - 'a'] == null) x.n[c - 'a'] = new N(); x = x.n[c - 'a']; } x.w = true; }
        List<int[]> o = new ArrayList<>(); for (int i = 0; i < t.length(); i++) { N x = r; for (int j = i; j < t.length(); j++) { x = x.n[t.charAt(j) - 'a']; if (x == null) break; if (x.w) o.add(new int[]{i, j}); } } return o.toArray(new int[0][]); }""", LIST)),
]
MUTANTS["find-median-from-data-stream"] = [
    ("heaps-unbalanced-routing", "reject", D("""
class MedianFinder {
    PriorityQueue<Integer> lo = new PriorityQueue<>(Collections.reverseOrder()), hi = new PriorityQueue<>();
    public void addNum(int x) { if (lo.size() <= hi.size()) lo.add(x); else hi.add(x); }
    public double findMedian() { return lo.size() > hi.size() ? lo.peek() : (lo.peek() + hi.peek()) / 2.0; }
}""")),
    ("sorted-list-insert", "accept", D("""
class MedianFinder {
    List<Integer> l = new ArrayList<>();
    public void addNum(int x) { int i = Collections.binarySearch(l, x); l.add(i < 0 ? -i - 1 : i, x); }
    public double findMedian() { int n = l.size(); return n % 2 == 1 ? l.get(n / 2) : (l.get(n / 2 - 1) + (double) l.get(n / 2)) / 2; }
}""")),
]
MUTANTS["range-sum-query-immutable"] = [
    ("exclusive-right", "reject", D("""
class NumArray {
    int[] p;
    public NumArray(int[] a) { p = new int[a.length + 1]; for (int i = 0; i < a.length; i++) p[i + 1] = p[i] + a[i]; }
    public int sumRange(int l, int r) { return p[r] - p[l]; }
}""")),
    ("loop-per-query", "accept", D("""
class NumArray {
    int[] a;
    public NumArray(int[] a) { this.a = a.clone(); }
    public int sumRange(int l, int r) { int s = 0; for (int i = l; i <= r; i++) s += a[i]; return s; }
}""")),
]
MUTANTS["maximum-frequency-stack"] = [
    ("tie-breaks-to-oldest", "reject", D("""
class FreqStack {
    Map<Integer, Integer> f = new HashMap<>(); List<Integer> s = new ArrayList<>();
    public void push(int x) { f.merge(x, 1, Integer::sum); s.add(x); }
    public int pop() { int best = -1, bi = -1; for (int i = 0; i < s.size(); i++) { int v = s.get(i); if (f.get(v) > best) { best = f.get(v); bi = i; } } int x = s.remove(bi); f.merge(x, -1, Integer::sum); return x; }
}""")),
    ("scan-latest-max", "accept", D("""
class FreqStack {
    Map<Integer, Integer> f = new HashMap<>(); List<Integer> s = new ArrayList<>();
    public void push(int x) { f.merge(x, 1, Integer::sum); s.add(x); }
    public int pop() { int best = -1, bi = -1; for (int i = 0; i < s.size(); i++) { int v = s.get(i); if (f.get(v) >= best) { best = f.get(v); bi = i; } } int x = s.remove(bi); f.merge(x, -1, Integer::sum); return x; }
}""")),
]
MUTANTS["encode-and-decode-strings"] = [
    ("static-field-cheat", "reject", D("""
public class Codec {
    static List<String> last;
    public String encode(List<String> s) { last = s; return ""; }
    public List<String> decode(String s) { return last; }
}""")),
    ("naive-delimiter", "reject", D("""
public class Codec {
    public String encode(List<String> s) { return String.join("#", s); }
    public List<String> decode(String s) { return s.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(s.split("#", -1))); }
}""")),
    ("escape-scheme", "accept", D("""
public class Codec {
    public String encode(List<String> s) { StringBuilder b = new StringBuilder(); for (String x : s) b.append(x.replace("/", "//").replace(",", "/,")).append(" ,"); return b.toString(); }
    public List<String> decode(String s) { List<String> o = new ArrayList<>(); StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) { char c = s.charAt(i); if (c == '/') cur.append(s.charAt(++i)); else if (c == ',') { o.add(cur.substring(0, cur.length() - 1)); cur.setLength(0); } else cur.append(c); } return o; }
}""")),
]
