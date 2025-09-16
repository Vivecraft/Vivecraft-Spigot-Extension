package org.vivecraft.network.packet.c2s;

import org.vivecraft.data.Pose;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * legacy packet, holds the seated flag and the head pose
 *
 */
public final class LegacyHeadDataPayloadC2S implements VivecraftPayloadC2S {
    public final boolean seated;
    public final Pose hmdPose;

    /**
     * @param seated  if the player is in seated mode
     * @param hmdPose pose of the players headset
     */
    public LegacyHeadDataPayloadC2S(boolean seated, Pose hmdPose) {
        this.seated = seated;
        this.hmdPose = hmdPose;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HEADDATA;
    }

    public static LegacyHeadDataPayloadC2S read(DataInputStream buffer) throws IOException {
        return new LegacyHeadDataPayloadC2S(buffer.readBoolean(), Pose.deserialize(buffer));
    }
}
