package org.vivecraft.compat_impl.mc_1_8;

import org.vivecraft.ViveMain;
import org.vivecraft.accessors.CreeperMapping;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.MobMapping;
import org.vivecraft.accessors.SwellGoalMapping;
import org.vivecraft.compat.entities.CreeperHelper;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionMethod;

public class CreeperHelper_1_8 implements CreeperHelper {

    protected ReflectionMethod Creeper_getSwellDir;
    protected ReflectionMethod Mob_getTarget;
    protected ReflectionMethod Entity_distanceToSqr;

    protected ReflectionConstructor VRSwellGoal_Constructor;
    protected Class<?> VRSwellGoal;
    protected Class<?> SwellGoal;

    public CreeperHelper_1_8() {
        this.init();
    }

    protected void init() {
        this.VRSwellGoal_Constructor = ReflectionConstructor.getCompat("org.vivecraft.compat_impl.mc_X_X.VRSwellGoal",
            ClassGetter.getClass(true, CreeperMapping.MAPPING));
        this.VRSwellGoal = this.VRSwellGoal_Constructor.constructor.getDeclaringClass();
        this.SwellGoal = ClassGetter.getClass(true, SwellGoalMapping.MAPPING);

        this.Creeper_getSwellDir = ReflectionMethod.getMethod(CreeperMapping.METHOD_GET_SWELL_DIR);
        this.Mob_getTarget = ReflectionMethod.getMethod(MobMapping.METHOD_GET_TARGET);
        this.Entity_distanceToSqr = ReflectionMethod.getMethod(EntityMapping.METHOD_DISTANCE_TO_SQR);
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
    public boolean creeperVrCheck(Object creeper) {
        Object target = this.Mob_getTarget.invoke(creeper);
        return (int) this.Creeper_getSwellDir.invoke(creeper) > 0 ||
            (double) this.Entity_distanceToSqr.invoke(creeper, target) <
                ViveMain.CONFIG.creeperSwellDistance.get() * ViveMain.CONFIG.creeperSwellDistance.get();
    }
}
