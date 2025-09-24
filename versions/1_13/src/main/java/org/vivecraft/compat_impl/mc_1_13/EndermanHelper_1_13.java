package org.vivecraft.compat_impl.mc_1_13;

import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.LevelMapping;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_11.EndermanHelper_1_11;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.function.Predicate;

public class EndermanHelper_1_13 extends EndermanHelper_1_11 {

    protected ReflectionMethod Level_getClosestPlayer;

    @Override
    protected void initFindPlayer() {
        this.Level_getClosestPlayer = ReflectionMethod.getMethod(LevelMapping.METHOD_FUNC_184150_A_1);
    }

    @Override
    public Object lookForPlayerNearest(EndermanLookForPlayerGoalAccessor goal, double distance) {
        Object level = ViveMain.NMS.getLevel(goal.getEnderman());
        Vector pos = ViveMain.NMS.getEntityPosition(goal.getEnderman());
        return this.Level_getClosestPlayer.invoke(level, pos.getX(), pos.getY(), pos.getZ(),
            distance, distance, null,
            (Predicate) player -> player != null && canPlayerSeeEnderman(player, goal.getEnderman()));
    }
}
