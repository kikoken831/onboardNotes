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

# --------------------------------------------------------------- group D: trees

MUTANTS["binary-tree-inorder-traversal"] = [
    ("actually-preorder", "reject", S("""
    public List<Integer> inorderTraversal(TreeNode r) { List<Integer> o = new ArrayList<>(); go(r, o); return o; }
    void go(TreeNode n, List<Integer> o) { if (n == null) return; o.add(n.val); go(n.left, o); go(n.right, o); }""", LIST)),
    ("iterative-left-spine", "accept", S("""
    public List<Integer> inorderTraversal(TreeNode r) {
        List<Integer> o = new ArrayList<>(); Deque<TreeNode> s = new ArrayDeque<>(); TreeNode c = r;
        while (c != null || !s.isEmpty()) { while (c != null) { s.push(c); c = c.left; } c = s.pop(); o.add(c.val); c = c.right; }
        return o;
    }""", LIST)),
]

MUTANTS["binary-tree-preorder-traversal"] = [
    ("left-pushed-first", "reject", S("""
    public List<Integer> preorderTraversal(TreeNode r) {
        List<Integer> o = new ArrayList<>(); Deque<TreeNode> s = new ArrayDeque<>(); if (r != null) s.push(r);
        while (!s.isEmpty()) { TreeNode n = s.pop(); o.add(n.val); if (n.left != null) s.push(n.left); if (n.right != null) s.push(n.right); }
        return o;
    }""", LIST)),
    ("recursive", "accept", S("""
    public List<Integer> preorderTraversal(TreeNode r) { List<Integer> o = new ArrayList<>(); go(r, o); return o; }
    void go(TreeNode n, List<Integer> o) { if (n == null) return; o.add(n.val); go(n.left, o); go(n.right, o); }""", LIST)),
]

MUTANTS["binary-tree-postorder-traversal"] = [
    ("reversed-preorder-not-mirrored", "reject", S("""
    public List<Integer> postorderTraversal(TreeNode r) {
        List<Integer> o = new ArrayList<>(); Deque<TreeNode> s = new ArrayDeque<>(); if (r != null) s.push(r);
        while (!s.isEmpty()) { TreeNode n = s.pop(); o.add(n.val); if (n.right != null) s.push(n.right); if (n.left != null) s.push(n.left); }
        Collections.reverse(o); return o;
    }""", LIST)),
    ("mirrored-preorder-reversed", "accept", S("""
    public List<Integer> postorderTraversal(TreeNode r) {
        List<Integer> o = new ArrayList<>(); Deque<TreeNode> s = new ArrayDeque<>(); if (r != null) s.push(r);
        while (!s.isEmpty()) { TreeNode n = s.pop(); o.add(n.val); if (n.left != null) s.push(n.left); if (n.right != null) s.push(n.right); }
        Collections.reverse(o); return o;
    }""", LIST)),
]

MUTANTS["diameter-of-binary-tree"] = [
    ("through-root-only", "reject", S("""
    public int diameterOfBinaryTree(TreeNode r) { return h(r.left) + h(r.right); }
    int h(TreeNode n) { return n == null ? 0 : 1 + Math.max(h(n.left), h(n.right)); }""")),
    ("counts-nodes", "reject", S("""
    int best = 0;
    public int diameterOfBinaryTree(TreeNode r) { best = 0; h(r); return best; }
    int h(TreeNode n) { if (n == null) return 0; int l = h(n.left), rr = h(n.right); best = Math.max(best, l + rr + 1); return 1 + Math.max(l, rr); }""")),
    ("quadratic-per-node", "accept", S("""
    public int diameterOfBinaryTree(TreeNode r) {
        if (r == null) return 0;
        return Math.max(h(r.left) + h(r.right), Math.max(diameterOfBinaryTree(r.left), diameterOfBinaryTree(r.right)));
    }
    int h(TreeNode n) { return n == null ? 0 : 1 + Math.max(h(n.left), h(n.right)); }""")),
]

