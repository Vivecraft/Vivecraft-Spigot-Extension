package org.vivecraft.network.packet.s2c;

import org.joml.Vector3fc;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * contains the last direction the player got damage from
 */
public final class DamageDirectionPayloadS2C implements VivecraftPayloadS2C {
    public final Vector3fc damageDir;

    public DamageDirectionPayloadS2C(Vector3fc damageDir) {
        this.damageDir = damageDir;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DAMAGE_DIRECTION;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        BufferUtils.writeVector3f(buffer, this.damageDir);
    }
}
