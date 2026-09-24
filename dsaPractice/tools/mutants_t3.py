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
