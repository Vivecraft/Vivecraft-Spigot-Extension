package org.vivecraft.network;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.util.Vector;
import org.vivecraft.PermissionManager;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.Platform;
import org.vivecraft.config.ConfigBuilder;
import org.vivecraft.data.Pose;
import org.vivecraft.data.VrPlayerState;
import org.vivecraft.debug.Debug;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.network.packet.c2s.*;
import org.vivecraft.network.packet.s2c.*;
import org.vivecraft.util.ItemOverride;
import org.vivecraft.util.LazySupplier;
import org.vivecraft.util.MetadataHelper;
import org.vivecraft.util.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;

public class NetworkHandler implements PluginMessageListener {

    private static JavaPlugin PLUGIN;

    // temporarily stores the packets from legacy clients to assemble a complete VrPlayerState
    private final Map<UUID, Map<PayloadIdentifier, VivecraftPayloadC2S>> legacyDataMap = new HashMap<>();

    public NetworkHandler(JavaPlugin plugin) {
        PLUGIN = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        VivecraftPayloadC2S payload = VivecraftPayloadC2S.readPacket(
            new DataInputStream(new ByteArrayInputStream(message)));

        // dont try to handle unknown data
        if (payload instanceof UnknownPayloadC2S) return;

        VivePlayer vivePlayer = ViveMain.getVivePlayer(player);

        // clients are expected to send a VERSION packet first
        if (vivePlayer == null && payload.payloadId() != PayloadIdentifier.VERSION) {
            return;
        }

        // the player object changes in some circumstances, like respawning, so need to make sure it's up to date
        if (vivePlayer != null) {
            vivePlayer.player = player;
        }
        switch (payload.payloadId()) {
            case VERSION:
                this.handleVersion(player, channel, (VersionPayloadC2S) payload);
                break;
            case IS_VR_ACTIVE:
                this.handleIsActive(vivePlayer, (VRActivePayloadC2S) payload);
                break;
            case DRAW:
                vivePlayer.draw = ((DrawPayloadC2S) payload).draw;
                break;
            case VR_PLAYER_STATE:
                vivePlayer.setVrPlayerState(((VRPlayerStatePayloadC2S) payload).playerState);
                break;
            case WORLDSCALE:
                vivePlayer.worldScale = ((WorldScalePayloadC2S) payload).worldScale;
                break;
            case HEIGHT:
                vivePlayer.heightScale = ((HeightPayloadC2S) payload).heightScale;
                break;
            case TELEPORT:
                this.handleTeleport(vivePlayer, (TeleportPayloadC2S) payload);
                break;
            case CLIMBING:
                this.handleClimbing(vivePlayer);
                break;
            case ACTIVEHAND:
                this.handleActiveHand(vivePlayer, (ActiveBodyPartPayloadC2S) payload);
                break;
            case CRAWL:
                this.handleCrawl(vivePlayer, (CrawlPayloadC2S) payload);
                break;
            case DAMAGE_DIRECTION:
                vivePlayer.wantsDamageDirection = true;
                break;
            // legacy support
            case CONTROLLER0DATA:
            case CONTROLLER1DATA:
            case HEADDATA:
                this.handleLegacyData(vivePlayer, payload);
                break;
            default:
                throw new IllegalStateException(
                    "Vivecraft: got unexpected packet on server: " + payload.payloadId());
        }
    }

