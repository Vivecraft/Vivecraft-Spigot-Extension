package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds weather the client is in VR or NONVR mode
 *
 */
public final class VRActivePayloadC2S implements VivecraftPayloadC2S {
    public final boolean vr;

    /**
     * @param vr if the client is actively in VR
     */
    public VRActivePayloadC2S(boolean vr) {
        this.vr = vr;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.IS_VR_ACTIVE;
    }

    public static VRActivePayloadC2S read(DataInputStream buffer) throws IOException {
        return new VRActivePayloadC2S(buffer.readBoolean());
    }
}