MUTANTS["balanced-binary-tree"] = [
    ("root-only", "reject", S("""
    public boolean isBalanced(TreeNode r) { return r == null || Math.abs(h(r.left) - h(r.right)) <= 1; }
    int h(TreeNode n) { return n == null ? 0 : 1 + Math.max(h(n.left), h(n.right)); }""")),
    ("top-down-recompute", "accept", S("""
    public boolean isBalanced(TreeNode r) {
        return r == null || (Math.abs(h(r.left) - h(r.right)) <= 1 && isBalanced(r.left) && isBalanced(r.right));
    }
    int h(TreeNode n) { return n == null ? 0 : 1 + Math.max(h(n.left), h(n.right)); }""")),
]

MUTANTS["symmetric-tree"] = [
    ("not-crossed", "reject", S("""
    public boolean isSymmetric(TreeNode r) { return m(r.left, r.right); }
    boolean m(TreeNode a, TreeNode b) { if (a == null || b == null) return a == b; return a.val == b.val && m(a.left, b.left) && m(a.right, b.right); }""")),
    ("level-palindrome-ignores-nulls", "reject", S("""
    public boolean isSymmetric(TreeNode r) {
        List<TreeNode> lvl = new ArrayList<>(); lvl.add(r);
        while (!lvl.isEmpty()) {
            List<Integer> v = new ArrayList<>(); List<TreeNode> nx = new ArrayList<>();
            for (TreeNode n : lvl) { v.add(n.val); if (n.left != null) nx.add(n.left); if (n.right != null) nx.add(n.right); }
            for (int i = 0, j = v.size() - 1; i < j; i++, j--) if (!v.get(i).equals(v.get(j))) return false;
            lvl = nx;
        }
        return true;
    }""", LIST)),
    ("iterative-pairs", "accept", S("""
    public boolean isSymmetric(TreeNode r) {
        Deque<TreeNode[]> q = new ArrayDeque<>(); q.add(new TreeNode[]{ r.left, r.right });
        while (!q.isEmpty()) {
            TreeNode[] p = q.poll(); TreeNode a = p[0], b = p[1];
            if (a == null && b == null) continue; if (a == null || b == null || a.val != b.val) return false;
            q.add(new TreeNode[]{ a.left, b.right }); q.add(new TreeNode[]{ a.right, b.left });
        }
        return true;
    }""", LIST)),
]

MUTANTS["search-in-a-binary-search-tree"] = [
    ("wrong-direction", "reject", S("""
    public TreeNode searchBST(TreeNode n, int v) { while (n != null && n.val != v) n = v > n.val ? n.left : n.right; return n; }""")),
    ("returns-copy-of-node-only", "reject", S("""
    public TreeNode searchBST(TreeNode n, int v) { while (n != null && n.val != v) n = v < n.val ? n.left : n.right; return n == null ? null : new TreeNode(n.val); }""")),
    ("full-dfs", "accept", S("""
    public TreeNode searchBST(TreeNode n, int v) { if (n == null || n.val == v) return n; TreeNode l = searchBST(n.left, v); return l != null ? l : searchBST(n.right, v); }""")),
]

MUTANTS["range-sum-of-bst"] = [
    ("exclusive-bounds", "reject", S("""
    public int rangeSumBST(TreeNode n, int lo, int hi) { if (n == null) return 0; return (n.val > lo && n.val < hi ? n.val : 0) + rangeSumBST(n.left, lo, hi) + rangeSumBST(n.right, lo, hi); }""")),
    ("prunes-wrong-side", "reject", S("""
    public int rangeSumBST(TreeNode n, int lo, int hi) {
        if (n == null) return 0; if (n.val < lo) return rangeSumBST(n.left, lo, hi); if (n.val > hi) return rangeSumBST(n.right, lo, hi);
        return n.val + rangeSumBST(n.left, lo, hi) + rangeSumBST(n.right, lo, hi);
    }""")),
    ("visit-everything", "accept", S("""
    public int rangeSumBST(TreeNode n, int lo, int hi) { if (n == null) return 0; return (n.val >= lo && n.val <= hi ? n.val : 0) + rangeSumBST(n.left, lo, hi) + rangeSumBST(n.right, lo, hi); }""")),
]

