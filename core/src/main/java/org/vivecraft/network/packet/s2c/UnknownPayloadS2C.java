package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * unknown received client packet
 */
public final class UnknownPayloadS2C implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return null;
    }
}
