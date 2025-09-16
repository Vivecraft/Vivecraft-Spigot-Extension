package org.vivecraft.compat_impl.mc_1_11;

import org.bukkit.entity.Evoker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Vindicator;
import org.vivecraft.compat_impl.mc_1_9.Mc_1_9;

public class Mc_1_11 extends Mc_1_9 {
    @Override
    public boolean hasHumanoidHead(LivingEntity entity) {
        return super.hasHumanoidHead(entity) ||
            entity instanceof Vindicator ||
            entity instanceof Evoker ||
            entity instanceof Vex;
    }
}
