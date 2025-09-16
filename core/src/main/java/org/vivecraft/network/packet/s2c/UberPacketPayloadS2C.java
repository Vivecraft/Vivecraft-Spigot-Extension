package org.vivecraft.network.packet.s2c;

import org.vivecraft.data.VrPlayerState;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * holds a players data
 *
 */
public final class UberPacketPayloadS2C implements VivecraftPayloadS2C {
    public final UUID playerID;
    public final VrPlayerState state;
    public final float worldScale;
    public final float heightScale;

    /**
     * @param playerID    UUID of the player this data is for
     * @param state       vr state of the player
     * @param worldScale  world scale of the player
     * @param heightScale calibrated height scale of the player
     */
    public UberPacketPayloadS2C(UUID playerID, VrPlayerState state, float worldScale, float heightScale) {
        this.playerID = playerID;
        this.state = state;
        this.worldScale = worldScale;
        this.heightScale = heightScale;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.UBERPACKET;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        BufferUtils.writeUUID(buffer, this.playerID);
        this.state.serialize(buffer);
        buffer.writeFloat(this.worldScale);
        buffer.writeFloat(this.heightScale);
    }
}
