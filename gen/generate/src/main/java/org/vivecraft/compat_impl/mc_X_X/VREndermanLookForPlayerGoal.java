package org.vivecraft.compat_impl.mc_X_X;

import org.vivecraft.ViveMain;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;

public class VREndermanLookForPlayerGoal
    extends net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
    implements EndermanLookForPlayerGoalAccessor
{

    private final net.minecraft.world.entity.monster.EnderMan enderman;
    private Object pendingTarget;
    private int aggroTime;
    private int teleportTime;

    private Object startAggroConditions;
    private Object continueAggroConditions;

    public VREndermanLookForPlayerGoal(net.minecraft.world.entity.monster.EnderMan enderman) {
        super(
//#1.8-1.13.2#        (net.minecraft.world.entity.PathfinderMob) (Object) enderman,
//#1.14-1.99.99#        (net.minecraft.world.entity.Mob) (Object) enderman,
            net.minecraft.world.entity.player.Player.class,
//#1.16-1.99.99#        10,
            ViveMain.MC_MODS.endermanHelper().lookForPlayerMustSee()
//#1.16-1.99.99#        , false, ViveMain.MC_MODS.endermanHelper().isAngryAtPredicate(enderman)
        );
        this.enderman = enderman;
        ViveMain.MC_MODS.endermanHelper().lookForPlayerInit(this, this.getFollowDistance());
    }

    @Override
    public boolean canUse() {
        this.pendingTarget = ViveMain.MC_MODS.endermanHelper()
            .lookForPlayerNearest(this, this.getFollowDistance());
        return this.pendingTarget != null;
    }

    @Override
    public void start() {
        ViveMain.MC_MODS.endermanHelper().lookForPlayerStart(this);
    }

    @Override
    public void stop() {
        ViveMain.MC_MODS.endermanHelper().lookForPlayerStop(this);
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        Boolean ret = ViveMain.MC_MODS.endermanHelper().lookForPlayerContinueUse(this);
        if (ret != null) {
            return ret;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (ViveMain.MC_MODS.endermanHelper().lookForPlayerTick(this)) {
            super.tick();
        }
    }

    @Override
    public Object getEnderman() {
        return this.enderman;
    }

    @Override
    public void setPendingTarget(Object pendingTarget) {
        this.pendingTarget = pendingTarget;
    }

    @Override
    public Object getPendingTarget() {
        return this.pendingTarget;
    }

    @Override
    public void setTarget(Object target) {
        if (target != null) {
            this.target = (net.minecraft.world.entity.LivingEntity) target;
        } else {
            this.target = null;
        }
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public void setAggroTime(int aggroTime) {
        this.aggroTime = aggroTime;
    }

    @Override
    public void setTeleportTime(int teleportTime) {
        this.teleportTime = teleportTime;
    }

    @Override
    public int getAggroTime() {
        return this.aggroTime;
    }

    @Override
    public int getTeleportTime() {
        return this.teleportTime;
    }

    @Override
    public void superStart() {
        super.start();
    }

    @Override
    public void setStartAggroConditions(Object condition) {
        this.startAggroConditions = condition;
    }

    @Override
    public Object getStartAggroConditions() {
        return this.startAggroConditions;
    }

    @Override
    public void setContinueAggroConditions(Object condition) {
        this.continueAggroConditions = condition;
    }

    @Override
    public Object getContinueAggroConditions() {
        return this.continueAggroConditions;
    }
}
