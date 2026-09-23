"""
Mutants for tier 2. See tools/mutants.py.

Each entry: (name, "reject" | "accept", full Solution.java source).
Rejects are the bugs a real first attempt makes; accepts are legitimate alternatives
the comparison mode must not refuse.
"""


def S(body, imports=""):
    return "%s\nclass Solution {\n%s\n}\n" % (imports, body)


LIST = "import java.util.*;"

MUTANTS = {}

# ------------------------------------------------------------------ group A: arrays

MUTANTS["concatenation-of-array"] = [
    ("offset-by-n-minus-1", "reject", S("""
    public int[] getConcatenation(int[] nums) {
        int n = nums.length; int[] a = new int[2 * n];
        for (int i = 0; i < n; i++) { a[i] = nums[i]; a[i + n - 1] = nums[i]; }
        return a;
    }""")),
    ("streams", "accept", S("""
    public int[] getConcatenation(int[] nums) {
        return java.util.stream.IntStream.concat(java.util.Arrays.stream(nums),
                                                 java.util.Arrays.stream(nums)).toArray();
    }""")),
]

MUTANTS["build-array-from-permutation"] = [
    ("in-place-no-encoding", "reject", S("""
    public int[] buildArray(int[] nums) {
        for (int i = 0; i < nums.length; i++) nums[i] = nums[nums[i]];
        return nums;
    }""")),
    ("o1-space-encoding", "accept", S("""
    public int[] buildArray(int[] nums) {
        int n = nums.length;
        for (int i = 0; i < n; i++) nums[i] += n * (nums[nums[i]] % n);
        for (int i = 0; i < n; i++) nums[i] /= n;
        return nums;
    }""")),
]

MUTANTS["shuffle-the-array"] = [
    ("y-before-x", "reject", S("""
    public int[] shuffle(int[] nums, int n) {
        int[] a = new int[2 * n];
        for (int i = 0; i < n; i++) { a[2 * i] = nums[n + i]; a[2 * i + 1] = nums[i]; }
        return a;
    }""")),
    ("counter", "accept", S("""
    public int[] shuffle(int[] nums, int n) {
        int[] a = new int[2 * n]; int w = 0;
        for (int i = 0; i < n; i++) { a[w++] = nums[i]; a[w++] = nums[i + n]; }
        return a;
    }""")),
]

MUTANTS["kids-with-the-greatest-number-of-candies"] = [
    ("strict-greater", "reject", S("""
    public List<Boolean> kidsWithCandies(int[] c, int e) {
        int m = 0; for (int x : c) m = Math.max(m, x);
        List<Boolean> o = new ArrayList<>(); for (int x : c) o.add(x + e > m); return o;
    }""", LIST)),
    ("compares-to-first", "reject", S("""
    public List<Boolean> kidsWithCandies(int[] c, int e) {
        List<Boolean> o = new ArrayList<>(); for (int x : c) o.add(x + e >= c[0]); return o;
    }""", LIST)),
    ("quadratic-but-correct", "accept", S("""
    public List<Boolean> kidsWithCandies(int[] c, int e) {
        List<Boolean> o = new ArrayList<>();
        for (int x : c) { boolean ok = true; for (int y : c) if (y > x + e) ok = false; o.add(ok); }
        return o;
    }""", LIST)),
]

MUTANTS["reverse-string"] = [
    ("reverses-twice", "reject", S("""
    public void reverseString(char[] s) {
        for (int i = 0, j = s.length - 1; i < s.length; i++, j--) { char t = s[i]; s[i] = s[j]; s[j] = t; }
    }""")),
    ("misses-last-pair", "reject", S("""
    public void reverseString(char[] s) {
        for (int i = 0, j = s.length - 1; i < j - 1; i++, j--) { char t = s[i]; s[i] = s[j]; s[j] = t; }
    }""")),
    ("half-loop", "accept", S("""
    public void reverseString(char[] s) {
        int n = s.length;
        for (int i = 0; i < n / 2; i++) { char t = s[i]; s[i] = s[n - 1 - i]; s[n - 1 - i] = t; }
    }""")),
]

