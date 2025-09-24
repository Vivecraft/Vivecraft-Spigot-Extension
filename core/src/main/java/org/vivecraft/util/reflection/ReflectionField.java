package org.vivecraft.util.reflection;

import me.kcra.takenaka.accessor.mapping.FieldMapping;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.util.MCVersion;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * reflection handler that catches any exceptions on the calls
 */
public class ReflectionField {
    public final Field field;

    private ReflectionField(Field field) {
        this.field = field;
    }

    private static ReflectionField getField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return new ReflectionField(cls.getDeclaredField(fieldName));
    }

    /**
     * Tries to find any reflection field matching the given mappings
     *
     * @param mappings one or multiple mappings to try
     * @return found reflection field
     * @throws RuntimeException When no matching field is found
     */
    public static ReflectionField getField(FieldMapping... mappings) {
        return getField(true, mappings);
    }

    /**
     * Tries to find any reflection field matching the given mappings
     *
     * @param critical when true, this will fail with an exception, instead of returning {@code null}
     * @param mappings one or multiple mappings to try
     * @return found reflection field, or {@code null}, if not found and {@code critical} is false
     * @throws RuntimeException When no matching field is found and {@code critical} is true
     */
    public static ReflectionField getField(boolean critical, FieldMapping... mappings) {
        // get the matching field with the closest matching version, preferring older ones, unless there is none
        Field f = null;
        // need to also try mojang, because of paper
        for (String namespace : new String[]{"spigot", "mojang"}) {
            f = getField(namespace, mappings);
            if (f != null) break;
        }

        if (f == null) {
            // if it is still null we don't support it yet
            if (critical) {
                throw new RuntimeException(
                    "Unsupported mc version: " + MCVersion.getCurrent().version + ", no mapping found for: " +
                        mappings[0].getParent().getName() + "." + mappings[0].getName());
            } else {
                return null;
            }
        }

        // make sure it is accessible
        f.setAccessible(true);
        return new ReflectionField(f);
    }

    @Nullable
    private static Field getField(String namespace, FieldMapping... mappings) {
        MCVersion mc = MCVersion.getCurrent();
        Field f = null;
        for (FieldMapping mapping : mappings) {
            int major = mc.major;
            int minor = mc.minor;
            while (major > 7 && f == null) {
                while (minor >= 0 && f == null) {
                    if (minor == 0) {
                        f = mapping.getField("1." + major, namespace);
                    } else {
                        f = mapping.getField("1." + major + "." + minor, namespace);
                    }
                    minor--;
                }
                minor = 10;
                major--;
            }
            if (f == null && mc.major <= 8) {
                // get 1.8.8 in this case, that is the oldest mapping that takenaka supports
                f = mapping.getField("1.8.8", namespace);
            }
            if (f == null) {
                f = mapping.getField();
            }
            if (f != null) {
                break;
            }
        }
        return f;
    }

    /**
     * Tries to find any reflection field matching the given class paths
     *
     * @param pre       path before the spigot api part
     * @param post      path after the spigot api part
     * @param fieldName name of the field
     * @return found reflection field
     * @throws RuntimeException When no matching field is found
     */
    public static ReflectionField getWithApi(String pre, String post, String fieldName) {
        try {
            Class<?> c = ClassGetter.getWithApi(pre, post);
            return getField(c, fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(
                "couldn't find any field matching " + pre + ".###." + post + "." + fieldName);
        }
    }

    /**
     * Tries to find the reflection field matching the given class path
     *
     * @param cls       path of the containing class
     * @param fieldName name of the field
     * @return found reflection field
     * @throws RuntimeException When no matching field is found
     */
    public static ReflectionField getRaw(String cls, String fieldName, boolean required) {
        try {
            return getField(ClassGetter.getRaw(cls), fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            if (!required) {
                return null;
            }
            throw new RuntimeException("couldn't find field " + cls + "." + fieldName, e);
        }
    }

    public Object get() {
        try {
            return this.field.get(null);
        } catch (IllegalAccessException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't get static field " + this.field.getName() + " from: " +
                    this.field.getDeclaringClass().getName(), e);
            return null;
        }
    }

    public void set(Object value) {
        try {
            this.field.set(null, value);
        } catch (IllegalAccessException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't set static field " + this.field.getName() + " of: " +
                    this.field.getDeclaringClass().getName(), e);
        }
    }

    public Object get(Object target) {
        try {
            return this.field.get(target);
        } catch (IllegalAccessException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't get field " + this.field.getName() + " from: " + target.getClass().getName(), e);
            return null;
        }
    }

    public void set(Object target, Object value) {
        try {
            this.field.set(target, value);
        } catch (IllegalAccessException e) {
            ViveMain.LOGGER.log(Level.SEVERE,
                "couldn't set field " + this.field.getName() + " of: " + target.getClass().getName(), e);
        }
    }
}