# ------------------------------------------------- group E/F: grids, graphs, matrix

MUTANTS["island-perimeter"] = [
    ("ignores-grid-edge", "reject", S("""
    public int islandPerimeter(int[][] g) {
        int p = 0, r = g.length, c = g[0].length;
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) if (g[i][j] == 1) {
            if (i > 0 && g[i-1][j] == 0) p++; if (i < r-1 && g[i+1][j] == 0) p++;
            if (j > 0 && g[i][j-1] == 0) p++; if (j < c-1 && g[i][j+1] == 0) p++;
        }
        return p;
    }""")),
    ("counts-each-adjacency-twice", "reject", S("""
    public int islandPerimeter(int[][] g) {
        int land = 0, sh = 0, r = g.length, c = g[0].length;
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) if (g[i][j] == 1) {
            land++; if (i > 0 && g[i-1][j] == 1) sh++; if (i < r-1 && g[i+1][j] == 1) sh++;
            if (j > 0 && g[i][j-1] == 1) sh++; if (j < c-1 && g[i][j+1] == 1) sh++;
        }
        return 4 * land - 2 * sh;
    }""")),
    ("dfs-edges-to-water", "accept", S("""
    public int islandPerimeter(int[][] g) {
        for (int i = 0; i < g.length; i++) for (int j = 0; j < g[0].length; j++) if (g[i][j] == 1) return dfs(g, i, j);
        return 0;
    }
    int dfs(int[][] g, int i, int j) {
        if (i < 0 || j < 0 || i >= g.length || j >= g[0].length || g[i][j] == 0) return 1;
        if (g[i][j] == 2) return 0; g[i][j] = 2;
        return dfs(g, i+1, j) + dfs(g, i-1, j) + dfs(g, i, j+1) + dfs(g, i, j-1);
    }""")),
]

MUTANTS["flood-fill"] = [
    ("no-same-colour-guard", "reject", S("""
    public int[][] floodFill(int[][] a, int sr, int sc, int color) { go(a, sr, sc, a[sr][sc], color); return a; }
    void go(int[][] a, int i, int j, int from, int to) {
        if (i < 0 || j < 0 || i >= a.length || j >= a[0].length || a[i][j] != from) return;
        a[i][j] = to; go(a, i+1, j, from, to); go(a, i-1, j, from, to); go(a, i, j+1, from, to); go(a, i, j-1, from, to);
    }""")),
    ("spreads-diagonally", "reject", S("""
    public int[][] floodFill(int[][] a, int sr, int sc, int color) { if (a[sr][sc] != color) go(a, sr, sc, a[sr][sc], color); return a; }
    void go(int[][] a, int i, int j, int from, int to) {
        if (i < 0 || j < 0 || i >= a.length || j >= a[0].length || a[i][j] != from) return;
        a[i][j] = to; for (int di = -1; di <= 1; di++) for (int dj = -1; dj <= 1; dj++) if (di != 0 || dj != 0) go(a, i+di, j+dj, from, to);
    }""")),
    ("dfs-with-guard", "accept", S("""
    public int[][] floodFill(int[][] a, int sr, int sc, int color) { if (a[sr][sc] != color) go(a, sr, sc, a[sr][sc], color); return a; }
    void go(int[][] a, int i, int j, int from, int to) {
        if (i < 0 || j < 0 || i >= a.length || j >= a[0].length || a[i][j] != from) return;
        a[i][j] = to; go(a, i+1, j, from, to); go(a, i-1, j, from, to); go(a, i, j+1, from, to); go(a, i, j-1, from, to);
    }""")),
]

