package org.vivecraft.network.packet.c2s;

import org.vivecraft.data.Pose;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * legacy packet, holds the reversed hand flag and the offhand controller pose
 *
 */
public final class LegacyController1DataPayloadC2S implements VivecraftPayloadC2S {
    public final boolean leftHanded;
    public final Pose offHand;

    /**
     * @param leftHanded if the player has reversed hands set
     * @param offHand    pose of the players main controller
     */
    public LegacyController1DataPayloadC2S(boolean leftHanded, Pose offHand) {
        this.leftHanded = leftHanded;
        this.offHand = offHand;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CONTROLLER1DATA;
    }

    public static LegacyController1DataPayloadC2S read(DataInputStream buffer) throws IOException {
        return new LegacyController1DataPayloadC2S(buffer.readBoolean(), Pose.deserialize(buffer));
    }
}
