package org.vivecraft.compat_impl.mc_1_19_4;

import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.ComponentMapping;
import org.vivecraft.accessors.ItemStackMapping;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_19_3.NMS_1_19_3;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_19_4 extends NMS_1_19_3 {

    protected ReflectionMethod Component_translationWithFallback;
    protected ReflectionMethod ItemStack_setHoverName;

    @Override
    protected void init() {
        // still need the super ones
        super.init();
        this.Component_translationWithFallback = ReflectionMethod.getMethod(
            ComponentMapping.METHOD_TRANSLATABLE_WITH_FALLBACK);
        this.ItemStack_setHoverName = ReflectionMethod.getMethod(false, ItemStackMapping.METHOD_SET_HOVER_NAME);
    }

    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        Object nmsStack = BukkitReflector.asNMSCopy(itemStack);
        this.ItemStack_setHoverName.invoke(nmsStack,
            this.Component_translationWithFallback.invokes(translationKey, fallback));
        return BukkitReflector.asBukkitCopy(nmsStack);
    }
}
