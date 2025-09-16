package org.vivecraft.commands;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.vivecraft.ViveMain;
import org.vivecraft.config.ConfigBuilder;

import java.util.List;

public class ConfigCommandExecutor implements CommandExecutor {

    private enum Action {
        GET,
        SET,
        RESET,
        ADD,
        REMOVE,
        PASS, // success, but no action
        ERROR
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if ("help".equals(args[0])) {
            sender.sendMessage(new String[]{
                "Usage: /" + label + " <config> <action> <value>",
                "accepted actions:",
                "- get: prints the current value",
                "- set: sets the new specified value",
                "- reset: resets the config to the default value",
                "- add/remove: for the climbey blocklist, to add/remove blocks"
            });
            if (args.length >= 2) {
                ConfigBuilder.ConfigValue value = ViveMain.CONFIG.getConfigValues().stream()
                    .filter(c -> c.getPath().equals(args[1])).findFirst().orElse(null);
                if (value != null) {
                    String path = "vivecraft.serverSettings." + value.getPath();
                    sender.sendMessage((String[]) ArrayUtils.addAll(
                        new String[]{ViveMain.TRANSLATIONS.get(path) + ": " + value.getPath()},
                        ViveMain.TRANSLATIONS.get(path + ".tooltip").split("\n")));
                }
            }
            return true;
        }

        ConfigBuilder.ConfigValue config = ViveMain.CONFIG.getConfigValues().stream()
            .filter(c -> c.getPath().equals(args[0])).findFirst().orElse(null);

        if (config == null) {
            sender.sendMessage("unknown config: '" + padRed(args[0]) + "'");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "no action specified");
            return false;
        }

        Action action = process(sender, args, config);
        if (action != Action.ERROR && action != Action.GET) {
            ViveMain.CONFIG.save();
            if (config.needsReload()) {
                sender.sendMessage("For this config to take effect a " + padGold("server reload") + " is required.");
            }
        }
        return action != Action.ERROR;
    }

    private static Action process(CommandSender sender, String[] args, ConfigBuilder.ConfigValue config) {
        String action = args[1];

        if ("get".equals(action)) {
            sender.sendMessage(config.getPath() + " is currently set to: '" + padGreen(config.get()) + "'");
            return Action.GET;
        } else if ("reset".equals(action)) {
            config.reset();
            sender.sendMessage("reset '" + config.getPath() + "' to: '" + padGreen(config.get()) + "'");
            return Action.RESET;
        } else if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "no value specified!");
            return Action.ERROR;
        } else if ("add".equals(action) || "remove".equals(action)) {
            if (!(config instanceof ConfigBuilder.ListValue)) {
                sender.sendMessage(ChatColor.RED + "'" + action + "' is only applicable to the climbey block list");
                return Action.ERROR;
            } else if (!(config instanceof ConfigBuilder.StringListValue)) {
                sender.sendMessage(ChatColor.RED + "General Lists cannot be set with commands atm.");
                return Action.ERROR;
            }
            ConfigBuilder.StringListValue stringList = (ConfigBuilder.StringListValue) config;
            List<String> list = stringList.get();
            String value = args[2].trim();
            if ("add".equals(action)) {
                if (!list.contains(value)) {
                    list.add(value);
                    stringList.set(list);
                    sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                    return Action.ADD;
                } else {
                    sender.sendMessage(value + " is already in the list.");
                    return Action.PASS;
                }
            } else {
                // remove
                list.remove(value);
                stringList.set(list);
                sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                return Action.REMOVE;
            }
        } else if ("set".equals(action)) {
            String value = args[2].trim();
            if (config instanceof ConfigBuilder.ListValue) {
                sender.sendMessage(ChatColor.RED + "List configs cannot be set directly!");
                return Action.ERROR;
            } else if (config instanceof ConfigBuilder.NumberValue) {
                try {
                    if (config instanceof ConfigBuilder.IntValue) {
                        config.set(Integer.parseInt(value));
                    } else if (config instanceof ConfigBuilder.DoubleValue) {
                        config.set(Double.parseDouble(value));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("'" + padRed(value) + "' is not a valid number.");
                    return Action.ERROR;
                }
                sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                return Action.SET;
            } else if (config instanceof ConfigBuilder.EnumValue) {
                ConfigBuilder.EnumValue enumValue = (ConfigBuilder.EnumValue) config;
                Enum<?> o = enumValue.getEnumValue(value);
                if (o == null) {
                    sender.sendMessage("'" + padRed(value) + "' is not a valid value.");
                    return Action.ERROR;
                } else {
                    enumValue.set(o);
                    sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                    return Action.SET;
                }
            } else if (config instanceof ConfigBuilder.BooleanValue) {
                config.set(Boolean.parseBoolean(value));
                sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                return Action.SET;
            } else if (config instanceof ConfigBuilder.InListValue) {
                ConfigBuilder.InListValue inList = (ConfigBuilder.InListValue) config;
                for (Object s : inList.getValidValues()) {
                    if (s instanceof String && value.equalsIgnoreCase((String) s)) {
                        config.set(Boolean.parseBoolean(value));
                        sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                        return Action.SET;
                    }
                }
                sender.sendMessage(
                    new String[]{"'" + padRed(value) + "' is not a valid value for " + config.getPath() + ".",
                        "Valid values are: '" + String.join("', '", inList.getValidValues()) + "'"});
                return Action.ERROR;
            } else {
                config.set(value);
                sender.sendMessage(config.getPath() + " is now set to: '" + padGreen(config.get()) + "'");
                return Action.SET;
            }
        } else {
            sender.sendMessage("'" + padRed(action) + "' is not a valid action");
            return Action.ERROR;
        }
    }

    private static String padGold(Object o) {
        return padColor(o, ChatColor.GOLD);
    }

    private static String padRed(Object o) {
        return padColor(o, ChatColor.RED);
    }

    private static String padGreen(Object o) {
        return padColor(o, ChatColor.GREEN);
    }

    private static String padColor(Object o, ChatColor color) {
        return color + o.toString() + ChatColor.RESET;
    }
}