MUTANTS["remove-duplicates-from-sorted-array"] = [
    ("returns-last-index", "reject", S("""
    public int removeDuplicates(int[] nums) {
        int w = 0;
        for (int r = 1; r < nums.length; r++) if (nums[r] != nums[w]) nums[++w] = nums[r];
        return w;
    }""")),
    ("counts-but-no-compact", "reject", S("""
    public int removeDuplicates(int[] nums) {
        int k = 1; for (int r = 1; r < nums.length; r++) if (nums[r] != nums[r - 1]) k++;
        return k;
    }""")),
    ("slow-fast", "accept", S("""
    public int removeDuplicates(int[] nums) {
        int w = 0;
        for (int r = 1; r < nums.length; r++) if (nums[r] != nums[w]) nums[++w] = nums[r];
        return w + 1;
    }""")),
]

MUTANTS["remove-element"] = [
    ("off-by-one-k", "reject", S("""
    public int removeElement(int[] nums, int val) {
        int w = 0; for (int x : nums) if (x != val) nums[w++] = x; return Math.max(0, w - 1);
    }""")),
    ("swap-advances-i", "reject", S("""
    public int removeElement(int[] nums, int val) {
        int n = nums.length;
        for (int i = 0; i < n; i++) if (nums[i] == val) { nums[i] = nums[n - 1]; n--; }
        return n;
    }""")),
    ("swap-with-end-reorders", "accept", S("""
    public int removeElement(int[] nums, int val) {
        int i = 0, n = nums.length;
        while (i < n) { if (nums[i] == val) nums[i] = nums[--n]; else i++; }
        return n;
    }""")),
]

MUTANTS["merge-sorted-array"] = [
    ("merge-from-front", "reject", S("""
    public void merge(int[] a, int m, int[] b, int n) {
        int i = 0, j = 0, w = 0;
        while (j < n) { if (i < m && a[i] <= b[j]) { a[w++] = a[i++]; } else { a[w++] = b[j++]; } }
    }""")),
    ("stops-when-nums1-runs-out", "reject", S("""
    public void merge(int[] a, int m, int[] b, int n) {
        int i = m - 1, j = n - 1, w = m + n - 1;
        while (i >= 0 && j >= 0) a[w--] = a[i] > b[j] ? a[i--] : b[j--];
    }""")),
    ("copy-then-sort", "accept", S("""
    public void merge(int[] a, int m, int[] b, int n) {
        System.arraycopy(b, 0, a, m, n); java.util.Arrays.sort(a);
    }""")),
]

MUTANTS["sort-array-by-parity"] = [
    ("odds-first", "reject", S("""
    public int[] sortArrayByParity(int[] nums) {
        int w = 0;
        for (int r = 0; r < nums.length; r++) if (nums[r] % 2 == 1) { int t = nums[w]; nums[w] = nums[r]; nums[r] = t; w++; }
        return nums;
    }""")),
    ("overwrites-instead-of-swap", "reject", S("""
    public int[] sortArrayByParity(int[] nums) {
        int w = 0; for (int r = 0; r < nums.length; r++) if (nums[r] % 2 == 0) nums[w++] = nums[r];
        return nums;
    }""")),
    ("two-ended", "accept", S("""
    public int[] sortArrayByParity(int[] nums) {
        int i = 0, j = nums.length - 1;
        while (i < j) {
            if (nums[i] % 2 > nums[j] % 2) { int t = nums[i]; nums[i] = nums[j]; nums[j] = t; }
            if (nums[i] % 2 == 0) i++;
            if (nums[j] % 2 == 1) j--;
        }
        return nums;
    }""")),
]

# ------------------------------------------------------ group B: hashing / strings

MUTANTS["jewels-and-stones"] = [
    ("case-insensitive", "reject", S("""
    public int numJewelsInStones(String j, String s) {
        int n = 0; for (char c : s.toCharArray()) if (j.toLowerCase().indexOf(Character.toLowerCase(c)) >= 0) n++; return n;
    }""")),
    ("indexOf-nested", "accept", S("""
    public int numJewelsInStones(String j, String s) {
        int n = 0; for (char c : s.toCharArray()) if (j.indexOf(c) >= 0) n++; return n;
    }""")),
]

