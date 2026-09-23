import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflective calls for Spec drivers (LC 297, 271), where the judge must call several
 * methods of YOUR class in a fixed order -- serialize, then deserialize -- rather than
 * one method on Solution.
 */
public final class Reflect {
    private Reflect() {}

    /** Call `name` on `target` with `args`, matching by name and arity. Rethrows the real cause. */
    public static Object call(Object target, String name, Object... args) throws Throwable {
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                try {
                    return m.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause() != null ? e.getCause() : e;
                }
            }
        }
        throw new NoSuchMethodException(target.getClass().getSimpleName() + "." + name
            + " with " + args.length + " parameter" + (args.length == 1 ? "" : "s"));
    }
}
