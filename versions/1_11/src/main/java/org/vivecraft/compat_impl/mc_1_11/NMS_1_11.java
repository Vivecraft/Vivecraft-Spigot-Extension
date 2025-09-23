package org.vivecraft.compat_impl.mc_1_11;

import org.bukkit.util.Vector;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.compat_impl.mc_1_9.NMS_1_9;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_11 extends NMS_1_9 {

    protected ReflectionMethod Entity_getEyePosition;

    @Override
    protected void init() {
        super.init();
        this.Entity_getEyePosition = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_EYE_POSITION);
    }

    @Override
    protected Vector getEyePosition(Object nmsEntity) {
        return vec3ToVector(this.Entity_getEyePosition.invoke(nmsEntity, 1F));
    }
}
