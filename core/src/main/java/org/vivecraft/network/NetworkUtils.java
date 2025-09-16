package org.vivecraft.network;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.debug.Debug;

public class NetworkUtils {

    public static void sendMessageToAll(String message, String playerName, String cause) {
        // only send if set
        if (!message.isEmpty()) {
            Bukkit.getServer().broadcastMessage(formatMessage(message, playerName, cause));
        }
    }

    public static String formatMessage(String message, String playerName, String cause) {
        message = message.replace("&player", playerName);
        message = message.replace("&cause", cause);
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
                player.kickPlayer(formatMessage(kickMessage, player.getName(), ""));
                Debug.log(player.getName() + " " + "got kicked for not using VR");
                return true;
            }

            // kick non vivecraft players
            if (!isOpAndAllowed && ViveMain.CONFIG.viveOnly.get() && vivePlayer == null) {
                String kickMessage = ViveMain.CONFIG.messagesKickViveOnly.get();
                player.kickPlayer(formatMessage(kickMessage, player.getName(), ""));
                Debug.log(player.getName() + " " + "got kicked for not using Vivecraft");
                return true;
            }
        }
        return false;
    }
}
