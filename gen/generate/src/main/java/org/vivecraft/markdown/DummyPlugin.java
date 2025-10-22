package org.vivecraft.markdown;

import com.avaje.ebean.EbeanServer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DummyPlugin extends PluginBase {

    @Override
    public File getDataFolder() {
        return new File("./");
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return null;
    }

    @Override
    public FileConfiguration getConfig() {
        return new FileConfiguration() {
            @Override
            public String saveToString() {
                return "";
            }

            @Override
            public void loadFromString(String contents) {}

            @Override
            protected String buildHeader() {
                return "";
            }
        };
    }

    @Override
    public InputStream getResource(String filename) {
        return null;
    }

    @Override
    public void saveConfig() {}

    @Override
    public void saveDefaultConfig() {}

    @Override
    public void saveResource(String resourcePath, boolean replace) {}

    @Override
    public void reloadConfig() {}

    @Override
    public PluginLoader getPluginLoader() {
        return null;
    }

    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void onDisable() {}

    @Override
    public void onLoad() {}

    @Override
    public void onEnable() {}

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean canNag) {}

    @Override
    public EbeanServer getDatabase() {
        return null;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return null;
    }

    @Override
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
