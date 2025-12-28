package org.vivecraft.network.packet.c2s;

import org.joml.Vector3fc;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds a direction the aim should be overridden to
 */
public final class AimDirOverridePayloadC2S implements VivecraftPayloadC2S {
    public final Vector3fc direction;

    /**
     * @param direction direction the player aimed in
     */
    public AimDirOverridePayloadC2S(Vector3fc direction) {
        this.direction = direction;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_DIRECTION_OVERRIDE;
    }

    public static AimDirOverridePayloadC2S read(DataInputStream buffer) throws IOException {
        return new AimDirOverridePayloadC2S(BufferUtils.readVector3f(buffer));
    }
}
