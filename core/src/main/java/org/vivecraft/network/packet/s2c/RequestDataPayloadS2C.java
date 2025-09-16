package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * indicates that the server wants vr data from the client
 */
public final class RequestDataPayloadS2C implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.REQUESTDATA;
    }
}
