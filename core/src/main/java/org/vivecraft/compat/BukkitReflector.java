package org.vivecraft.compat;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

/**
 * to access non api bukkit classes
 */
public class BukkitReflector {

    private static final String BUKKIT = "org.bukkit.craftbukkit";

    private static final ReflectionMethod CraftEntity_getHandle = ReflectionMethod.getWithApi(BUKKIT,
        "entity.CraftEntity", "getHandle", true);

    private static final ReflectionMethod CraftWorld_getHandle = ReflectionMethod.getWithApi(BUKKIT,
        "CraftWorld", "getHandle", true);

    private static final ReflectionMethod CraftDamageSource_getHandle = ReflectionMethod.getWithApi(BUKKIT,
        "damage.CraftDamageSource", "getHandle", false);

    private static final ReflectionMethod CraftItemStack_asNMSCopy = ReflectionMethod.getWithApi(BUKKIT,
        "inventory.CraftItemStack", "asNMSCopy", true, ItemStack.class);

    private static final ReflectionMethod CraftItemStack_asBukkitCopy = ReflectionMethod.getWithApi(BUKKIT,
        "inventory.CraftItemStack", "asBukkitCopy", true, ViveMain.NMS.getItemstackClass());

    private static final ReflectionField CraftItemStack_handle = ReflectionField.getWithApi(BUKKIT,
        "inventory.CraftItemStack", "handle");


    public static Object getEntityHandle(Entity entity) {
        return CraftEntity_getHandle.invoke(entity);
    }

    // only available since 1.20.4
    public static Object getDamageSourceHandle(Object damageSource) {
        return CraftDamageSource_getHandle != null ? CraftDamageSource_getHandle.invoke(damageSource) : null;
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

    @Nullable
    public static Object getItemHandle(ItemStack itemStack) {
        return CraftItemStack_handle.get(itemStack);
    }
}
