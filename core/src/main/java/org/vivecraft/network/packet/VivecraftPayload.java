package org.vivecraft.network.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public interface VivecraftPayload {

    /**
     * writes this data packet to the given buffer
     *
     * @param buffer Buffer to write to
     */
    default void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
    }

    /**
     * returns the PacketIdentifier associated with this packet
     */
    PayloadIdentifier payloadId();
}
