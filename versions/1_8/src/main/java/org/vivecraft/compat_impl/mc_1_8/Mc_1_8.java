package org.vivecraft.compat_impl.mc_1_8;

import org.bukkit.entity.*;
import org.vivecraft.ViveMain;
import org.vivecraft.compat.McHelper;

public class Mc_1_8 implements McHelper {

    @Override
    public boolean isArrow(Entity entity) {
        return entity instanceof Arrow;
    }

    @Override
    public boolean hasHumanoidHead(LivingEntity entity) {
        return (entity instanceof Player && !ViveMain.API.hasSmallHitbox((Player) entity)) ||
            entity instanceof Zombie ||
            entity instanceof Skeleton ||
            entity instanceof Witch ||
            entity instanceof Blaze ||
            entity instanceof Creeper ||
            entity instanceof Enderman ||
            entity instanceof Villager ||
            entity instanceof Snowman;
    }

    @Override
    public boolean hasAnimalHead(LivingEntity entity) {
        return !(entity instanceof EnderDragon);
    }

    @Override
    public boolean supportsCrawling() {
        return false;
    }
}
