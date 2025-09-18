package org.vivecraft.compat_impl.mc_1_13_2;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.vivecraft.compat_impl.mc_1_13.Api_1_13;

import java.util.Collection;

public class Api_1_13_2 extends Api_1_13 {

    @Override
    public double applyArmorModifiers(double baseArmor, ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasAttributeModifiers()) {
            Collection<AttributeModifier> mods = meta.getAttributeModifiers(this.getArmorAttribute());
            if (mods == null) {
                return baseArmor;
            }
            for (AttributeModifier modifier : mods) {
                double amount = modifier.getAmount();
                switch (modifier.getOperation()) {
                    case ADD_NUMBER:
                        baseArmor += amount;
                        break;
                    case ADD_SCALAR:
                        baseArmor += amount * baseArmor;
                        break;
                }
            }
        }
        return baseArmor;
    }

    protected Attribute getArmorAttribute() {
        return Attribute.GENERIC_ARMOR;
    }
}
