package org.vivecraft.compat_impl.mc_1_16;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Strider;
import org.vivecraft.compat_impl.mc_1_15.Mc_1_15;

public class Mc_1_16 extends Mc_1_15 {

    @Override
    public boolean hasHumanoidHead(LivingEntity entity) {
        return super.hasHumanoidHead(entity) ||
            entity instanceof Piglin ||
            entity instanceof Strider;
    }
}
