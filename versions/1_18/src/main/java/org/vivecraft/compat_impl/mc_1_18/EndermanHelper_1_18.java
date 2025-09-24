package org.vivecraft.compat_impl.mc_1_18;

import org.vivecraft.accessors.GoalMapping;
import org.vivecraft.compat_impl.mc_1_17.EndermanHelper_1_17;
import org.vivecraft.util.reflection.ReflectionMethod;

public class EndermanHelper_1_18 extends EndermanHelper_1_17 {

    protected ReflectionMethod Goal_adjustedTickDelay;

    @Override
    protected void init() {
        super.init();
        this.Goal_adjustedTickDelay = ReflectionMethod.getMethod(GoalMapping.METHOD_ADJUSTED_TICK_DELAY);
    }

    @Override
    protected int adjustedTickDelay(Object goal, int tickDelay) {
        return (int) this.Goal_adjustedTickDelay.invoke(goal, tickDelay);
    }
}
