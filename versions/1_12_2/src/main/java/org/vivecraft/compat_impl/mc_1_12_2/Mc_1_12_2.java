package org.vivecraft.compat_impl.mc_1_12_2;

import org.bukkit.entity.Illager;
import org.bukkit.entity.LivingEntity;
import org.vivecraft.compat_impl.mc_1_11.Mc_1_11;

public class Mc_1_12_2 extends Mc_1_11 {
    @Override
    public boolean hasHumanoidHead(LivingEntity entity) {
        return super.hasHumanoidHead(entity) ||
            entity instanceof Illager;
    }
}
