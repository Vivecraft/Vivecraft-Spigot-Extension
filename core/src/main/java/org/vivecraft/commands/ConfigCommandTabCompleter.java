package org.vivecraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.vivecraft.ViveMain;
import org.vivecraft.config.ConfigBuilder;

import java.util.ArrayList;
import java.util.List;

public class ConfigCommandTabCompleter implements TabCompleter {

    private final static String[] baseCommands = new String[]{"help", "list", "reload"};


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        String partial = "";
        if (args.length > 0) {
            partial = args[args.length - 1].toLowerCase();
        } else {
            return completions;
        }

        boolean noConfig = false;

        for (String base : baseCommands) {
            if (args.length == 1 && base.startsWith(partial)) {
                completions.add(base);
            } else if (args.length > 1 && base.equals(args[0])) {
                noConfig = true;
            }
        }

        if ("help".equals(args[0])) {
            // config names
            for (ConfigBuilder.ConfigValue<?> value : ViveMain.CONFIG.getConfigValues()) {
                if (value.getPath().toLowerCase().startsWith(partial)) {
                    completions.add(value.getPath());
                }
            }
        }

        if ("list".equals(args[0])) {
            if ("withVersion".startsWith(partial)) {
                completions.add("withVersion");
            }
        }

        if (args.length > 1 && !noConfig) {
            ConfigBuilder.ConfigValue value = ViveMain.CONFIG.getConfigValues().stream()
                .filter(c -> c.getPath().equals(args[0])).findFirst().orElse(null);
            if (args.length == 2 && value != null) {
                // action
                if ("get".startsWith(partial)) {
                    completions.add("get");
                }
                if (value instanceof ConfigBuilder.ListValue) {
                    if ("add".startsWith(partial)) {
                        completions.add("add");
                    }
                    if ("remove".startsWith(partial)) {
                        completions.add("remove");
                    }
                } else {
                    if ("set".startsWith(partial)) {
                        completions.add("set");
                    }
                }
                // reset last
                if ("reset".startsWith(partial)) {
                    completions.add("reset");
                }
            }

            if (args.length == 3) {
                if ("set".equals(args[1])) {
                    // values
                    if (value instanceof ConfigBuilder.InListValue) {
                        ConfigBuilder.InListValue listValue = (ConfigBuilder.InListValue) value;
                        for (Object obj : listValue.getValidValues()) {
                            completions.add(String.valueOf(obj));
                        }
                    } else if (value instanceof ConfigBuilder.EnumValue) {
                        ConfigBuilder.EnumValue enumValue = (ConfigBuilder.EnumValue) value;
                        for (Object obj : enumValue.getValidValues()) {
                            completions.add(String.valueOf(obj));
                        }
                    } else if (value instanceof ConfigBuilder.BooleanValue) {
                        if ("true".startsWith(partial)) {
                            completions.add("true");
                        }
                        if ("false".startsWith(partial)) {
                            completions.add("false");
                        }
                    }
                } else if (value instanceof ConfigBuilder.ListValue) {
                    ConfigBuilder.StringListValue list = (ConfigBuilder.StringListValue) value;
                    if ("add".equals(args[1])) {
                        // TODO no suggestions until I find out how to get them across versions
                    } else if ("remove".equals(args[1])) {
                        for (String s : list.get()) {
                            if (s.startsWith(partial)) {
                                completions.add(s);
                            }
                        }
                    }
                }
            }
        }

        return completions;
    }
}
