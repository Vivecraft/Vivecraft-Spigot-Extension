package org.vivecraft.network;

import org.vivecraft.ViveMain;
import org.vivecraft.config.enums.ClimbeyBlockmode;
import org.vivecraft.network.packet.s2c.ClimbingPayloadS2C;
import org.vivecraft.network.packet.s2c.SettingOverridePayloadS2C;
import org.vivecraft.network.packet.s2c.VRSwitchingPayloadS2C;
import org.vivecraft.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class PacketUtils {
    /**
     * @return CLIMBING payload holding blockmode and list of blocks
     */
    public static VivecraftPayloadS2C getClimbeyServerPayload() {
        List<String> blocks = null;
        if (ViveMain.CONFIG.climbeyBlockmode.get() != ClimbeyBlockmode.DISABLED) {
            blocks = new ArrayList<>(ViveMain.CONFIG.climbeyBlocklist.get());
        }
        return new ClimbingPayloadS2C(ViveMain.CONFIG.climbeyEnabled.get(), ViveMain.CONFIG.climbeyBlockmode.get(),
            blocks);
    }

    /**
     * @return VR switching payload for the current settings
     */
    public static VivecraftPayloadS2C getVRSwitchingPayload() {
        return new VRSwitchingPayloadS2C(ViveMain.CONFIG.vrSwitchingEnabled.get() && !ViveMain.CONFIG.vrOnly.get());
    }

    /**
     * @return Survival TP override payload for the current settings
     */
    public static VivecraftPayloadS2C getSurvivalTeleportOverridePayload() {
        return new SettingOverridePayloadS2C(Utils.MapOf(
            "limitedTeleport", "true",
            "teleportLimitUp", String.valueOf(ViveMain.CONFIG.teleportUpLimit.get()),
            "teleportLimitDown", String.valueOf(ViveMain.CONFIG.teleportDownLimit.get()),
            "teleportLimitHoriz", String.valueOf(ViveMain.CONFIG.teleportHorizontalLimit.get())
        ), !ViveMain.CONFIG.teleportLimitedSurvival.get());
    }

    /**
     * @return world scale override payload for the current settings
     */
    public static VivecraftPayloadS2C getWorldScaleOverridePayload() {
        return new SettingOverridePayloadS2C(Utils.MapOf(
            "worldScale.min", String.valueOf(ViveMain.CONFIG.worldscaleMin.get()),
            "worldScale.max", String.valueOf(ViveMain.CONFIG.worldscaleMax.get())
        ), !ViveMain.CONFIG.worldscaleLimited.get());
    }

    /**
     * @return third person transforms override payload for the current settings
     */
    public static VivecraftPayloadS2C getThirdPersonItemsOverridePayload() {
        return new SettingOverridePayloadS2C(Utils.MapOf(
            "thirdPersonItems", "true"
        ), !ViveMain.CONFIG.forceThirdPersonItems.get());
    }

    /**
     * @return custom third person transforms override payload for the current settings
     */
    public static VivecraftPayloadS2C getThirdPersonItemsCustomOverridePayload() {
        return new SettingOverridePayloadS2C(Utils.MapOf(
            "thirdPersonItemsCustom", "true"
        ), !ViveMain.CONFIG.forceThirdPersonItemsCustom.get());
    }
}