MUTANTS["find-if-path-exists-in-graph"] = [
    ("directed-edges", "reject", S("""
    public boolean validPath(int n, int[][] e, int s, int d) {
        List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] x : e) g.get(x[0]).add(x[1]);
        boolean[] seen = new boolean[n]; Deque<Integer> q = new ArrayDeque<>(); q.add(s); seen[s] = true;
        while (!q.isEmpty()) { int u = q.poll(); if (u == d) return true; for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; q.add(v); } }
        return false;
    }""", LIST)),
    ("source-equals-dest-false", "reject", S("""
    public boolean validPath(int n, int[][] e, int s, int d) {
        List<List<Integer>> g = new ArrayList<>(); for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] x : e) { g.get(x[0]).add(x[1]); g.get(x[1]).add(x[0]); }
        boolean[] seen = new boolean[n]; Deque<Integer> q = new ArrayDeque<>(); q.add(s); seen[s] = true;
        while (!q.isEmpty()) { int u = q.poll(); for (int v : g.get(u)) { if (v == d) return true; if (!seen[v]) { seen[v] = true; q.add(v); } } }
        return false;
    }""", LIST)),
    ("union-find", "accept", S("""
    int[] p;
    int find(int x) { while (p[x] != x) x = p[x] = p[p[x]]; return x; }
    public boolean validPath(int n, int[][] e, int s, int d) {
        p = new int[n]; for (int i = 0; i < n; i++) p[i] = i;
        for (int[] x : e) p[find(x[0])] = find(x[1]);
        return find(s) == find(d);
    }""")),
]

MUTANTS["find-the-town-judge"] = [
    ("in-degree-only", "reject", S("""
    public int findJudge(int n, int[][] t) {
        int[] in = new int[n + 1]; for (int[] x : t) in[x[1]]++;
        for (int p = 1; p <= n; p++) if (in[p] == n - 1) return p; return -1;
    }""")),
    ("n-equals-1-returns-minus-1", "reject", S("""
    public int findJudge(int n, int[][] t) {
        if (t.length == 0) return -1; int[] s = new int[n + 1]; for (int[] x : t) { s[x[0]]--; s[x[1]]++; }
        for (int p = 1; p <= n; p++) if (s[p] == n - 1) return p; return -1;
    }""")),
    ("two-arrays", "accept", S("""
    public int findJudge(int n, int[][] t) {
        int[] in = new int[n + 1], out = new int[n + 1]; for (int[] x : t) { out[x[0]]++; in[x[1]]++; }
        for (int p = 1; p <= n; p++) if (in[p] == n - 1 && out[p] == 0) return p; return -1;
    }""")),
]

MUTANTS["flipping-an-image"] = [
    ("middle-inverted-twice", "reject", S("""
    public int[][] flipAndInvertImage(int[][] a) {
        int n = a.length;
        for (int[] r : a) for (int i = 0, j = n - 1; i <= j; i++, j--) { int t = r[i] ^ 1; r[i] = r[j] ^ 1; r[j] = t; if (i == j) r[i] ^= 1; }
        return a;
    }""")),
    ("flip-only", "reject", S("""
    public int[][] flipAndInvertImage(int[][] a) {
        int n = a.length; for (int[] r : a) for (int i = 0, j = n - 1; i < j; i++, j--) { int t = r[i]; r[i] = r[j]; r[j] = t; } return a;
    }""")),
    ("in-place-swap-invert", "accept", S("""
    public int[][] flipAndInvertImage(int[][] a) {
        int n = a.length; for (int[] r : a) for (int i = 0, j = n - 1; i <= j; i++, j--) { int t = r[i] ^ 1; r[i] = r[j] ^ 1; r[j] = t; } return a;
    }""")),
]