MUTANTS["number-of-good-pairs"] = [
    ("increment-before-add", "reject", S("""
    public int numIdenticalPairs(int[] nums) {
        int[] c = new int[101]; int p = 0; for (int x : nums) { c[x]++; p += c[x]; } return p;
    }""")),
    ("counts-ordered-pairs", "reject", S("""
    public int numIdenticalPairs(int[] nums) {
        int p = 0; for (int i = 0; i < nums.length; i++) for (int j = 0; j < nums.length; j++) if (i != j && nums[i] == nums[j]) p++; return p;
    }""")),
    ("closed-form", "accept", S("""
    public int numIdenticalPairs(int[] nums) {
        int[] c = new int[101]; for (int x : nums) c[x]++; int p = 0; for (int k : c) p += k * (k - 1) / 2; return p;
    }""")),
]

MUTANTS["intersection-of-two-arrays"] = [
    ("keeps-duplicates", "reject", S("""
    public int[] intersection(int[] a, int[] b) {
        Set<Integer> s = new HashSet<>(); for (int x : a) s.add(x);
        List<Integer> o = new ArrayList<>(); for (int x : b) if (s.contains(x)) o.add(x);
        return o.stream().mapToInt(Integer::intValue).toArray();
    }""", LIST)),
    ("sort-two-pointers", "accept", S("""
    public int[] intersection(int[] a, int[] b) {
        Arrays.sort(a); Arrays.sort(b); List<Integer> o = new ArrayList<>(); int i = 0, j = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) i++; else if (a[i] > b[j]) j++;
            else { if (o.isEmpty() || o.get(o.size() - 1) != a[i]) o.add(a[i]); i++; j++; }
        }
        return o.stream().mapToInt(Integer::intValue).toArray();
    }""", LIST)),
]

MUTANTS["ransom-note"] = [
    ("membership-not-quantity", "reject", S("""
    public boolean canConstruct(String r, String m) {
        for (char c : r.toCharArray()) if (m.indexOf(c) < 0) return false; return true;
    }""")),
    ("remove-from-builder", "accept", S("""
    public boolean canConstruct(String r, String m) {
        StringBuilder b = new StringBuilder(m);
        for (char c : r.toCharArray()) { int k = b.indexOf(String.valueOf(c)); if (k < 0) return false; b.deleteCharAt(k); }
        return true;
    }""")),
]

MUTANTS["first-unique-character-in-a-string"] = [
    ("alphabetical-first", "reject", S("""
    public int firstUniqChar(String s) {
        int[] c = new int[26]; for (char ch : s.toCharArray()) c[ch - 'a']++;
        for (int k = 0; k < 26; k++) if (c[k] == 1) return s.indexOf((char) ('a' + k)); return -1;
    }""")),
    ("indexOf-lastIndexOf", "accept", S("""
    public int firstUniqChar(String s) {
        for (int i = 0; i < s.length(); i++) if (s.indexOf(s.charAt(i)) == s.lastIndexOf(s.charAt(i))) return i; return -1;
    }""")),
]

MUTANTS["how-many-numbers-are-smaller-than-the-current-number"] = [
    ("counts-equal-too", "reject", S("""
    public int[] smallerNumbersThanCurrent(int[] nums) {
        int[] o = new int[nums.length];
        for (int i = 0; i < nums.length; i++) for (int x : nums) if (x <= nums[i] && x != nums[i] || x < nums[i]) o[i]++; else if (x == nums[i]) o[i]++;
        for (int i = 0; i < o.length; i++) o[i]--;
        return o;
    }""")),
    ("sorted-first-index", "accept", S("""
    public int[] smallerNumbersThanCurrent(int[] nums) {
        int[] s = nums.clone(); Arrays.sort(s); Map<Integer, Integer> first = new HashMap<>();
        for (int i = 0; i < s.length; i++) first.putIfAbsent(s[i], i);
        int[] o = new int[nums.length]; for (int i = 0; i < nums.length; i++) o[i] = first.get(nums[i]); return o;
    }""", LIST)),
]

MUTANTS["merge-strings-alternately"] = [
    ("drops-longer-tail", "reject", S("""
    public String mergeAlternately(String a, String b) {
        StringBuilder s = new StringBuilder(); for (int i = 0; i < Math.min(a.length(), b.length()); i++) s.append(a.charAt(i)).append(b.charAt(i)); return s.toString();
    }""")),
    ("both-then-tails", "accept", S("""
    public String mergeAlternately(String a, String b) {
        StringBuilder s = new StringBuilder(); int i = 0;
        for (; i < a.length() && i < b.length(); i++) s.append(a.charAt(i)).append(b.charAt(i));
        return s.append(a.substring(i)).append(b.substring(i)).toString();
    }""")),
]

