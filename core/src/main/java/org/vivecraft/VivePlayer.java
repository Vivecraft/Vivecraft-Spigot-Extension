package org.vivecraft;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api_impl.VRAPIImpl;
import org.vivecraft.data.VrPlayerState;
import org.vivecraft.network.NetworkConstants;
import org.vivecraft.network.NetworkVersion;
import org.vivecraft.util.ItemOverride;
import org.vivecraft.util.MCVersion;
import org.vivecraft.util.MathUtils;
import org.vivecraft.util.MetadataHelper;

public class VivePlayer {
    // player movement state
    @Nullable
    private VrPlayerState vrPlayerState;
    private VRPose vrPlayerStateAsPose;
    // how much the player is drawing the roomscale bow
    public float draw;
    public float worldScale = 1.0F;
    public float heightScale = 1.0F;
    public VRBodyPart activeBodyPart = VRBodyPart.MAIN_HAND;
    // dual wielding switches out hte main hand item, this keeps track of the original item
    public ItemOverride itemOverride = null;
    public boolean useBodyPartForAim = false;
    public boolean crawling;
    // if the player has VR active
    private boolean isVR = false;
    // offset set during aimFix to keep the original data positions
    public Vector offset = new Vector();
    // player this data belongs to
    public Player player;
    // network protocol this player is communicating with
    public NetworkVersion networkVersion = NetworkVersion.fromProtocolVersion(
        NetworkConstants.MAX_SUPPORTED_NETWORK_PROTOCOL);
    // version string the player sent on join
    public String version = "none";
    // if the client requested damage direction data
    public boolean wantsDamageDirection = false;

    // pre 1.13 the channel id was 'Vivecraft', new one is 'vivecraft:data'. store the one the client used
    public String channel = "none";

    // pre 1.16 clients sent world space positions, so need to check legacy clients for that
    public MCVersion mcVersion = MCVersion.INVALID;

    public VivePlayer(Player player) {
        this.player = player;
    }

    /**
     * gets the orientation of the given bodypart
     *
     * @param bodyPart BodyPart to get the orientation of, if not available, will use the MAIN_HAND
     * @return orientation in world space
     */
    public Quaternionfc getBodyPartOrientation(VRBodyPart bodyPart) {
        if (this.vrPlayerState != null) {
            if (this.isSeated() || !bodyPart.availableInMode(this.vrPlayerState.fbtMode)) {
                bodyPart = VRBodyPart.MAIN_HAND;
            }
            return this.vrPlayerState.getBodyPartPose(bodyPart).orientation;
        } else {
            return new Quaternionf().lookAlong(MathUtils.toJomlVec(this.player.getLocation().getDirection()),
                MathUtils.UP);
        }
    }

    /**
     * transforms the local {@code direction} vector on BodyPart {@code bodyPart} into world space
     *
     * @param bodyPart  BodyPart to get the custom direction on, if not available, will use the MAIN_HAND
     * @param direction local direction to transform
     * @return direction in world space
     */
    public Vector3fc getBodyPartVectorCustom(VRBodyPart bodyPart, Vector3fc direction) {
        if (this.vrPlayerState != null) {
            if (this.isSeated() || !bodyPart.availableInMode(this.vrPlayerState.fbtMode)) {
                bodyPart = VRBodyPart.MAIN_HAND;
            }

            return this.vrPlayerState.getBodyPartPose(bodyPart).orientation.transform(direction, new Vector3f());
        } else {
            return MathUtils.toJomlVec(this.player.getLocation().getDirection());
        }
    }

    /**
     * @param bodyPart BodyPart to get the direction from, if not available, will use the MAIN_HAND
     * @return forward direction of the given BodyPart
     */
    public Vector3fc getBodyPartDir(VRBodyPart bodyPart) {
        return this.getBodyPartVectorCustom(bodyPart, MathUtils.BACK);
    }

    /**
     * @param ignoreUseForAim ignores the useBodyPartForAim state when set, and always uses the active BodyPart for the aim
     * @return the direction the player is aiming, accounts for the roomscale bow
     */
    public Vector3fc getAimDir(boolean ignoreUseForAim) {
        if (!this.isSeated() && this.draw > 0.0F) {
            return MathUtils.subToJomlVec(this.getBodyPartPos(this.activeBodyPart.opposite()),
                this.getBodyPartPos(this.activeBodyPart)).normalize();
        } else if (ignoreUseForAim || this.useBodyPartForAim) {
            return this.getBodyPartDir(this.activeBodyPart);
        } else {
            return this.getBodyPartDir(VRBodyPart.MAIN_HAND);
        }
    }

    /**
     * @param ignoreUseForAim ignores the useBodyPartForAim state when set, and always uses the active BodyPart for the aim
     * @return the orientation the player is aiming, accounts for the roomscale bow
     */
    public Quaternionfc getAimOrientation(boolean ignoreUseForAim) {
        if (!this.isSeated() && this.draw > 0.0F) {
            return new Quaternionf().lookAlong(getAimDir(ignoreUseForAim),
                getBodyPartVectorCustom(this.activeBodyPart.opposite(), MathUtils.GRIP_FORWARD)).conjugate();
        } else if (ignoreUseForAim || this.useBodyPartForAim) {
            return this.getBodyPartOrientation(this.activeBodyPart);
        } else {
            return this.getBodyPartOrientation(VRBodyPart.MAIN_HAND);
        }
    }

