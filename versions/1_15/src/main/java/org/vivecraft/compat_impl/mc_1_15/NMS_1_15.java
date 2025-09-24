package org.vivecraft.compat_impl.mc_1_15;

import org.bukkit.util.Vector;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.compat_impl.mc_1_14_4.NMS_1_14_4;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_15 extends NMS_1_14_4 {
    protected ReflectionMethod Entity_position;
    protected ReflectionMethod Entity_setPosRaw;

    @Override
    protected void initPosition() {
        this.Entity_position = ReflectionMethod.getMethod(EntityMapping.METHOD_POSITION);
        this.Entity_setPosRaw = ReflectionMethod.getMethod(EntityMapping.METHOD_SET_POS_RAW);
    }

    @Override
    public Vector getEntityPosition(Object nmsEntity) {
        Object pos = this.Entity_position.invoke(nmsEntity);
        return new Vector(
            (double) this.Vec3_X.get(pos),
            (double) this.Vec3_Y.get(pos),
            (double) this.Vec3_Z.get(pos));
    }

    @Override
    protected void setPosition(Object serverPlayer, double x, double y, double z) {
        this.Entity_setPosRaw.invoke(serverPlayer, x, y, z);
    }
}
