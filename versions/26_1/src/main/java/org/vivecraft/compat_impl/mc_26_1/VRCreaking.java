package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.Level;
import org.vivecraft.ViveMain;

public class VRCreaking extends Creaking {

    public VRCreaking(EntityType<Creaking> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLookingAtMe(
        LivingEntity entity, double tolerance, boolean scaleByDistance, boolean visual, double... yValues)
    {
        return ViveMain.NMS.canSeeEachOther(entity, this, tolerance, scaleByDistance, visual, yValues);
    }
}
