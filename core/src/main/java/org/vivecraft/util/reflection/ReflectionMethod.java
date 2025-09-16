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


    public static ReflectionMethod getMethod(MethodMapping mapping) {
        return getMethod(mapping, true);
    }

    public static ReflectionMethod getMethod(MethodMapping mapping, boolean critical) {
        // get the matching filed with the closest matching version, preferring older ones, unless there is none
        MCVersion mc = MCVersion.getCurrent();
        int major = mc.major;
        int minor = mc.minor;
        Method m = null;
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

    public static ReflectionMethod getWithApi(String pre, String post, String methodName, Class<?>... args) {
        try {
            Class<?> c = ClassGetter.getWithApi(pre, post);
            return getMethod(c, methodName, args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("couldn't find any method matching " + pre + ".###." + post + "." + methodName,
                e);
        }
    }

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
