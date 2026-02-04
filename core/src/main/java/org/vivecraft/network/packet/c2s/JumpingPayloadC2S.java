package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * indicates that the client did a climbey jump
 */
public final class JumpingPayloadC2S implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.JUMPING;
    }
}
