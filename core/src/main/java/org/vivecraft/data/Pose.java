package org.vivecraft.data;

import org.bukkit.util.Vector;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api_impl.data.VRBodyPartDataImpl;
import org.vivecraft.util.BufferUtils;
import org.vivecraft.util.MathUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * holds a device Pose
 *
 */
public final class Pose {
    public final Vector3fc position;
    public final Quaternionfc orientation;

    /**
     * @param position    position of the device in player local space
     * @param orientation orientation of the device in world space
     */
    public Pose(Vector3fc position, Quaternionfc orientation) {
        this.position = position;
        this.orientation = orientation;
    }

    /**
     * offsets teh Pose by th given offset
     *
     * @param offset offset to add
     * @return new offset position
     */
    public Pose offset(Vector offset) {
        return new Pose(new Vector3f(
            (float) (this.position.x() + offset.getX()),
            (float) (this.position.y() + offset.getY()),
            (float) (this.position.z() + offset.getZ())), this.orientation);
    }

    /**
     * @param buffer buffer to read from
     * @return a Pose read from the given {@code buffer}
     */
    public static Pose deserialize(DataInputStream buffer) throws IOException {
        return new Pose(
            BufferUtils.readVector3f(buffer),
            BufferUtils.readQuat(buffer)
        );
    }

    /**
     * writes this Pose to the given {@code buffer}
     *
     * @param buffer buffer to write to
     */
    public void serialize(DataOutputStream buffer) throws IOException {
        BufferUtils.writeVector3f(buffer, this.position);
        BufferUtils.writeQuat(buffer, this.orientation);
    }

    /**
     * @param playerPos The current position of the player.
     * @return This Pose as VRBodyPartData for use with the API.
     */
    public VRBodyPartData asBodyPartData(Vector playerPos) {
        return new VRBodyPartDataImpl(MathUtils.toBukkitVec(this.position).add(playerPos),
            MathUtils.toBukkitVec(this.orientation.transform(MathUtils.BACK, new Vector3f())), this.orientation);
    }
}
