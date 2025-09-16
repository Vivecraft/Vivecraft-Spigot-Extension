package org.vivecraft.data;

import org.joml.Vector3fc;
import org.vivecraft.util.BufferUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * holds a tracker device Pose
 *
 */
public final class TrackerPose {
    public final Vector3fc position;

    /**
     * @param position position of the device in player local space
     */
    public TrackerPose(Vector3fc position) {
        this.position = position;
    }

    /**
     * @param buffer buffer to read from
     * @return a Pose read from the given {@code buffer}
     */
    public static TrackerPose deserialize(DataInputStream buffer) throws IOException {
        return new TrackerPose(BufferUtils.readVector3f(buffer));
    }

    /**
     * writes this Pose to the given {@code buffer}
     *
     * @param buffer buffer to write to
     */
    public void serialize(DataOutputStream buffer) throws IOException {
        BufferUtils.writeVector3f(buffer, this.position);
    }
}
