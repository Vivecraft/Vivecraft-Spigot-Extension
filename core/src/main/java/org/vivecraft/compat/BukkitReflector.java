package org.vivecraft.compat;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.vivecraft.util.reflection.ReflectionMethod;

/**
 * to access non api bukkit classes
 */
public class BukkitReflector {

    private static final String BUKKIT = "org.bukkit.craftbukkit";

    private static final ReflectionMethod CraftEntity_getHandle = ReflectionMethod.getWithApi(BUKKIT,
        "entity.CraftEntity", "getHandle");

    private static final ReflectionMethod CraftWorld_getHandle = ReflectionMethod.getWithApi(BUKKIT,
        "CraftWorld", "getHandle");

    public static Object getHandle(Entity entity) {
        return CraftEntity_getHandle.invoke(entity);
    }

    public static Object getHandle(World world) {
        return CraftWorld_getHandle.invoke(world);
    }
}
