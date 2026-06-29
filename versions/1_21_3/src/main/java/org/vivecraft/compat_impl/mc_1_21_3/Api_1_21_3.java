package org.vivecraft.compat_impl.mc_1_21_3;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.ViveMain;
import org.vivecraft.compat_impl.mc_1_21.Api_1_21;

public class Api_1_21_3 extends Api_1_21 {

    @Override
    protected Attribute getArmorAttribute() {
        return Attribute.ARMOR;
    }

    @Override
    public void applyItemCooldown(Player player, ItemStack itemStack, int ticks, boolean force) {
        if (force || ticks > player.getCooldown(itemStack)) {
            player.setCooldown(itemStack, ticks);
            ViveMain.NMS.playShieldDisableSound(player, itemStack);
        }
    }
}
