package org.vivecraft.network;

public class NetworkConstants {

    public static final String CHANNEL = "vivecraft:data";

    // maximum supported network version
    public static final int MAX_SUPPORTED_NETWORK_PROTOCOL = NetworkVersion.values()
        [NetworkVersion.values().length - 1].protocolVersion();
    // minimum supported network version
    public static final int MIN_SUPPORTED_NETWORK_PROTOCOL = NetworkVersion.NEW_NETWORKING.protocolVersion();
}
