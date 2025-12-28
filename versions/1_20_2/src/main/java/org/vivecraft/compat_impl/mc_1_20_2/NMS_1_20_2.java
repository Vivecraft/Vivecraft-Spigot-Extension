package org.vivecraft.compat_impl.mc_1_20_2;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.AABBMapping;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.LivingEntityMapping;
import org.vivecraft.accessors.MobMapping;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_19_4.NMS_1_19_4;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_20_2 extends NMS_1_19_4 {

    protected ReflectionMethod Mob_getAttackBoundingBox;
    protected ReflectionMethod LivingEntity_getHitbox;
    protected ReflectionMethod AABB_inflate;
    protected ReflectionMethod AABB_intersects;

    @Override
    protected void initReducedAttack() {
        this.Mob = ClassGetter.getClass(true, MobMapping.MAPPING);
        this.Mob_getAttackBoundingBox = ReflectionMethod.getMethod(MobMapping.METHOD_GET_ATTACK_BOUNDING_BOX,
            MobMapping.METHOD_GET_ATTACK_BOUNDING_BOX_1);
        this.LivingEntity_getHitbox = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_GET_HITBOX);
        this.AABB_inflate = ReflectionMethod.getMethod(AABBMapping.METHOD_INFLATE);
        this.AABB_intersects = ReflectionMethod.getMethod(AABBMapping.METHOD_INTERSECTS);
        this.Entity_distanceToSqr = ReflectionMethod.getMethod(EntityMapping.METHOD_DISTANCE_TO_SQR);
    }

    @Override
    public boolean inReducedAttackRange(Player player, Entity entity) {
        Object nmsEntity = BukkitReflector.getEntityHandle(entity);
        if (!this.Mob.isInstance(nmsEntity)) {
            // no attack range
            return true;
        }
        Object attackAABB = this.getAttackAABB(nmsEntity);
        attackAABB = this.AABB_inflate.invoke(attackAABB,
            ViveMain.CONFIG.mobAttackRangeAdjustment.get(),
            0,
            ViveMain.CONFIG.mobAttackRangeAdjustment.get());
        return (boolean) this.AABB_intersects.invoke(attackAABB,
            this.LivingEntity_getHitbox.invoke(BukkitReflector.getEntityHandle(player))) ||
            // if they stop let the attack through, or they will stand there forever
            entity.getVelocity().lengthSquared() <= 0.01;
    }

    protected Object getAttackAABB(Object nmsEntity) {
        return this.Mob_getAttackBoundingBox.invoke(nmsEntity);
    }

    @Override
    protected double getAttackReachSqr(Object mob, Object target) {
        throw new UnsupportedOperationException("getAttackReachSqr not supported past 1.20.1");
    }

    @Override
    protected double getAttackDistanceSqr(Object mob, Object target) {
        throw new UnsupportedOperationException("getAttackDistanceSqr not supported past 1.20.1");
    }
}
