package org.vivecraft.compat_impl.mc_26_3;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;
import org.vivecraft.ViveMain;

import java.util.function.Function;

public class VREnderMan extends Enderman {

    public VREnderMan(EntityType<Enderman> entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLookingAtMe(
        LivingEntity entity, double tolerance, boolean scaleByDistance, boolean visual, double... yValues)
    {
        return ViveMain.NMS.canSeeEachOther(entity, this,
            ViveMain.MC_MODS.endermanHelper().adjustedVRTolerance(tolerance, entity), scaleByDistance, visual, yValues);
    }


    public static boolean isVREnderMan(Object entity) {
        return entity instanceof VREnderMan;
    }

    @SuppressWarnings("unchecked")
    public static Function<Entity, Entity> VREnderManSupplier() {
        return entity -> new VREnderMan((EntityType<Enderman>) entity.getType(), entity.level());
    }
}
