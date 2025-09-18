package org.vivecraft.compat_impl.mc_1_21_3;

import org.bukkit.entity.Creaking;
import org.bukkit.entity.LivingEntity;
import org.vivecraft.compat_impl.mc_1_21.Mc_1_21;

public class Mc_1_21_3 extends Mc_1_21 {

    @Override
    public boolean hasHumanoidHead(LivingEntity entity) {
        return super.hasHumanoidHead(entity) ||
            entity instanceof Creaking;
    }
}
