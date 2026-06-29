package org.vivecraft.util.reflection;

import me.kcra.takenaka.accessor.mapping.MethodMapping;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.MCVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * reflection handler that catches any exceptions on the calls
 */
public class ReflectionMethod {
    public final Method method;

    private ReflectionMethod(Method method) {
        this.method = method;
        // make sure it is accessible
        this.method.setAccessible(true);
    }

    public static ReflectionMethod getMethod(
        Class<?> cls, String fieldName, Class<?>... args) throws NoSuchMethodException
    {
        return new ReflectionMethod(cls.getDeclaredMethod(fieldName, args));
    }

    /**
     * Tries to find any reflection method matching the given mappings
     *
     * @param mappings one or multiple mappings to try, when supplying multiple, they need to be given in order new > old
     * @return found reflection method
     * @throws RuntimeException When no matching method is found
     */
    public static ReflectionMethod getMethod(MethodMapping... mappings) {
        return getMethod(true, mappings);
    }

    /**
     * Tries to find any reflection method matching the given mappings
     *
     * @param critical when true, this will fail with an exception, instead of returning {@code null}
     * @param mappings one or multiple mappings to try
     * @return found reflection method, or {@code null}, if not found and {@code critical} is false
     * @throws RuntimeException When no matching method is found and {@code critical} is true
     */
    public static ReflectionMethod getMethod(boolean critical, MethodMapping... mappings) {
        // get the matching method with the closest matching version, preferring older ones, unless there is none
        Method m = null;
        // need to also try mojang, because of paper
        for (String namespace : new String[]{"spigot", "mojang"}) {
            m = getMethod(namespace, mappings);
            if (m != null) break;
        }

        if (m == null) {
            // if it is still null we don't support it yet
            if (critical) {
                throw new RuntimeException(
                    "Unsupported mc version: " + MCVersion.getCurrent() + ", no mapping found for: " +
                        mappings[0].getParent().getName() + "." + mappings[0].getName());
            } else {
                return null;
            }
        }
        return new ReflectionMethod(m);
    }

    @Nullable
    private static Method getMethod(String namespace, MethodMapping... mappings) {
        // get the matching method with the closest matching version, preferring older ones, unless there is none
        MCVersion mc = MCVersion.getCurrentCorrected();
        Method m = null;
        for (MethodMapping mapping : mappings) {
            int major = mc.minor;
            int minor = mc.patch;
            while (major > 7 && m == null) {
                while (minor >= 0 && m == null) {
                    if (minor == 0) {
                        m = mapping.getMethod("1." + major, namespace);
                    } else {
                        m = mapping.getMethod("1." + major + "." + minor, namespace);
                    }
                    minor--;
                }
                minor = 10;
                major--;
            }
            if (m == null && mc.minor <= 8) {
                // get 1.8.8 in this case, that is the oldest mapping that takenaka supports
                m = mapping.getMethod("1.8.8", namespace);
            }
        }
        return m;
    }

    /**
     * Tries to find any reflection method matching the given class paths
     *
     * @param pre        path before the spigot api part
     * @param post       path after the spigot api part
     * @param methodName name of the method
     * @param critical   if this method is needed, if this is false null is returned if it is missing
     * @param args       Classes of the arguments for the method
     * @return found reflection method
     * @throws RuntimeException When no matching method is found and critical is true
     */
    public static ReflectionMethod getWithApi(
        String pre, String post, String methodName, boolean critical, Class<?>... args)
    {
        try {
            Class<?> c = ClassGetter.getWithApi(pre, post);
            return getMethod(c, methodName, args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            if (critical) {
                throw new RuntimeException(
                    "couldn't find any method matching " + pre + ".###." + post + "." + methodName + " with args: " +
                        Arrays.toString(args),
                    e);
            } else {
                return null;
            }
        }
    }

    /**
     * Tries to find the reflection method matching the given class path
     *
     * @param cls        path of the containing class
     * @param methodName name of the method
     * @param critical   if true will throw an exception when not found, else returns null
     * @param args       Classes of the arguments for the method
     * @return found reflection method
     * @throws RuntimeException When no matching method is found
     */
    public static ReflectionMethod getRaw(String cls, String methodName, boolean critical, Class<?>... args) {
        try {
            return getRaw(ClassGetter.getRaw(cls), methodName, critical, args);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "couldn't find method " + cls + "." + methodName + " with args: " + Arrays.toString(args), e);
        }
    }

    /**
     * Tries to find the reflection method matching the given class path
     *
     * @param cls        containing class
     * @param methodName name of the method
     * @param critical   if true will throw an exception when not found, else returns null
     * @param args       Classes of the arguments for the method
     * @return found reflection method
     * @throws RuntimeException When no matching method is found
     */
    public static ReflectionMethod getRaw(Class<?> cls, String methodName, boolean critical, Class<?>... args) {
        try {
            return getMethod(cls, methodName, args);
        } catch (NoSuchMethodException e) {
            if (!critical) {
                Debug.log("couldn't find method %s.%s with args: %s", cls, methodName, Arrays.toString(args));
                return null;
            }
            throw new RuntimeException(
                "couldn't find method " + cls + "." + methodName + " with args: " + Arrays.toString(args), e);
        }
    }

    public Object invokes(Object... args) {
        try {
            return this.method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't invoke static method " + this.method.getName() + " on: " +
                    this.method.getDeclaringClass().getName(),
                e);
            return null;
        }
    }

    public Object invoke(Object target, Object... args) {
        try {
            return this.method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't invoke method " + this.method.getName() + " on: " + target.getClass().getName(), e);
            return null;
        }
    }
}