    private void handleVersion(Player player, String channel, VersionPayloadC2S version) {
        // Vivecraft client connected, send server settings
        VivePlayer vivePlayer = new VivePlayer(player);
        vivePlayer.channel = channel;
        vivePlayer.version = version.version;
        vivePlayer.mcVersion = Utils.getMCVersion(vivePlayer.version);

        Debug.log("player '%s' joined with vivecraft '%s' on MC '%s'", player.getName(), vivePlayer.version,
            vivePlayer.mcVersion);

        if (!version.legacy) {
            // check if client supports a supported version
            if (NetworkConstants.MIN_SUPPORTED_NETWORK_PROTOCOL <= version.maxVersion &&
                version.minVersion <= NetworkConstants.MAX_SUPPORTED_NETWORK_PROTOCOL)
            {
                vivePlayer.networkVersion = NetworkVersion.fromProtocolVersion(
                    Math.min(version.maxVersion, NetworkConstants.MAX_SUPPORTED_NETWORK_PROTOCOL));
                Debug.log("%s networking supported, using version %s", player.getName(),
                    vivePlayer.networkVersion);
            } else {
                // unsupported version, send notification, and disregard
                player.sendMessage("Unsupported vivecraft version, VR features will not work");
                Debug.log(
                    "%s networking not supported. client range [%s,%s], server range [%s,%s]",
                    player.getName(),
                    version.minVersion,
                    version.maxVersion,
                    NetworkConstants.MIN_SUPPORTED_NETWORK_PROTOCOL,
                    NetworkConstants.MAX_SUPPORTED_NETWORK_PROTOCOL);
                return;
            }
        } else {
            // client didn't send a version, so it's a legacy client
            vivePlayer.networkVersion = NetworkVersion.LEGACY;
            Debug.log("%s using legacy networking", player.getName());
        }

        vivePlayer.setVR(version.vr);

        ViveMain.VIVE_PLAYERS.put(player.getUniqueId(), vivePlayer);

        sendPacket(vivePlayer, new VersionPayloadS2C(ViveMain.VERSION));
        if (ViveMain.CONFIG.requestData.get()) {
            sendPacket(vivePlayer, new RequestDataPayloadS2C());
        }

        // send server settings
        if (ViveMain.CONFIG.climbeyEnabled.get()) {
            sendPacket(vivePlayer, PacketUtils.getClimbeyServerPayload(vivePlayer));
        }

        // always send in new versions to allow disabling of teleports
        if (ViveMain.CONFIG.teleportEnabled.get() || NetworkVersion.OPTION_TOGGLE.accepts(vivePlayer.networkVersion)) {
            sendPacket(vivePlayer,
                new TeleportPayloadS2C(ViveMain.CONFIG.teleportEnabled.get(), vivePlayer.networkVersion));
        }

        if (ViveMain.CONFIG.teleportLimitedSurvival.get()) {
            sendPacket(vivePlayer, PacketUtils.getSurvivalTeleportOverridePayload());
        }

        if (ViveMain.CONFIG.worldscaleLimited.get()) {
            sendPacket(vivePlayer, PacketUtils.getWorldScaleOverridePayload());
        }

        if (ViveMain.CONFIG.forceThirdPersonItems.get()) {
            sendPacket(vivePlayer, PacketUtils.getThirdPersonItemsOverridePayload());
        }

        if (ViveMain.CONFIG.forceThirdPersonItemsCustom.get()) {
            sendPacket(vivePlayer, PacketUtils.getThirdPersonItemsCustomOverridePayload());
        }

        if (ViveMain.MC.supportsCrawling()) {
            if (vivePlayer.isVR()) {
                ViveMain.NMS.addCrawlPoseWrapper(vivePlayer.player);
            }
            if (ViveMain.CONFIG.crawlingEnabled.get()) {
                sendPacket(vivePlayer, new CrawlPayloadS2C(true, vivePlayer.networkVersion));
            }
        }

        // send if hotswitching is allowed
        sendPacket(vivePlayer, PacketUtils.getVRSwitchingPayload());

        if (NetworkVersion.DUAL_WIELDING.accepts(vivePlayer.networkVersion)) {
            sendPacket(vivePlayer, new DualWieldingPayloadS2C(ViveMain.CONFIG.dualWielding.get()));
        }

        // send vr changes settings, to inform the client what is non default
        if (NetworkVersion.SERVER_VR_CHANGES.accepts(vivePlayer.networkVersion)) {
            Map<String, String> settings = new HashMap<>();
            for (ConfigBuilder.ConfigValue<?> config : ViveMain.CONFIG.getConfigValues()) {
                if (config.getPath().startsWith("vrChanges") && !config.isDefault()) {
                    settings.put(config.getPath(), String.valueOf(config.get()));
                }
            }
            if (!settings.isEmpty()) {
                sendPacket(vivePlayer, new ServerVrChangesS2CPacket(settings));
            }
        }

        sendPacket(vivePlayer, new NetworkVersionPayloadS2C(vivePlayer.networkVersion));
    }

