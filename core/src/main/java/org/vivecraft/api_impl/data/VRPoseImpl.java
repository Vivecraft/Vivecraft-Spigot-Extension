package org.vivecraft.api_impl.data;

import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

public final class VRPoseImpl implements VRPose {
    public final VRBodyPartData hmd;
    public final VRBodyPartData c0;
    public final VRBodyPartData c1;
    public final VRBodyPartData rightFoot;
    public final VRBodyPartData leftFoot;
    public final VRBodyPartData waist;
    public final VRBodyPartData rightKnee;
    public final VRBodyPartData leftKnee;
    public final VRBodyPartData rightElbow;
    public final VRBodyPartData leftElbow;
    public final boolean isSeated;
    public final boolean isLeftHanded;
    public final FBTMode fbtMode;

    public VRPoseImpl(
        VRBodyPartData hmd, VRBodyPartData c0, VRBodyPartData c1,
        VRBodyPartData rightFoot, VRBodyPartData leftFoot,
        VRBodyPartData waist,
        VRBodyPartData rightKnee, VRBodyPartData leftKnee,
        VRBodyPartData rightElbow, VRBodyPartData leftElbow,
        boolean isSeated, boolean isLeftHanded, FBTMode fbtMode)
    {
        this.hmd = hmd;
        this.c0 = c0;
        this.c1 = c1;
        this.rightFoot = rightFoot;
        this.leftFoot = leftFoot;
        this.waist = waist;
        this.rightKnee = rightKnee;
        this.leftKnee = leftKnee;
        this.rightElbow = rightElbow;
        this.leftElbow = leftElbow;
        this.isSeated = isSeated;
        this.isLeftHanded = isLeftHanded;
        this.fbtMode = fbtMode;
    }

    @Nullable
    @Override
    public VRBodyPartData getBodyPartData(VRBodyPart vrBodyPart) {
        if (vrBodyPart == null) {
            throw new IllegalArgumentException("Cannot get a null body part's data!");
        }
        switch (vrBodyPart) {
            case HEAD:
                return this.hmd;
            case MAIN_HAND:
                return this.c0;
            case OFF_HAND:
                return this.c1;
            case RIGHT_FOOT:
                return this.rightFoot;
            case LEFT_FOOT:
                return this.leftFoot;
            case WAIST:
                return this.waist;
            case RIGHT_KNEE:
                return this.rightKnee;
            case LEFT_KNEE:
                return this.leftKnee;
            case RIGHT_ELBOW:
                return this.rightElbow;
            case LEFT_ELBOW:
                return this.leftElbow;
            default:
                throw new IllegalArgumentException("invalid VRBodyPart! " + vrBodyPart);
        }
    }

    @Override
    public boolean isSeated() {
        return this.isSeated;
    }

    @Override
    public boolean isLeftHanded() {
        return this.isLeftHanded;
    }

    @Override
    public FBTMode getFBTMode() {
        return this.fbtMode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VRPose:\nHMD: ").append(this.hmd)
            .append("\nmain hand: ").append(this.c0)
            .append("\noffhand: ").append(this.c1);
        if (this.fbtMode != FBTMode.ARMS_ONLY) {
            sb.append("\nright foot: ").append(this.rightFoot)
                .append("\nleft foot: ").append(this.leftFoot)
                .append("\nwaist: ").append(this.waist);
        }
        if (this.fbtMode == FBTMode.WITH_JOINTS) {
            sb.append("\nright knee: ").append(this.rightKnee)
                .append("\nleft knee: ").append(this.leftKnee)
                .append("\nright elbow: ").append(this.rightElbow)
                .append("\nleft elbow: ").append(this.leftElbow);
        }
        sb.append("\nseated: ").append(this.isSeated)
            .append(", leftHanded: ").append(this.isLeftHanded)
            .append(", fbtMode: ").append(this.fbtMode);
        return sb.toString();
    }
}
