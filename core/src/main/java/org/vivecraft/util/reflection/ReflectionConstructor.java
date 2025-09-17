package org.vivecraft.util.reflection;

import me.kcra.takenaka.accessor.mapping.ConstructorMapping;
import org.vivecraft.ViveMain;
import org.vivecraft.util.MCVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * reflection handler that catches any exceptions on the calls
 */
public class ReflectionConstructor {
    private final Constructor<?> constructor;

    private ReflectionConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    private static ReflectionConstructor getConstructor(Class<?> cls, Class<?>... args) throws NoSuchMethodException
    {
        return new ReflectionConstructor(cls.getConstructor(args));
    }

    /**
     * Tries to find any reflection constructor matching the given mappings
     *
     * @param mappings one or multiple mappings to try
     * @return found reflection constructor
     * @throws RuntimeException When no matching constructor is found
     */
    public static ReflectionConstructor getConstructor(ConstructorMapping... mappings) {
        return getConstructor(true, mappings);
    }

    /**
     * Tries to find any reflection constructor matching the given mappings
     *
     * @param critical when true, this will fail with an exception, instead of returning {@code null}
     * @param mappings one or multiple mappings to try
     * @return found reflection constructor, or {@code null}, if not found and {@code critical} is false
     * @throws RuntimeException When no matching constructor is found and {@code critical} is true
     */
    public static ReflectionConstructor getConstructor(boolean critical, ConstructorMapping... mappings) {
        // get the matching constructor with the closest matching version, preferring older ones, unless there is none
        MCVersion mc = MCVersion.getCurrent();
        Constructor<?> m = null;
        for (ConstructorMapping mapping : mappings) {
            int major = mc.major;
            int minor = mc.minor;
            while (major > 7 && m == null) {
                while (minor >= 0 && m == null) {
                    if (minor == 0) {
                        m = mapping.getConstructor("1." + major, "spigot");
                    } else {
                        m = mapping.getConstructor("1." + major + "." + minor, "spigot");
                    }
                    minor--;
                }
                minor = 10;
                major--;
            }
            if (m == null && mc.major <= 8) {
                // get 1.8.8 in this case, that is the oldest mapping that takenaka supports
                m = mapping.getConstructor("1.8.8", "spigot");
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
        return new ReflectionConstructor(m);
    }

    /**
     * Tries to find any reflection constructor matching the given class paths
     *
     * @param pre  path before the spigot api part
     * @param post path after the spigot api part
     * @param args Classes of the arguments for the constructor
     * @return found reflection constructor
     * @throws RuntimeException When no matching constructor is found
     */
    public static ReflectionConstructor getWithApi(String pre, String post, Class<?>... args) {
        try {
            Class<?> c = ClassGetter.getWithApi(pre, post);
            return getConstructor(c, args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(
                "couldn't find constructor " + pre + ".###." + post + " with args: " + Arrays.toString(args), e);
        }
    }

    /**
     * Tries to find the reflection constructor matching the given class path
     *
     * @param cls  path of the containing class
     * @param args Classes of the arguments for the constructor
     * @return found reflection constructor
     * @throws RuntimeException When no matching constructor is found
     */
    public static ReflectionConstructor getRaw(String cls, Class<?>... args) {
        try {
            return getConstructor(ClassGetter.getRaw(cls), args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("couldn't find constructor " + cls + " with args: " + Arrays.toString(args), e);
        }
    }

    public Object newInstance(Object... args) {
        try {
            return this.constructor.newInstance(args);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            ViveMain.LOGGER.log(Level.SEVERE, "couldn't create instance " + this.constructor.getClass().getName(), e);
            return null;
        }
    }
}