MUTANTS["longest-palindrome"] = [
    ("plus-one-per-odd", "reject", S("""
    public int longestPalindrome(String s) {
        int[] c = new int[128]; for (char ch : s.toCharArray()) c[ch]++; int n = 0;
        for (int k : c) n += k / 2 * 2 + (k % 2); return n;
    }""")),
    ("case-folded", "reject", S("""
    public int longestPalindrome(String s) {
        int[] c = new int[128]; for (char ch : s.toLowerCase().toCharArray()) c[ch]++; int n = 0; boolean o = false;
        for (int k : c) { n += k / 2 * 2; if (k % 2 == 1) o = true; } return n + (o ? 1 : 0);
    }""")),
    ("set-toggle", "accept", S("""
    public int longestPalindrome(String s) {
        Set<Character> odd = new HashSet<>(); for (char c : s.toCharArray()) if (!odd.add(c)) odd.remove(c);
        return odd.isEmpty() ? s.length() : s.length() - odd.size() + 1;
    }""", LIST)),
]

MUTANTS["goal-parser-interpretation"] = [
    ("paren-always-o", "reject", S("""
    public String interpret(String c) {
        StringBuilder b = new StringBuilder(); int i = 0;
        while (i < c.length()) { if (c.charAt(i) == 'G') { b.append('G'); i++; } else { b.append('o'); i += (c.charAt(i + 1) == ')' ? 2 : 4); } }
        return b.toString();
    }""")),
    ("replace-chain", "accept", S("""
    public String interpret(String c) { return c.replace("()", "o").replace("(al)", "al"); }""")),
]

MUTANTS["maximum-number-of-words-found-in-sentences"] = [
    ("counts-spaces-only", "reject", S("""
    public int mostWordsFound(String[] ss) {
        int m = 0; for (String s : ss) { int k = 0; for (char c : s.toCharArray()) if (c == ' ') k++; m = Math.max(m, k); } return m;
    }""")),
    ("split", "accept", S("""
    public int mostWordsFound(String[] ss) { int m = 0; for (String s : ss) m = Math.max(m, s.split(" ").length); return m; }""")),
]

MUTANTS["running-sum-of-1d-array"] = [
    ("exclusive-prefix", "reject", S("""
    public int[] runningSum(int[] nums) {
        int[] o = new int[nums.length]; int s = 0; for (int i = 0; i < nums.length; i++) { o[i] = s; s += nums[i]; } return o;
    }""")),
    ("in-place", "accept", S("""
    public int[] runningSum(int[] nums) { for (int i = 1; i < nums.length; i++) nums[i] += nums[i - 1]; return nums; }""")),
]

MUTANTS["find-pivot-index"] = [
    ("includes-self-on-left", "reject", S("""
    public int pivotIndex(int[] nums) {
        int t = 0; for (int x : nums) t += x; int l = 0;
        for (int i = 0; i < nums.length; i++) { l += nums[i]; if (l == t - l) return i; } return -1;
    }""")),
    ("rightmost-pivot", "reject", S("""
    public int pivotIndex(int[] nums) {
        int t = 0; for (int x : nums) t += x; int l = 0, ans = -1;
        for (int i = 0; i < nums.length; i++) { if (l == t - l - nums[i]) ans = i; l += nums[i]; } return ans;
    }""")),
    ("two-prefix-arrays", "accept", S("""
    public int pivotIndex(int[] nums) {
        int n = nums.length; int[] p = new int[n + 1]; for (int i = 0; i < n; i++) p[i + 1] = p[i] + nums[i];
        for (int i = 0; i < n; i++) if (p[i] == p[n] - p[i + 1]) return i; return -1;
    }""")),
]

# --------------------------------------- group C: search / stack / greedy / DP / bits

MUTANTS["search-insert-position"] = [
    ("hi-is-n-minus-1", "reject", S("""
    public int searchInsert(int[] a, int t) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) { int m = (lo + hi) / 2; if (a[m] < t) lo = m + 1; else hi = m; } return lo;
    }""")),
    ("closed-interval", "accept", S("""
    public int searchInsert(int[] a, int t) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) { int m = lo + (hi - lo) / 2; if (a[m] == t) return m; if (a[m] < t) lo = m + 1; else hi = m - 1; }
        return lo;
    }""")),
]

