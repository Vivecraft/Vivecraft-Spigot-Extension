package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * packet that holds if the server allows switching between VR and NONVR
 *
 */
public final class VRSwitchingPayloadS2C implements VivecraftPayloadS2C {
    public final boolean allowed;

    /**
     * @param allowed if hot switching is allowed
     */
    public VRSwitchingPayloadS2C(boolean allowed) {
        this.allowed = allowed;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.VR_SWITCHING;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
    }
}
