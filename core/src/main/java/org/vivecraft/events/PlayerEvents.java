package org.vivecraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.PermissionManager;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.compat.Platform;
import org.vivecraft.compat.types.Item;
import org.vivecraft.debug.Debug;
import org.vivecraft.network.AimFixHandler;
import org.vivecraft.network.NetworkUtils;
import org.vivecraft.util.MetadataHelper;
import org.vivecraft.util.UpdateChecker;
import org.vivecraft.util.Utils;

import java.util.Random;

public class PlayerEvents implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ViveMain.VIVE_PLAYERS.remove(player.getUniqueId());
        MetadataHelper.cleanupMetadata(player);

        if (ViveMain.CONFIG.messagesEnabled.get() && !ViveMain.CONFIG.messagesLeaveMessage.get().isEmpty()) {
            NetworkUtils.sendMessageToAll(ViveMain.CONFIG.messagesLeaveMessage.get(), player.getDisplayName(), "");
        }
    }

    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Debug.log(player.getName() + " has joined the server");
        Debug.log("Waiting for Vivecraft info of: " + player.getName());

        Platform.getInstance().getScheduler().runGlobalDelayed(() -> {
            // only do stuff, if the player is still on the server
            if (player.isOnline()) {
                VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
                String message = "";

                if (NetworkUtils.kickIfNotAllowed(player)) {
                    // player got kicked
                    return;
                }

                if (vivePlayer == null) {
                    Debug.log(player.getName() + " joined without vivecraft");
                }

                // welcome message
                if (ViveMain.CONFIG.messagesEnabled.get()) {
                    // get the right message
                    if (vivePlayer == null) {
                        message = ViveMain.CONFIG.messagesWelcomeVanilla.get();
                    } else if (!vivePlayer.isVR()) {
                        message = ViveMain.CONFIG.messagesWelcomeNonvr.get();
                    } else if (vivePlayer.isSeated()) {
                        message = ViveMain.CONFIG.messagesWelcomeSeated.get();
                    } else {
                        message = ViveMain.CONFIG.messagesWelcomeVr.get();
                    }
                    // actually send the message, if there is one set
                    NetworkUtils.sendMessageToAll(message, player.getDisplayName(), "");
                }

                PermissionManager.updatePlayerPermissionGroup(player);
            } else {
                Debug.log(player.getName() + " no longer online!");
            }
        }, ViveMain.CONFIG.messageKickDelay.get());

        if (ViveMain.CONFIG.checkForUpdates.get() && player.isOp()) {
            // check for update on not the main thread
            Platform.getInstance().getScheduler().runAsync(() -> {
                if (UpdateChecker.checkForUpdates()) {
                    Platform.getInstance().getScheduler().runGlobal(
                        () -> player.sendMessage(ViveMain.translate("vivecraft.plugin.update",
                            Utils.green(UpdateChecker.NEWEST_VERSION))));
                }
            });
        }

        new AimFixHandler(player, ViveMain.NMS.getConnection(player));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (ViveMain.CONFIG.vrFun.get() && ViveMain.isVivePlayer(event.getPlayer())) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer(event.getPlayer());
            if (vivePlayer.isVR() && this.random.nextInt(40) == 3) {
                ItemStack easterEggItem;
                if (this.random.nextInt(2) == 1) {
                    easterEggItem = ViveMain.API.createItemStack(
                        Item.PUMPKIN_PIE,
                        null, "EAT ME",
                        null);
                } else {
                    easterEggItem = ViveMain.API.createItemStack(
                        Item.WATER_POTION,
                        null, "DRINK ME",
                        null);
                }
                event.getPlayer().getInventory().addItem(easterEggItem);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (ViveMain.CONFIG.messagesEnabled.get()) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer(event.getEntity());
            String message = "";
            String entity = ViveMain.API.getCausingEntityName(event);

            // get the right message
            if (!entity.isEmpty()) {
                // death by mob
                if (vivePlayer == null) {
                    message = ViveMain.CONFIG.messagesDeathByMobVanilla.get();
                } else if (!vivePlayer.isVR()) {
                    message = ViveMain.CONFIG.messagesDeathByMobNonvr.get();
                } else if (vivePlayer.isSeated()) {
                    message = ViveMain.CONFIG.messagesDeathByMobSeated.get();
                } else {
                    message = ViveMain.CONFIG.messagesDeathByMobVr.get();
                }
            }

            if (message.isEmpty()) {
                // general death, of if the mob one isn't set
                if (vivePlayer == null) {
                    message = ViveMain.CONFIG.messagesDeathVanilla.get();
                } else if (!vivePlayer.isVR()) {
                    message = ViveMain.CONFIG.messagesDeathNonvr.get();
                } else if (vivePlayer.isSeated()) {
                    message = ViveMain.CONFIG.messagesDeathSeated.get();
                } else {
                    message = ViveMain.CONFIG.messagesDeathVr.get();
                }
            }

            // actually send the message, if there is one set
            if (!message.isEmpty()) {
                event.setDeathMessage(NetworkUtils.formatMessage(message, event.getEntity().getDisplayName(), entity));
            }
        }
    }
}
