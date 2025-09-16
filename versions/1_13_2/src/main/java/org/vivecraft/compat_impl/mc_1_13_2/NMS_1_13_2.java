package org.vivecraft.compat_impl.mc_1_13_2;

import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.vivecraft.accessors.LivingEntityMapping;
import org.vivecraft.compat_impl.mc_1_13.NMS_1_13;
import org.vivecraft.util.AABB;
import org.vivecraft.util.reflection.ReflectionField;

public class NMS_1_13_2 extends NMS_1_13 {


    @Override
    protected void initAABB() {
        // don't need the AABB ones
    }

    @Override
    public AABB getEntityAABB(Entity entity) {
        BoundingBox aabb = entity.getBoundingBox();
        return new AABB(
            aabb.getMinX(), aabb.getMinY(), aabb.getMinZ(),
            aabb.getMaxX(), aabb.getMaxY(), aabb.getMaxZ());
    }
}
