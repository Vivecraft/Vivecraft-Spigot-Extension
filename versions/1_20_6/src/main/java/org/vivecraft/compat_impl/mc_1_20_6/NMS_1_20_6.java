package org.vivecraft.compat_impl.mc_1_20_6;

import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.DataComponentsMapping;
import org.vivecraft.accessors.ItemStackMapping;
import org.vivecraft.compat_impl.mc_1_19_4.NMS_1_19_4;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_20_6 extends NMS_1_19_4 {

    protected ReflectionMethod ItemStack_set;
    protected ReflectionField DataComponents_CUSTOM_NAME;

    @Override
    protected void init() {
        super.init();
        this.ItemStack_set = ReflectionMethod.getMethod(ItemStackMapping.METHOD_SET);
        this.DataComponents_CUSTOM_NAME = ReflectionField.getField(DataComponentsMapping.FIELD_CUSTOM_NAME);
    }

    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        Object nmsStack = this.CraftItemStack_asNMSCopy.invoke(null, itemStack);
        this.ItemStack_set.invoke(nmsStack, this.DataComponents_CUSTOM_NAME.get(null),
            this.Component_translationWithFallback.invoke(null, translationKey, fallback));
        return (ItemStack) this.CraftItemStack_asBukkitCopy.invoke(null, nmsStack);
    }
}
