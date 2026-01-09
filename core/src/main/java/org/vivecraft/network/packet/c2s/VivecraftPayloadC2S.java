package org.vivecraft.network.packet.c2s;

import org.vivecraft.ViveMain;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.network.packet.VivecraftPayload;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Vivecraft packet sent from Clients to the Server
 */
public interface VivecraftPayloadC2S extends VivecraftPayload {

    /**
     * creates the correct VivecraftPacket based on the {@link PayloadIdentifier} stored in the first byte
     *
     * @param buffer Buffer to read the VivecraftPacket from
     * @return parsed VivecraftPacket
     */
    static VivecraftPayloadC2S readPacket(DataInputStream buffer) {
        try {
            int index = buffer.readByte();
            if (index < PayloadIdentifier.values().length) {
                PayloadIdentifier id = PayloadIdentifier.values()[index];
                switch (id) {
                    case VERSION:
                        return VersionPayloadC2S.read(buffer);
                    case HEADDATA:
                        return LegacyHeadDataPayloadC2S.read(buffer);
                    case CONTROLLER0DATA:
                        return LegacyController0DataPayloadC2S.read(buffer);
                    case CONTROLLER1DATA:
                        return LegacyController1DataPayloadC2S.read(buffer);
                    case WORLDSCALE:
                        return WorldScalePayloadC2S.read(buffer);
                    case DRAW:
                        return DrawPayloadC2S.read(buffer);
                    case TELEPORT:
                        return TeleportPayloadC2S.read(buffer);
                    case CLIMBING:
                        return new ClimbingPayloadC2S();
                    case HEIGHT:
                        return HeightPayloadC2S.read(buffer);
                    case ACTIVEHAND:
                        return ActiveBodyPartPayloadC2S.read(buffer);
                    case CRAWL:
                        return CrawlPayloadC2S.read(buffer);
                    case IS_VR_ACTIVE:
                        return VRActivePayloadC2S.read(buffer);
                    case VR_PLAYER_STATE:
                        return VRPlayerStatePayloadC2S.read(buffer);
                    case DAMAGE_DIRECTION:
                        return new DamageDirectionPayloadC2S();
                    case AIM_DIRECTION_OVERRIDE:
                        return AimDirOverridePayloadC2S.read(buffer);
                    case AIM_OVERRIDE_RESET:
                        return AimOverrideResetPayloadC2S.read(buffer);
                    case AIM_POSITION_OVERRIDE:
                        return AimPosOverridePayloadC2S.read(buffer);
                    default:
                        ViveMain.LOGGER.severe("Got unexpected payload identifier on server: " + id);
                        return new UnknownPayloadC2S();
                }
            } else {
                ViveMain.LOGGER.severe("Got unknown payload identifier on server: " + index);
                return new UnknownPayloadC2S();
            }
        } catch (IOException e) {
            ViveMain.LOGGER.log(Level.SEVERE, "Error reading vivecraft packer: ", e);
            return new UnknownPayloadC2S();
        }
    }
}
