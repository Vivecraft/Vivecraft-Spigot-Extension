package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * holds all server vr changes that are at non-default values
 *
 */
public final class ServerVrChangesS2CPacket implements VivecraftPayloadS2C {
    public final Map<String, String> changes;

    /**
     * @param changes map of setting/value pairs of non-default settings
     */
    public ServerVrChangesS2CPacket(Map<String, String> changes) {
        this.changes = changes;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.SERVER_VR_CHANGES;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        for (Map.Entry<String, String> entry : this.changes.entrySet()) {
            BufferUtils.writeMCString(buffer, entry.getKey());
            BufferUtils.writeMCString(buffer, entry.getValue());
        }
    }
}
