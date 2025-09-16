package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * indicates that the server allows attacks while blocking
 *
 */
public final class AttackWhileBlockingPayloadS2C implements VivecraftPayloadS2C {
    public final boolean allowed;

    /**
     * @param allowed if attacks while blocking are allowed
     */
    public AttackWhileBlockingPayloadS2C(boolean allowed) {
        this.allowed = allowed;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ATTACK_WHILE_BLOCKING;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
    }
}
