package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.NetworkVersion;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * indicates that the server supports direct teleports
 *
 */
public final class TeleportPayloadS2C implements VivecraftPayloadS2C {
    public final boolean allowed;
    public final NetworkVersion targetNetworkVersion;

    /**
     * @param allowed              indicates if teleports are allowed
     * @param targetNetworkVersion network version of the target player, to not send additional data, if they don't support it
     */
    public TeleportPayloadS2C(boolean allowed, NetworkVersion targetNetworkVersion) {
        this.allowed = allowed;
        this.targetNetworkVersion = targetNetworkVersion;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.TELEPORT;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        // old clients don't expect additional data
        if (NetworkVersion.OPTION_TOGGLE.accepts(this.targetNetworkVersion)) {
            buffer.writeBoolean(this.allowed);
        }
    }
}
