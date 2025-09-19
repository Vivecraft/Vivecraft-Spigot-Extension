package org.vivecraft.compat_impl.mc_1_13_2;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.vivecraft.compat_impl.mc_1_13.Api_1_13;
import org.vivecraft.util.AABB;

import java.util.Collection;

public class Api_1_13_2 extends Api_1_13 {

    @Override
    protected void initAABB() {
        // don't need the AABB ones
    }

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

    @Override
    public AABB getEntityAABB(Entity entity) {
        BoundingBox aabb = entity.getBoundingBox();
        return new AABB(
            aabb.getMinX(), aabb.getMinY(), aabb.getMinZ(),
            aabb.getMaxX(), aabb.getMaxY(), aabb.getMaxZ());
    }
}
