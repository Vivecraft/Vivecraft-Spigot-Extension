package org.vivecraft.compat_impl.mc_1_14;

import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.compat_impl.mc_1_8.EndermanHelper_1_8;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.EnumSet;

public class EndermanHelper_1_14 extends EndermanHelper_1_8 {

    protected ReflectionMethod Goal_setFlags;
    protected ReflectionField GoalFlag_MOVE;
    protected ReflectionField GoalFlag_JUMP;

    protected ReflectionMethod Mob_getLookControl;
    protected ReflectionMethod LookControl_setLookAt;
    protected ReflectionMethod Mob_getNavigation;
    protected ReflectionMethod PathNavigation_stop;

    protected Class<?> VREndermanFreezeWhenLookAt;
    protected ReflectionConstructor VREndermanFreezeWhenLookAt_Constructor;

    protected Class<?> EndermanFreeze;

    public EndermanHelper_1_14() {
        this.init();
    }

    protected void init() {
        super.init();
        this.Goal_setFlags = ReflectionMethod.getMethod(GoalMapping.METHOD_SET_FLAGS);
        this.GoalFlag_MOVE = ReflectionField.getField(Goal$FlagMapping.FIELD_MOVE);
        this.GoalFlag_JUMP = ReflectionField.getField(Goal$FlagMapping.FIELD_JUMP);

        this.Mob_getLookControl = ReflectionMethod.getMethod(MobMapping.METHOD_GET_LOOK_CONTROL);
        this.LookControl_setLookAt = ReflectionMethod.getMethod(LookControlMapping.METHOD_SET_LOOK_AT);
        this.Mob_getNavigation = ReflectionMethod.getMethod(MobMapping.METHOD_GET_NAVIGATION);
        this.PathNavigation_stop = ReflectionMethod.getMethod(PathNavigationMapping.METHOD_STOP);

        this.EndermanFreeze = ClassGetter.getClass(true, EnderMan$EndermanFreezeWhenLookedAtMapping.MAPPING);

        this.VREndermanFreezeWhenLookAt_Constructor = ReflectionConstructor.getCompat(
            "org.vivecraft.compat_impl.mc_X_X.VREndermanFreezeWhenLookAt",
            ClassGetter.getClass(true, EnderManMapping.MAPPING));
        this.VREndermanFreezeWhenLookAt = this.VREndermanFreezeWhenLookAt_Constructor.constructor.getDeclaringClass();
    }

    @Override
    public boolean isFreezeGoal(Object goal) {
        return this.EndermanFreeze.isInstance(goal) || this.VREndermanFreezeWhenLookAt.isInstance(goal);
    }

    @Override
    public Object getEndermanFreezeWhenLookAt(Object enderman) {
        return this.VREndermanFreezeWhenLookAt_Constructor.newInstance(enderman);
    }

    @Override
    public void freezeSetFlags(Object goal) {
        this.Goal_setFlags.invoke(goal, EnumSet.of((Enum) this.GoalFlag_MOVE.get(), (Enum) this.GoalFlag_JUMP.get()));
    }

    @Override
    public boolean freezeCanUse(Object enderman) {
        Object target = this.Mob_getTarget.invoke(enderman);
        if (this.Player.isInstance(target) &&
            (double) this.Entity_distanceToSqr.invoke(target, enderman) < 256.0)
        {
            return this.canPlayerSeeEnderman(target, enderman);
        }

        return false;
    }

    @Override
    public void freezeTick(Object enderman) {
        Object target = this.Mob_getTarget.invoke(enderman);
        Vector pos = ViveMain.NMS.getViewVectorVR(target);
        if (pos != null) {
            this.LookControl_setLookAt.invoke(this.Mob_getLookControl.invoke(enderman), pos.getX(), pos.getY(),
                pos.getZ());
        }
    }

    @Override
    public void freezeStart(Object enderman) {
        this.PathNavigation_stop.invoke(this.Mob_getNavigation.invoke(enderman));
    }
}
