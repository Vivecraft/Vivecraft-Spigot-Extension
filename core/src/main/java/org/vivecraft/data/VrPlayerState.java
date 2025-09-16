package org.vivecraft.data;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api_impl.data.VRPoseImpl;
import org.vivecraft.network.NetworkConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * holds all data from a player
 *
 */
public final class VrPlayerState {
    public final boolean seated;
    public final Pose hmd;
    public final boolean leftHanded;
    public final Pose mainHand;
    public final boolean reverseHands1legacy;
    public final Pose offHand;
    public final FBTMode fbtMode;
    @Nullable
    public final Pose waist;
    @Nullable
    public final Pose rightFoot;
    @Nullable
    public final Pose leftFoot;
    @Nullable
    public final Pose rightKnee;
    @Nullable
    public final Pose leftKnee;
    @Nullable
    public final Pose rightElbow;
    @Nullable
    public final Pose leftElbow;

    /**
     * @param seated              if the player is in seated mode
     * @param hmd                 device Pose of the headset
     * @param leftHanded          if true, {@code mainHand} is the left hand, else {@code offHand} is
     * @param mainHand            device Pose of the main hand
     * @param reverseHands1legacy same as {@code leftHanded}, just here for legacy compatibility
     * @param offHand             device Pose of the offhand
     * @param fbtMode             determines what additional trackers are in the player state
     * @param waist               waist tracker pos, can be {@code null}
     * @param rightFoot           right foot tracker pos, can be {@code null}
     * @param leftFoot            left foot tracker pos, can be {@code null}
     * @param rightKnee           right knee tracker pos, can be {@code null}
     * @param leftKnee            left knee tracker pos, can be {@code null}
     * @param rightElbow          right elbow tracker pos, can be {@code null}
     * @param leftElbow           left elbow tracker pos, can be {@code null}
     */
    public VrPlayerState(
        boolean seated, Pose hmd, boolean leftHanded, Pose mainHand,
        boolean reverseHands1legacy, Pose offHand,
        FBTMode fbtMode, @Nullable Pose waist,
        @Nullable Pose rightFoot, @Nullable Pose leftFoot,
        @Nullable Pose rightKnee, @Nullable Pose leftKnee,
        @Nullable Pose rightElbow, @Nullable Pose leftElbow)
    {
        this.seated = seated;
        this.hmd = hmd;
        this.leftHanded = leftHanded;
        this.mainHand = mainHand;
        this.reverseHands1legacy = reverseHands1legacy;
        this.offHand = offHand;
        this.fbtMode = fbtMode;
        this.waist = waist;
        this.rightFoot = rightFoot;
        this.leftFoot = leftFoot;
        this.rightKnee = rightKnee;
        this.leftKnee = leftKnee;
        this.rightElbow = rightElbow;
        this.leftElbow = leftElbow;
    }

    /**
     * strips the VrPlayerState down to only contain legacy data
     *
     * @param other   VrPlayerState to strip down
     * @param version version to strip the packet down to
     * @param offset  offset to apply to the data
     */
    public VrPlayerState(VrPlayerState other, int version, @Nullable Vector offset) {
        this(
            other.seated,
            offset == null ? other.hmd : other.hmd.offset(offset),
            other.leftHanded,
            offset == null ? other.mainHand : other.mainHand.offset(offset),
            other.reverseHands1legacy,
            offset == null ? other.offHand : other.offHand.offset(offset),
            version < NetworkConstants.NETWORK_VERSION_FBT ? FBTMode.ARMS_ONLY : other.fbtMode,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.waist,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.rightFoot,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.leftFoot,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.rightKnee,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.leftKnee,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.rightElbow,
            version < NetworkConstants.NETWORK_VERSION_FBT ? null : other.leftElbow
        );
    }

