package org.vivecraft.markdown;

import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;
import org.vivecraft.config.Config;
import org.vivecraft.config.ConfigBuilder;
import org.vivecraft.util.JsonUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * just a helper to generate the markdown for the wiki entry
 */
public class MarkdownGenerator {

    private static Logger LOGGER;
    private static Map<String, String> TRANSLATIONS;

    public static void main(String[] args) {
        Plugin plugin = new DummyPlugin();
        LOGGER = plugin.getLogger();
        Config c = new Config(plugin, true);

        // regular vivecraft strings
        TRANSLATIONS = JsonUtils.readJsonMap(
            MarkdownGenerator.class.getClassLoader().getResourceAsStream("lang/en_us.json"));
        // spigot only or overrides
        TRANSLATIONS.putAll(JsonUtils.readJsonMap(
            MarkdownGenerator.class.getClassLoader().getResourceAsStream("lang/spigot_en_us.json")));

        File configFile = new File("./docs/Config.md");
        configFile.getParentFile().mkdirs();
        if (!writeLinesToFile(configFile, getLines(c))) {
            LOGGER.severe("Failed to write " + configFile);
        }
    }

    @SuppressWarnings("rawtypes")
    public static List<String> getLines(Config config) {
        List<String> lines = new ArrayList<>();
        LinkedList<String> path = new LinkedList<>();
        for (ConfigBuilder.ConfigValue value : config.getConfigValues()) {
            String[] configPath = value.getPath().split("\\.");
            for (int i = 0; i < configPath.length - 1; i++) {
                if (!path.isEmpty() && path.size() > i && !path.get(i).equals(configPath[i])) {
                    // remove this and all after it
                    while (path.size() > i) {
                        path.removeLast();
                    }
                }
                if (path.size() < i + 1) {
                    // not in there yet
                    path.addLast(configPath[i]);
                    String group = "vivecraft.serverSettings." + String.join(".", path);
                    addHeader(lines, group, i + 1);
                }
            }
            String tString = "vivecraft.serverSettings." + value.getPath();
            addHeader(lines, tString, 3);
            addComments(lines, tString + ".tooltip");
            addComments(lines, tString + ".tooltipall");
            addComments(lines, tString + ".tooltipspigot");
            lines.add(String.format("- Config path: `%s`", value.getPath()));
            if (value instanceof ConfigBuilder.StringValue) {
                lines.add(String.format("- Default Value: `'%s'`", value.getDefaultValue()));
            } else {
                lines.add(String.format("- Default Value: `%s`", value.getDefaultValue()));
            }
            if (value instanceof ConfigBuilder.NumberValue) {
                ConfigBuilder.NumberValue n = (ConfigBuilder.NumberValue) value;
                lines.add(String.format("- Min: `%s`", n.getMin()));
                lines.add(String.format("- Max: `%s`", n.getMax()));
            }
            lines.add("");
        }
        return lines;
    }

    private static void addComments(List<String> lines, String key) {
        if (TRANSLATIONS.containsKey(key)) {
            String comment = TRANSLATIONS.get(key);
            // remove any formatting codes
            comment = comment.replaceAll("§.", "");
            comment = comment.replaceAll("(\\W)(['\"])", "$1`");
            comment = comment.replaceAll("['\"](\\W|$)", "`$1");
            comment = comment.replaceAll("``", "`''`");
            comment = comment.replaceAll("(?<![^\\s()])\\d+(\\.\\d+){0,2}\\+?(?!\\S)", "`$0`");
            for (String s : comment.split("\n")) {
                if (!s.trim().isEmpty()) {
                    lines.add(s.trim() + "  ");
                }
            }
        }
    }

    private static void addHeader(List<String> lines, String key, int count) {
        if (TRANSLATIONS.containsKey(key)) {
            String header = TRANSLATIONS.get(key);
            // remove any formatting codes
            header = header.replaceAll("§.", "");
            lines.add(StringUtils.repeat("#", count) + " " + header);
            addComments(lines, key + ".tooltipall");
        } else {
            LOGGER.severe("Unknown header key: " + key);
        }
    }

    private static boolean writeLinesToFile(File file, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write markdown file", e);
            return false;
        }
        return true;
    }
}
