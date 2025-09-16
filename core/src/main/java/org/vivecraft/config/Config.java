package org.vivecraft.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.vivecraft.ViveMain;
import org.vivecraft.config.enums.ClimbeyBlockmode;
import org.vivecraft.config.enums.HeadshotIndicator;
import org.vivecraft.network.NetworkHandler;
import org.vivecraft.network.PacketUtils;
import org.vivecraft.network.packet.s2c.AttackWhileBlockingPayloadS2C;
import org.vivecraft.network.packet.s2c.CrawlPayloadS2C;
import org.vivecraft.network.packet.s2c.DualWieldingPayloadS2C;
import org.vivecraft.network.packet.s2c.TeleportPayloadS2C;
import org.vivecraft.util.MCVersion;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Config {

    // config keys
    // debug
    public final ConfigBuilder.BooleanValue debug;
    public final ConfigBuilder.BooleanValue debugParticlesEnabled;
    public final ConfigBuilder.BooleanValue debugParticlesOpOnly;
    public final ConfigBuilder.BooleanValue debugParticlesVrDevice;
    public final ConfigBuilder.BooleanValue debugParticlesVrHead;
    public final ConfigBuilder.BooleanValue debugParticlesHeadHitbox;

    // general
    public final ConfigBuilder.BooleanValue checkForUpdates;
    public final ConfigBuilder.BooleanValue vrOnly;
    public final ConfigBuilder.BooleanValue viveOnly;
    public final ConfigBuilder.BooleanValue allowOp;
    public final ConfigBuilder.IntValue messageKickDelay;
    public final ConfigBuilder.BooleanValue vrFun;
    public final ConfigBuilder.BooleanValue requestData;
    public final ConfigBuilder.BooleanValue sendData;
    public final ConfigBuilder.BooleanValue sendDataToOwner;

    public final ConfigBuilder.BooleanValue spigotSettingsEnabled;
    public final ConfigBuilder.DoubleValue spigotSettingsMovedTooQuickly;
    public final ConfigBuilder.DoubleValue spigotSettingsMovedWronglyThreshold;

    // permissions
    public final ConfigBuilder.BooleanValue permissionsGroupsEnabled;
    public final ConfigBuilder.StringValue permissionsNonVRGroup;
    public final ConfigBuilder.StringValue permissionsVRGroup;
    public final ConfigBuilder.StringValue permissionsClimbPermission;

    // messages
    public final ConfigBuilder.BooleanValue messagesEnabled;

    public final ConfigBuilder.StringValue messagesWelcomeVr;
    public final ConfigBuilder.StringValue messagesWelcomeNonvr;
    public final ConfigBuilder.StringValue messagesWelcomeSeated;
    public final ConfigBuilder.StringValue messagesWelcomeVanilla;

    public final ConfigBuilder.StringValue messagesDeathVr;
    public final ConfigBuilder.StringValue messagesDeathNonvr;
    public final ConfigBuilder.StringValue messagesDeathSeated;
    public final ConfigBuilder.StringValue messagesDeathVanilla;
    public final ConfigBuilder.StringValue messagesDeathByMobVr;
    public final ConfigBuilder.StringValue messagesDeathByMobNonvr;
    public final ConfigBuilder.StringValue messagesDeathByMobSeated;
    public final ConfigBuilder.StringValue messagesDeathByMobVanilla;

    public final ConfigBuilder.StringValue messagesLeaveMessage;

    public final ConfigBuilder.StringValue messagesKickViveOnly;
    public final ConfigBuilder.StringValue messagesKickVrOnly;

    // vrChanges
    public final ConfigBuilder.BooleanValue dualWielding;
    public final ConfigBuilder.DoubleValue bootsArmorDamage;
    public final ConfigBuilder.DoubleValue creeperSwellDistance;
    public final ConfigBuilder.DoubleValue projectileInaccuracyMultiplier;
    public final ConfigBuilder.BooleanValue allowFasterBlockBreaking;
    public final ConfigBuilder.BooleanValue allowRoomscaleShieldBlocking;
    public final ConfigBuilder.BooleanValue allowAttacksWhileBlocking;
    // bow
    public final ConfigBuilder.DoubleValue bowStandingMultiplier;
    public final ConfigBuilder.DoubleValue bowSeatedMultiplier;
    public final ConfigBuilder.DoubleValue bowStandingHeadshotMultiplier;
    public final ConfigBuilder.DoubleValue bowSeatedHeadshotMultiplier;
    public final ConfigBuilder.DoubleValue bowVanillaHeadshotMultiplier;
    public final ConfigBuilder.EnumValue<HeadshotIndicator> bowHeadshotIndicator;

    // pvp
    public final ConfigBuilder.BooleanValue pvpVrVsVr;
    public final ConfigBuilder.BooleanValue pvpSeatedvrVsSeatedvr;
    public final ConfigBuilder.BooleanValue pvpVrVsNonvr;
    public final ConfigBuilder.BooleanValue pvpSeatedvrVsNonvr;
    public final ConfigBuilder.BooleanValue pvpVrVsSeatedvr;
    public final ConfigBuilder.BooleanValue pvpNotifyBlockedDamage;

    // climbey
    public final ConfigBuilder.BooleanValue climbeyEnabled;
    public final ConfigBuilder.EnumValue<ClimbeyBlockmode> climbeyBlockmode;
    public final ConfigBuilder.StringListValue climbeyBlocklist;

    // crawling
    public final ConfigBuilder.BooleanValue crawlingEnabled;

    // teleport
    public final ConfigBuilder.BooleanValue teleportEnabled;
    public final ConfigBuilder.BooleanValue teleportLimitedSurvival;
    public final ConfigBuilder.IntValue teleportUpLimit;
    public final ConfigBuilder.IntValue teleportDownLimit;
    public final ConfigBuilder.IntValue teleportHorizontalLimit;

    // worldscale
    public final ConfigBuilder.BooleanValue worldscaleLimited;
    public final ConfigBuilder.DoubleValue worldscaleMax;
    public final ConfigBuilder.DoubleValue worldscaleMin;

    // settingOverrides
    public final ConfigBuilder.BooleanValue forceThirdPersonItems;
    public final ConfigBuilder.BooleanValue forceThirdPersonItemsCustom;

    // vr switching
    public final ConfigBuilder.BooleanValue vrSwitchingEnabled;

    private final ConfigBuilder builder;

    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;

        MCVersion mc = MCVersion.getCurrent();

        this.builder = new ConfigBuilder(plugin.getConfig());
        this.builder
            .push("general");
        this.checkForUpdates = this.builder
            .push("checkForUpdate")
            .define(true);
        this.vrOnly = this.builder
            .push("vr_only")
            .define(false)
            .setOnUpdate((oV, nV) -> NetworkHandler.updateViveVROnly());
        this.viveOnly = this.builder
            .push("vive_only")
            .define(false)
            .setOnUpdate((oV, nV) -> NetworkHandler.updateViveVROnly());
        this.allowOp = this.builder
            .push("allow_op")
            .define(true)
            .setOnUpdate((oV, nV) -> NetworkHandler.updateViveVROnly());
        this.messageKickDelay = this.builder
            .push("messageAndKickDelay")
            .defineInRange(200, 100, 1000);
        this.vrFun = this.builder
            .push("vrFun")
            .define(true);
        this.requestData = this.builder
            .push("requestData")
            .define(true)
            .setNeedsReload(true);
        this.sendData = this.builder
            .push("sendData")
            .define(true)
            .setOnUpdate((oV, nV) -> ViveMain.INSTANCE.toggleDataTask(nV));
        this.sendDataToOwner = this.builder
            .push("sendDataToOwner")
            .define(false);
        // end general
        this.builder.pop();

        this.builder.push("setSpigotSettings");
        this.spigotSettingsEnabled = this.builder
            .push("enabled")
            .define(true)
            .setNeedsReload(true);
        this.spigotSettingsMovedWronglyThreshold = this.builder
            .push("movedWronglyThreshold")
            .defineInRange(15.0, 0.0, 10000.0)
            .setNeedsReload(true);
        this.spigotSettingsMovedTooQuickly = this.builder
            .push("movedTooQuickly")
            .defineInRange(64.0, 0.0, 10000.0)
            .setNeedsReload(true);
        // end spigot settings
        this.builder.pop();

        this.builder.push("permissions");
        this.permissionsGroupsEnabled = this.builder
            .push("groupsEnabled")
            .define(true);
        this.permissionsNonVRGroup = this.builder
            .push("NonVRgroup")
            .define("vive.non-vivegroup");
        this.permissionsVRGroup = this.builder
            .push("VRgroup")
            .define("vive.vivegroup");
        this.permissionsClimbPermission = this.builder
            .push("climbperm")
            .define("vive.climbanywhere")
            .setPacketFunction((v, p) -> PacketUtils.getClimbeyServerPayload(p));
        // end permissions
        this.builder.pop();

        this.builder
            .push("messages");
        this.messagesEnabled = this.builder
            .push("enabled")
            .define(false);

        // welcome messages
        this.messagesWelcomeVr = this.builder
            .push("welcomeVR")
            .define("&player has joined with standing VR!");
        this.messagesWelcomeNonvr = this.builder
            .push("welcomeNonVR")
            .define("&player has joined with Non-VR companion!");
        this.messagesWelcomeSeated = this.builder
            .push("welcomeSeated")
            .define("&player has joined with seated VR!");
        this.messagesWelcomeVanilla = this.builder
            .push("welcomeVanilla")
            .define("&player has joined as a Muggle!");

        this.messagesLeaveMessage = this.builder
            .push("leaveMessage")
            .define("&player has disconnected from the server!");

        // general death messages
        this.messagesDeathVr = this.builder
            .push("deathVR")
            .define("&player died in standing VR!");
        this.messagesDeathNonvr = this.builder
            .push("deathNonVR")
            .define("&player died in Non-VR companion!");
        this.messagesDeathSeated = this.builder
            .push("deathSeated")
            .define("&player died in seated VR!");
        this.messagesDeathVanilla = this.builder
            .push("deathVanilla")
            .define("&player died as a Muggle!");

        // death messages by mobs
        this.messagesDeathByMobVr = this.builder
            .push("deathByMobVR")
            .define("&player was slain by &cause in standing VR!");
        this.messagesDeathByMobNonvr = this.builder
            .push("deathByMobNonVR")
            .define("&player was slain by &cause in Non-VR companion!");
        this.messagesDeathByMobSeated = this.builder
            .push("deathByMobSeated")
            .define("&player was slain by &cause in seated VR!");
        this.messagesDeathByMobVanilla = this.builder
            .push("deathByMobVanilla")
            .define("&player was slain by &cause as a Muggle!");

        // kick messages
        this.messagesKickViveOnly = this.builder
            .push("KickViveOnly")
            .define("This server is configured for Vivecraft players only.");
        this.messagesKickVrOnly = this.builder
            .push("KickVROnly")
            .define("This server is configured for VR players only.");
        // end messages
        this.builder.pop();

        this.builder
            .push("vrChanges");
        this.creeperSwellDistance = this.builder
            .push("creeperSwellDistance")
            .defineInRange(1.75, 0.1, 10.0);
        this.dualWielding = this.builder
            .push("dualWielding")
            .define(true)
            .setPacketFunction((v, p) -> new DualWieldingPayloadS2C(v));
        this.bootsArmorDamage = this.builder
            .push("bootsArmorDamage")
            .defineInRange(0.0, 0.0, 5.0);
        this.projectileInaccuracyMultiplier = this.builder
            .push("projectileInaccuracyMultiplier")
            .defineInRange(1.0, 0.0, 1.0);
        this.allowFasterBlockBreaking = this.builder
            .push("allowFasterBlockBreaking")
            .define(true);
        this.allowRoomscaleShieldBlocking = this.builder
            .push("allowRoomscaleShieldBlocking")
            .define(true);
        this.allowAttacksWhileBlocking = this.builder
            .push("allowAttacksWhileBlocking")
            .define(true)
            .setPacketFunction((v, p) -> new AttackWhileBlockingPayloadS2C(v));

        this.builder
            .push("bow");
        this.bowStandingMultiplier = this.builder
            .push("standingMultiplier")
            .defineInRange(2.0, 1.0, 10.0);
        this.bowSeatedMultiplier = this.builder
            .push("seatedMultiplier")
            .defineInRange(1.0, 1.0, 10.0);
        this.bowStandingHeadshotMultiplier = this.builder
            .push("standingHeadshotMultiplier")
            .defineInRange(3.0, 1.0, 10.0);
        this.bowSeatedHeadshotMultiplier = this.builder
            .push("seatedHeadshotMultiplier")
            .defineInRange(2.0, 1.0, 10.0);
        this.bowVanillaHeadshotMultiplier = this.builder
            .push("vanillaHeadshotMultiplier")
            .defineInRange(1.0, 1.0, 10.0);
        this.bowHeadshotIndicator = this.builder
            .push("headshotIndicator")
            .defineEnum(HeadshotIndicator.BOTH, HeadshotIndicator.class);
        // end bow
        this.builder.pop();
        // end vrChanges
        this.builder.pop();

        this.builder
            .push("pvp");
        this.pvpNotifyBlockedDamage = this.builder
            .push("notifyBlockedDamage")
            .define(false);
        this.pvpVrVsVr = this.builder
            .push("VRvsVR")
            .define(true);
        this.pvpSeatedvrVsSeatedvr = this.builder
            .push("SEATEDVRvsSEATEDVR")
            .define(true);
        this.pvpVrVsNonvr = this.builder
            .push("VRvsNONVR")
            .define(true);
        this.pvpSeatedvrVsNonvr = this.builder
            .push("SEATEDVRvsNONVR")
            .define(true);
        this.pvpVrVsSeatedvr = this.builder
            .push("VRvsSEATEDVR")
            .define(true);
        // end pvp
        this.builder.pop();

        this.builder
            .push("climbey");
        this.climbeyEnabled = this.builder
            .push("enabled")
            .define(true)
            .setPacketFunction((v, p) -> PacketUtils.getClimbeyServerPayload(p));
        this.climbeyBlockmode = this.builder
            .push("blockmode")
            .defineEnum(ClimbeyBlockmode.DISABLED, ClimbeyBlockmode.class)
            .setPacketFunction((v, p) -> PacketUtils.getClimbeyServerPayload(p));
        this.climbeyBlocklist = this.builder
            .push("blocklist")
            .defineStringList(mc.major > 12 ?
                Arrays.asList("white_wool", "dirt", "grass_block") :
                Arrays.asList("wool:0", "dirt", "grass"))
            .setPacketFunction((v, p) -> PacketUtils.getClimbeyServerPayload(p));
        // end climbey
        this.builder.pop();

        this.builder
            .push("crawling");
        this.crawlingEnabled = this.builder
            .push("enabled")
            .define(true)
            .setPacketFunction((v, p) -> new CrawlPayloadS2C(v, p.networkVersion));
        // end crawling
        this.builder.pop();

        this.builder
            .push("teleport");
        this.teleportEnabled = this.builder
            .push("enabled")
            .define(true)
            .setPacketFunction((v, p) -> new TeleportPayloadS2C(v, p.networkVersion));
        this.teleportLimitedSurvival = this.builder
            .push("limitedSurvival")
            .define(false)
            .setPacketFunction((v, p) -> PacketUtils.getSurvivalTeleportOverridePayload());
        this.teleportUpLimit = this.builder
            .push("upLimit")
            .defineInRange(4, 1, 16)
            .setPacketFunction((v, p) -> PacketUtils.getSurvivalTeleportOverridePayload());
        this.teleportDownLimit = this.builder
            .push("downLimit")
            .defineInRange(4, 1, 16)
            .setPacketFunction((v, p) -> PacketUtils.getSurvivalTeleportOverridePayload());
        this.teleportHorizontalLimit = this.builder
            .push("horizontalLimit")
            .defineInRange(16, 1, 32)
            .setPacketFunction((v, p) -> PacketUtils.getSurvivalTeleportOverridePayload());
        // end teleport
        this.builder.pop();

        this.builder
            .push("worldScale");
        this.worldscaleLimited = this.builder
            .push("limitRange")
            .define(false)
            .setPacketFunction((v, p) -> PacketUtils.getWorldScaleOverridePayload());
        this.worldscaleMin = this.builder
            .push("min")
            .defineInRange(0.5, 0.1, 100.0)
            .setPacketFunction((v, p) -> PacketUtils.getWorldScaleOverridePayload());
        this.worldscaleMax = this.builder
            .push("max")
            .defineInRange(2.0, 0.1, 100.0)
            .setPacketFunction((v, p) -> PacketUtils.getWorldScaleOverridePayload());
        // end worldScale
        this.builder.pop();

        this.builder
            .push("settingOverrides");
        this.forceThirdPersonItems = this.builder
            .push("thirdPersonItems")
            .define(false)
            .setPacketFunction((v, p) -> PacketUtils.getThirdPersonItemsOverridePayload());
        this.forceThirdPersonItemsCustom = this.builder
            .push("thirdPersonItemsCustom")
            .define(false)
            .setPacketFunction((v, p) -> PacketUtils.getThirdPersonItemsCustomOverridePayload());
        // end settingOverrides
        this.builder.pop();

        this.builder
            .push("vrSwitching");
        this.vrSwitchingEnabled = this.builder
            .push("enabled")
            .define(true)
            .setPacketFunction((v, p) -> PacketUtils.getVRSwitchingPayload());
        // end vrSwitching
        this.builder.pop();

        this.builder
            .push("debug");
        this.debug = this.builder
            .push("debugLogging")
            .define(false);

        this.builder
            .push("particles");
        this.debugParticlesEnabled = this.builder
            .push("enabled")
            .define(false)
            .setOnUpdate((oV, nV) -> ViveMain.INSTANCE.toggleParticleTask(nV));
        this.debugParticlesOpOnly = this.builder
            .push("opOnly")
            .define(true);
        this.debugParticlesVrDevice = this.builder
            .push("vrDevices")
            .define(false);
        this.debugParticlesVrHead = this.builder
            .push("vrHead")
            .define(false);
        this.debugParticlesHeadHitbox = this.builder
            .push("headHitbox")
            .define(false);
        // end particles
        this.builder.pop();
        // end debug
        this.builder.pop();

        // fix any enums that are loaded as strings first
        for (ConfigBuilder.ConfigValue<?> configValue : this.builder.getConfigValues()) {
            if (configValue instanceof ConfigBuilder.EnumValue) {
                ConfigBuilder.EnumValue enumValue = (ConfigBuilder.EnumValue) configValue;
                if (enumValue.get() != null) {
                    enumValue.set(enumValue.getEnumValue(enumValue.get()));
                }
            }
        }

        fixValues();

        // save defaults
        save();
    }

    public void fixValues() {
        // if the config is outdated, or is missing keys, re add them
        this.builder.correct(s -> ViveMain.LOGGER.warning(s));
    }

    public void save() {
        // save the current state
        this.plugin.saveConfig();

        // add comments to it and resave
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        List<String> lines = commentLines(configFile);
        if (lines == null || !writeLinesToFile(configFile, lines)) {
            ViveMain.LOGGER.severe("Failed to comment config file");
            return;
        }

        // load the new commented file
        this.plugin.reloadConfig();
        this.builder.setNewConfigFile(this.plugin.getConfig());
    }

    public List<ConfigBuilder.ConfigValue> getConfigValues() {
        return this.builder.getConfigValues();
    }

    public List<String> commentLines(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> lines = new ArrayList<>();
            String line;
            String indent = null;
            Deque<String> stack = new ArrayDeque<>();
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    // skip comments or list values
                    continue;
                } else if (trimmed.contains(":")) {
                    // regular line
                    String newIndent = line.substring(0, line.indexOf(trimmed));
                    String entry = trimmed.substring(0, trimmed.indexOf(":"));
                    boolean leaf = false;
                    if (trimmed.endsWith(":")) {
                        if (indent == null || newIndent.length() >= indent.length()) {
                            stack.add(entry);
                        } else if (newIndent.isEmpty()) {
                            stack.clear();
                            stack.add(entry);
                            lines.add("");
                        } else {
                            stack.removeLast();
                            stack.add(entry);
                        }
                    } else {
                        stack.add(entry);
                        leaf = true;
                    }
                    indent = newIndent;
                    // add the comment
                    String tString = "vivecraft.serverSettings." + String.join(".", stack);

                    if (!leaf) {
                        addComments(lines, indent, tString, false);
                    }
                    addComments(lines, indent, tString + ".tooltip", leaf);
                    addComments(lines, indent, tString + ".tooltipall", false);
                    ConfigBuilder.ConfigValue c = this.builder.getConfigValue(String.join(".", stack));
                    if (c instanceof ConfigBuilder.NumberValue) {
                        ConfigBuilder.NumberValue n = (ConfigBuilder.NumberValue) c;
                        lines.add(indent +
                            String.format("# default: %s, min: %s, max: %s", n.getDefaultValue(), n.getMin(),
                                n.getMax()));
                    }

                    if (leaf) {
                        stack.removeLast();
                    }
                }
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            ViveMain.LOGGER.log(Level.SEVERE, "error commenting config: ", e);
            return null;
        }
    }

    private void addComments(List<String> lines, String indent, String key, boolean required) {
        if (ViveMain.TRANSLATIONS.containsKey(key)) {
            String comment = ViveMain.TRANSLATIONS.get(key);
            for (String s : comment.split("\n")) {
                if (!s.trim().isEmpty()) {
                    lines.add(indent + "#" + s);
                }
            }
        } else if (required) {
            ViveMain.LOGGER.severe("no comment for key: " + key);
        }
    }

    public boolean writeLinesToFile(File file, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            ViveMain.LOGGER.severe("Failed to comment config file2");
            return false;
        }
        return true;
    }
}
