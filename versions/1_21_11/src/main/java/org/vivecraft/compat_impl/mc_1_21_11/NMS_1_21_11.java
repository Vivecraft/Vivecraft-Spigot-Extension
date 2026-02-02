package org.vivecraft.compat_impl.mc_1_21_11;

import org.vivecraft.accessors.AttackRangeMapping;
import org.vivecraft.accessors.DataComponentsMapping;
import org.vivecraft.accessors.LivingEntityMapping;
import org.vivecraft.accessors.MobMapping;
import org.vivecraft.compat_impl.mc_1_21_9.NMS_1_21_9;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_21_11 extends NMS_1_21_9 {

    protected ReflectionMethod LivingEntity_getActiveItem;
    protected ReflectionMethod AttackRange_effectiveMaxRange;
    protected ReflectionField DataComponents_ATTACK_RANGE;
    protected ReflectionField Mob_DEFAULT_ATTACK_REACH;

    @Override
    protected void initReducedAttack() {
        super.initReducedAttack();
        this.LivingEntity_getActiveItem = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_GET_ACTIVE_ITEM);
        this.AttackRange_effectiveMaxRange = ReflectionMethod.getMethod(AttackRangeMapping.METHOD_EFFECTIVE_MAX_RANGE);
        this.DataComponents_ATTACK_RANGE = ReflectionField.getField(DataComponentsMapping.FIELD_ATTACK_RANGE);
        this.Mob_DEFAULT_ATTACK_REACH = ReflectionField.getField(MobMapping.FIELD_DEFAULT_ATTACK_REACH);
    }

    @Override
    protected Object getAttackAABB(Object nmsEntity) {
        Object attackReach = this.DataComponentHolder_get.invoke(this.LivingEntity_getActiveItem.invoke(nmsEntity),
            this.DataComponents_ATTACK_RANGE.get());
        double range = attackReach == null ? (double) this.Mob_DEFAULT_ATTACK_REACH.get() :
            (float) this.AttackRange_effectiveMaxRange.invoke(attackReach, nmsEntity);
        return this.Mob_getAttackBoundingBox.invoke(nmsEntity, range);
    }
}
