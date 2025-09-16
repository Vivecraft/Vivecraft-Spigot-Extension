package org.vivecraft;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.vivecraft.config.ConfigBuilder;
import org.vivecraft.debug.Debug;

public class PermissionManager {

    private static Permission PERMISSIONS = null;

    /**
     * @return true if vault is available
     */
    public static boolean checkForVault() {
        boolean available = Bukkit.getServer().getPluginManager().getPlugin("Vault") != null &&
            Bukkit.getServer().getPluginManager().getPlugin("Vault").isEnabled();
        if (!available) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager()
            .getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        PERMISSIONS = rsp.getProvider();
        if (PERMISSIONS == null) {
            ViveMain.LOGGER.severe("Permissions error: Registered permissions provider is null!");
            return false;
        }
        if (!PERMISSIONS.hasGroupSupport()) {
            ViveMain.LOGGER.severe("Permissions error: Permission plugin does not support groups.");
            PERMISSIONS = null;
        }
        return PERMISSIONS != null;
    }

    private static boolean isDisabled() {
        return PERMISSIONS == null || !ViveMain.CONFIG.permissionsGroupsEnabled.get();
    }

    public static void changeGroupName(String from, String to) {
        if (isDisabled()) return;

        // remove the old permissions and keep track of who had it
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PERMISSIONS.playerInGroup(player, from)) {
                PERMISSIONS.playerRemoveGroup(player, from);
                PERMISSIONS.playerAddGroup(player, from);
            }
        }
    }

    public static void updatePlayerPermissionGroup(Player player) {
        if (isDisabled()) return;

        VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
        // not a vive player
        if (vivePlayer == null) {
            setVR(player, false);
            setNonVR(player, false);
        } else {
            setVR(player, vivePlayer.isVR());
            setNonVR(player, !vivePlayer.isVR());
        }
    }

    private static void setVR(Player player, boolean add) {
        setGroup(player, add, ViveMain.CONFIG.permissionsVRGroup);
    }

    private static void setNonVR(Player player, boolean add) {
        setGroup(player, add, ViveMain.CONFIG.permissionsNonVRGroup);
    }

    private static void setGroup(Player player, boolean add, ConfigBuilder.StringValue group) {
        String groupString = group.get();
        if (groupString.trim().isEmpty()) {
            Debug.log("Not setting %s group because it is empty!", group.getPath());
        }

        if (add && !PERMISSIONS.playerInGroup(player, groupString)) {
            Debug.log("Adding player '%s' to %s group", player.getName(), group.getPath());
            if (!PERMISSIONS.playerAddGroup(player, groupString)) {
                ViveMain.LOGGER.info(
                    "Couldn't add " + player.getName() + " to group " + groupString + ". Group may not exist!");
            }
        } else if (!add && PERMISSIONS.playerInGroup(player, groupString)) {
            Debug.log("Removing player '%s' from %s group", player.getName(), group.getPath());
            if (!PERMISSIONS.playerRemoveGroup(player, groupString)) {
                ViveMain.LOGGER.info(
                    "Couldn't remove " + player.getName() + " from group " + groupString + ". Group may not exist!");
            }
        }
    }
}
