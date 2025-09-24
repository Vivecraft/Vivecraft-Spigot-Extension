package org.vivecraft.compat_impl.mc_1_15;

import org.vivecraft.accessors.EnderManMapping;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_14.EndermanHelper_1_14;
import org.vivecraft.util.reflection.ReflectionMethod;

public class EndermanHelper_1_15 extends EndermanHelper_1_14 {

    protected ReflectionMethod Enderman_setBeingStaredAt;

    @Override
    protected void initFindPlayer() {
        super.initFindPlayer();
        this.Enderman_setBeingStaredAt = ReflectionMethod.getMethod(EnderManMapping.METHOD_SET_BEING_STARED_AT);
    }

    @Override
    public void lookForPlayerStart(EndermanLookForPlayerGoalAccessor goal) {
        super.lookForPlayerStart(goal);
        this.Enderman_setBeingStaredAt.invoke(goal.getEnderman());
    }
}
