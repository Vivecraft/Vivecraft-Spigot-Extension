package org.vivecraft.compat;

import org.vivecraft.util.reflection.ReflectionField;

/**
 * to access non api bukkit classes
 */
public class SpigotReflector {

    private static final ReflectionField SpigotConfig_movedWrong = ReflectionField.getRaw(
        "org.spigotmc.SpigotConfig", "movedWronglyThreshold");

    private static final ReflectionField SpigotConfig_movedTooQuickly = ReflectionField.getRaw(
        "org.spigotmc.SpigotConfig", "movedTooQuicklyMultiplier");

    public static void setMovedWrongly(double d) {
        SpigotConfig_movedWrong.set(null, d);
    }

    public static void setMovedTooQuickly(double d) {
        SpigotConfig_movedTooQuickly.set(null, d);
    }
}
