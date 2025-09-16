package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds the clients world scale
 *
 */
public final class WorldScalePayloadC2S implements VivecraftPayloadC2S {
    public final float worldScale;

    /**
     * @param worldScale world scale set by the player
     */
    public WorldScalePayloadC2S(float worldScale) {
        this.worldScale = worldScale;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.WORLDSCALE;
    }

    public static WorldScalePayloadC2S read(DataInputStream buffer) throws IOException {
        return new WorldScalePayloadC2S(buffer.readFloat());
    }
}