MUTANTS["transpose-matrix"] = [
    ("square-in-place-swap", "reject", S("""
    public int[][] transpose(int[][] a) {
        int m = a.length, n = a[0].length, k = Math.min(m, n);
        for (int i = 0; i < k; i++) for (int j = i + 1; j < k; j++) { int t = a[i][j]; a[i][j] = a[j][i]; a[j][i] = t; } return a;
    }""")),
    ("streams", "accept", S("""
    public int[][] transpose(int[][] a) {
        int m = a.length, n = a[0].length; int[][] t = new int[n][m];
        for (int j = 0; j < n; j++) for (int i = 0; i < m; i++) t[j][i] = a[i][j]; return t;
    }""")),
]

MUTANTS["reshape-the-matrix"] = [
    ("wrong-divisor", "reject", S("""
    public int[][] matrixReshape(int[][] a, int r, int c) {
        int m = a.length, n = a[0].length; if (r * c != m * n) return a; int[][] o = new int[r][c];
        for (int k = 0; k < m * n; k++) o[k / c][k % c] = a[k / c % m][k % n]; return o;
    }""")),
    ("returns-null-when-impossible", "reject", S("""
    public int[][] matrixReshape(int[][] a, int r, int c) {
        int m = a.length, n = a[0].length; if (r * c != m * n) return null; int[][] o = new int[r][c];
        for (int k = 0; k < m * n; k++) o[k / c][k % c] = a[k / n][k % n]; return o;
    }""")),
    ("queue-of-values", "accept", S("""
    public int[][] matrixReshape(int[][] a, int r, int c) {
        int m = a.length, n = a[0].length; if (r * c != m * n) return a;
        Deque<Integer> q = new ArrayDeque<>(); for (int[] row : a) for (int x : row) q.add(x);
        int[][] o = new int[r][c]; for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) o[i][j] = q.poll(); return o;
    }""", LIST)),
]

MUTANTS["richest-customer-wealth"] = [
    ("sums-columns", "reject", S("""
    public int maximumWealth(int[][] a) {
        int best = 0; for (int j = 0; j < a[0].length; j++) { int s = 0; for (int[] r : a) s += r[j]; best = Math.max(best, s); } return best;
    }""")),
    ("streams", "accept", S("""
    public int maximumWealth(int[][] a) { return Arrays.stream(a).mapToInt(r -> Arrays.stream(r).sum()).max().getAsInt(); }""", LIST)),
]

# ------------------------------------------------ group G: design and interactive
# Design mutants are the class itself (class MyQueue ...), not a Solution.


def D(src):
    return LIST + "\n" + src + "\n"


MUTANTS["implement-queue-using-stacks"] = [
    ("transfers-while-out-nonempty", "reject", D("""
class MyQueue {
    Deque<Integer> in = new ArrayDeque<>(), out = new ArrayDeque<>();
    public void push(int x) { in.push(x); }
    public int pop()  { while (!in.isEmpty()) out.push(in.pop()); return out.pop(); }
    public int peek() { while (!in.isEmpty()) out.push(in.pop()); return out.peek(); }
    public boolean empty() { return in.isEmpty() && out.isEmpty(); }
}""")),
    ("empty-checks-one-stack", "reject", D("""
class MyQueue {
    Deque<Integer> in = new ArrayDeque<>(), out = new ArrayDeque<>();
    public void push(int x) { in.push(x); }
    void sh() { if (out.isEmpty()) while (!in.isEmpty()) out.push(in.pop()); }
    public int pop()  { sh(); return out.pop(); }
    public int peek() { sh(); return out.peek(); }
    public boolean empty() { return in.isEmpty(); }
}""")),
    ("transfer-back-each-time", "accept", D("""
class MyQueue {
    Deque<Integer> s = new ArrayDeque<>(), t = new ArrayDeque<>();
    public void push(int x) { while (!s.isEmpty()) t.push(s.pop()); s.push(x); while (!t.isEmpty()) s.push(t.pop()); }
    public int pop() { return s.pop(); }
    public int peek() { return s.peek(); }
    public boolean empty() { return s.isEmpty(); }
}""")),
]

