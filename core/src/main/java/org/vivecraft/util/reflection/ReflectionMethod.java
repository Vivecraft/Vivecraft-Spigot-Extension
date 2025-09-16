package org.vivecraft.util.reflection;

import me.kcra.takenaka.accessor.mapping.MethodMapping;
import org.vivecraft.ViveMain;
import org.vivecraft.util.MCVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * reflection handler that catches any exceptions on the calls
 */
public class ReflectionMethod {
    private final Method method;

    private ReflectionMethod(Method method) {
        this.method = method;
    }

    private static ReflectionMethod getMethod(
        Class<?> cls, String fieldName, Class<?>... args) throws NoSuchMethodException
    {
        return new ReflectionMethod(cls.getDeclaredMethod(fieldName, args));
    }

    /**
     * Tries to find any reflection method matching the given mappings
     *
     * @param mappings one or multiple mappings to try
     * @return found reflection method
     * @throws RuntimeException When no matching method is found
     */
    public static ReflectionMethod getMethod(MethodMapping... mappings) {
        return getMethod(true, mappings);
    }

    /**
     * Tries to find any reflection field matching the given mappings
     *
     * @param critical when true, if this method is required to function
     * @param mappings one or multiple mappings to try
     * @return found reflection method, or {@code null}, if not found and {@code critical} is false
     * @throws RuntimeException When no matching method is found and {@code critical} is true
     */
    public static ReflectionMethod getMethod(boolean critical, MethodMapping... mappings) {
        // get the matching filed with the closest matching version, preferring older ones, unless there is none
        MCVersion mc = MCVersion.getCurrent();
        Method m = null;
        for (MethodMapping mapping : mappings) {
            int major = mc.major;
            int minor = mc.minor;
            while (major > 7 && m == null) {
                while (minor >= 0 && m == null) {
                    if (minor == 0) {
                        m = mapping.getMethod("1." + major, "spigot");
                    } else {
                        m = mapping.getMethod("1." + major + "." + minor, "spigot");
                    }
                    minor--;
                }
                minor = 10;
                major--;
            }
            if (m == null && mc.major <= 8) {
                // get 1.8.8 in this case, that is the oldest mapping that takenaka supports
                m = mapping.getMethod("1.8.8", "spigot");
            }
        }
        if (m == null) {
            // if it is still null we don't support it yet
            if (critical) {
                throw new RuntimeException("Unsupported mc version: " + mc.version);
            } else {
                return null;
            }
        }
        return new ReflectionMethod(m);
    }

    /**
     * Tries to find any reflection method matching the given class paths
     *
     * @param pre        path before the spigot api part
     * @param post       path after the spigot api part
     * @param methodName name of the method
     * @param args       Classes of the arguments for the method
     * @return found reflection method
     * @throws RuntimeException When no matching method is found
     */
    public static ReflectionMethod getWithApi(String pre, String post, String methodName, Class<?>... args) {
        try {
            Class<?> c = ClassGetter.getWithApi(pre, post);
            return getMethod(c, methodName, args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("couldn't find any method matching " + pre + ".###." + post + "." + methodName,
                e);
        }
    }

    /**
     * Tries to find the reflection method matching the given class path
     *
     * @param cls        path of the containing class
     * @param methodName name of the method
     * @param args       Classes of the arguments for the method
     * @return found reflection method
     * @throws RuntimeException When no matching method is found
     */
    public static ReflectionMethod getRaw(String cls, String methodName, Class<?>... args) {
        try {
            return getMethod(ClassGetter.getRaw(cls), methodName, args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(
                "couldn't find method " + cls + "." + methodName + " with args: " + Arrays.toString(args), e);
        }
    }

    public Object invoke(Object target, Object... args) {
        try {
            return this.method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't invoke method " + this.method.getName() + " on: " + target.getClass().getName(), e);
            return null;
        }
    }
}
