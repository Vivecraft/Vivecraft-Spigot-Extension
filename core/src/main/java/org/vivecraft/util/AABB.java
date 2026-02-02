package org.vivecraft.util;

import org.bukkit.util.Vector;

import java.util.Optional;

/**
 * this is basically a stripped copy of mcs AABB
 */
public class AABB {

    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public AABB(Vector start, Vector end) {
        this(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
    }

    public Vector getCenter() {
        return new Vector(this.minX + this.maxX, this.minY + this.maxY, this.minZ + this.maxZ).multiply(0.5);
    }

    public float getWidth() {
        return (float) (this.maxX - this.minX);
    }

    /**
     * makes the AABB larger in all directions
     */
    public AABB inflate(double x, double y, double z) {
        double d = this.minX - x;
        double e = this.minY - y;
        double f = this.minZ - z;
        double g = this.maxX + x;
        double h = this.maxY + y;
        double i = this.maxZ + z;
        return new AABB(d, e, f, g, h, i);
    }

    public AABB inflate(double value) {
        return this.inflate(value, value, value);
    }

    /**
     * makes the AABB larger in the given direction
     */
    public AABB expandTowards(Vector vector) {
        return this.expandTowards(vector.getX(), vector.getY(), vector.getZ());
    }

    public AABB expandTowards(double x, double y, double z) {
        double newMinX = this.minX;
        double newMinY = this.minY;
        double newMinz = this.minZ;
        double newMaxX = this.maxX;
        double newMaxY = this.maxY;
        double newMaxZ = this.maxZ;

        if (x < 0.0) {
            newMinX += x;
        } else if (x > 0.0) {
            newMaxX += x;
        }

        if (y < 0.0) {
            newMinY += y;
        } else if (y > 0.0) {
            newMaxY += y;
        }

        if (z < 0.0) {
            newMinz += z;
        } else if (z > 0.0) {
            newMaxZ += z;
        }

        return new AABB(newMinX, newMinY, newMinz, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * check if the given point is inside this AABB
     *
     * @param v point to check
     * @return if the point in inside this AABB
     */
    public boolean contains(Vector v) {
        return v.getX() >= this.minX && v.getX() < this.maxX &&
            v.getY() >= this.minY && v.getY() < this.maxY &&
            v.getZ() >= this.minZ && v.getZ() < this.maxZ;
    }

    public boolean intersects(AABB other) {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(double x1, double y1, double z1, double x2, double y2, double z2) {
        return this.minX < x2 && this.maxX > x1 && this.minY < y2 && this.maxY > y1 && this.minZ < z2 && this.maxZ > z1;
    }

    /**
     * clips the ray defined by {@code from} and {@code to} with this AABB
     * only counts intersections from outside, si if the ray starts inside, no hit will be returned.
     * Algorithm from <a href="https://tavianator.com/2011/ray_box.html">here</a>
     *
     * @param from start of the ray
     * @param to   end of the ray
     * @return Optional containing the hit point, or empty if there is none or hte ray starts inside the AABB
     */
    public Optional<Vector> clip(Vector from, Vector to) {
        Vector dir = new Vector().copy(to).subtract(from);
        double l = dir.length();
        dir.normalize();

        Vector t1 = new Vector(this.minX, this.minY, this.minZ).subtract(from).divide(dir);
        Vector t2 = new Vector(this.maxX, this.maxY, this.maxZ).subtract(from).divide(dir);

        Vector tMin = Vector.getMinimum(t1, t2);
        Vector tMax = Vector.getMaximum(t1, t2);

        double min = Math.max(tMin.getX(), Math.max(tMin.getY(), tMin.getZ()));
        double max = Math.min(tMax.getX(), Math.min(tMax.getY(), tMax.getZ()));

        if (max >= Math.max(min, 0) && min < l) {
            return Optional.of(dir.multiply(min).add(from));
        } else {
            return Optional.empty();
        }
    }
}
