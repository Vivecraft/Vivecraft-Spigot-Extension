package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds weather the next attack is a roomscale attack
 */
public final class RoomscaleAttackPayloadC2S implements VivecraftPayloadC2S {

    public final boolean isRoomscaleAttack;
    public final int hitsMade;

    /**
     * @param isRoomscaleAttack if the next attack is roomscale
     * @param hitsMade          number of ticks worth of hits made during this roomscale attack, only valide if {@code isRoomscaleAttack} is false
     */
    public RoomscaleAttackPayloadC2S(boolean isRoomscaleAttack, int hitsMade) {
        this.isRoomscaleAttack = isRoomscaleAttack;
        this.hitsMade = hitsMade;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ROOMSCALE_ATTACK;
    }

    public static RoomscaleAttackPayloadC2S read(DataInputStream buffer) throws IOException {
        return new RoomscaleAttackPayloadC2S(buffer.readBoolean(), buffer.readByte());
    }
}