    private void handleIsActive(VivePlayer vivePlayer, VRActivePayloadC2S active) {
        if (vivePlayer.isVR() == active.vr) return;

        vivePlayer.setVR(!vivePlayer.isVR());
        PermissionManager.updatePlayerPermissionGroup(vivePlayer.player);

        if (!vivePlayer.isVR()) {
            // send all nearby players that the state changed
            // this is only needed for OFF, to delete the clientside vr player state
            sendPacketToTrackingPlayers(vivePlayer, new VRActivePayloadS2C(false, vivePlayer.player.getUniqueId()));
            MetadataHelper.cleanupMetadata(vivePlayer.player);
        } else if (ViveMain.MC.supportsCrawling()) {
            ViveMain.NMS.addCrawlPoseWrapper(vivePlayer.player);
        }
    }

    private void handleTeleport(VivePlayer vivePlayer, TeleportPayloadC2S teleport) {
        if (!ViveMain.CONFIG.teleportEnabled.get()) return;
        Location loc = vivePlayer.player.getLocation();
        loc.setX(teleport.x);
        loc.setY(teleport.y);
        loc.setZ(teleport.z);
        Platform.getInstance().teleportEntity(vivePlayer.player, loc, null);
    }

    private void handleClimbing(VivePlayer vivePlayer) {
        if (!ViveMain.CONFIG.climbeyEnabled.get()) return;
        ViveMain.NMS.resetFallDistance(vivePlayer.player);
    }

    private void handleActiveHand(VivePlayer vivePlayer, ActiveBodyPartPayloadC2S activeBodypart) {
        VRBodyPart newBodyPart = activeBodypart.bodyPart;
        if (vivePlayer.isSeated() && newBodyPart != VRBodyPart.HEAD) {
            newBodyPart = VRBodyPart.MAIN_HAND;
        }
        vivePlayer.useBodyPartForAim = activeBodypart.useForAim;
        if (vivePlayer.activeBodyPart != newBodyPart && ViveMain.CONFIG.dualWielding.get() &&
            NetworkVersion.DUAL_WIELDING.accepts(vivePlayer.networkVersion))
        {
            // handle equipment changes
            if (vivePlayer.itemOverride != null) {
                // restore original items
                // if the item broke, make itembreak effects
                Object newNmsItem = ViveMain.NMS.getHandItemInternal(vivePlayer.player, VRBodyPart.MAIN_HAND);
                if (!ViveMain.NMS.itemStackMatch(vivePlayer.itemOverride.override, newNmsItem)) {
                    ItemStack newItem = BukkitReflector.asBukkitCopy(newNmsItem);
                    if (newItem == null || newItem.getType() == Material.AIR) {
                        ViveMain.API.breakItemEffects(vivePlayer.player, vivePlayer.itemOverride.overridePart,
                            BukkitReflector.asBukkitCopy(vivePlayer.itemOverride.override));
                    }
                }

                // in case the item broke, we need to move the new empty item to the old slot
                ViveMain.NMS.setHandItemInternal(vivePlayer.player,
                    vivePlayer.itemOverride.overridePart,
                    ViveMain.NMS.getHandItemInternal(vivePlayer.player, VRBodyPart.MAIN_HAND));

                ViveMain.NMS.setHandItemInternal(vivePlayer.player,
                    VRBodyPart.MAIN_HAND,
                    vivePlayer.itemOverride.original);

                // undo the overridden equipment change
                ViveMain.NMS.applyEquipmentChange(vivePlayer.player,
                    vivePlayer.itemOverride.override,
                    vivePlayer.itemOverride.original);

                vivePlayer.itemOverride = null;
            }

            // we only need to cache it the new hand is not the main hand
            if (newBodyPart != VRBodyPart.MAIN_HAND) {
                vivePlayer.itemOverride = new ItemOverride(
                    newBodyPart,
                    ViveMain.NMS.getHandItemInternal(vivePlayer.player, VRBodyPart.MAIN_HAND),
                    // copy because the item break sets it to air
                    ViveMain.NMS.getItemStackCopy(ViveMain.NMS.getHandItemInternal(vivePlayer.player, newBodyPart)));
            }

            vivePlayer.activeBodyPart = newBodyPart;

            // change attributes to the new item
            ViveMain.NMS.applyEquipmentChange(vivePlayer.player,
                ViveMain.NMS.getHandItemInternal(vivePlayer.player, VRBodyPart.MAIN_HAND),
                ViveMain.NMS.getHandItemInternal(vivePlayer.player, newBodyPart));

            // set the new active item
            ViveMain.NMS.setHandItemInternal(vivePlayer.player,
                VRBodyPart.MAIN_HAND,
                ViveMain.NMS.getHandItemInternal(vivePlayer.player, newBodyPart));
        }
    }

