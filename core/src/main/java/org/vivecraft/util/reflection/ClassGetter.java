package org.vivecraft.util.reflection;

import me.kcra.takenaka.accessor.mapping.ClassMapping;
import org.vivecraft.util.MCVersion;

public class ClassGetter {

    /**
     * Tries to find any reflection class matching the given mappings
     *
     * @param critical if set will throw an exception on failure
     * @param mappings one or multiple mappings to try
     * @return found reflection class
     * @throws RuntimeException When no matching class is found
     */
    public static Class<?> getClass(boolean critical, ClassMapping... mappings) {
        // get the matching filed with the closest matching version, preferring older ones, unless there is none
        MCVersion mc = MCVersion.getCurrent();
        Class<?> c = null;
        for (String namespace : new String[]{"spigot", "mojang"}) {
            for (ClassMapping mapping : mappings) {
                int major = mc.major;
                int minor = mc.minor;
                while (major > 7 && c == null) {
                    while (minor >= 0 && c == null) {
                        if (minor == 0) {
                            c = mapping.getClass("1." + major, namespace);
                        } else {
                            c = mapping.getClass("1." + major + "." + minor, namespace);
                        }
                        minor--;
                    }
                    minor = 10;
                    major--;
                }
                if (c == null && mc.major <= 8) {
                    // get 1.8.8 in this case, that is the oldest mapping that takenaka supports
                    c = mapping.getClass("1.8.8", namespace);
                }
                if (c != null) {
                    break;
                }
            }
            if (c != null) {
                break;
            }
        }
        if (c == null && critical) {
            // if it is still null we don't support it yet
            throw new RuntimeException(
                "Unsupported mc version: " + mc.version + ", no mapping found for: " + mappings[0].getName());
        }
        return c;
    }

    public static Class<?> getRaw(String cls) throws ClassNotFoundException {
        return Class.forName(cls);
    }

    public static Class<?> getWithApi(String pre, String post) throws ClassNotFoundException {
        MCVersion mc = MCVersion.getCurrent();
        for (int i = 0; i <= 10; i++) {
            String apiClass;
            if (i != 10) {
                apiClass = String.format("%s.v1_%s_R%s.%s", pre, mc.major, i, post);
            } else {
                apiClass = pre + "." + post;
            }
            try {
                return getRaw(apiClass);
            } catch (ClassNotFoundException ignored) {}
        }

        throw new ClassNotFoundException("couldn't find any class matching " + pre + ".###." + post);
    }

    public static Class<?> getCompat(String pattern) throws ClassNotFoundException {
        MCVersion mc = MCVersion.getCurrent();
        for (int i = mc.minor; i >= 0; i--) {
            String apiClass;
            if (i != 0) {
                apiClass = pattern.replace("X_X", "1_" + mc.major + "_" + i);
            } else {
                apiClass = pattern.replace("X_X", "1_" + mc.major);
            }
            try {
                return getRaw(apiClass);
            } catch (NoClassDefFoundError e) {
                // happens when paper tries to load a spigot class that it has no mappings for, so try the mojang one
                if (!pattern.contains("mojang")) {
                    return getCompat(pattern.replace("mc_X_X.", "mc_X_X.mojang."));
                }
                throw e;
            } catch (ClassNotFoundException ignored) {}
        }
        if (mc.major <= 8) {
            try {
                return getRaw(pattern.replace("X_X", "1_8_8"));
            } catch (ClassNotFoundException ignored) {}
        }

        throw new ClassNotFoundException("couldn't find any class matching " + pattern);
    }
}
