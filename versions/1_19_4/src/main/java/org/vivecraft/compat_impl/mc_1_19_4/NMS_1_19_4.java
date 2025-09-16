package org.vivecraft.compat_impl.mc_1_19_4;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.vivecraft.accessors.ComponentMapping;
import org.vivecraft.accessors.ItemStackMapping;
import org.vivecraft.compat_impl.mc_1_13_2.NMS_1_13_2;
import org.vivecraft.util.AABB;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_19_4 extends NMS_1_13_2 {

    protected ReflectionMethod Component_translationWithFallback;
    protected ReflectionMethod CraftItemStack_asNMSCopy;
    protected ReflectionMethod CraftItemStack_asBukkitCopy;
    protected ReflectionMethod ItemStack_setHoverName;

    @Override
    protected void init() {
        // still need the super ones
        super.init();
        this.Component_translationWithFallback = ReflectionMethod.getMethod(
            ComponentMapping.METHOD_TRANSLATABLE_WITH_FALLBACK);
        this.ItemStack_setHoverName = ReflectionMethod.getMethod(false, ItemStackMapping.METHOD_SET_HOVER_NAME);
        this.CraftItemStack_asNMSCopy = ReflectionMethod.getWithApi("org.bukkit.craftbukkit",
            "inventory.CraftItemStack", "asNMSCopy", ItemStack.class);
        try {
            this.CraftItemStack_asBukkitCopy = ReflectionMethod.getWithApi("org.bukkit.craftbukkit",
                "inventory.CraftItemStack", "asBukkitCopy", ClassGetter.getRaw("net.minecraft.world.item.ItemStack"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("error initializing CraftItemStack", e);
        }
    }

    @Override
    public AABB getEntityAABB(Entity entity) {
        BoundingBox aabb = entity.getBoundingBox();
        return new AABB(
            aabb.getMinX(), aabb.getMinY(), aabb.getMinZ(),
            aabb.getMaxX(), aabb.getMaxY(), aabb.getMaxZ());
    }

    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        Object nmsStack = this.CraftItemStack_asNMSCopy.invoke(null, itemStack);
        this.ItemStack_setHoverName.invoke(nmsStack,
            this.Component_translationWithFallback.invoke(null, translationKey, fallback));
        return (ItemStack) this.CraftItemStack_asBukkitCopy.invoke(null, nmsStack);
    }
}
