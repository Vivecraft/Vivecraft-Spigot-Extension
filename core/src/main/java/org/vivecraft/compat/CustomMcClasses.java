package org.vivecraft.compat;

import org.vivecraft.accessors.CreeperMapping;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;

public class CustomMcClasses {

    private ReflectionConstructor VRSwellGoal;

    public CustomMcClasses() {
        this.VRSwellGoal = ReflectionConstructor.getCompat("org.vivecraft.compat_impl.mc_X_X.VRSwellGoal",
            ClassGetter.getClass(true, CreeperMapping.MAPPING));
    }

    public Object getCreeperSwellGoal(Object creeper) {
        return this.VRSwellGoal.newInstance(creeper);
    }
}
