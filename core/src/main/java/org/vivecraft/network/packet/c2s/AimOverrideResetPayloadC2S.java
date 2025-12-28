package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

/**
 * resets the aim override
 *
 */
public final class AimOverrideResetPayloadC2S implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_OVERRIDE_RESET;
    }
}
