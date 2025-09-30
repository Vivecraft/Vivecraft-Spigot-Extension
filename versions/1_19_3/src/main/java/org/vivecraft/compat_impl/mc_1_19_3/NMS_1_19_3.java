package org.vivecraft.compat_impl.mc_1_19_3;

import org.vivecraft.accessors.MobMapping;
import org.vivecraft.compat_impl.mc_1_18.NMS_1_18;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_19_3 extends NMS_1_18 {

    protected ReflectionMethod Mob_getPerceivedTargetDistanceSquareForMeleeAttack;

    @Override
    protected void initReducedAttack() {
        super.initReducedAttack();
        this.Mob_getPerceivedTargetDistanceSquareForMeleeAttack = ReflectionMethod.getMethod(
            MobMapping.METHOD_GET_PERCEIVED_TARGET_DISTANCE_SQUARE_FOR_MELEE_ATTACK);
    }

    @Override
    protected double getAttackDistanceSqr(Object mob, Object target) {
        return (double) this.Mob_getPerceivedTargetDistanceSquareForMeleeAttack.invoke(mob, target);
    }
}