    private void handleCrawl(VivePlayer vivePlayer, CrawlPayloadC2S crawl) {
        if (!ViveMain.MC.supportsCrawling() || !ViveMain.CONFIG.crawlingEnabled.get()) return;
        vivePlayer.crawling = crawl.crawling;
        ViveMain.NMS.setSwimPose(vivePlayer.player);
    }

    private void handleLegacyData(VivePlayer vivePlayer, VivecraftPayloadC2S payload) {
        Map<PayloadIdentifier, VivecraftPayloadC2S> playerData = this.legacyDataMap.computeIfAbsent(
            vivePlayer.player.getUniqueId(), id -> new HashMap<>());

        // keep the payload around
        playerData.put(payload.payloadId(), payload);

        if (playerData.size() == 3) {
            // we have all data
            LegacyController0DataPayloadC2S controller0Data = (LegacyController0DataPayloadC2S) playerData
                .get(PayloadIdentifier.CONTROLLER0DATA);
            LegacyController1DataPayloadC2S controller1Data = (LegacyController1DataPayloadC2S) playerData
                .get(PayloadIdentifier.CONTROLLER1DATA);
            LegacyHeadDataPayloadC2S headData = (LegacyHeadDataPayloadC2S) playerData
                .get(PayloadIdentifier.HEADDATA);

            Pose head = headData.hmdPose;
            Pose main = controller0Data.mainHand;
            Pose off = controller1Data.offHand;
            if (vivePlayer.mcVersion.major < 13) {
                Vector pos = vivePlayer.player.getLocation().toVector().multiply(-1.0);
                // old clients sent world position
                head = head.offset(pos);
                main = main.offset(pos);
                off = off.offset(pos);
            }

            vivePlayer.setVrPlayerState(new VrPlayerState(
                headData.seated, // isSeated
                head, // head pose
                controller0Data.leftHanded, // leftHanded 0
                main, // mainHand pose
                controller1Data.leftHanded, // leftHanded 1
                off, // offHand pose
                FBTMode.ARMS_ONLY, null,
                null, null,
                null, null,
                null, null));

            this.legacyDataMap.remove(vivePlayer.player.getUniqueId());
        }
    }

