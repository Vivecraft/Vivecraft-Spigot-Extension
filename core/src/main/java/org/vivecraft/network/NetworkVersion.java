package org.vivecraft.network;

public enum NetworkVersion {
    // clients that used the old installer
    LEGACY,
    // allows the client to send the data in a single packet and switch back to NONVR
    NEW_NETWORKING,
    // adds full body tracker data
    FBT,
    // adds dual wielding packet and server logic
    DUAL_WIELDING,
    // adds the head as a valid active BodyPart, and adds a useForAim flag
    HEAD_AIM,
    // allows sending haptic events to the client
    HAPTIC_PACKET,
    // adds a packet, to inform the client what vr changes are on non default values
    SERVER_VR_CHANGES,
    // adds packets to send/receive damage directions
    DAMAGE_DIRECTION,
    // adds possibility to toggle settings after initial connection
    OPTION_TOGGLE;

    public static NetworkVersion fromProtocolVersion(int protocolVersion) {
        return values()[protocolVersion + 1];
    }

    /**
     * @return The protocol version that is sent between server/client. This is different to the ordinal,
     * because legacy is -1
     */
    public int protocolVersion() {
        return this.ordinal() - 1;
    }

    /**
     * @return The vivecraft version that added this network version
     */
    public String getViveVersion() {
        switch (this) {
            case LEGACY:
                return "legacy";
            case NEW_NETWORKING:
                return "1.0.0";
            case FBT:
            case DUAL_WIELDING:
                return "1.2.0";
            case HEAD_AIM:
            case HAPTIC_PACKET:
            case SERVER_VR_CHANGES:
            case DAMAGE_DIRECTION:
            case OPTION_TOGGLE:
                return "1.3.0";
            default:
                return "unknown";
        }
    }

    /**
     * checks if {@code other} supports the features of {@code this} NetworkVersion
     *
     * @param other other NetworkVersion to test
     * @return if the other Network version supports the features of this version
     */
    public boolean accepts(NetworkVersion other) {
        return this.ordinal() <= other.ordinal();
    }

    @Override
    public String toString() {
        return this.name() + ": " + this.protocolVersion();
    }
}
