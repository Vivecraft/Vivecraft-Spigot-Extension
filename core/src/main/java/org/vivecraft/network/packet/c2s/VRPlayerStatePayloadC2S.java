package org.vivecraft.network.packet.c2s;

import org.vivecraft.data.VrPlayerState;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds the clients complete vr state
 *
 */
public final class VRPlayerStatePayloadC2S implements VivecraftPayloadC2S {
    public final VrPlayerState playerState;

    /**
     * @param playerState vr state of the player
     */
    public VRPlayerStatePayloadC2S(VrPlayerState playerState) {
        this.playerState = playerState;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.VR_PLAYER_STATE;
    }

    public static VRPlayerStatePayloadC2S read(DataInputStream buffer) throws IOException {
        return new VRPlayerStatePayloadC2S(VrPlayerState.deserialize(buffer, 0));
    }
}
