package org.vivecraft.compat_impl.mc_1_20_6;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.vivecraft.compat_impl.mc_1_19_4.Api_1_19_4;

public class Api_1_20_6 extends Api_1_19_4 {

    @Override
    public String getCausingEntityName(PlayerDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() != null) {
            Entity entity = event.getDamageSource().getCausingEntity();
            if (entity instanceof Player) {
                return ((Player) entity).getDisplayName();
            } else {
                return entity.getName();
            }
        } else {
            return "";
        }
    }

    @Override
    public boolean addDamage(ItemStack itemStack, int damage) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();
            int durability = damageable.getDamage();
            int newDurability = durability + damage;
            damageable.setDamage(newDurability);
            itemStack.setItemMeta(damageable);
            if (damageable.hasMaxDamage()) {
                return newDurability >= damageable.getMaxDamage();
            } else {
                return newDurability >= itemStack.getType().getMaxDurability();
            }
        } else {
            return false;
        }
    }
}
