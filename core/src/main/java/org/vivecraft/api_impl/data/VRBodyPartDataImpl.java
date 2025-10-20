package org.vivecraft.api_impl.data;

import org.bukkit.util.Vector;
import org.joml.Quaternionfc;
import org.vivecraft.api.data.VRBodyPartData;

public final class VRBodyPartDataImpl implements VRBodyPartData {
    private final Vector pos;
    private final Vector dir;
    private final Quaternionfc rot;

    public VRBodyPartDataImpl(Vector pos, Vector dir, Quaternionfc rot) {
        this.pos = pos;
        this.dir = dir;
        this.rot = rot;
    }

    @Override
    public Vector getPos() {
        return new Vector().copy(this.pos);
    }

    @Override
    public Vector getDir() {
        return new Vector().copy(this.dir);
    }

    @Override
    public double getPitch() {
        return Math.asin(this.dir.getY() / this.dir.length());
    }

    @Override
    public double getYaw() {
        return Math.atan2(-this.dir.getX(), this.dir.getZ());
    }

    @Override
    public double getRoll() {
        return -Math.atan2(2.0F * (this.rot.x() * this.rot.y() + this.rot.w() * this.rot.z()),
            this.rot.w() * this.rot.w() - this.rot.x() * this.rot.x() +
                this.rot.y() * this.rot.y() -
                this.rot.z() * this.rot.z());
    }

    @Override
    public Quaternionfc getRotation() {
        return this.rot;
    }

    @Override
    public String toString() {
        return String.format("Position: %s\n" +
            "Direction: %s\n" +
            "Rotation: %s", this.pos, this.dir, this.rot);
    }
}
