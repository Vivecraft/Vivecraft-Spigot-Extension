package org.vivecraft.compat_impl.mc_1_19_4;

import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_18.EndermanHelper_1_18;
import org.vivecraft.util.reflection.ReflectionMethod;

public class EndermanHelper_1_19_4 extends EndermanHelper_1_18 {

    protected ReflectionMethod Entity_hasIndirectPassenger;

    @Override
    protected void initFindPlayer() {
        super.initFindPlayer();
        this.Entity_hasIndirectPassenger = ReflectionMethod.getMethod(EntityMapping.METHOD_HAS_INDIRECT_PASSENGER);
    }

    @Override
    public Boolean lookForPlayerContinueUse(EndermanLookForPlayerGoalAccessor goal) {
        if (goal.getPendingTarget() != null) {
            if (!this.isAngerInducing(goal.getPendingTarget(), goal.getEnderman())) {
                return false;
            }
            this.Mob_lookAt.invoke(goal.getEnderman(), goal.getPendingTarget(), 10F, 10F);
            return true;
        }
        if (goal.getTarget() != null) {
            if ((boolean) this.Entity_hasIndirectPassenger.invoke(goal.getEnderman(), goal.getTarget())) {
                return false;
            }
            if ((boolean) this.TargetingConditions_test.invoke(goal.getContinueAggroConditions(), goal.getEnderman(),
                goal.getTarget()))
            {
                return true;
            }
        }
        return null;
    }

    @Override
    protected boolean isAngerInducing(Object target, Object enderman) {
        return
            (canPlayerSeeEnderman(target, enderman) || (boolean) this.NeutralMob_isAngryAt.invoke(enderman, target)) &&
                !(boolean) this.Entity_hasIndirectPassenger.invoke(enderman, target);
    }
}
