package org.vivecraft.compat;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.ItemStackMapping;
import org.vivecraft.util.reflection.ClassGetter;
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

    private static final ReflectionMethod CraftItemStack_asNMSCopy = ReflectionMethod.getWithApi(
        "org.bukkit.craftbukkit", "inventory.CraftItemStack", "asNMSCopy", ItemStack.class);

    private static final ReflectionMethod CraftItemStack_asBukkitCopy = ReflectionMethod.getWithApi(
        "org.bukkit.craftbukkit", "inventory.CraftItemStack", "asBukkitCopy",
        ClassGetter.getClass(true, ItemStackMapping.MAPPING));

    public static Object getEntityHandle(Entity entity) {
        return CraftEntity_getHandle.invoke(entity);
    }

    public static Object getWorldHandle(World world) {
        return CraftWorld_getHandle.invoke(world);
    }

    public static Object asNMSCopy(ItemStack itemStack) {
        return CraftItemStack_asNMSCopy.invokes(itemStack);
    }

    public static ItemStack asBukkitCopy(Object itemStack) {
        return (ItemStack) CraftItemStack_asBukkitCopy.invokes(itemStack);
    }
}
