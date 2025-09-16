package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * packet that holds if a player switched to VR or NONVR
 *
 */
public final class VRActivePayloadS2C implements VivecraftPayloadS2C {
    public final boolean vr;
    public final UUID playerID;

    /**
     * @param vr       if the player is now in VR
     * @param playerID uuid of the player that switched vr state
     */
    public VRActivePayloadS2C(boolean vr, UUID playerID) {
        this.vr = vr;
        this.playerID = playerID;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.IS_VR_ACTIVE;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.vr);
        BufferUtils.writeUUID(buffer, this.playerID);
    }
}