    /**
     * Sends the given {@code payload} to the given {@code player}
     *
     * @param vivePlayer Player to send the packet to
     * @param payload    packet to send
     */
    public static void sendPacket(VivePlayer vivePlayer, VivecraftPayloadS2C payload) {
        if (PLUGIN == null) {
            ViveMain.LOGGER.warning("tried to send a packet before init: " + payload.payloadId());
            return;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out))
        {
            payload.write(data);
            vivePlayer.player.sendPluginMessage(PLUGIN, vivePlayer.channel, out.toByteArray());
        } catch (IOException e) {
            ViveMain.LOGGER.log(Level.SEVERE, "Failed to send packet: " + payload.payloadId(), e);
        }
    }

    /**
     * Sends an update packet for the given {@code config} to all VivePlayers on the server
     *
     * @param config ConfigValue to send an update for
     */
    public static void sendUpdatePacketToAll(ConfigBuilder.ConfigValue config) {
        BiFunction<Object, VivePlayer, VivecraftPayloadS2C> function = config.getPacketFunction();
        if (function != null) {
            for (VivePlayer vivePlayer : ViveMain.VIVE_PLAYERS.values()) {
                VivecraftPayloadS2C payload = function.apply(config.get(), vivePlayer);
                // old clients cannot clear server overrides, crawl or tp
                if (!NetworkVersion.OPTION_TOGGLE.accepts(vivePlayer.networkVersion) &&
                    ((payload instanceof SettingOverridePayloadS2C && ((SettingOverridePayloadS2C) payload).clear) ||
                        (payload instanceof CrawlPayloadS2C && !((CrawlPayloadS2C) payload).allowed) ||
                        (payload instanceof TeleportPayloadS2C && !((TeleportPayloadS2C) payload).allowed)
                    ))
                {
                    continue;
                }
                sendPacket(vivePlayer, payload);
            }
        }
    }

    /**
     * kicks any players that are not allowed based on the current vive/vr only settings, and sends if vr switching is allowed
     */
    public static void updateViveVROnly() {
        // get all players
        // need to make a copy, since kicking a player causes a concurrent modification exception
        for (Player player : new ArrayList<>(Bukkit.getServer().getOnlinePlayers())) {
            // this could technically cause a race condition, where a player didn't send the vivecraft packet yet and
            // gets kicked because of that, but that should be neglectable, since server settings don't change that often
            NetworkUtils.kickIfNotAllowed(player);
        }

        // update if vr switching is allowed
        for (VivePlayer vivePlayer : ViveMain.VIVE_PLAYERS.values()) {
            sendPacket(vivePlayer, PacketUtils.getVRSwitchingPayload());
        }
    }

    /**
     * sends a haptic event to the given player if they are in VR, to be processed on the client
     */
    public static void sendHapticToClient(
        Player player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay)
    {
        VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
        if (vivePlayer != null && vivePlayer.isVR() &&
            NetworkVersion.HAPTIC_PACKET.accepts(vivePlayer.networkVersion))
        {
            sendPacket(vivePlayer, new HapticPayloadS2C(bodyPart, duration, frequency, amplitude, delay));
        }
    }

    /**
     * send the players VR data to all other players that can see them
     *
     * @param vivePlayer player to send the VR data for
     */
    public static void sendVrPlayerStateToClients(VivePlayer vivePlayer) {
        // create the packets here, to try to avoid unnecessary memory copies when creating multiple packets
        VrPlayerState state = vivePlayer.vrPlayerState();
        // old legacy clients (pre 16) expect the player data to be in world space
        LazySupplier<UberPacketPayloadS2C> oldLegacy = new LazySupplier<>(
            () -> new UberPacketPayloadS2C(vivePlayer.player.getUniqueId(),
                new VrPlayerState(state, NetworkVersion.LEGACY, vivePlayer.player.getLocation().toVector()),
                vivePlayer.worldScale, vivePlayer.heightScale));

        LazySupplier<UberPacketPayloadS2C> legacy = new LazySupplier<>(
            () -> new UberPacketPayloadS2C(vivePlayer.player.getUniqueId(),
                new VrPlayerState(state, NetworkVersion.LEGACY, null), vivePlayer.worldScale, vivePlayer.heightScale));

        LazySupplier<UberPacketPayloadS2C> regular = new LazySupplier<>(
            () -> new UberPacketPayloadS2C(vivePlayer.player.getUniqueId(), state, vivePlayer.worldScale,
                vivePlayer.heightScale));

        sendPacketToTrackingPlayers(vivePlayer, player -> player.networkVersion == NetworkVersion.LEGACY ?
            (player.mcVersion.major < 16 ? oldLegacy.get() : legacy.get()) : regular.get());
    }

    /**
     * sends a packet to all players that can see {@code vivePlayer}
     *
     * @param vivePlayer player that needs to be seen to get the packet
     * @param payload    payload to send
     */
    private static void sendPacketToTrackingPlayers(VivePlayer vivePlayer, VivecraftPayloadS2C payload) {
        sendPacketToTrackingPlayers(vivePlayer, v -> payload);
    }

    /**
     * sends a packet to all players that can see {@code vivePlayer}
     *
     * @param vivePlayer     player that needs to be seen to get the packet
     * @param packetProvider provider for network packets, based on client network version
     */
    private static void sendPacketToTrackingPlayers(
        VivePlayer vivePlayer, Function<VivePlayer, VivecraftPayloadS2C> packetProvider)
    {
        // this is in chunks
        double distance = (vivePlayer.player.getServer().getViewDistance() + 1) * 16;
        // square it
        distance *= distance;
        for (VivePlayer other : ViveMain.VIVE_PLAYERS.values()) {
            if (other == vivePlayer || !other.player.isOnline() ||
                other.player.getWorld() != vivePlayer.player.getWorld())
            {
                continue;
            }
            if (vivePlayer.player.getLocation().distanceSquared(other.player.getLocation()) < distance) {
                sendPacket(other, packetProvider.apply(vivePlayer));
            }
        }

        if (ViveMain.CONFIG.sendDataToOwner.get()) {
            // send it to themselves
            sendPacket(vivePlayer, packetProvider.apply(vivePlayer));
        }
    }
}
