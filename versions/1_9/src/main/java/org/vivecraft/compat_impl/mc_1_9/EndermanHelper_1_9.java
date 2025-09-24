package org.vivecraft.compat_impl.mc_1_9;

import com.google.common.base.Predicate;
import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.LevelMapping;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_8.EndermanHelper_1_8;
import org.vivecraft.util.reflection.ReflectionMethod;

public class EndermanHelper_1_9 extends EndermanHelper_1_8 {

    protected ReflectionMethod Level_getClosestPlayer;
    protected ReflectionMethod Entity_isAlive;

    @Override
    protected void init() {
        super.init();
        this.Entity_isAlive = ReflectionMethod.getMethod(EntityMapping.METHOD_IS_ALIVE);
    }

    @Override
    protected void initFindPlayer() {
        this.Level_getClosestPlayer = ReflectionMethod.getMethod(LevelMapping.METHOD_FUNC_184150_A);
    }

    @Override
    public boolean lookForPlayerMustSee() {
        return false;
    }

    @Override
    public Object lookForPlayerNearest(EndermanLookForPlayerGoalAccessor goal, double distance) {
        Object level = ViveMain.NMS.getLevel(goal.getEnderman());
        Vector pos = ViveMain.NMS.getEntityPosition(goal.getEnderman());
        return this.Level_getClosestPlayer.invoke(level, pos.getX(), pos.getY(), pos.getZ(),
            distance, distance, null,
            (Predicate) player -> player != null && canPlayerSeeEnderman(player, goal.getEnderman()));
    }

    @Override
    public void lookForPlayerStop(EndermanLookForPlayerGoalAccessor goal) {
        goal.setPendingTarget(null);
    }

    @Override
    public Boolean lookForPlayerContinueUse(EndermanLookForPlayerGoalAccessor goal) {
        if (goal.getPendingTarget() != null) {
            if (!this.canPlayerSeeEnderman(goal.getPendingTarget(), goal.getEnderman())) {
                return false;
            }
            this.Mob_lookAt.invoke(goal.getEnderman(), goal.getPendingTarget(), 10F, 10F);
            return true;
        }
        if (goal.getTarget() != null && (boolean) this.Entity_isAlive.invoke(goal.getTarget())) {
            return true;
        }
        return null;
    }

    @Override
    public boolean lookForPlayerTick(EndermanLookForPlayerGoalAccessor goal) {
        if (goal.getPendingTarget() != null) {
            goal.setAggroTime(goal.getAggroTime() - 1);
            if (goal.getAggroTime() <= 0) {
                goal.setTarget(goal.getPendingTarget());
                goal.setPendingTarget(null);
                goal.superStart();
            }
            return false;
        } else {
            if (goal.getTarget() != null && this.canTp(goal.getEnderman())) {
                if (canPlayerSeeEnderman(goal.getTarget(), goal.getEnderman())) {
                    if ((double) this.Entity_distanceToSqr.invoke(goal.getTarget(), goal.getEnderman()) < 16.0) {
                        this.Enderman_teleport.invoke(goal.getEnderman());
                    }
                    goal.setTeleportTime(0);
                } else if ((double) this.Entity_distanceToSqr.invoke(goal.getTarget(), goal.getEnderman()) > 256.0) {
                    goal.setTeleportTime(goal.getTeleportTime() + 1);
                    if (goal.getTeleportTime() > 30) {
                    }
                    if (goal.getTeleportTime() > 30 &&
                        (boolean) this.Enderman_teleportTowards.invoke(goal.getEnderman(), goal.getTarget()))
                    {
                        goal.setTeleportTime(0);
                    }
                }
            }
            return true;
        }
    }

    protected boolean canTp(Object enderman) {
        return true;
    }
}