MUTANTS["arranging-coins"] = [
    ("int-overflow", "reject", S("""
    public int arrangeCoins(int n) {
        int lo = 1, hi = n;
        while (lo < hi) { int m = lo + (hi - lo + 1) / 2; if (m * (m + 1) / 2 <= n) lo = m; else hi = m - 1; } return lo;
    }""")),
    ("quadratic-formula", "accept", S("""
    public int arrangeCoins(int n) { return (int) ((Math.sqrt(8.0 * n + 1) - 1) / 2); }""")),
]

MUTANTS["sqrtx"] = [
    ("int-overflow", "reject", S("""
    public int mySqrt(int x) {
        int lo = 0, hi = x;
        while (lo < hi) { int m = lo + (hi - lo + 1) / 2; if (m * m <= x) lo = m; else hi = m - 1; } return lo;
    }""")),
    ("divide-instead", "accept", S("""
    public int mySqrt(int x) {
        if (x < 2) return x; int lo = 1, hi = x / 2;
        while (lo < hi) { int m = lo + (hi - lo + 1) / 2; if (m <= x / m) lo = m; else hi = m - 1; } return lo;
    }""")),
]

MUTANTS["valid-perfect-square"] = [
    ("int-overflow", "reject", S("""
    public boolean isPerfectSquare(int num) {
        int lo = 1, hi = num;
        while (lo <= hi) { int m = lo + (hi - lo) / 2; if (m * m == num) return true; if (m * m < num) lo = m + 1; else hi = m - 1; }
        return false;
    }""")),
    ("odd-number-sum", "accept", S("""
    public boolean isPerfectSquare(int num) { long n = num; for (long odd = 1; n > 0; odd += 2) n -= odd; return n == 0; }""")),
]

MUTANTS["baseball-game"] = [
    ("plus-consumes-top", "reject", S("""
    public int calPoints(String[] ops) {
        Deque<Integer> s = new ArrayDeque<>();
        for (String o : ops) {
            if (o.equals("C")) s.pop(); else if (o.equals("D")) s.push(2 * s.peek());
            else if (o.equals("+")) { int a = s.pop(), b = s.peek(); s.push(a + b); } else s.push(Integer.parseInt(o));
        }
        int t = 0; for (int x : s) t += x; return t;
    }""", LIST)),
    ("array-stack", "accept", S("""
    public int calPoints(String[] ops) {
        int[] s = new int[ops.length]; int k = 0;
        for (String o : ops) {
            switch (o) { case "C": k--; break; case "D": s[k] = 2 * s[k - 1]; k++; break;
                         case "+": s[k] = s[k - 1] + s[k - 2]; k++; break; default: s[k++] = Integer.parseInt(o); }
        }
        int t = 0; for (int i = 0; i < k; i++) t += s[i]; return t;
    }""")),
]

MUTANTS["remove-all-adjacent-duplicates-in-string"] = [
    ("single-pass-no-cascade", "reject", S("""
    public String removeDuplicates(String s) {
        StringBuilder b = new StringBuilder(); int i = 0;
        while (i < s.length()) { if (i + 1 < s.length() && s.charAt(i) == s.charAt(i + 1)) i += 2; else b.append(s.charAt(i++)); }
        return b.toString();
    }""")),
    ("rescan-until-stable", "accept", S("""
    public String removeDuplicates(String s) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i + 1 < s.length(); i++) if (s.charAt(i) == s.charAt(i + 1)) { s = s.substring(0, i) + s.substring(i + 2); changed = true; break; }
        }
        return s;
    }""")),
]

MUTANTS["fibonacci-number"] = [
    ("off-by-one", "reject", S("""
    public int fib(int n) { int a = 0, b = 1; for (int i = 0; i <= n; i++) { int t = a + b; a = b; b = t; } return a; }""")),
    ("naive-recursion", "accept", S("""
    public int fib(int n) { return n < 2 ? n : fib(n - 1) + fib(n - 2); }""")),
]

