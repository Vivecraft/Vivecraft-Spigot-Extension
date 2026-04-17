package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import org.vivecraft.ViveMain;

public class VREnderMan extends EnderMan {

    public VREnderMan(EntityType<EnderMan> entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLookingAtMe(
        LivingEntity entity, double tolerance, boolean scaleByDistance, boolean visual, double... yValues)
    {
        return ViveMain.NMS.canSeeEachOther(entity, this,
            ViveMain.MC_MODS.endermanHelper().adjustedVRTolerance(tolerance, entity), scaleByDistance, visual, yValues);
    }
}
