package org.vivecraft.compat_impl.mc_1_8;

import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.entities.CreeperHelper;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class CreeperHelper_1_8 implements CreeperHelper {

    protected ReflectionMethod Creeper_getSwellDir;
    protected ReflectionMethod Creeper_setSwellDir;
    protected ReflectionMethod Mob_getTarget;
    protected ReflectionMethod Entity_distanceToSqr;

    protected ReflectionMethod Mob_getSensing;
    protected ReflectionMethod Sensing_hasLineOfSight;
    protected ReflectionField SwellGoal_target;

    protected ReflectionConstructor VRSwellGoal_Constructor;
    protected Class<?> VRSwellGoal;
    protected Class<?> SwellGoal;

    public CreeperHelper_1_8() {
        this.init();
    }

    protected void init() {
        this.VRSwellGoal_Constructor = ReflectionConstructor.getCompat("VRSwellGoal",
            ClassGetter.getClass(true, CreeperMapping.MAPPING));
        this.VRSwellGoal = this.VRSwellGoal_Constructor.constructor.getDeclaringClass();
        this.SwellGoal = ClassGetter.getClass(true, SwellGoalMapping.MAPPING);

        this.Creeper_getSwellDir = ReflectionMethod.getMethod(CreeperMapping.METHOD_GET_SWELL_DIR);
        this.Creeper_setSwellDir = ReflectionMethod.getMethod(CreeperMapping.METHOD_SET_SWELL_DIR);
        this.Mob_getTarget = ReflectionMethod.getMethod(MobMapping.METHOD_GET_TARGET);
        this.Entity_distanceToSqr = ReflectionMethod.getMethod(EntityMapping.METHOD_DISTANCE_TO_SQR);
        this.Mob_getSensing = ReflectionMethod.getMethod(MobMapping.METHOD_GET_SENSING);
        this.Sensing_hasLineOfSight = ReflectionMethod.getMethod(SensingMapping.METHOD_HAS_LINE_OF_SIGHT);
        this.SwellGoal_target = ReflectionField.getField(SwellGoalMapping.FIELD_TARGET);
    }

    @Override
    public boolean isSwellGoal(Object goal) {
        return this.SwellGoal.isInstance(goal) || this.VRSwellGoal.isInstance(goal);
    }

    @Override
    public Object getCreeperSwellGoal(Object creeper) {
        return this.VRSwellGoal_Constructor.newInstance(creeper);
    }

    @Override
    public boolean creeperVrSwellCheck(Object creeper) {
        Object target = this.Mob_getTarget.invoke(creeper);
        return (int) this.Creeper_getSwellDir.invoke(creeper) > 0 ||
            (double) this.Entity_distanceToSqr.invoke(creeper, target) <
                ViveMain.CONFIG.creeperSwellDistance.get() * ViveMain.CONFIG.creeperSwellDistance.get();
    }

    @Override
    public void creeperVrSwellTick(Object creeper, Object swellGoal) {
        Object target = this.SwellGoal_target.get(swellGoal);
        double distance = ViveMain.CONFIG.creeperSwellDistance.get() + 4;
        if (target == null ||
            (double) this.Entity_distanceToSqr.invoke(creeper, target) > distance * distance ||
            !(boolean) this.Sensing_hasLineOfSight.invoke(this.Mob_getSensing.invoke(creeper), target))
        {
            this.Creeper_setSwellDir.invoke(creeper, -1);
        } else {
            this.Creeper_setSwellDir.invoke(creeper, 1);
        }
    }
}
