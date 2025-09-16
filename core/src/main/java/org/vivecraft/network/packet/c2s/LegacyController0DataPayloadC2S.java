package org.vivecraft.network.packet.c2s;

import org.vivecraft.data.Pose;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * legacy packet, holds the reversed hand flag and the main hand controller pose
 *
 */
public final class LegacyController0DataPayloadC2S implements VivecraftPayloadC2S {
    public final boolean leftHanded;
    public final Pose mainHand;

    /**
     * @param leftHanded if the player has reversed hands set
     * @param mainHand   pose of the players offhand controller
     */
    public LegacyController0DataPayloadC2S(boolean leftHanded, Pose mainHand) {
        this.leftHanded = leftHanded;
        this.mainHand = mainHand;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CONTROLLER0DATA;
    }

    public static LegacyController0DataPayloadC2S read(DataInputStream buffer) throws IOException {
        return new LegacyController0DataPayloadC2S(buffer.readBoolean(), Pose.deserialize(buffer));
    }
}
