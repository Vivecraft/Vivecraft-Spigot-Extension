package org.vivecraft.network;

public class NetworkConstants {

    public static final String CHANNEL = "vivecraft:data";

    public static final int NETWORK_VERSION_LEGACY = -1;
    // adds full body tracker data
    public static final int NETWORK_VERSION_FBT = 1;
    // adds dual wielding packet and server logic
    public static final int NETWORK_VERSION_DUAL_WIELDING = 2;
    // adds the head as a valid active BodyPart, and adds a useForAim flag
    public static final int NETWORK_VERSION_HEAD_AIM = 3;
    // allows sending haptic events to the client
    public static final int NETWORK_VERSION_HAPTIC_PACKET = 4;
    // adds a packet, to inform the client what vr changes are on non default values
    public static final int NETWORK_VERSION_SERVER_VR_CHANGES = 5;
    // adds packets to send/receive damage directions
    public static final int NETWORK_VERSION_DAMAGE_DIRECTION = 6;
    // adds features to toggle settings after initial connection
    public static final int NETWORK_VERSION_OPTION_TOGGLE = 7;

    // maximum supported network version
    public static final int MAX_SUPPORTED_NETWORK_VERSION = NETWORK_VERSION_OPTION_TOGGLE;
    // minimum supported network version
    public static final int MIN_SUPPORTED_NETWORK_VERSION = 0;
}
