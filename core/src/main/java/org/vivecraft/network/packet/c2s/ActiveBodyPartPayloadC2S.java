package org.vivecraft.network.packet.c2s;

import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds the clients current active BodyPart, that will cause the next action
 *
 */
public final class ActiveBodyPartPayloadC2S implements VivecraftPayloadC2S {
    public final VRBodyPart bodyPart;
    public final boolean useForAim;

    /**
     * @param bodyPart  the active BodyPart
     * @param useForAim when set, will use this BodyPart for aim when using items, when unset, the server can stil decide to use it for aiming in certain situations
     */
    public ActiveBodyPartPayloadC2S(VRBodyPart bodyPart, boolean useForAim) {
        this.bodyPart = bodyPart;
        this.useForAim = useForAim;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ACTIVEHAND;
    }

    public static ActiveBodyPartPayloadC2S read(DataInputStream buffer) throws IOException {
        VRBodyPart bodyPart = VRBodyPart.values()[buffer.readByte()];
        boolean useForAim = false;
        if (buffer.available() > 0) {
            useForAim = buffer.readBoolean();
        }
        return new ActiveBodyPartPayloadC2S(bodyPart, useForAim);
    }
}