    /**
     * @param ignoreUseForAim ignores the useBodyPartForAim state when set, and always uses the active BodyPart for the aim
     * @return the position from which the player is aiming
     */
    public Vector getAimPos(boolean ignoreUseForAim) {
        if (ignoreUseForAim || this.useBodyPartForAim) {
            return this.getBodyPartPos(this.activeBodyPart);
        } else {
            return this.getBodyPartPos(VRBodyPart.MAIN_HAND);
        }
    }

    /**
     * @param ignoreUseForAim ignores the useBodyPartForAim state when set, and always uses the active BodyPart for the aim
     * @return the position from which the player is aiming
     */
    public Quaternionfc getAimRot(boolean ignoreUseForAim) {
        if (ignoreUseForAim || this.useBodyPartForAim) {
            return this.getBodyPartOrientation(this.activeBodyPart);
        } else {
            return this.getBodyPartOrientation(VRBodyPart.MAIN_HAND);
        }
    }

    /**
     * @return looking direction of the head
     */
    public Vector3fc getHMDDir() {
        if (this.vrPlayerState != null) {
            return this.vrPlayerState.hmd.orientation.transform(MathUtils.BACK, new Vector3f());
        } else {
            return MathUtils.toJomlVec(this.player.getLocation().getDirection());
        }
    }

    /**
     * @return position of the head, in world space
     */
    public Vector getHMDPos() {
        Vector playerPos = this.player.getLocation().toVector();
        if (this.vrPlayerState != null) {
            return MathUtils.toBukkitVec(this.vrPlayerState.hmd.position).add(this.offset).add(playerPos);
        } else {
            return new Vector(0.0, 1.62, 0.0).add(playerPos);
        }
    }

    /**
     * @param bodyPart     BodyPart to get the position for, if not available, will use the MAIN_HAND
     * @param realPosition if true disables the seated override
     * @return BodyPart position in world space
     */
    public Vector getBodyPartPos(VRBodyPart bodyPart, boolean realPosition) {
        Vector playerPos = this.player.getLocation().toVector();
        if (this.vrPlayerState != null) {
            if (!bodyPart.availableInMode(this.vrPlayerState.fbtMode)) {
                bodyPart = VRBodyPart.MAIN_HAND;
            }

            // in seated the realPosition is at the head,
            // so reconstruct the seated position when wanting the visual position
            if (this.isSeated() && bodyPart.isHand() && !realPosition) {
                Vector3f dir = this.getHMDDir()
                    .rotateY(MathUtils.DEG_TO_RAD * (bodyPart == VRBodyPart.MAIN_HAND ? -35.0F : 35.0F),
                        new Vector3f());
                dir.y = 0;
                dir = dir.normalize();
                Vector pos = this.getHMDPos();
                pos.setX(pos.getX() + dir.x * 0.3F * this.worldScale);
                pos.setY(pos.getY() + -0.4F * this.worldScale);
                pos.setZ(pos.getZ() + dir.z * 0.3F * this.worldScale);
                return pos;
            }

            Vector3fc conPos = this.vrPlayerState.getBodyPartPose(bodyPart).position;

            return playerPos.add(this.offset).add(MathUtils.toBukkitVec(conPos));
        } else {
            return new Vector(0.0, 1.62, 0.0).add(playerPos);
        }
    }


    /**
     * @param bodyPart BodyPart to get the position for, if not available, will use the MAIN_HAND
     * @return BodyPart position in world space
     */
    public Vector getBodyPartPos(VRBodyPart bodyPart) {
        return getBodyPartPos(bodyPart, false);
    }

    /**
     * @return if the player has VR active
     */
    public boolean isVR() {
        return this.isVR;
    }

    /**
     * set VR state of the player
     */
    public void setVR(boolean vr) {
        this.isVR = vr;
    }

    /**
     * @return if the player is using seated VR
     */
    public boolean isSeated() {
        return this.vrPlayerState != null && this.vrPlayerState.seated;
    }

    /**
     * @return if the player is using left-handed mode
     */
    public boolean isLeftHanded() {
        return this.vrPlayerState != null && this.vrPlayerState.leftHanded;
    }

    @Nullable
    public VrPlayerState vrPlayerState() {
        return this.vrPlayerState;
    }

    public void setVrPlayerState(VrPlayerState vrPlayerState) {
        this.vrPlayerState = vrPlayerState;
        this.vrPlayerStateAsPose = null;
        Vector pos = this.player.getLocation().toVector();
        VRAPIImpl.INSTANCE.addPoseToHistory(this.player.getUniqueId(), vrPlayerState.asVRPose(pos), pos);
        MetadataHelper.updateMetadata(this);
    }

    public VRPose asVRPose() {
        if (this.vrPlayerState == null) {
            return null;
        }
        if (this.vrPlayerStateAsPose == null) {
            this.vrPlayerStateAsPose = this.vrPlayerState.asVRPose(this.player.getLocation().toVector());
        }
        return this.vrPlayerStateAsPose;
    }
}
