package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds the clients current bow draw percent
 *
 */
public final class DrawPayloadC2S implements VivecraftPayloadC2S {
    public final float draw;

    /**
     * @param draw how far the player has pulled the bow
     */
    public DrawPayloadC2S(float draw) {
        this.draw = draw;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DRAW;
    }

    public static DrawPayloadC2S read(DataInputStream buffer) throws IOException {
        return new DrawPayloadC2S(buffer.readFloat());
    }
}