MUTANTS["implement-stack-using-queues"] = [
    ("fifo-not-lifo", "reject", D("""
class MyStack {
    Queue<Integer> q = new ArrayDeque<>();
    public void push(int x) { q.add(x); }
    public int pop() { return q.poll(); }
    public int top() { return q.peek(); }
    public boolean empty() { return q.isEmpty(); }
}""")),
    ("rotates-once-too-many", "reject", D("""
class MyStack {
    Queue<Integer> q = new ArrayDeque<>();
    public void push(int x) { q.add(x); for (int i = 0; i < q.size(); i++) q.add(q.poll()); }
    public int pop() { return q.poll(); }
    public int top() { return q.peek(); }
    public boolean empty() { return q.isEmpty(); }
}""")),
    ("two-queues-costly-pop", "accept", D("""
class MyStack {
    Queue<Integer> a = new ArrayDeque<>(), b = new ArrayDeque<>();
    public void push(int x) { a.add(x); }
    public int pop() { while (a.size() > 1) b.add(a.poll()); int v = a.poll(); Queue<Integer> t = a; a = b; b = t; return v; }
    public int top() { int v = pop(); a.add(v); return v; }
    public boolean empty() { return a.isEmpty(); }
}""")),
]

MUTANTS["number-of-recent-calls"] = [
    ("exclusive-window", "reject", D("""
class RecentCounter {
    Deque<Integer> q = new ArrayDeque<>();
    public int ping(int t) { q.add(t); while (q.peek() <= t - 3000) q.poll(); return q.size(); }
}""")),
    ("counts-everything", "reject", D("""
class RecentCounter {
    int n = 0;
    public int ping(int t) { return ++n; }
}""")),
    ("list-binary-search", "accept", D("""
class RecentCounter {
    List<Integer> l = new ArrayList<>();
    public int ping(int t) {
        l.add(t); int lo = 0, hi = l.size() - 1;
        while (lo < hi) { int m = (lo + hi) / 2; if (l.get(m) < t - 3000) lo = m + 1; else hi = m; }
        return l.size() - lo;
    }
}""")),
]

MUTANTS["kth-largest-element-in-a-stream"] = [
    ("max-heap-returns-largest", "reject", D("""
class KthLargest {
    PriorityQueue<Integer> h = new PriorityQueue<>(Collections.reverseOrder());
    public KthLargest(int k, int[] nums) { for (int x : nums) h.add(x); }
    public int add(int v) { h.add(v); return h.peek(); }
}""")),
    ("heap-size-k-plus-1", "reject", D("""
class KthLargest {
    PriorityQueue<Integer> h = new PriorityQueue<>(); int k;
    public KthLargest(int k, int[] nums) { this.k = k; for (int x : nums) { h.add(x); if (h.size() > k + 1) h.poll(); } }
    public int add(int v) { h.add(v); if (h.size() > k + 1) h.poll(); return h.peek(); }
}""")),
    ("sort-every-add", "accept", D("""
class KthLargest {
    List<Integer> l = new ArrayList<>(); int k;
    public KthLargest(int k, int[] nums) { this.k = k; for (int x : nums) l.add(x); }
    public int add(int v) { l.add(v); l.sort(Collections.reverseOrder()); return l.get(k - 1); }
}""")),
]

MUTANTS["design-parking-system"] = [
    ("big-can-take-any-car", "reject", D("""
class ParkingSystem {
    int[] f = new int[4];
    public ParkingSystem(int b, int m, int s) { f[1] = b; f[2] = m; f[3] = s; }
    public boolean addCar(int c) { for (int t = c; t >= 1; t--) if (f[t] > 0) { f[t]--; return true; } return false; }
}""")),
    ("never-decrements", "reject", D("""
class ParkingSystem {
    int[] f = new int[4];
    public ParkingSystem(int b, int m, int s) { f[1] = b; f[2] = m; f[3] = s; }
    public boolean addCar(int c) { return f[c] > 0; }
}""")),
    ("three-fields", "accept", D("""
class ParkingSystem {
    int b, m, s;
    public ParkingSystem(int b, int m, int s) { this.b = b; this.m = m; this.s = s; }
    public boolean addCar(int c) {
        if (c == 1) return b-- > 0; if (c == 2) return m-- > 0; return s-- > 0;
    }
}""")),
]

