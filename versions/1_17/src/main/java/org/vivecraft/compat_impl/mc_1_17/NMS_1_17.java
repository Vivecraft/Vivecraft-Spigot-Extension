package org.vivecraft.compat_impl.mc_1_17;

import org.bukkit.util.Vector;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.MobMapping;
import org.vivecraft.compat_impl.mc_1_16.NMS_1_16;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_17 extends NMS_1_16 {

    protected ReflectionMethod Entity_getXRot;
    protected ReflectionMethod Entity_getYRot;
    protected ReflectionMethod Entity_setXRot;
    protected ReflectionMethod Entity_setYRot;

    protected ReflectionMethod Mob_getMeleeAttackRangeSqr;

    @Override
    protected void initRotation() {
        this.Entity_getXRot = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_XROT);
        this.Entity_getYRot = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_YROT);
        this.Entity_setXRot = ReflectionMethod.getMethod(EntityMapping.METHOD_SET_XROT);
        this.Entity_setYRot = ReflectionMethod.getMethod(EntityMapping.METHOD_SET_YROT);
    }

    @Override
    protected void initReducedAttack() {
        super.initReducedAttack();
        this.Mob_getMeleeAttackRangeSqr = ReflectionMethod.getMethod(MobMapping.METHOD_GET_MELEE_ATTACK_RANGE_SQR);
    }

    @Override
    protected Vector getRotation(Object serverPlayer) {
        return new Vector(
            (float) this.Entity_getXRot.invoke(serverPlayer),
            (float) this.Entity_getYRot.invoke(serverPlayer), 0);
    }

    @Override
    protected void setRotation(Object serverPlayer, float xRot, float yRot) {
        this.Entity_setXRot.invoke(serverPlayer, xRot);
        this.Entity_setYRot.invoke(serverPlayer, yRot);
    }

    @Override
    protected double getAttackReachSqr(Object mob, Object target) {
        double attackRangeSqr = super.getAttackReachSqr(mob, target);
        if (attackRangeSqr < 0) {
            // no attack goal, fall back to general attack distance
            return (double) this.Mob_getMeleeAttackRangeSqr.invoke(mob, target);
        }
        return attackRangeSqr;
    }
}
