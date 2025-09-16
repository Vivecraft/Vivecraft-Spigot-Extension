package org.vivecraft.api_impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.api_impl.data.VRPoseHistoryImpl;
import org.vivecraft.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VRAPIImpl implements VRAPI {

    public static final VRAPIImpl INSTANCE = new VRAPIImpl();

    public static final int MAX_HISTORY_TICKS = 200;
    private final Map<UUID, VRPoseHistoryImpl> serverPoseHistories = new HashMap<>();

    public void clearPoseHistory(UUID player) {
        this.serverPoseHistories.remove(player);
    }

    public void addPoseToHistory(UUID player, VRPose pose) {
        VRPoseHistoryImpl poseHistory = this.serverPoseHistories.get(player);
        if (poseHistory == null) {
            poseHistory = new VRPoseHistoryImpl();
            this.serverPoseHistories.put(player, poseHistory);
        }
        poseHistory.addPose(pose);
    }

    public void clearAllPoseHistories() {
        this.serverPoseHistories.clear();
    }

    @Override
    public boolean isVRPlayer(Player player) {
        VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
        return vivePlayer != null && vivePlayer.isVR();
    }


    @Nullable
    @Override
    public VRPose getVRPose(Player player) {
        if (!isVRPlayer(player)) {
            return null;
        } else {
            return ViveMain.getVivePlayer(player).asVRPose();
        }
    }

    @Nullable
    @Override
    public VRPoseHistory getHistoricalVRPoses(Player player) {
        if (isVRPlayer(player)) {
            return this.serverPoseHistories.get(player.getUniqueId());
        } else {
            return null;
        }
    }

    @Override
    public void sendHapticPulse(
        Player player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay)
    {
        if (VRAPIImpl.INSTANCE.isVRPlayer(player)) {
            NetworkHandler.sendHapticToClient(player, bodyPart, duration, frequency, amplitude, delay);
        }
    }
}
