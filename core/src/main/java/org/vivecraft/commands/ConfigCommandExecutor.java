package org.vivecraft.commands;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.config.ConfigBuilder;
import org.vivecraft.util.Utils;

import java.util.List;
import java.util.function.Consumer;

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
                ViveMain.translate("vivecraft.command.usage", label, Utils.gold("help"), Utils.gold("list"),
                    Utils.gold("reload")),
                ViveMain.translate("vivecraft.command.usage.config", label, Utils.gold("get"), Utils.gold("set"),
                    Utils.gold("reset"), Utils.gold("add/remove"))});
            if (args.length >= 2) {
                ConfigBuilder.ConfigValue value = ViveMain.CONFIG.getConfigValues().stream()
                    .filter(c -> c.getPath().equals(args[1])).findFirst().orElse(null);
                if (value != null) {
                    String path = "vivecraft.serverSettings." + value.getPath();
                    sender.sendMessage((String[]) ArrayUtils.addAll(
                        new String[]{ViveMain.translate(path) + ": " + value.getPath()}, getComment(path)));
                }
            }
            return true;
        }

        if ("list".equals(args[0])) {
            int playerCount = Bukkit.getOnlinePlayers().size();
            if (playerCount == 1) {
                sender.sendMessage(ViveMain.translate("vivecraft.command.listOnePlayer"));
            } else {
                sender.sendMessage(ViveMain.translate("vivecraft.command.listPlayers", playerCount));
            }

            playerCount = ViveMain.VIVE_PLAYERS.size();
            if (playerCount == 1) {
                sender.sendMessage(ViveMain.translate("vivecraft.command.listOneVivePlayer"));
            } else {
                sender.sendMessage(ViveMain.translate("vivecraft.command.listVivePlayers", playerCount));
            }
            for (VivePlayer vivePlayer : ViveMain.VIVE_PLAYERS.values()) {
                String player = vivePlayer.player.getDisplayName() + ": " + ViveMain.translate(vivePlayer.isVR() ?
                    (vivePlayer.isSeated() ? "vivecraft.mode.seatedVR" : "vivecraft.mode.standingVR") :
                    "vivecraft.mode.nonVR");
                if (args.length > 1 && "withVersion".equals(args[1])) {
                    player += ", " + vivePlayer.version;
                }
                sender.sendMessage(player);
            }
            return true;
        }

        if ("reload".equals(args[0])) {
            int changes = ViveMain.CONFIG.reload(sender::sendMessage);
            sender.sendMessage(ViveMain.translate("vivecraft command.reload", changes));
            return true;
        }

        ConfigBuilder.ConfigValue config = ViveMain.CONFIG.getConfigValues().stream()
            .filter(c -> c.getPath().equals(args[0])).findFirst().orElse(null);

        if (config == null) {
            sender.sendMessage(ViveMain.translate("vivecraft.command.unknownConfig", Utils.red(args[0])));
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(Utils.red(ViveMain.translate("vivecraft.command.noAction")));
            return false;
        }

        Action action = process(sender, args, config);
        if (action != Action.ERROR && action != Action.GET) {
            ViveMain.CONFIG.save();
            if (config.needsReload()) {
                sender.sendMessage(ViveMain.translate("vivecraft.command.needsReload"));
            }
        }
        return action != Action.ERROR;
    }

    private static String[] getComment(String key) {
        StringBuilder comment = new StringBuilder();
        if (ViveMain.TRANSLATIONS.containsKey(key + ".tooltip")) {
            comment.append(ViveMain.TRANSLATIONS.get(key + ".tooltip"));
        }
        if (ViveMain.TRANSLATIONS.containsKey(key + ".tooltipall")) {
            if (comment.length() > 0) {
                comment.append("\n");
            }
            comment.append(ViveMain.TRANSLATIONS.get(key + ".tooltipall"));
        }
        if (ViveMain.TRANSLATIONS.containsKey(key + ".tooltipspigot")) {
            if (comment.length() > 0) {
                comment.append("\n");
            }
            comment.append(ViveMain.TRANSLATIONS.get(key + ".tooltipspigot"));
        }
        return comment.toString().split("\n");
    }

    private static Action process(CommandSender sender, String[] args, ConfigBuilder.ConfigValue config) {
        String action = args[1];

        Consumer<String> notifier = sender::sendMessage;

        if ("get".equals(action)) {
            sender.sendMessage(
                ViveMain.translate("vivecraft.command.configGet", config.getPath(), Utils.green(config.get())));
            return Action.GET;
        } else if ("reset".equals(action)) {
            config.reset(notifier);
            return Action.RESET;
        } else if (args.length < 3) {
            sender.sendMessage(Utils.red(ViveMain.translate("vivecraft.command.noValue")));
            return Action.ERROR;
        } else if ("add".equals(action) || "remove".equals(action)) {
            if (!(config instanceof ConfigBuilder.ListValue)) {
                sender.sendMessage(Utils.red(ViveMain.translate("vivecraft.command.nonList")));
                return Action.ERROR;
            } else if (!(config instanceof ConfigBuilder.StringListValue)) {
                sender.sendMessage(Utils.red("General Lists cannot be set with commands atm."));
                return Action.ERROR;
            }
            ConfigBuilder.StringListValue stringList = (ConfigBuilder.StringListValue) config;
            List<String> list = stringList.get();
            String value = args[2].trim();
            if ("add".equals(action)) {
                if (!list.contains(value)) {
                    list.add(value);
                    stringList.set(list, notifier);
                    sender.sendMessage(
                        ViveMain.translate("vivecraft.command.configSet", config.getPath(), Utils.green(config.get())));
                    return Action.ADD;
                } else {
                    sender.sendMessage(ViveMain.translate("vivecraft.command.list.alreadyIn"));
                    return Action.PASS;
                }
            } else {
                // remove
                list.remove(value);
                stringList.set(list, notifier);
                sender.sendMessage(
                    ViveMain.translate("vivecraft.command.configSet", config.getPath(), Utils.green(config.get())));
                return Action.REMOVE;
            }
        } else if ("set".equals(action)) {
            String value = args[2].trim();
            if (config instanceof ConfigBuilder.ListValue) {
                sender.sendMessage(Utils.red(ViveMain.translate("vivecraft.command.list.set")));
                return Action.ERROR;
            } else if (config instanceof ConfigBuilder.NumberValue) {
                try {
                    double val = Double.parseDouble(value);
                    ConfigBuilder.NumberValue numVal = (ConfigBuilder.NumberValue) config;
                    if (val < numVal.getMin().doubleValue() || val > numVal.getMax().doubleValue()) {
                        sender.sendMessage(ViveMain.translate("vivecraft.command.number.outOfRange", Utils.red(value),
                            Utils.green(numVal.getMin()), Utils.green(numVal.getMax()), config.getPath()));
                        return Action.ERROR;
                    }
                    if (config instanceof ConfigBuilder.IntValue) {
                        config.set(Integer.parseInt(value), notifier);
                    } else if (config instanceof ConfigBuilder.DoubleValue) {
                        config.set(Double.parseDouble(value), notifier);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ViveMain.translate("vivecraft.command.number.invalid", Utils.red(value)));
                    return Action.ERROR;
                }
                sender.sendMessage(
                    ViveMain.translate("vivecraft.command.configSet", config.getPath(), Utils.green(config.get())));
                return Action.SET;
            } else if (config instanceof ConfigBuilder.EnumValue) {
                ConfigBuilder.EnumValue enumValue = (ConfigBuilder.EnumValue) config;
                Enum<?> o = enumValue.getEnumValue(value);
                if (o == null) {
                    sender.sendMessage(
                        ViveMain.translate("vivecraft.command.invalid", Utils.red(value), config.getPath()));
                    return Action.ERROR;
                } else {
                    enumValue.set(o, notifier);
                    sender.sendMessage(
                        ViveMain.translate("vivecraft.command.configSet", config.getPath(), Utils.green(config.get())));
                    return Action.SET;
                }
            } else if (config instanceof ConfigBuilder.BooleanValue) {
                config.set(Boolean.parseBoolean(value), notifier);
                sender.sendMessage(
                    ViveMain.translate("vivecraft.command.configSet", config.getPath(), Utils.green(config.get())));
                return Action.SET;
            } else if (config instanceof ConfigBuilder.InListValue) {
                ConfigBuilder.InListValue inList = (ConfigBuilder.InListValue) config;
                for (Object s : inList.getValidValues()) {
                    if (s instanceof String && value.equalsIgnoreCase((String) s)) {
                        config.set(Boolean.parseBoolean(value), notifier);
                        sender.sendMessage(ViveMain.translate("vivecraft.command.configSet", config.getPath(),
                            Utils.green(config.get())));
                        return Action.SET;
                    }
                }
                sender.sendMessage(new String[]{
                    ViveMain.translate("vivecraft.command.invalid", Utils.red(value), config.getPath()),
                    ViveMain.translate("vivecraft.command.validValues",
                        String.join("', '", inList.getValidValues()))});
                return Action.ERROR;
            } else {
                config.set(value, notifier);
                sender.sendMessage(
                    ViveMain.translate("vivecraft.command.configSet", config.getPath(), Utils.green(config.get())));
                return Action.SET;
            }
        } else {
            sender.sendMessage(ViveMain.translate("vivecraft.command.notValidAction", Utils.red(action)));
            return Action.ERROR;
        }
    }
}
