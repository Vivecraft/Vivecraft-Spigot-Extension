package org.vivecraft.compat_impl.mc_1_21_3;

import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_19_4.EndermanHelper_1_19_4;

import java.util.function.Predicate;

public class EndermanHelper_1_21_3 extends EndermanHelper_1_19_4 {

    @Override
    protected void init() {}

    @Override
    protected void initFindPlayer() {}

    @Override
    protected void initInventory() {}

    @Override
    public Boolean lookForPlayerContinueUse(EndermanLookForPlayerGoalAccessor goal) {
        throw new AssertionError();
    }

    @Override
    protected boolean isAngerInducing(Object target, Object enderman) {
        throw new AssertionError();
    }

    @Override
    protected int adjustedTickDelay(Object goal, int tickDelay) {
        throw new AssertionError();
    }

    @Override
    public void lookForPlayerInit(EndermanLookForPlayerGoalAccessor goal, double distance) {
        throw new AssertionError();
    }

    @Override
    public boolean hasProtection(Object nmsPlayer) {
        throw new AssertionError();
    }

    @Override
    public Predicate isAngryAtPredicate(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public boolean lookForPlayerTick(EndermanLookForPlayerGoalAccessor goal) {
        throw new AssertionError();
    }

    @Override
    public void lookForPlayerStart(EndermanLookForPlayerGoalAccessor goal) {
        throw new AssertionError();
    }

    @Override
    public boolean isFreezeGoal(Object goal) {
        throw new AssertionError();
    }

    @Override
    public Object getEndermanFreezeWhenLookAt(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public void freezeSetFlags(Object goal) {
        throw new AssertionError();
    }

    @Override
    public boolean freezeCanUse(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public void freezeTick(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public void freezeStart(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public Object lookForPlayerNearest(EndermanLookForPlayerGoalAccessor goal, double distance) {
        throw new AssertionError();
    }

    @Override
    public boolean lookForPlayerMustSee() {
        throw new AssertionError();
    }

    @Override
    public void lookForPlayerStop(EndermanLookForPlayerGoalAccessor goal) {
        throw new AssertionError();
    }

    @Override
    public boolean isLookForPlayerGoal(Object goal) {
        throw new AssertionError();
    }

    @Override
    public Object getEndermanLookForPlayer(Object enderman) {
        throw new AssertionError();
    }

    @Override
    protected boolean canPlayerSeeEnderman(Object target, Object enderman) {
        throw new AssertionError();
    }
}
