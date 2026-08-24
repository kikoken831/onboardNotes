/**
 * Implemented by every reference solution in .ref/&lt;slug&gt;/Ref.java.
 *
 * The Ref class carries two things: the test specification, and a known-good
 * implementation of the same method the Solution class must implement. The judge
 * invokes both by reflection and compares.
 */
public interface Problem {
    Spec spec();
}
