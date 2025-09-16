package org.vivecraft.network.packet.s2c;

import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * sends a haptic event to the client to trigger on the given BodyPart
 *
 */
public final class HapticPayloadS2C implements VivecraftPayloadS2C {
    public final VRBodyPart bodyPart;
    public final float duration;
    public final float frequency;
    public final float amplitude;
    public final float delay;

    /**
     * @param bodyPart  VRBodyPart to trigger on
     * @param duration  duration in seconds
     * @param frequency frequency in Hz
     * @param amplitude amplitude, 0-1
     * @param delay     delay in seconds
     */
    public HapticPayloadS2C(VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay) {
        this.bodyPart = bodyPart;
        this.duration = duration;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.delay = delay;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HAPTIC;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.bodyPart.ordinal());
        buffer.writeFloat(this.duration);
        buffer.writeFloat(this.frequency);
        buffer.writeFloat(this.amplitude);
        buffer.writeFloat(this.delay);
    }
}