MUTANTS["design-hashset"] = [
    ("array-one-too-small", "reject", D("""
class MyHashSet {
    boolean[] a = new boolean[1_000_000];
    public void add(int k) { a[k] = true; }
    public void remove(int k) { a[k] = false; }
    public boolean contains(int k) { return a[k]; }
}""")),
    ("remove-only-first-bucket-entry", "reject", D("""
class MyHashSet {
    List<Integer>[] b = new List[16];
    List<Integer> at(int k) { int h = k % 16; if (b[h] == null) b[h] = new ArrayList<>(); return b[h]; }
    public void add(int k) { at(k).add(k); }
    public void remove(int k) { at(k).remove(Integer.valueOf(k)); }
    public boolean contains(int k) { return at(k).contains(k); }
}""")),
    ("direct-address", "accept", D("""
class MyHashSet {
    boolean[] a = new boolean[1_000_001];
    public void add(int k) { a[k] = true; }
    public void remove(int k) { a[k] = false; }
    public boolean contains(int k) { return a[k]; }
}""")),
]

MUTANTS["design-hashmap"] = [
    ("zero-means-absent", "reject", D("""
class MyHashMap {
    int[] a = new int[1_000_001];
    public void put(int k, int v) { a[k] = v; }
    public int get(int k) { return a[k] == 0 ? -1 : a[k]; }
    public void remove(int k) { a[k] = 0; }
}""")),
    ("put-appends-duplicates", "reject", D("""
class MyHashMap {
    List<int[]>[] b = new List[64];
    List<int[]> at(int k) { int h = k % 64; if (b[h] == null) b[h] = new ArrayList<>(); return b[h]; }
    public void put(int k, int v) { at(k).add(new int[]{ k, v }); }
    public int get(int k) { for (int[] e : at(k)) if (e[0] == k) return e[1]; return -1; }
    public void remove(int k) { for (int[] e : at(k)) if (e[0] == k) { at(k).remove(e); return; } }
}""")),
    ("direct-address-filled", "accept", D("""
class MyHashMap {
    int[] a = new int[1_000_001];
    MyHashMap() { Arrays.fill(a, -1); }
    public void put(int k, int v) { a[k] = v; }
    public int get(int k) { return a[k]; }
    public void remove(int k) { a[k] = -1; }
}""")),
]

MUTANTS["guess-number-higher-or-lower"] = [
    ("sign-inverted", "reject", D("""
public class Solution extends GuessGame {
    public int guessNumber(int n) {
        int lo = 1, hi = n;
        while (lo <= hi) { int m = lo + (hi - lo) / 2; int g = guess(m); if (g == 0) return m; if (g < 0) lo = m + 1; else hi = m - 1; }
        return -1;
    }
}""")),
    ("mid-overflows", "reject", D("""
public class Solution extends GuessGame {
    public int guessNumber(int n) {
        int lo = 1, hi = n;
        while (lo <= hi) { int m = (lo + hi) / 2; int g = guess(m); if (g == 0) return m; if (g < 0) hi = m - 1; else lo = m + 1; }
        return -1;
    }
}""")),
    ("closed-interval", "accept", D("""
public class Solution extends GuessGame {
    public int guessNumber(int n) {
        int lo = 1, hi = n;
        while (lo <= hi) { int m = lo + (hi - lo) / 2; int g = guess(m); if (g == 0) return m; if (g < 0) hi = m - 1; else lo = m + 1; }
        return -1;
    }
}""")),
]
