package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * indicates that the client requested damage direction data
 */
public final class DamageDirectionPayloadC2S implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DAMAGE_DIRECTION;
    }
}
