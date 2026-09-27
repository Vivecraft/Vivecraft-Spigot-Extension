package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * indicates that the server supports the roomscale attack packet
 */
public final class RoomscaleAttackPayloadS2C implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ROOMSCALE_ATTACK;
    }
}
