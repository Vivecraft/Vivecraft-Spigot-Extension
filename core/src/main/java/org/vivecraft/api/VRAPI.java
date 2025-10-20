package org.vivecraft.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.api_impl.VRAPIImpl;

/**
 * The main interface for interacting with Vivecraft from code.
 *
 * @since 1.3.0
 */
public interface VRAPI {

    /**
     * Gets API instance for interacting with Vivecraft's common API
     *
     * @return The Vivecraft API instance for interacting with Vivecraft's common API.
     * @since 1.3.0
     */
    static VRAPI instance() {
        return VRAPIImpl.INSTANCE;
    }

    /**
     * Check whether a given player is currently in VR.
     *
     * @param player The player to check the VR status of.
     * @return true if the player is in VR.
     * @since 1.3.0
     */
    boolean isVRPlayer(Player player);

    /**
     * Returns the VR pose for the given player. Will return {@code null} if the player isn't in VR
     * <br>
     * The VRPose can still be {@code null} if {@link VRAPI#isVRPlayer(Player)} returned true,
     * since those two properties are independent of each other.
     *
     * @param player Player to get the VR pose of.
     * @return The VR pose for a player, or {@code null} if the player isn't in VR or no data has been received for said player.
     * @since 1.3.0
     */
    @Nullable
    VRPose getVRPose(Player player);

    /**
     * Returns the history of VR poses for the player.
     * <br>
     * Note that due to the inherent latency of networking, historical VR data retrieved by the server may be unideal.
     *
     * @param player Player to get the VR pose history of.
     * @return The history of VR poses for the player. Will be {@code null} if the player isn't in VR or if VR-specific data
     * hasn't been received.
     * @since 1.3.0
     */
    @Nullable
    VRPoseHistory getHistoricalVRPoses(Player player);

    /**
     * Sends a haptic pulse (vibration/rumble) for the specified VRBodyPart, if possible, to the given player.
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param player    Player to send the haptic pulse to.
     * @param bodyPart  The VRBodyPart to trigger a haptic pulse on.
     * @param duration  The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                  underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                  very short pulses.
     * @param frequency The frequency of the haptic pulse in Hz. (might be ignored if the targeted device doesn't support it)
     *                  <br>
     *                  160 Hz is a safe bet for this number, with Vivecraft's codebase
     *                  using anywhere from 160 Hz for actions such as a bite on a fishing line, to 1000 Hz for things such
     *                  as a chat notification.
     * @param amplitude The amplitude of the haptic pulse. This should be kept between 0 and 1.
     * @param delay     An amount of time to delay until creating the haptic pulse. The majority of the time, one should use 0 here. This starts counting when the client receives the packet.
     * @since 1.3.0
     */
    void sendHapticPulse(
        Player player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay);

    /**
     * Sends a haptic pulse (vibration/rumble) at full strength with 160 Hz for the specified VRBodyPart, if possible, to the given player.
     * <br>
     * If one wants more control over the used parameters one should use {@link #sendHapticPulse(Player, VRBodyPart, float, float, float, float)} instead.
     * <br>
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param player   Player to send the haptic pulse to.
     * @param bodyPart The VRBodyPart to trigger a haptic pulse on.
     * @param duration The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                 underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                 very short pulses.
     * @since 1.3.0
     */
    default void sendHapticPulse(Player player, VRBodyPart bodyPart, float duration) {
        sendHapticPulse(player, bodyPart, duration, 160F, 1F, 0F);
    }
}
