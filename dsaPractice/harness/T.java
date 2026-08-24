/**
 * Value types the judge can marshal between LeetCode literal notation and Java.
 *
 * Everything in the harness lives in the default (unnamed) package on purpose: your
 * Solution.java must look exactly like it would on leetcode.com, with no package
 * declaration and no imports for ListNode/TreeNode.
 */
public enum T {
    VOID,
    INT, LONG, DOUBLE, BOOL, CHAR, STRING,
    INT_ARRAY, LONG_ARRAY, DOUBLE_ARRAY, BOOL_ARRAY, CHAR_ARRAY, STRING_ARRAY,
    INT_ARRAY_2D, CHAR_ARRAY_2D, STRING_ARRAY_2D,
    LIST_INT, LIST_DOUBLE, LIST_STRING, LIST_LIST_INT, LIST_LIST_STRING,
    LIST_NODE, TREE_NODE;

    /** The erased Java type, used to disambiguate overloaded methods during reflection lookup. */
    public Class<?> javaType() {
        switch (this) {
            case VOID:            return void.class;
            case INT:             return int.class;
            case LONG:            return long.class;
            case DOUBLE:          return double.class;
            case BOOL:            return boolean.class;
            case CHAR:            return char.class;
            case STRING:          return String.class;
            case INT_ARRAY:       return int[].class;
            case LONG_ARRAY:      return long[].class;
            case DOUBLE_ARRAY:    return double[].class;
            case BOOL_ARRAY:      return boolean[].class;
            case CHAR_ARRAY:      return char[].class;
            case STRING_ARRAY:    return String[].class;
            case INT_ARRAY_2D:    return int[][].class;
            case CHAR_ARRAY_2D:   return char[][].class;
            case STRING_ARRAY_2D: return String[][].class;
            case LIST_NODE:       return ListNode.class;
            case TREE_NODE:       return TreeNode.class;
            default:              return java.util.List.class;
        }
    }

    public boolean isList() {
        return this == LIST_INT || this == LIST_DOUBLE || this == LIST_STRING
            || this == LIST_LIST_INT || this == LIST_LIST_STRING;
    }
}
