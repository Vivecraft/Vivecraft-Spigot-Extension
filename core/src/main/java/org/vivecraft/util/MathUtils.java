package org.vivecraft.util;

import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class MathUtils {

    public static final float PI = (float) Math.PI;
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
    public static final float RAD_TO_DEG = (float) (180.0 / Math.PI);

    public static final Vector3fc FORWARD = new Vector3f(0.0F, 0.0F, 1.0F);
    public static final Vector3fc BACK = new Vector3f(0.0F, 0.0F, -1.0F);
    public static final Vector3fc LEFT = new Vector3f(1.0F, 0.0F, 0.0F);
    public static final Vector3fc RIGHT = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static final Vector3fc UP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3fc DOWN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static final Vector3fc ZERO = new Vector3f();

    // hand grip is usually offset 0° to 45° from the controller pointing direction, this is probably a solid middle ground
    public static final Vector3fc GRIP_FORWARD = new Vector3f(0, 0, 1).rotateX(DEG_TO_RAD * 20F);

    public static Vector toBukkitVec(Vector3fc v) {
        return new Vector(v.x(), v.y(), v.z());
    }

    public static Vector3f toJomlVec(Vector v) {
        return new Vector3f((float) v.getX(), (float) v.getY(), (float) v.getZ());
    }

    public static Vector3f subToJomlVec(Vector a, Vector b) {
        return new Vector3f((float) (a.getX() - b.getX()), (float) (a.getY() - b.getY()),
            (float) (a.getZ() - b.getZ()));
    }
}
