package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * packet that holds the network protocol version the server will use to talk to the client
 *
 */
public final class NetworkVersionPayloadS2C implements VivecraftPayloadS2C {
    public final int version;

    /**
     * @param version network protocol version the server will use
     */
    public NetworkVersionPayloadS2C(int version) {
        this.version = version;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.NETWORK_VERSION;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.version);
    }
}
