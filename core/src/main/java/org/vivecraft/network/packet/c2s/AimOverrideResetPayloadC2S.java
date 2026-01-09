package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * resets the aim override
 */
public final class AimOverrideResetPayloadC2S implements VivecraftPayloadC2S {

    public final int ticks;

    public AimOverrideResetPayloadC2S(final int ticks) {
        this.ticks = ticks;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_OVERRIDE_RESET;
    }

    public static AimOverrideResetPayloadC2S read(DataInputStream buffer) throws IOException {
        return new AimOverrideResetPayloadC2S(buffer.readByte());
    }
}
