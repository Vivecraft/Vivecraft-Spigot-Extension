package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds the clients height scale
 *
 */
public final class HeightPayloadC2S implements VivecraftPayloadC2S {
    public final float heightScale;

    /**
     * @param heightScale players calibrated height scale
     */
    public HeightPayloadC2S(float heightScale) {
        this.heightScale = heightScale;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HEIGHT;
    }

    public static HeightPayloadC2S read(DataInputStream buffer) throws IOException {
        return new HeightPayloadC2S(buffer.readFloat());
    }
}
