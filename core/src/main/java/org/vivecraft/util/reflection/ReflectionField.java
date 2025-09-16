package org.vivecraft.util.reflection;

import me.kcra.takenaka.accessor.mapping.FieldMapping;
import org.vivecraft.ViveMain;
import org.vivecraft.util.MCVersion;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * reflection handler that catches any exceptions on the calls
 */
public class ReflectionField {
    private final Field field;

    private ReflectionField(Field field) {
        this.field = field;
    }

    private static ReflectionField getField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return new ReflectionField(cls.getDeclaredField(fieldName));
    }

    public static ReflectionField getField(FieldMapping mapping) {
        // get the matching filed with the closest matching version, preferring older ones, unless there is none
        MCVersion mc = MCVersion.getCurrent();
        int major = mc.major;
        int minor = mc.minor;
        Field f = null;
        while (major > 7 && f == null) {
            while (minor >= 0 && f == null) {
                if (minor == 0) {
                    f = mapping.getField("1." + major, "spigot");
                } else {
                    f = mapping.getField("1." + major + "." + minor, "spigot");
                }
                minor--;
            }
            minor = 10;
            major--;
        }
        if (f == null && mc.major <= 8) {
            // get 1.8.8 in this case, that is the oldest mapping that takenaka supports
            f = mapping.getField("1.8.8", "spigot");
        }
        if (f == null) {
            // if it is still null we don't support it yet
            throw new RuntimeException("Unsupported mc version: " + mc.version);
        }
        return new ReflectionField(f);
    }

    public static ReflectionField getWithApi(String pre, String post, String fieldName) {
        try {
            Class<?> c = ClassGetter.getWithApi(pre, post);
            return getField(c, fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException("couldn't find any field matching " + pre + ".###." + post + "." + fieldName);
        }
    }

    public static ReflectionField getRaw(String cls, String fieldName) {
        try {
            return getField(ClassGetter.getRaw(cls), fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException("couldn't find field " + cls + "." + fieldName, e);
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
