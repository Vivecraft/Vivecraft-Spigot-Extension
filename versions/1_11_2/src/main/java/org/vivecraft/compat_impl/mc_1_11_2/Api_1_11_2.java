package org.vivecraft.compat_impl.mc_1_11_2;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.compat_impl.mc_1_11.Api_1_11;

public class Api_1_11_2 extends Api_1_11 {
    @Override
    public boolean hasItemCooldown(Player player, ItemStack itemStack) {
        return player.hasCooldown(itemStack.getType());
    }
}
