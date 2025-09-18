package org.vivecraft.compat_impl.mc_1_16;

import com.google.common.collect.Multimap;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_15.NMS_1_15;

public class NMS_1_16 extends NMS_1_15 {
    @Override
    @SuppressWarnings("unchecked")
    public double getArmorValue(ItemStack itemStack) {
        Object item = this.ItemStack_getItem.invoke(BukkitReflector.asNMSCopy(itemStack));
        Multimap map = (Multimap) this.Item_getDefaultAttributeModifiers.invoke(item, this.EquipmentSlot_FEET.get());
        return applyAttributeModifiers(0, map.get(this.Attributes_ARMOR.get()));
    }
}
