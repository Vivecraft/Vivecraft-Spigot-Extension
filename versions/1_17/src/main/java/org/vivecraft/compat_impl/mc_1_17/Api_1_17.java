package org.vivecraft.compat_impl.mc_1_17;

import org.bukkit.entity.Player;
import org.joml.Vector3ic;
import org.vivecraft.compat_impl.mc_1_16.Api_1_16;

public class Api_1_17 extends Api_1_16 {

    @Override
    protected void initDestroySpeed() {}

    @Override
    public float getBlockDestroySpeed(Player player, Vector3ic blockPosition) {
        return player.getWorld().getBlockAt(blockPosition.x(), blockPosition.y(), blockPosition.z())
            .getBreakSpeed(player);
    }
}
