package org.vivecraft.compat_impl.mc_1_16;

import org.vivecraft.accessors.EnderManMapping;
import org.vivecraft.accessors.NeutralMobMapping;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_15.EndermanHelper_1_15;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.function.Predicate;

public class EndermanHelper_1_16 extends EndermanHelper_1_15 {

    protected ReflectionMethod Enderman_setBeingStaredAt;
    protected ReflectionMethod NeutralMob_isAngryAt;

    @Override
    protected void initFindPlayer() {
        super.initFindPlayer();
        this.Enderman_setBeingStaredAt = ReflectionMethod.getMethod(EnderManMapping.METHOD_SET_BEING_STARED_AT);
        this.NeutralMob_isAngryAt = ReflectionMethod.getMethod(NeutralMobMapping.METHOD_IS_ANGRY_AT);
    }

    @Override
    public Predicate isAngryAtPredicate(Object enderman) {
        return (livingEntity) -> (boolean) this.NeutralMob_isAngryAt.invoke(enderman, livingEntity);
    }

    @Override
    public boolean lookForPlayerTick(EndermanLookForPlayerGoalAccessor goal) {
        if (this.Mob_getTarget.invoke(goal.getEnderman()) == null) {
            goal.setTarget(null);
        }
        return super.lookForPlayerTick(goal);
    }
}
