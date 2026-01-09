package org.vivecraft.network.packet.c2s;

import org.joml.Vector3fc;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds a position the aim should start from
 */
public final class AimPosOverridePayloadC2S implements VivecraftPayloadC2S {
    public final Vector3fc position;

    /**
     * @param position position the player aimed from
     */
    public AimPosOverridePayloadC2S(Vector3fc position) {
        this.position = position;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_POSITION_OVERRIDE;
    }

    public static AimPosOverridePayloadC2S read(DataInputStream buffer) throws IOException {
        return new AimPosOverridePayloadC2S(BufferUtils.readVector3f(buffer));
    }
}