MUTANTS["n-th-tribonacci-number"] = [
    ("wrong-seeds", "reject", S("""
    public int tribonacci(int n) { int a = 0, b = 0, c = 1; for (int i = 0; i < n; i++) { int t = a + b + c; a = b; b = c; c = t; } return a; }""")),
    ("memo", "accept", S("""
    int[] memo = new int[38];
    public int tribonacci(int n) {
        if (n == 0) return 0; if (n <= 2) return 1; if (memo[n] != 0) return memo[n];
        return memo[n] = tribonacci(n - 1) + tribonacci(n - 2) + tribonacci(n - 3);
    }""")),
]

MUTANTS["min-cost-climbing-stairs"] = [
    ("answer-at-last-step", "reject", S("""
    public int minCostClimbingStairs(int[] c) {
        int n = c.length; int[] d = new int[n]; d[0] = c[0]; d[1] = c[1];
        for (int i = 2; i < n; i++) d[i] = c[i] + Math.min(d[i - 1], d[i - 2]); return d[n - 1];
    }""")),
    ("pay-on-landing", "accept", S("""
    public int minCostClimbingStairs(int[] c) {
        int n = c.length; int[] d = new int[n]; d[0] = c[0]; d[1] = c[1];
        for (int i = 2; i < n; i++) d[i] = c[i] + Math.min(d[i - 1], d[i - 2]); return Math.min(d[n - 1], d[n - 2]);
    }""")),
]

MUTANTS["power-of-two"] = [
    ("bitcount-no-sign-check", "reject", S("""
    public boolean isPowerOfTwo(int n) { return Integer.bitCount(n) == 1; }""")),
    ("and-trick-no-sign-check", "reject", S("""
    public boolean isPowerOfTwo(int n) { return n != 0 && (n & (n - 1)) == 0; }""")),
    ("divide-loop", "accept", S("""
    public boolean isPowerOfTwo(int n) { if (n <= 0) return false; while (n % 2 == 0) n /= 2; return n == 1; }""")),
]

MUTANTS["assign-cookies"] = [
    ("no-sort", "reject", S("""
    public int findContentChildren(int[] g, int[] s) {
        int k = 0; for (int c = 0; c < s.length && k < g.length; c++) if (s[c] >= g[k]) k++; return k;
    }""")),
    ("greediest-first-largest-cookie", "accept", S("""
    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(g); Arrays.sort(s); int i = g.length - 1, j = s.length - 1, k = 0;
        for (; i >= 0; i--) if (j >= 0 && s[j] >= g[i]) { j--; k++; } return k;
    }""", LIST)),
]

MUTANTS["lemonade-change"] = [
    ("fives-before-ten", "reject", S("""
    public boolean lemonadeChange(int[] bills) {
        int f = 0, t = 0;
        for (int b : bills) {
            if (b == 5) f++; else if (b == 10) { if (f == 0) return false; f--; t++; }
            else { if (f >= 3) f -= 3; else if (t > 0 && f > 0) { t--; f--; } else return false; }
        }
        return true;
    }""")),
    ("counts-total-money", "reject", S("""
    public boolean lemonadeChange(int[] bills) {
        int cash = 0; for (int b : bills) { if (b - 5 > cash) return false; cash += 5; } return true;
    }""")),
    ("same-greedy-if-chain", "accept", S("""
    public boolean lemonadeChange(int[] bills) {
        int f = 0, t = 0;
        for (int b : bills) {
            if (b == 5) { f++; continue; }
            if (b == 10) { t++; f--; } else if (t > 0) { t--; f--; } else f -= 3;
            if (f < 0) return false;
        }
        return true;
    }""")),
]

MUTANTS["last-stone-weight"] = [
    ("min-heap", "reject", S("""
    public int lastStoneWeight(int[] s) {
        PriorityQueue<Integer> q = new PriorityQueue<>(); for (int x : s) q.add(x);
        while (q.size() > 1) { int a = q.poll(), b = q.poll(); if (a != b) q.add(Math.abs(a - b)); }
        return q.isEmpty() ? 0 : q.peek();
    }""", LIST)),
    ("resort-each-round", "accept", S("""
    public int lastStoneWeight(int[] s) {
        List<Integer> l = new ArrayList<>(); for (int x : s) l.add(x);
        while (l.size() > 1) { Collections.sort(l); int y = l.remove(l.size() - 1), x = l.remove(l.size() - 1); if (y != x) l.add(y - x); }
        return l.isEmpty() ? 0 : l.get(0);
    }""", LIST)),
]
