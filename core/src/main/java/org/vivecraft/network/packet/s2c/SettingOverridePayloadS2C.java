package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * holds a map of settings the server has overridden
 *
 */
public final class SettingOverridePayloadS2C implements VivecraftPayloadS2C {
    public final Map<String, String> overrides;
    public final boolean clear;

    /**
     * @param overrides map with the key as the setting, and the value as the override
     * @param clear     tells the client to remove any override mentioned in this packet
     */
    public SettingOverridePayloadS2C(Map<String, String> overrides, boolean clear) {
        this.overrides = overrides;
        this.clear = clear;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.SETTING_OVERRIDE;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        for (Map.Entry<String, String> entry : this.overrides.entrySet()) {
            BufferUtils.writeMCString(buffer, entry.getKey());
            BufferUtils.writeMCString(buffer, entry.getValue());
        }

        BufferUtils.writeMCString(buffer, "clearOverrides");
        BufferUtils.writeMCString(buffer, String.valueOf(this.clear));
    }
}
