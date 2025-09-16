package org.vivecraft.api.data;

/**
 * Corresponds to the different tracked device roles that are supported by Vivecraft.
 *
 * @since 1.3.0
 */
public enum VRBodyPart {
    /**
     * Main hand of the player, this is the hand the player points with. Which one that is can be identified with
     * {@link VRPose#isLeftHanded()}
     *
     * @since 1.3.0
     */
    MAIN_HAND,
    /**
     * @since 1.3.0
     */
    OFF_HAND,
    /**
     * @since 1.3.0
     */
    RIGHT_FOOT,
    /**
     * @since 1.3.0
     */
    LEFT_FOOT,
    /**
     * @since 1.3.0
     */
    WAIST,
    /**
     * @since 1.3.0
     */
    RIGHT_KNEE,
    /**
     * @since 1.3.0
     */
    LEFT_KNEE,
    /**
     * @since 1.3.0
     */
    RIGHT_ELBOW,
    /**
     * @since 1.3.0
     */
    LEFT_ELBOW,
    /**
     * corresponds to the player's headset, so it is at their eye position
     *
     * @since 1.3.0
     */
    HEAD;

    /**
     * Gets the VRBodyPart which is the same type but on the opposite side of the body. VRBodyParts that don't have an
     * opposite counterpart will return itself.
     *
     * @return the opposite VRBodyPart
     * @since 1.3.0
     */
    public VRBodyPart opposite() {
        switch (this) {
            case MAIN_HAND:
                return OFF_HAND;
            case OFF_HAND:
                return MAIN_HAND;
            case RIGHT_FOOT:
                return LEFT_FOOT;
            case LEFT_FOOT:
                return RIGHT_FOOT;
            case RIGHT_KNEE:
                return LEFT_KNEE;
            case LEFT_KNEE:
                return RIGHT_KNEE;
            case RIGHT_ELBOW:
                return LEFT_ELBOW;
            case LEFT_ELBOW:
                return RIGHT_ELBOW;
            default:
                return this;
        }
    }

    /**
     * Whether this body part type is available in the provided full-body tracking mode.
     *
     * @param fbtMode The full-body tracking mode to check.
     * @return Whether this body part has available data in the provided mode.
     * @since 1.3.0
     */
    public boolean availableInMode(FBTMode fbtMode) {
        switch (this) {
            case MAIN_HAND:
            case OFF_HAND:
            case HEAD:
                return true;
            case RIGHT_FOOT:
            case LEFT_FOOT:
            case WAIST:
                return fbtMode != FBTMode.ARMS_ONLY;
            case RIGHT_KNEE:
            case LEFT_KNEE:
            case RIGHT_ELBOW:
            case LEFT_ELBOW:
                return fbtMode == FBTMode.WITH_JOINTS;
            default:
                return false;
        }
    }

    /**
     * Checks if {@code this} VRBodyPart is a foot
     *
     * @return Whether this body part is a foot.
     * @since 1.3.0
     */
    public boolean isFoot() {
        return this == RIGHT_FOOT || this == LEFT_FOOT;
    }

    /**
     * Checks if {@code this} VRBodyPart is a hand
     *
     * @return Whether this body part is a hand.
     * @since 1.3.0
     */
    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }
}
