package org.vivecraft.network;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.debug.Debug;

public class NetworkUtils {

    public static void sendMessageToAll(String message, String playerName) {
        // only send if set
        if (!message.isEmpty()) {
            Bukkit.getServer().broadcastMessage(formatMessage(message, playerName));
        }
    }

    /**
     * replaces the patterns in the original string and returns it
     *
     * @param message    original message to replace things in
     * @param playerName name of the player that the message is sent to
     * @param other      pair of other replacements, in the order of (placeholder, replacement)
     * @return formatted String
     */
    public static String formatMessage(String message, String playerName, String... other) {
        message = message.replace("&player", playerName);
        if (other != null && other.length > 0) {
            for (int i = 0; i < other.length - 1; i += 2) {
                message = message.replace(other[i], other[i + 1]);
            }
        }
        return message;
    }

    /**
     * kicks the given player if the server settings don't allow them
     *
     * @param player player to maybe kick
     * @return if the player got kicked
     */
    public static boolean kickIfNotAllowed(Player player) {
        if (player.isOnline()) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer(player);

            boolean isOpAndAllowed = ViveMain.CONFIG.allowOp.get() && player.isOp();

            // kick non VR players
            if (!isOpAndAllowed && ViveMain.CONFIG.vrOnly.get() && (vivePlayer == null || !vivePlayer.isVR())) {
                String kickMessage = ViveMain.CONFIG.messagesKickVrOnly.get();
                player.kickPlayer(formatMessage(kickMessage, player.getName()));
                Debug.log(player.getName() + " got kicked for not using VR");
                return true;
            }

            // kick non vivecraft players
            if (!isOpAndAllowed && ViveMain.CONFIG.viveOnly.get() && vivePlayer == null) {
                String kickMessage = ViveMain.CONFIG.messagesKickViveOnly.get();
                player.kickPlayer(formatMessage(kickMessage, player.getName()));
                Debug.log(player.getName() + " got kicked for not using Vivecraft");
                return true;
            }

            // kick vivecraft players with outdated vivecraft
            if (!isOpAndAllowed && ViveMain.CONFIG.minViveVersion != null && vivePlayer != null &&
                vivePlayer.version.compareTo(ViveMain.CONFIG.minViveVersion) > 0)
            {
                String kickMessage = ViveMain.CONFIG.messagesKickOutdatedViveVersion.get();
                player.kickPlayer(formatMessage(kickMessage, player.getName(),
                    "&minVersion", ViveMain.CONFIG.minViveVersion.versionString(),
                    "&userVersion", vivePlayer.version.versionString())
                );
                Debug.log(player.getName() + " got kicked for outdated Vivecraft. has: %s, needs: %s",
                    vivePlayer.version, ViveMain.CONFIG.minViveVersion.versionString());
                return true;
            }
        }
        return false;
    }
}
