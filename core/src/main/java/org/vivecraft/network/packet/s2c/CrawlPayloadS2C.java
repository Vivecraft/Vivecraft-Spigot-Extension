package org.vivecraft.network.packet.s2c;

import org.vivecraft.network.NetworkConstants;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * indicates that the server supports roomscale crawling
 *
 */
public final class CrawlPayloadS2C implements VivecraftPayloadS2C {
    public final boolean allowed;
    public final int targetNetworkVersion;

    /**
     * @param allowed              indicates if crawling is allowed
     * @param targetNetworkVersion network version of the target player, to not send additional data, if they don't support it
     */
    public CrawlPayloadS2C(boolean allowed, int targetNetworkVersion) {
        this.allowed = allowed;
        this.targetNetworkVersion = targetNetworkVersion;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CRAWL;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        // old clients don't expect additional data
        if (this.targetNetworkVersion >= NetworkConstants.NETWORK_VERSION_OPTION_TOGGLE) {
            buffer.writeBoolean(this.allowed);
        }
    }
}
