package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.NetworkConstants;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * packet that holds the Vivecraft client version, mode and network version support
 *
 */
public final class VersionPayloadC2S implements VivecraftPayloadC2S {
    public final String version;
    public final boolean vr;
    public final int maxVersion;
    public final int minVersion;
    public final boolean legacy;

    /**
     * @param version    Version String of the client
     * @param vr         if the client was in vr when they connected
     * @param maxVersion maximum supported network protocol version
     * @param minVersion minimum supported network protocol version
     * @param legacy     if the client is a legacy client, before the network protocol version was added
     */
    public VersionPayloadC2S(String version, boolean vr, int maxVersion, int minVersion, boolean legacy) {
        this.version = version;
        this.vr = vr;
        this.maxVersion = maxVersion;
        this.minVersion = minVersion;
        this.legacy = legacy;
    }

    public VersionPayloadC2S(String version, boolean vr, int maxVersion, int minVersion) {
        this(version, vr, maxVersion, minVersion, false);
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.VERSION;
    }

    public static VersionPayloadC2S read(DataInputStream buffer) throws IOException {
        // need to use String(bytes[]) for legacy compatibility
        byte[] stringBytes = new byte[buffer.available()];
        buffer.read(stringBytes);
        String[] parts = new String(stringBytes).split("\\n");

        boolean vr = !parts[0].contains("NONVR");
        if (parts.length >= 3) {
            return new VersionPayloadC2S(parts[0], vr, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), false);
        } else {
            return new VersionPayloadC2S(parts[0], vr, NetworkConstants.NETWORK_VERSION_LEGACY,
                NetworkConstants.NETWORK_VERSION_LEGACY, true);
        }
    }
}
