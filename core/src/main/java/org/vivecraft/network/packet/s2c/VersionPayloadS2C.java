package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * packet that holds the Vivecraft server version
 *
 */
public final class VersionPayloadS2C implements VivecraftPayloadS2C {
    public final String version;

    /**
     * @param version Version String of the server
     */
    public VersionPayloadS2C(String version) {
        this.version = version;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.VERSION;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        BufferUtils.writeMCString(buffer, this.version);
    }
}
