package org.vivecraft;

import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.vivecraft.commands.ConfigCommandExecutor;
import org.vivecraft.commands.ConfigCommandTabCompleter;
import org.vivecraft.compat.*;
import org.vivecraft.config.Config;
import org.vivecraft.debug.Debug;
import org.vivecraft.events.DamageEvents;
import org.vivecraft.events.EntityEvents;
import org.vivecraft.events.PlayerEvents;
import org.vivecraft.events.ProjectileEvents;
import org.vivecraft.linker.Helpers;
import org.vivecraft.network.NetworkConstants;
import org.vivecraft.network.NetworkHandler;
import org.vivecraft.util.JsonUtils;
import org.vivecraft.util.MCVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Main entrypoint for the plugin
 */
public class ViveMain extends JavaPlugin {

    public static ViveMain INSTANCE;

    public static Logger LOGGER;

    public static Config CONFIG;

    public static Map<UUID, VivePlayer> VIVE_PLAYERS = new HashMap<>();

    public static ApiHelper API;

    public static McHelper MC;

    public static NMSHelper NMS;

    public static MCMods MC_MODS;

    public static String VERSION;

    public static Map<String, String> TRANSLATIONS;

    private BukkitTask dataTask;
    private BukkitTask particleTask;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();
        VERSION = getDescription().getName() + " " + getDescription().getVersion();

        // regular vivecraft strings
        TRANSLATIONS = JsonUtils.readJsonMap(getResource("lang/en_us.json"));
        // spigot only or overrides
        TRANSLATIONS.putAll(JsonUtils.readJsonMap(getResource("lang/spigot_en_us.json")));

        API = Helpers.getApi();
        MC = Helpers.getMc();
        NMS = Helpers.getNMS();
        MC_MODS = new MCMods();

        // set up config
        CONFIG = new Config(this);

        if (!PermissionManager.checkForVault() && ViveMain.CONFIG.permissionsGroupsEnabled.get()) {
            ViveMain.LOGGER.warning("To use the permission groups feature, 'Vault' needs to be installed");
        }

        // set up commands
        String configCommandString = "vivecraft-server-config";
        PluginCommand configCommand = this.getCommand(configCommandString);
        if (configCommand != null) {
            configCommand.setExecutor(new ConfigCommandExecutor());
            configCommand.setTabCompleter(new ConfigCommandTabCompleter());
        } else {
            LOGGER.severe("Command '" + configCommandString + "' is missing and couldn't be set up!");
        }

        // set up networking
        NetworkHandler handler = new NetworkHandler(this);
        getServer().getMessenger()
            .registerIncomingPluginChannel(this, NetworkConstants.CHANNEL, handler);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NetworkConstants.CHANNEL);

        if (MCVersion.getCurrent().major < 13) {
            // old versions used the channel 'Vivecraft'
            getServer().getMessenger()
                .registerIncomingPluginChannel(this, "Vivecraft", handler);
            getServer().getMessenger().registerOutgoingPluginChannel(this, "Vivecraft");
        }

        // register events
        this.registerEvents(getServer().getPluginManager());

        // register recipes
        registerRecipes();

        this.toggleParticleTask(CONFIG.debugParticlesEnabled.get());
        this.toggleDataTask(CONFIG.debugParticlesEnabled.get());
        if (CONFIG.spigotSettingsEnabled.get()) {
            SpigotReflector.setMovedTooQuickly(CONFIG.spigotSettingsMovedTooQuickly.get());
            SpigotReflector.setMovedWrongly(CONFIG.spigotSettingsMovedWronglyThreshold.get());
        }
        this.modifyEntities();
    }

    private void modifyEntities() {
        List<World> wrl = this.getServer().getWorlds();
        for (World world : wrl) {
            for (Entity e : world.getLivingEntities()) {
                NMS.modifyEntity(e);
            }
        }
    }

    private void registerEvents(PluginManager manager) {
        manager.registerEvents(new ProjectileEvents(), this);
        manager.registerEvents(new PlayerEvents(), this);
        manager.registerEvents(new DamageEvents(), this);
        manager.registerEvents(new EntityEvents(), this);
    }

    private void registerRecipes() {
        Recipes.addClawsRecipe();
        Recipes.addBootsRecipe();
    }

    @Override
    public void onDisable() {
        if (this.dataTask != null) {
            this.dataTask.cancel();
            this.dataTask = null;
        }
        if (this.particleTask != null) {
            this.particleTask.cancel();
            this.particleTask = null;
        }
    }

    private static void sendViveData() {
        for (VivePlayer vivePlayer : VIVE_PLAYERS.values()) {
            if (vivePlayer.isVR() && vivePlayer.vrPlayerState() != null) {
                NetworkHandler.sendVrPlayerStateToClients(vivePlayer);
            }
        }
    }

    public static boolean isVivePlayer(Entity entity) {
        return entity instanceof Player && VIVE_PLAYERS.containsKey(entity.getUniqueId());
    }

    public static boolean isVRPlayer(Entity entity) {
        return isVivePlayer(entity) && getVivePlayer(entity).isVR();
    }

    public static boolean isVRPlayer(UUID uuid) {
        return VIVE_PLAYERS.containsKey(uuid) && VIVE_PLAYERS.get(uuid).isVR();
    }

    public static VivePlayer getVivePlayer(Entity entity) {
        return VIVE_PLAYERS.get(entity.getUniqueId());
    }

    public static VivePlayer getVivePlayer(UUID uuid) {
        return VIVE_PLAYERS.get(uuid);
    }

    public void toggleParticleTask(boolean enabled) {
        if (enabled && this.particleTask == null) {
            this.particleTask = this.getServer().getScheduler().runTaskTimer(this, Debug::debugParticles, 20, 1);
        } else if (!enabled && this.particleTask != null) {
            this.particleTask.cancel();
            this.particleTask = null;
        }
    }

    public void toggleDataTask(boolean enabled) {
        Debug.log("setting data task to %s", enabled);
        if (enabled && this.dataTask == null) {
            this.dataTask = this.getServer().getScheduler().runTaskTimer(this, ViveMain::sendViveData, 20, 1);
        } else if (!enabled && this.dataTask != null) {
            this.dataTask.cancel();
            this.dataTask = null;
        }
    }
}