    /**
     * @param buffer     buffer to read from
     * @param bytesAfter specifies how many bytes in the buffer are meant to be left unread
     * @return a VrPlayerState read from the given {@code buffer}
     */
    public static VrPlayerState deserialize(DataInputStream buffer, int bytesAfter) throws IOException {
        boolean seated = buffer.readBoolean();
        Pose hmd = Pose.deserialize(buffer);
        boolean reverseHands = buffer.readBoolean();
        Pose mainController = Pose.deserialize(buffer);
        boolean reverseHandsLegacy = buffer.readBoolean();
        Pose offController = Pose.deserialize(buffer);

        // the rest here is only sent when the client has any fbt trackers
        FBTMode fbtMode = FBTMode.ARMS_ONLY;
        Pose waist = null;
        Pose rightFoot = null;
        Pose leftFoot = null;
        Pose rightKnee = null;
        Pose leftKnee = null;
        Pose rightElbow = null;
        Pose leftElbow = null;
        if (buffer.available() > bytesAfter) {
            fbtMode = FBTMode.values()[buffer.readByte()];
        }
        if (fbtMode != FBTMode.ARMS_ONLY) {
            waist = Pose.deserialize(buffer);
            rightFoot = Pose.deserialize(buffer);
            leftFoot = Pose.deserialize(buffer);
        }
        if (fbtMode == FBTMode.WITH_JOINTS) {
            rightKnee = Pose.deserialize(buffer);
            leftKnee = Pose.deserialize(buffer);
            rightElbow = Pose.deserialize(buffer);
            leftElbow = Pose.deserialize(buffer);
        }
        return new VrPlayerState(seated,
            hmd,
            reverseHands,
            mainController,
            reverseHandsLegacy,
            offController,
            fbtMode, waist,
            rightFoot, leftFoot,
            rightKnee, leftKnee,
            rightElbow, leftElbow);
    }

    /**
     * gets the Pose for the given body part
     *
     * @param bodyPart BodyPart to get the pose for
     * @return Pose of the {@code bodyPart}, or {@code null} if the body part is not valid for the current FBT mode
     */
    @Nullable
    public Pose getBodyPartPose(VRBodyPart bodyPart) {
        switch (bodyPart) {
            case MAIN_HAND:
                return this.mainHand;
            case OFF_HAND:
                return this.offHand;
            case LEFT_FOOT:
                return this.leftFoot;
            case RIGHT_FOOT:
                return this.rightFoot;
            case LEFT_ELBOW:
                return this.leftElbow;
            case RIGHT_ELBOW:
                return this.rightElbow;
            case LEFT_KNEE:
                return this.leftKnee;
            case RIGHT_KNEE:
                return this.rightKnee;
            case WAIST:
                return this.waist;
            case HEAD:
                return this.hmd;
            default:
                return this.mainHand;
        }
    }

    /**
     * writes this VrPlayerState to the given {@code buffer}
     *
     * @param buffer buffer to write to
     */
    public void serialize(DataOutputStream buffer) throws IOException {
        buffer.writeBoolean(this.seated);
        this.hmd.serialize(buffer);
        buffer.writeBoolean(this.leftHanded);
        this.mainHand.serialize(buffer);
        buffer.writeBoolean(this.leftHanded);
        this.offHand.serialize(buffer);
        // only send those, if it is there and the server supports it
        if (this.fbtMode != FBTMode.ARMS_ONLY) {
            buffer.writeByte(this.fbtMode.ordinal());
            this.waist.serialize(buffer);
            this.rightFoot.serialize(buffer);
            this.leftFoot.serialize(buffer);
            if (this.fbtMode == FBTMode.WITH_JOINTS) {
                this.rightKnee.serialize(buffer);
                this.leftKnee.serialize(buffer);
                this.rightElbow.serialize(buffer);
                this.leftElbow.serialize(buffer);
            }
        }
    }

    /**
     * @param playerPos The current position of the player.
     * @return This object as a pose for use with the API.
     */
    public VRPoseImpl asVRPose(Vector playerPos) {
        return new VRPoseImpl(
            this.hmd.asBodyPartData(playerPos),
            this.mainHand.asBodyPartData(playerPos),
            this.offHand.asBodyPartData(playerPos),
            getDataOrNull(this.rightFoot, playerPos),
            getDataOrNull(this.leftFoot, playerPos),
            getDataOrNull(this.waist, playerPos),
            getDataOrNull(this.rightKnee, playerPos),
            getDataOrNull(this.leftKnee, playerPos),
            getDataOrNull(this.rightElbow, playerPos),
            getDataOrNull(this.leftElbow, playerPos),
            this.seated,
            this.leftHanded,
            this.fbtMode
        );
    }

    @Nullable
    private static VRBodyPartData getDataOrNull(Pose pose, Vector playerPos) {
        return pose == null ? null : pose.asBodyPartData(playerPos);
    }
}
