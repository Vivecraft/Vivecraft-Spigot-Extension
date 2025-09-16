package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * unknown received server packet
 */
public final class UnknownPayloadC2S implements VivecraftPayloadC2S {
    @Override
    public PayloadIdentifier payloadId() {
        return null;
    }
}
