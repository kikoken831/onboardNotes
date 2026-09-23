import java.util.ArrayList;
import java.util.List;

/**
 * LeetCode's graph node (LC 133 Clone Graph). Provided, exactly as on leetcode.com --
 * do not declare it in your solution.
 *
 * It lives outside harness/ on purpose and is put on the classpath only for problems
 * that use it. A global `Node` would silently replace the `class Node` that half of
 * all trie solutions declare for themselves.
 */
public class Node {
    public int val;
    public List<Node> neighbors;

    public Node() { val = 0; neighbors = new ArrayList<>(); }
    public Node(int _val) { val = _val; neighbors = new ArrayList<>(); }
    public Node(int _val, ArrayList<Node> _neighbors) { val = _val; neighbors = _neighbors; }
}
