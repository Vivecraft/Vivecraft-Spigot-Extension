package org.vivecraft.compat;

import org.vivecraft.ViveMain;
import org.vivecraft.util.reflection.ReflectionField;

/**
 * to access non api bukkit classes
 */
public class SpigotReflector {

    private static final ReflectionField SpigotConfig_movedWrong = ReflectionField.getRaw(
        "org.spigotmc.SpigotConfig", "movedWronglyThreshold", false);

    private static final ReflectionField SpigotConfig_movedTooQuickly = ReflectionField.getRaw(
        "org.spigotmc.SpigotConfig", "movedTooQuicklyMultiplier", false);

    public static void setMovedWrongly(double d) {
        if (SpigotConfig_movedWrong != null) {
            SpigotConfig_movedWrong.set(null, Math.max((double) SpigotConfig_movedWrong.get(null), d));
        } else {
            ViveMain.LOGGER.warning("spigot config 'movedWronglyThreshold' not set, not available");
        }
    }

    public static void setMovedTooQuickly(double d) {
        if (SpigotConfig_movedTooQuickly != null) {
            SpigotConfig_movedTooQuickly.set(null, Math.max((double) SpigotConfig_movedTooQuickly.get(null), d));
        } else {
            ViveMain.LOGGER.warning("spigot config 'movedTooQuicklyMultiplier' not set, not available");
        }
    }
}
