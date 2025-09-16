package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds the clients wanted teleport position
 *
 */
public final class TeleportPayloadC2S implements VivecraftPayloadC2S {
    public final float x;
    public final float y;
    public final float z;

    /**
     * @param x x coordinate the player want to teleport to
     * @param y y coordinate the player want to teleport to
     * @param z z coordinate the player want to teleport to
     */
    public TeleportPayloadC2S(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.TELEPORT;
    }

    public static TeleportPayloadC2S read(DataInputStream buffer) throws IOException {
        return new TeleportPayloadC2S(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }
}
