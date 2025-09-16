package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * indicates that the server supports dual wielding
 *
 */
public final class DualWieldingPayloadS2C implements VivecraftPayloadS2C {
    public final boolean allowed;

    /**
     * @param allowed if dual wielding is allowed
     */
    public DualWieldingPayloadS2C(boolean allowed) {
        this.allowed = allowed;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DUAL_WIELDING;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
    }
}
