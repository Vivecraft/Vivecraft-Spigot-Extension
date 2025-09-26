package org.vivecraft.compat_impl.mc_X_X;

import org.vivecraft.ViveMain;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

public class VRCreaking extends net.minecraft.world.entity.monster.creaking.Creaking {

    public VRCreaking(net.minecraft.world.entity.EntityType entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLookingAtMe(
        net.minecraft.world.entity.LivingEntity entity, double tolerance, boolean scaleByDistance, boolean visual,
        Predicate filter, DoubleSupplier... yValues)
    {
        if (!filter.test(entity)) return false;
        double[] ys = Arrays.stream(yValues).mapToDouble(DoubleSupplier::getAsDouble).toArray();
        return this.isLookingAtMe(entity, tolerance, scaleByDistance, visual, ys);
    }

    @Override
    public boolean isLookingAtMe(
        net.minecraft.world.entity.LivingEntity entity, double tolerance, boolean scaleByDistance, boolean visual,
        double... yValues)
    {
        return ViveMain.NMS.canSeeEachOther(entity, this, tolerance, scaleByDistance, visual, yValues);
    }
}
